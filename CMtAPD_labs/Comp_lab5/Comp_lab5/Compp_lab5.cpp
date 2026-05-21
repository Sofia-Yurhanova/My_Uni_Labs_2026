#include <windows.h>
#include <iostream>
#include <string>
using namespace std;

int main(int argc, char* argv[]) {
    if (argc < 2) {
        cout << "Launch error. Please specify the process number (1 or 2).\n";
        cout << "Usage example: program.exe 1\n";
        return 1;
    }

    int processId = stoi(argv[1]);

    HANDLE mutexA = CreateMutexA(NULL, FALSE, "Global\\DeadlockMutexA");
    HANDLE mutexB = CreateMutexA(NULL, FALSE, "Global\\DeadlockMutexB");

    if (mutexA == NULL || mutexB == NULL) {
        cerr << "Error creating mutexes.\n";
        return 1;
    }

    cout << "=== Process " << processId << " started ===\n\n";

    if (processId == 1) {
        cout << "[Process 1]: Trying to acquire Resource A (Mutex A)...\n";
        WaitForSingleObject(mutexA, INFINITE); 
        cout << "[Process 1]: Resource A acquired!\n";

        cout << "[Process 1]: Working... (sleeping for 3 seconds)\n";
        Sleep(3000); 

        cout << "[Process 1]: Trying to acquire Resource B (Mutex B)...\n";
        WaitForSingleObject(mutexB, INFINITE);
        cout << "[Process 1]: Resource B acquired! (If you see this text, Deadlock FAILED to occur)\n";

        ReleaseMutex(mutexB);
        ReleaseMutex(mutexA);
    }
    else if (processId == 2) {
        cout << "[Process 2]: Trying to acquire Resource B (Mutex B)...\n";
        WaitForSingleObject(mutexB, INFINITE);
        cout << "[Process 2]: Resource B acquired!\n";

        cout << "[Process 2]: Working... (sleeping for 3 seconds)\n";
        Sleep(3000);

        cout << "[Process 2]: Trying to acquire Resource A (Mutex A)...\n";
        WaitForSingleObject(mutexA, INFINITE);
        cout << "[Process 2]: Resource A acquired!\n";

        ReleaseMutex(mutexA);
        ReleaseMutex(mutexB);
    }
    else {
        cout << "Unknown process number. Please enter 1 or 2.\n";
    }

    cout << "\n=== Process " << processId << " successfully finished ===\n";

    CloseHandle(mutexA);
    CloseHandle(mutexB);

    return 0;
}