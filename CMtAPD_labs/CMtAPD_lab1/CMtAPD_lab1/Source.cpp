#include <windows.h>
#include <iostream>
#include <string>
#include <vector>
using namespace std;

int main(int argc, char* argv[]) {
    SetConsoleOutputCP(1251);
    SetConsoleCP(1251);

    // 1. Перевірка на єдиний екземпляр
    HANDLE hSingleInstanceMutex = CreateMutexA(NULL, FALSE, "Global\\MyUniqueLab1Mutex");
    if (GetLastError() == ERROR_ALREADY_EXISTS) {
        cout << "ПОМИЛКА: Інша копія головної програми вже запущена!" << endl;
        return 0;
    }

    SECURITY_ATTRIBUTES sa;
    sa.nLength = sizeof(SECURITY_ATTRIBUTES);
    sa.bInheritHandle = TRUE;
    sa.lpSecurityDescriptor = NULL;

    cout << "=== Головний процес запущено ===" << endl;

    // 2. Створення об'єктів
    HANDLE hAnonMutex = CreateMutexA(&sa, FALSE, NULL); 
    HANDLE hSemaphore = CreateSemaphoreA(&sa, 3, 3, NULL); 

    vector<HANDLE> childHandles;

    cout << "\nЗапуск 10 дочірніх процесів (семафор на 3)..." << endl;
    for (int i = 1; i <= 10; ++i) {
        string cmdLine = "Child.exe " + std::to_string(i) + " "
            + to_string((unsigned long long)hAnonMutex) + " "
            + to_string((unsigned long long)hSemaphore);

        STARTUPINFOA si = { sizeof(si) };
        PROCESS_INFORMATION pi;

        if (CreateProcessA(NULL, (LPSTR)cmdLine.c_str(), NULL, NULL, TRUE, 0, NULL, NULL, &si, &pi)) {
            childHandles.push_back(pi.hProcess);
            CloseHandle(pi.hThread);

            Sleep(50);
        }
    }

    HANDLE hTimer = CreateWaitableTimerA(NULL, TRUE, NULL);
    LARGE_INTEGER liDueTime;
    liDueTime.QuadPart = -50000000LL;

    cout << "\nГоловний процес чекає 5 секунд (Таймер)..." << endl;
    SetWaitableTimer(hTimer, &liDueTime, 0, NULL, NULL, 0);
    WaitForSingleObject(hTimer, INFINITE);
    cout << "5 секунд минуло!" << endl;

    // 4. Перевірка стану 10 процесів
    DWORD waitResult = WaitForMultipleObjects(childHandles.size(), childHandles.data(), TRUE, 0);

    if (waitResult == WAIT_TIMEOUT) {
        cout << "Статус: На даний момент НЕ ВСІ процеси завершили роботу." << endl;
        cout << "Очікуємо їх повного завершення..." << endl;

        WaitForMultipleObjects(childHandles.size(), childHandles.data(), TRUE, INFINITE);
        cout << "Статус: Усі об'єкти процесів перейшли в сигнальний стан (завершено)." << endl;
    }
    else if (waitResult >= WAIT_OBJECT_0 && waitResult < WAIT_OBJECT_0 + childHandles.size()) {
        cout << "Статус: Усі об'єкти процесів вже перейшли в сигнальний стан (завершено)." << endl;
    }

    CloseHandle(hAnonMutex);
    CloseHandle(hSemaphore);
    CloseHandle(hTimer);
    CloseHandle(hSingleInstanceMutex);
    for (HANDLE h : childHandles) {
        CloseHandle(h);
    }

    cout << "=== Головний процес завершує роботу ===" << endl;
    return 0;
}