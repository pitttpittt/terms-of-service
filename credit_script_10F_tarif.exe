
#Persistent
#InstallKeybdHook
#UseHook
SetTimer, CheckReset, 1000
pieceCount := 0
firstInsertTime := 0
lastPressTime := 0

$1::
currentTime := A_TickCount
if (currentTime - lastPressTime < 300) {
    return ; anti-rebond
}
lastPressTime := currentTime

if (pieceCount = 0) {
    Loop, 4 {
        Send, 5
        Sleep, 100
    }
    firstInsertTime := currentTime
    pieceCount := 1
} else if (pieceCount = 1 && (currentTime - firstInsertTime <= 10000)) {
    Loop, 6 {
        Send, 5
        Sleep, 100
    }
    pieceCount := 0
} else {
    Loop, 4 {
        Send, 5
        Sleep, 100
    }
    firstInsertTime := currentTime
    pieceCount := 1
}
return

CheckReset:
if (pieceCount = 1 && (A_TickCount - firstInsertTime > 10000)) {
    pieceCount := 0
}
return
