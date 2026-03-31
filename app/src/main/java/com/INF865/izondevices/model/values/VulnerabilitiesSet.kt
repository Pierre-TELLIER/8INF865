package com.INF865.izondevices.model.values

import com.INF865.izondevices.model.Vulnerability

val TELNET_EXPOSED: Vulnerability = Vulnerability(
    name = "Port 23 exposé",
    remediation = "Fermer le port 23",
    moreInfo = "L'exposition du port 23 indique généralement l'exposition d'un service telnet généralement dangereux."
)

val HTTP_EXPOSED: Vulnerability = Vulnerability(
    name = "Port 80 exposé",
    remediation = "Fermer le port 80",
    moreInfo = "L'exposition du port 80 indique généralement l'exposition d'un service HTTP sans chiffrement."
)