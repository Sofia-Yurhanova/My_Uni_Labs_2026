#include <windows.h>
#include <iostream>
#include <string>
#include <cstdlib>
using namespace std;

int main(int argc, char* argv[]) {
    SetConsoleOutputCP(1251);
    SetConsoleCP(1251);

    if (argc < 4) return 1;

    int processNumber = stoi(argv[1]);
    HANDLE hMutex = (HANDLE)stoull(argv[2]);
    HANDLE hSemaphore = (HANDLE)stoull(argv[3]);

    srand(GetTickCount() + processNumber);
    int workTime = 1000 + rand() % 3000;

    WaitForSingleObject(hSemaphore, INFINITE);

    WaitForSingleObject(hMutex, INFINITE);
    cout << "Процес №" << processNumber << " працює..." << endl;
    ReleaseMutex(hMutex);

    Sleep(workTime);

    WaitForSingleObject(hMutex, INFINITE);
    cout << "Процес №" << processNumber << " завершив роботу." << endl;
    ReleaseMutex(hMutex);

    ReleaseSemaphore(hSemaphore, 1, NULL);

    return 0;
}