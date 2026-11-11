package com.alessandro.openlingo.data.remote

import kotlinx.serialization.Serializable

/** Una parola nuova del testo di lettura con la sua traduzione italiana. */
@Serializable
data class VoceGlossario(
    val tedesco: String,
    val italiano: String
)
