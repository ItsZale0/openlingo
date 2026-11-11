package com.alessandro.openlingo.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Un solo client HTTP per tutta l'app.
 *
 * Prima ogni servizio creava il proprio OkHttpClient: 7 client, quindi 7 pool di
 * connessioni e 7 thread pool (fino a ~35 thread con stack da 1 MB). Questo test
 * blocca il ritorno a quel pattern.
 */
class HttpCondivisoTest {

    @Test
    fun `il client http e' sempre la stessa istanza`() {
        assertSame(HttpCondiviso.client, HttpCondiviso.client)
    }

    @Test
    fun `anche il client del feed e' quello condiviso`() {
        assertSame(HttpCondiviso.client, AppModule.provideOkHttp())
    }

    @Test
    fun `i timeout coprono le chiamate all AI`() {
        assertEquals(90_000, HttpCondiviso.client.readTimeoutMillis)
        assertEquals(20_000, HttpCondiviso.client.connectTimeoutMillis)
    }
}
