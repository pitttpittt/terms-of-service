
#Persistent
#InstallKeybdHook
pieces := 0
SetTimer, ResetCounter, 1000

~1::
    pieces++
    if (pieces = 1) {
        Send, 55 ; 2 crédits
    } else if (pieces = 2) {
        Send, 5 ; 1 crédit en plus (total 5)
    } else {
        Send, 55 ; 2 crédits pour chaque pièce supplémentaire
    }
    lastInsertTime := A_TickCount
return

ResetCounter:
    if (A_TickCount - lastInsertTime > 10000) {
        pieces := 0
    }
return
