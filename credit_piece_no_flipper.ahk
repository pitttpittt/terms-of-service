
#Persistent
#InstallKeybdHook
#UseHook
SetTimer, CheckReset, 1000
pieceCount := 0
firstInsertTime := 0
lastPressTime := 0

$1:: ; Le $ empêche que la touche soit envoyée aux autres apps comme VPX
currentTime := A_TickCount
if (currentTime - lastPressTime < 300) {
    return ; anti-rebond : on ignore les pressions trop rapprochées
}
lastPressTime := currentTime

if (pieceCount = 0) {
    Send, 55 ; 2 crédits
    firstInsertTime := currentTime
    pieceCount := 1
} else if (pieceCount = 1 && (currentTime - firstInsertTime <= 10000)) {
    Send, 555 ; 3 crédits supplémentaires (total = 5)
    pieceCount := 0 ; Reset immédiat après la 2e pièce
} else {
    Send, 55 ; recommence avec 2 crédits
    firstInsertTime := currentTime
    pieceCount := 1
}
return

CheckReset:
if (pieceCount = 1 && (A_TickCount - firstInsertTime > 10000)) {
    pieceCount := 0
}
return
