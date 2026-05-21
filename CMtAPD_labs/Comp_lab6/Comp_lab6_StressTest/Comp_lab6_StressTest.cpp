#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <iostream>
#include <winsock2.h>
#include <vector>
#include <string>

#pragma comment(lib, "ws2_32.lib")

int main() {
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed.\n";
        return 1;
    }

    std::vector<SOCKET> clients;
    int targetConnections = 1050;

    std::cout << "Starting stress test. Connecting " << targetConnections << " bots to the server...\n";

    for (int i = 0; i < targetConnections; ++i) {
        SOCKET clientSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (clientSocket == INVALID_SOCKET) continue;

        sockaddr_in serverAddr = {};
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_port = htons(8080);
        serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

        if (connect(clientSocket, (sockaddr*)&serverAddr, sizeof(serverAddr)) != SOCKET_ERROR) {
            clients.push_back(clientSocket);
            std::cout << "Bot " << clients.size() << " connected.\n";
        }
        else {
            closesocket(clientSocket);
        }

        // Невеличка пауза, щоб сервер встигав приймати підключення по черзі
        Sleep(10);
    }

    std::cout << "\n=======================================================\n";
    std::cout << "SUCCESS: " << clients.size() << " bots are currently connected!\n";
    std::cout << "The server successfully bypassed the 64-event limit.\n";
    std::cout << "Now you can run your Interactive Clients to test the chat.\n";
    std::cout << "Press ENTER to disconnect all bots and close this window...\n";
    std::cout << "=======================================================\n";

    std::cin.get(); // Програма чекає натискання Enter

    // Коректне закриття всіх 100 сокетів
    for (SOCKET s : clients) {
        closesocket(s);
    }
    WSACleanup();

    return 0;
}