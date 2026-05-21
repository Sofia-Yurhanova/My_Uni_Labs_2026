#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <iostream>
#include <winsock2.h>
#include <vector>
#include <string>
#include <thread>
#include <mutex>
#include <queue>
#include <algorithm>

#pragma comment(lib, "ws2_32.lib")

// Глобальні структури для синхронізації між потоками
std::vector<SOCKET> SocketArray;
std::vector<WSAEVENT> EventArray;
std::mutex dataMutex;

struct Message {
    SOCKET sender;
    std::string text;
};
std::queue<Message> messageQueue;
bool serverRunning = true;

// Потік для розсилки повідомлень усім клієнтам
void BroadcastThreadFunc() {
    while (serverRunning) {
        std::vector<Message> msgsToSend;
        std::vector<SOCKET> clients;

        // Блокуємо доступ до спільних даних на час копіювання
        {
            std::lock_guard<std::mutex> lock(dataMutex);
            while (!messageQueue.empty()) {
                msgsToSend.push_back(messageQueue.front());
                messageQueue.pop();
            }
            // Копіюємо сокети (починаючи з 1, бо 0 - це слухаючий сокет)
            for (size_t i = 1; i < SocketArray.size(); ++i) {
                clients.push_back(SocketArray[i]);
            }
        }

        // Розсилаємо повідомлення
        for (const auto& msg : msgsToSend) {
            for (SOCKET s : clients) {
                if (s != msg.sender) { // Не відправляємо повідомлення тому, хто його написав
                    send(s, msg.text.c_str(), msg.text.length(), 0);
                }
            }
        }
        Sleep(10); // Уникаємо 100% навантаження на процесор
    }
}

int main() {
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed.\n";
        return 1;
    }

    SOCKET ListenSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    sockaddr_in serverAddr = {};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(8080);
    serverAddr.sin_addr.s_addr = INADDR_ANY;

    bind(ListenSocket, (sockaddr*)&serverAddr, sizeof(serverAddr));
    listen(ListenSocket, SOMAXCONN);

    WSAEVENT ListenEvent = WSACreateEvent();
    WSAEventSelect(ListenSocket, ListenEvent, FD_ACCEPT);

    SocketArray.push_back(ListenSocket);
    EventArray.push_back(ListenEvent);

    std::cout << "Server is running on port 8080...\n";

    // Запускаємо потік розсилки
    std::thread broadcastThread(BroadcastThreadFunc);

    // Головний потік: обробка подій
    while (serverRunning) {
        bool vectorsModified = false;
        DWORD totalEvents;

        {
            std::lock_guard<std::mutex> lock(dataMutex);
            totalEvents = EventArray.size();
        }

        // Розбиваємо масив на блоки максимум по 64
        for (DWORD i = 0; i < totalEvents; i += WSA_MAXIMUM_WAIT_EVENTS) {
            DWORD count = min((DWORD)WSA_MAXIMUM_WAIT_EVENTS, totalEvents - i);

            // Таймаут 0, щоб не блокувати перехід до наступного блоку
            DWORD waitResult = WSAWaitForMultipleEvents(count, &EventArray[i], FALSE, 0, FALSE);

            if (waitResult == WSA_WAIT_TIMEOUT || waitResult == WSA_WAIT_FAILED) {
                continue;
            }

            DWORD eventIndex = i + (waitResult - WSA_WAIT_EVENT_0);

            WSANETWORKEVENTS NetworkEvents;
            WSAEnumNetworkEvents(SocketArray[eventIndex], EventArray[eventIndex], &NetworkEvents);

            // Обробка підключення нового клієнта
            if (NetworkEvents.lNetworkEvents & FD_ACCEPT) {
                if (NetworkEvents.iErrorCode[FD_ACCEPT_BIT] == 0) {
                    SOCKET ClientSocket = accept(SocketArray[eventIndex], NULL, NULL);
                    WSAEVENT ClientEvent = WSACreateEvent();
                    WSAEventSelect(ClientSocket, ClientEvent, FD_READ | FD_CLOSE);

                    std::lock_guard<std::mutex> lock(dataMutex);
                    SocketArray.push_back(ClientSocket);
                    EventArray.push_back(ClientEvent);
                    std::cout << "New client connected. Total clients: " << SocketArray.size() - 1 << "\n";
                    vectorsModified = true;
                }
            }

            // Обробка вхідного повідомлення
            if (NetworkEvents.lNetworkEvents & FD_READ) {
                if (NetworkEvents.iErrorCode[FD_READ_BIT] == 0) {
                    char buffer[1024];
                    ZeroMemory(buffer, sizeof(buffer));
                    int bytesReceived = recv(SocketArray[eventIndex], buffer, sizeof(buffer) - 1, 0);

                    if (bytesReceived > 0) {
                        std::lock_guard<std::mutex> lock(dataMutex);
                        messageQueue.push({ SocketArray[eventIndex], std::string(buffer) });
                    }
                }
            }

            // Обробка відключення клієнта
            if (NetworkEvents.lNetworkEvents & FD_CLOSE) {
                std::lock_guard<std::mutex> lock(dataMutex);
                closesocket(SocketArray[eventIndex]);
                WSACloseEvent(EventArray[eventIndex]);

                SocketArray.erase(SocketArray.begin() + eventIndex);
                EventArray.erase(EventArray.begin() + eventIndex);

                std::cout << "Client disconnected.\n";
                vectorsModified = true;
            }

            // Якщо масиви змінилися, перериваємо поточний цикл for, 
            // щоб уникнути виходу за межі пам'яті (інвалідація ітераторів/індексів)
            if (vectorsModified) break;
        }

        // Невелика пауза головного циклу для зняття навантаження
        Sleep(1);
    }

    serverRunning = false;
    broadcastThread.join();

    for (SOCKET s : SocketArray) closesocket(s);
    for (WSAEVENT e : EventArray) WSACloseEvent(e);
    WSACleanup();

    return 0;
}