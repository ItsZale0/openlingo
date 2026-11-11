package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.LivelloIniziale

/** Estensione per convertire LivelloIniziale in LivelloCEFR. */
fun LivelloIniziale.toLivelloCEFR(): LivelloCEFR = when (this) {
    LivelloIniziale.A0 -> LivelloCEFR.A0
    LivelloIniziale.A1 -> LivelloCEFR.A1
    LivelloIniziale.A2 -> LivelloCEFR.A2
    LivelloIniziale.B1 -> LivelloCEFR.B1
    LivelloIniziale.B2 -> LivelloCEFR.B2
}
