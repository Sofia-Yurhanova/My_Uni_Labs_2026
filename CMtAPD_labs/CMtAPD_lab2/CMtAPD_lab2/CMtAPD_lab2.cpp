#include <windows.h>
#include <iostream>
#include <string>
#include <iomanip>

using namespace std;

struct ThreadData {
    int mode;              // 0 - Без синхрону, 1 - Подія, 2 - Критична секція
    bool isPositive;       // true (додатні), false (від'ємні)
    int* sharedIndex;      // Лічильник загальної кількості записаних чисел
    int* sharedArray;      // Вказівник на початок масиву у спільній пам'яті
    HANDLE hSemaphore;     // Семафор
    HANDLE hHeap;          // Індивідуальна куча
};

CRITICAL_SECTION g_cs;
HANDLE g_eventPos;
HANDLE g_eventNeg;

void CheckError(HANDLE handle, const char* errorMessage) {
    if (handle == NULL) {
        cerr << "\n[Помилка] " << errorMessage << ". Код: " << GetLastError() << endl;
        exit(1);
    }
}

DWORD WINAPI ThreadProc(LPVOID lpParam) {
    ThreadData* data = (ThreadData*)lpParam;

    // Семафор: дозволяє працювати максимум 2 потокам одночасно
    WaitForSingleObject(data->hSemaphore, INFINITE);

    // Пам'ять з індивідуальної купи потоку
    int* localValue = (int*)HeapAlloc(data->hHeap, HEAP_ZERO_MEMORY, sizeof(int));
    if (localValue == NULL) {
        ReleaseSemaphore(data->hSemaphore, 1, NULL);
        return 1;
    }

    if (!data->isPositive) Sleep(15);

    int lineBreakCounter = 0; // Для виводу по 15 чисел у рядок

    if (data->mode == 0) {
        // === БЛОК 1: БЕЗ СИНХРОНІЗАЦІЇ ===
        for (int i = 1; i <= 500; ++i) {
            *localValue = data->isPositive ? i : -i;

            data->sharedArray[*data->sharedIndex] = *localValue;
            (*data->sharedIndex)++;

            printf("%5d", *localValue);

            Sleep(1);
        }
    }
    else if (data->mode == 1) {
        // === БЛОК 2: ПОДІЯ (Event) ===
        if (data->isPositive) {
            WaitForSingleObject(g_eventPos, INFINITE);
            for (int i = 1; i <= 500; ++i) {
                *localValue = i;

                data->sharedArray[*data->sharedIndex] = *localValue;
                (*data->sharedIndex)++;

                printf("%5d", *localValue);
                if (++lineBreakCounter % 15 == 0) printf("\n");
            }
            SetEvent(g_eventNeg);
        }
        else {
            WaitForSingleObject(g_eventNeg, INFINITE);
            for (int i = 1; i <= 500; ++i) {
                *localValue = -i;

                data->sharedArray[*data->sharedIndex] = *localValue;
                (*data->sharedIndex)++;

                printf("%5d", *localValue);
            }
        }
    }
    else if (data->mode == 2) {
        // === БЛОК 3: КРИТИЧНА СЕКЦІЯ (Логіка Ярини) ===

        EnterCriticalSection(&g_cs); 

        for (int i = 1; i <= 500; ++i) {
            *localValue = data->isPositive ? i : -i;

            data->sharedArray[*data->sharedIndex] = *localValue;
            (*data->sharedIndex)++;

            printf("%5d", *localValue);
        }

        LeaveCriticalSection(&g_cs); 
    }

    HeapFree(data->hHeap, 0, localValue);
    ReleaseSemaphore(data->hSemaphore, 1, NULL);
    return 0;
}

int main() {
    SetConsoleOutputCP(1251);
    SetConsoleCP(1251);

    int totalSize = sizeof(int) * 3005;
    HANDLE hFile = CreateFileA("shared_data.bin", GENERIC_READ | GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    CheckError(hFile, "CreateFile");

    HANDLE hMapFile = CreateFileMappingA(hFile, NULL, PAGE_READWRITE, 0, totalSize, "Local\\MySharedMem");
    CheckError(hMapFile, "CreateFileMapping");

    int* pSharedMem = (int*)MapViewOfFile(hMapFile, FILE_MAP_ALL_ACCESS, 0, 0, totalSize);
    if (pSharedMem == NULL) { cout << "Помилка MapViewOfFile: " << GetLastError() << endl; return 1; }
    pSharedMem[0] = 0;

    InitializeCriticalSection(&g_cs);

    g_eventPos = CreateEventA(NULL, FALSE, TRUE, NULL);
    CheckError(g_eventPos, "CreateEvent Pos");
    g_eventNeg = CreateEventA(NULL, FALSE, FALSE, NULL);
    CheckError(g_eventNeg, "CreateEvent Neg");

    HANDLE hSemaphore = CreateSemaphoreA(NULL, 2, 2, NULL);
    CheckError(hSemaphore, "CreateSemaphore");

    string headers[3] = {
        "\n\n=== ПАРА 1: БЕЗ СИНХРОНІЗАЦІЇ (Очікується хаос і втрата чисел) ===\n",
        "\n\n=== ПАРА 2: СИНХРОНІЗАЦІЯ ПОДІЄЮ (Спочатку всі додатні, потім всі від'ємні) ===\n",
        "\n\n=== ПАРА 3: КРИТИЧНА СЕКЦІЯ (Спочатку один потік, потім інший) ===\n"
    };

    for (int mode = 0; mode < 3; ++mode) {
        cout << headers[mode];

        HANDLE hHeapPos = HeapCreate(0, 4096, 0);
        CheckError(hHeapPos, "HeapCreate Pos");
        HANDLE hHeapNeg = HeapCreate(0, 4096, 0);
        CheckError(hHeapNeg, "HeapCreate Neg");

        ThreadData dataPos = { mode, true,  &pSharedMem[0], pSharedMem + 1, hSemaphore, hHeapPos };
        ThreadData dataNeg = { mode, false, &pSharedMem[0], pSharedMem + 1, hSemaphore, hHeapNeg };

        HANDLE hThreadPos = CreateThread(NULL, 0, ThreadProc, &dataPos, CREATE_SUSPENDED, NULL);
        CheckError(hThreadPos, "CreateThread Pos");
        HANDLE hThreadNeg = CreateThread(NULL, 0, ThreadProc, &dataNeg, CREATE_SUSPENDED, NULL);
        CheckError(hThreadNeg, "CreateThread Neg");

        SetThreadPriority(hThreadPos, THREAD_PRIORITY_ABOVE_NORMAL);
        SetThreadPriority(hThreadNeg, THREAD_PRIORITY_BELOW_NORMAL);

        if (mode == 1) {
            SetEvent(g_eventPos);
            ResetEvent(g_eventNeg);
        }

        ResumeThread(hThreadPos);

        if (mode == 0 || mode == 2) Sleep(10); // Забезпечуємо старт додатнього

        ResumeThread(hThreadNeg);

        HANDLE currentPair[2] = { hThreadPos, hThreadNeg };
        WaitForMultipleObjects(2, currentPair, TRUE, INFINITE);
        cout << "\n";

        CloseHandle(hThreadPos);
        CloseHandle(hThreadNeg);
        HeapDestroy(hHeapPos);
        HeapDestroy(hHeapNeg);
    }

    cout << "\n==========================================" << endl;
    cout << "Всі потоки завершили роботу успішно!" << endl;
    cout << "Всього записано чисел у бінарний файл: " << pSharedMem[0] << " (Має бути 3000)" << endl;
    cout << "==========================================\n" << endl;

    UnmapViewOfFile(pSharedMem);
    CloseHandle(hMapFile);
    CloseHandle(hFile);
    CloseHandle(hSemaphore);
    CloseHandle(g_eventPos);
    CloseHandle(g_eventNeg);
    DeleteCriticalSection(&g_cs);

    system("pause");
    return 0;
}