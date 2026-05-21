#include <iostream>
#include <windows.h>

using namespace std;

HANDLE hReadPipe, hWritePipe; // Дескриптори для каналу (Pipe)
HANDLE hSemaphore;            
HANDLE hMutex;                
HANDLE hStartEvent;         

int matrix[4][4] = {
    { 2,  1,  3,  4},
    { 5,  6,  7,  8},
    { 9,  1,  2,  3},
    { 4,  5,  6,  7}
};

int calcDet3x3(int m[3][3]) {
    return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
        - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
        + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
}

DWORD WINAPI Producer(LPVOID lpParam) {
    // Отримуємо індекс стовпця, який обробляє цей потік (від 0 до 3)
    int col = (int)(intptr_t)lpParam;

    SetEvent(hStartEvent);

    int minor[3][3];
    int r = 0;
    for (int i = 1; i < 4; i++) {
        int c = 0;
        for (int j = 0; j < 4; j++) {
            if (j == col) continue;
            minor[r][c] = matrix[i][j];
            c++;
        }
        r++;
    }

    int det3x3 = calcDet3x3(minor);
    int sign = (col % 2 == 0) ? 1 : -1;
    int result = sign * matrix[0][col] * det3x3;

    DWORD bytesWritten;
    WriteFile(hWritePipe, &result, sizeof(result), &bytesWritten, NULL);

    WaitForSingleObject(hMutex, INFINITE);
    cout << "[Producer " << col << "] calculated part: " << result << endl;
    ReleaseMutex(hMutex);

    ReleaseSemaphore(hSemaphore, 1, NULL);

    return 0;
}

DWORD WINAPI Consumer(LPVOID lpParam) {
    int totalDet = 0;
    int itemsToConsume = 4; // Чекаємо на 4 мінори

    for (int i = 0; i < itemsToConsume; i++) {
        WaitForSingleObject(hSemaphore, INFINITE);

        int partialDet;
        DWORD bytesRead;
        ReadFile(hReadPipe, &partialDet, sizeof(partialDet), &bytesRead, NULL);

        totalDet += partialDet;
    }


    WaitForSingleObject(hStartEvent, INFINITE);

    WaitForSingleObject(hMutex, INFINITE);
    cout << "\n[Consumer] All data collected. Finish." << endl;
    cout << ">>> Total determinant: " << totalDet << " <<<\n" << endl;
    ReleaseMutex(hMutex);

    return 0;
}

int main() {
    setlocale(LC_ALL, "uk_UA.UTF-8"); 

    // 1. Ініціалізація примітивів синхронізації
    hMutex = CreateMutex(NULL, FALSE, NULL);
    hSemaphore = CreateSemaphore(NULL, 0, 4, NULL); // Початкове значення 0, максимум 4

    // Створюємо подію для Старт-Фініш (Manual reset = TRUE, Initial state = FALSE)
    hStartEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

    SECURITY_ATTRIBUTES sa = { sizeof(SECURITY_ATTRIBUTES), NULL, TRUE };
    if (!CreatePipe(&hReadPipe, &hWritePipe, &sa, 0)) {
        cout << "Error creating pipe!" << endl;
        return 1;
    }

    HANDLE hThreads[5]; 

    hThreads[0] = CreateThread(NULL, 0, Consumer, NULL, 0, NULL);

    for (int i = 0; i < 4; i++) {
        hThreads[i + 1] = CreateThread(NULL, 0, Producer, (LPVOID)(intptr_t)i, 0, NULL);
    }

    WaitForMultipleObjects(5, hThreads, TRUE, INFINITE);

    for (int i = 0; i < 5; i++) CloseHandle(hThreads[i]);
    CloseHandle(hReadPipe);
    CloseHandle(hWritePipe);
    CloseHandle(hSemaphore);
    CloseHandle(hMutex);
    CloseHandle(hStartEvent);

    system("pause"); 
    return 0;
}