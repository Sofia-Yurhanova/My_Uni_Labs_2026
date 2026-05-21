#include <windows.h>
#include <iostream>
#include <vector>
#include <thread>
#include <mutex>
#include <string>
#include <algorithm>
using namespace std;

constexpr const char* CHAT_NETWORK_PIPE = "\\\\.\\pipe\\GlobalChatPipe";
constexpr const char* SERVER_DISCOVERY_SLOT = "\\\\.\\mailslot\\ServerSlot";

vector<HANDLE> activeConnections;
mutex connectionGuard;
int nextUserId = 1;

// Функція для пересилки повідомлень
void RelayMessageToOthers(const string& text, HANDLE senderNode) {
    lock_guard<mutex> lock(connectionGuard);
    for (HANDLE pipeHandle : activeConnections) {
        if (pipeHandle != senderNode) {
            DWORD written;
            WriteFile(pipeHandle, text.c_str(), text.size() + 1, &written, nullptr);
        }
    }
}

// Робочий потік для кожного підключеного клієнта
void ClientSessionWorker(HANDLE clientPipe, int uid) {
    char incomingData[1024];
    DWORD bytesRead, availableData;

    while (true) {
        // Перевіряємо наявність даних без блокування каналу
        if (PeekNamedPipe(clientPipe, nullptr, 0, nullptr, &availableData, nullptr)) {
            if (availableData > 0) {
                if (ReadFile(clientPipe, incomingData, sizeof(incomingData) - 1, &bytesRead, nullptr) && bytesRead > 0) {
                    incomingData[bytesRead] = '\0';

                    // Формуємо фінальний рядок
                    string messageToRelay = "[Client " + to_string(uid) + "]: " + incomingData;
                    RelayMessageToOthers(messageToRelay, clientPipe);
                }
                else {
                    break;
                }
            }
            else {
                Sleep(40);
            }
        }
        else {
            break; // Розрив з'єднання
        }
    }

    // відключення
    {
        lock_guard<mutex> lock(connectionGuard);
        auto it = find(activeConnections.begin(), activeConnections.end(), clientPipe);
        if (it != activeConnections.end()) {
            activeConnections.erase(it);
        }
    }
    DisconnectNamedPipe(clientPipe);
    CloseHandle(clientPipe);
    cout << "User " << uid << " left the session.\n";
}

// Служба виявлення сервера у мережі
void DiscoveryServiceTask() {
    HANDLE mSlot = CreateMailslotA(SERVER_DISCOVERY_SLOT, 0, MAILSLOT_WAIT_FOREVER, nullptr);
    if (mSlot == INVALID_HANDLE_VALUE) return;

    char replyAddress[256];
    DWORD readSize;

    while (true) {
        if (ReadFile(mSlot, replyAddress, sizeof(replyAddress) - 1, &readSize, nullptr)) {
            replyAddress[readSize] = '\0';

            // Відкриваємо зворотній канал до клієнта
            HANDLE replySlot = CreateFileA(replyAddress, GENERIC_WRITE, FILE_SHARE_READ, nullptr, OPEN_EXISTING, 0, nullptr);
            if (replySlot != INVALID_HANDLE_VALUE) {
                DWORD wBytes;
                string targetPipe = CHAT_NETWORK_PIPE;
                WriteFile(replySlot, targetPipe.c_str(), targetPipe.size() + 1, &wBytes, nullptr);
                CloseHandle(replySlot);
            }
        }
    }
}

int main() {
    cout << "--- Server Engine is Online ---\n";
    cout << "Listening for incoming requests...\n";

    thread backgroundService(DiscoveryServiceTask);
    backgroundService.detach();

    while (true) {
        HANDLE newPipe = CreateNamedPipeA(
            CHAT_NETWORK_PIPE,
            PIPE_ACCESS_DUPLEX,
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            PIPE_UNLIMITED_INSTANCES,
            1024, 1024, 0, nullptr
        );

        if (newPipe == INVALID_HANDLE_VALUE) {
            Sleep(150);
            continue;
        }

        bool isConnected = ConnectNamedPipe(newPipe, nullptr) ? true : (GetLastError() == ERROR_PIPE_CONNECTED);

        if (isConnected) {
            int currentId = nextUserId++;
            cout << "User " << currentId << " joined.\n";

            {
                lock_guard<mutex> lock(connectionGuard);
                activeConnections.push_back(newPipe);
            }

            thread sessionThread(ClientSessionWorker, newPipe, currentId);
            sessionThread.detach();
        }
        else {
            CloseHandle(newPipe);
        }
    }
    return 0;
}