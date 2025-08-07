
#Persistent
#InstallKeybdHook
#UseHook
SetTimer, CheckReset, 1000
pieceCount := 0
firstInsertTime := 0

$1::
currentTime := A_TickCount
if (pieceCount = 0) {
    Send, 55 ; 2 crédits
    firstInsertTime := currentTime
    pieceCount := 1
} else if (pieceCount = 1 && (currentTime - firstInsertTime <= 10000)) {
    Send, 555 ; 3 crédits supplémentaires (total = 5)
    pieceCount := 0 ; Reset immédiat après 2e pièce
} else {
    ; Plus de 10s écoulées, on recommence
    Send, 55 ; nouvelle série : 2 crédits
    firstInsertTime := currentTime
    pieceCount := 1
}
return

CheckReset:
if (pieceCount = 1 && (A_TickCount - firstInsertTime > 10000)) {
    pieceCount := 0
}
return
