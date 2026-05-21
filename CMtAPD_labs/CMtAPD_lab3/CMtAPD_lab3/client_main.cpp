#include <windows.h>
#include <iostream>
#include <string>
#include <thread>
using namespace std;

constexpr const char* SERVER_DISCOVERY_SLOT = "\\\\.\\mailslot\\ServerSlot";

// Допоміжна функція для очищення рядка вводу
void ClearInputLine() {
    HANDLE consoleHandle = GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_SCREEN_BUFFER_INFO bufferInfo;
    GetConsoleScreenBufferInfo(consoleHandle, &bufferInfo);

    COORD startPosition = bufferInfo.dwCursorPosition;
    startPosition.X = 0;

    DWORD charsWritten;
    FillConsoleOutputCharacterA(consoleHandle, ' ', bufferInfo.dwSize.X, startPosition, &charsWritten);
    SetConsoleCursorPosition(consoleHandle, startPosition);
}

// Потік, що моніторить вхідні повідомлення
void NetworkListener(HANDLE communicationPipe) {
    char dataBuffer[1024];
    DWORD bytesRead, bytesAvailable;

    while (true) {
        if (PeekNamedPipe(communicationPipe, nullptr, 0, nullptr, &bytesAvailable, nullptr)) {
            if (bytesAvailable > 0) {
                if (ReadFile(communicationPipe, dataBuffer, sizeof(dataBuffer) - 1, &bytesRead, nullptr) && bytesRead > 0) {
                    dataBuffer[bytesRead] = '\0';

                    ClearInputLine();
                    cout << dataBuffer << "\n> " << flush;
                }
                else {
                    cout << "\n[Server connection dropped]\n";
                    exit(0);
                }
            }
            else {
                Sleep(40);
            }
        }
        else {
            cout << "\n[Server connection dropped]\n";
            exit(0);
        }
    }
}

// Окрема функція для автоматичного пошуку сервера
string LocateServer() {
    string mySlotAddress = "\\\\.\\mailslot\\CallbackSlot_" + to_string(GetCurrentProcessId());
    HANDLE mySlot = CreateMailslotA(mySlotAddress.c_str(), 0, MAILSLOT_WAIT_FOREVER, nullptr);

    if (mySlot == INVALID_HANDLE_VALUE) return "";

    cout << "Scanning network for active server...\n";

    HANDLE serverSlot = CreateFileA(SERVER_DISCOVERY_SLOT, GENERIC_WRITE, FILE_SHARE_READ, nullptr, OPEN_EXISTING, 0, nullptr);
    if (serverSlot == INVALID_HANDLE_VALUE) {
        CloseHandle(mySlot);
        return "";
    }

    DWORD wBytes;
    WriteFile(serverSlot, mySlotAddress.c_str(), mySlotAddress.size() + 1, &wBytes, nullptr);
    CloseHandle(serverSlot);

    char receivedPipeAddress[256];
    while (true) {
        DWORD messageCount;
        GetMailslotInfo(mySlot, nullptr, nullptr, &messageCount, nullptr);
        if (messageCount > 0) {
            DWORD rBytes;
            ReadFile(mySlot, receivedPipeAddress, sizeof(receivedPipeAddress) - 1, &rBytes, nullptr);
            receivedPipeAddress[rBytes] = '\0';
            break;
        }
        Sleep(150);
    }

    CloseHandle(mySlot);
    return string(receivedPipeAddress);
}

int main() {
    // 1. Шукаємо сервер
    string targetPipeName = LocateServer();
    if (targetPipeName.empty()) {
        cerr << "Failed to locate server. Is it online?\n";
        return 1;
    }

    // 2. Підключаємося
    HANDLE chatPipe;
    while (true) {
        chatPipe = CreateFileA(targetPipeName.c_str(), GENERIC_READ | GENERIC_WRITE, 0, nullptr, OPEN_EXISTING, 0, nullptr);
        if (chatPipe != INVALID_HANDLE_VALUE) break;

        if (GetLastError() != ERROR_PIPE_BUSY || !WaitNamedPipeA(targetPipeName.c_str(), 5000)) {
            std::cerr << "Timeout while connecting to server.\n";
            return 1;
        }
    }

    DWORD pipeMode = PIPE_READMODE_MESSAGE;
    SetNamedPipeHandleState(chatPipe, &pipeMode, nullptr, nullptr);

    cout << "Joined the room!\n> " << flush;

    // 3. Запускаємо слухача
    thread listenerThread(NetworkListener, chatPipe);
    listenerThread.detach();

    // 4. Цикл відправки повідомлень
    string userInput;
    while (true) {
        getline(std::cin, userInput);

        if (userInput == "exit") break;
        if (userInput.empty()) {
            cout << "> " << flush;
            continue;
        }

        DWORD bytesWritten;
        WriteFile(chatPipe, userInput.c_str(), userInput.size() + 1, &bytesWritten, nullptr);
        cout << "> " << flush;
    }

    CloseHandle(chatPipe);
    return 0;
}