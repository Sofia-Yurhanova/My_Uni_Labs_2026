#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <iostream>
#include <winsock2.h>
#include <string>
#include <thread>

#pragma comment(lib, "ws2_32.lib")

bool isRunning = true;

// Потік для паралельного отримання повідомлень від сервера
void ReceiveMessages(SOCKET clientSocket) {
    char buffer[1024];
    while (isRunning) {
        ZeroMemory(buffer, sizeof(buffer));
        int bytesReceived = recv(clientSocket, buffer, sizeof(buffer) - 1, 0);

        if (bytesReceived > 0) {
            std::cout << "\r" << buffer << "\n>>> ";
            std::cout.flush();
        }
        else if (bytesReceived == 0 || bytesReceived == SOCKET_ERROR) {
            std::cout << "\nDisconnected from server.\n";
            isRunning = false;
            break;
        }
    }
}

int main() {
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed.\n";
        return 1;
    }

    std::string nickname;
    std::cout << "Enter your nickname: ";
    std::getline(std::cin, nickname);

    std::cout << "Connecting to server...\n";

    SOCKET clientSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    sockaddr_in serverAddr = {};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(8080);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

    if (connect(clientSocket, (sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "Failed to connect to server.\n";
        closesocket(clientSocket);
        WSACleanup();
        return 1;
    }

    std::cout << "Successfully connected!\n";

    // Запускаємо окремий потік, щоб одночасно і читати, і писати
    std::thread recvThread(ReceiveMessages, clientSocket);

    std::string message;
    std::cout << ">>> ";

    // Головний цикл введення повідомлень
    while (isRunning) {
        std::getline(std::cin, message);

        if (message.empty()) {
            std::cout << ">>> ";
            continue;
        }

        std::string fullMessage = "[" + nickname + "]: " + message;

        // Відправляємо на сервер
        send(clientSocket, fullMessage.c_str(), fullMessage.length(), 0);
        std::cout << ">>> ";
    }

    // Завершення роботи
    isRunning = false;
    closesocket(clientSocket);
    recvThread.join();
    WSACleanup();

    return 0;
}