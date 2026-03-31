package com.INF865.izondevices.model.values

import com.INF865.izondevices.model.Vulnerability

val TELNET_EXPOSED: Vulnerability = Vulnerability(
    name = "Port 23 exposé",
    description = "Un port 23 semble être exposé sur l'appareil",
    mitigation = "Fermer le port 23",
    details = "L'exposition du port 23 indique généralement l'exposition d'un service telnet généralement dangereux."
)

val HTTP_EXPOSED: Vulnerability = Vulnerability(
    name = "Port 80 exposé",
    description = "Un port 80 semble être exposé sur l'appareil",
    mitigation = "Fermer le port 80",
    details = "L'exposition du port 80 indique généralement l'exposition d'un service HTTP sans chiffrement."
)