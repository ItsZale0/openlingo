package com.alessandro.openlingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

/**
 * Test del catalogo Podcast & Video.
 *
 * Il valore della sezione sta nella qualita' dei contenuti: link reali,
 * copertura di tutti i livelli, almeno una fonte per piattaforma e nessun
 * segnaposto. Questi test bloccano regressioni sui dati, non sull'interfaccia.
 */
class PodcastDataTest {

    private val segnaposto = listOf("TODO", "FIXME", "XXX", "lorem", "placeholder", "TBD", "esempio.com")

    private fun host(url: String): String =
        runCatching { URI(url).host.orEmpty().lowercase() }.getOrDefault("")

    @Test
    fun `gli id sono unici`() {
        val id = PodcastData.episodi.map { it.id }
        assertEquals("Ci sono id duplicati", id.size, id.distinct().size)
    }

    @Test
    fun `ogni livello ha almeno sei contenuti`() {
        PodcastData.livelli().forEach { livello ->
            val contenuti = PodcastData.perLivello(livello)
            assertTrue(
                "Livello $livello: solo ${contenuti.size} contenuti",
                contenuti.size >= 6
            )
        }
    }

    @Test
    fun `ogni livello copre YouTube Spotify e una fonte istituzionale`() {
        PodcastData.livelli().forEach { livello ->
            val contenuti = PodcastData.perLivello(livello)
            assertTrue(
                "Livello $livello senza YouTube",
                contenuti.any { it.piattaforma == PodcastData.Piattaforma.YOUTUBE }
            )
            assertTrue(
                "Livello $livello senza Spotify",
                contenuti.any { it.piattaforma == PodcastData.Piattaforma.SPOTIFY }
            )
            assertTrue(
                "Livello $livello senza DW/Web",
                contenuti.any {
                    it.piattaforma == PodcastData.Piattaforma.DW ||
                        it.piattaforma == PodcastData.Piattaforma.WEB
                }
            )
        }
    }

    @Test
    fun `ogni livello ha un contenuto consigliato`() {
        PodcastData.livelli().forEach { livello ->
            val consigliati = PodcastData.perLivello(livello).filter { it.consigliato }
            assertEquals("Livello $livello: consigliati=${consigliati.size}", 1, consigliati.size)
            assertNotNull(PodcastData.consigliatoPer(livello))
        }
    }

    @Test
    fun `i link sono https e coerenti con la piattaforma`() {
        PodcastData.episodi.forEach { ep ->
            assertTrue("${ep.id}: link non https (${ep.url})", ep.url.startsWith("https://"))
            val dominio = host(ep.url)
            assertTrue("${ep.id}: host mancante (${ep.url})", dominio.isNotBlank())
            when (ep.piattaforma) {
                PodcastData.Piattaforma.YOUTUBE ->
                    assertTrue("${ep.id}: host non YouTube ($dominio)", dominio.endsWith("youtube.com"))
                PodcastData.Piattaforma.SPOTIFY ->
                    assertTrue("${ep.id}: host non Spotify ($dominio)", dominio.endsWith("spotify.com"))
                PodcastData.Piattaforma.DW ->
                    assertTrue("${ep.id}: host non DW ($dominio)", dominio.endsWith("dw.com"))
                PodcastData.Piattaforma.WEB ->
                    assertTrue(
                        "${ep.id}: host web inatteso ($dominio)",
                        dominio.endsWith("deutschlandfunk.de") || dominio.endsWith("ardaudiothek.de")
                    )
            }
        }
    }

    @Test
    fun `i contenuti sono descritti e senza segnaposto`() {
        PodcastData.episodi.forEach { ep ->
            assertTrue("${ep.id}: titolo vuoto", ep.titolo.isNotBlank())
            assertTrue("${ep.id}: canale vuoto", ep.canale.isNotBlank())
            assertTrue("${ep.id}: descrizione troppo corta", ep.descrizione.length >= 60)
            assertTrue("${ep.id}: durata vuota", ep.durata.isNotBlank())
            assertTrue("${ep.id}: supporto vuoto", ep.supporto.isNotBlank())
            assertTrue("${ep.id}: tema vuoto", ep.tema.isNotBlank())
            val testo = "${ep.titolo} ${ep.descrizione} ${ep.canale}".lowercase()
            segnaposto.forEach { marcatore ->
                assertFalse("${ep.id}: contiene '$marcatore'", testo.contains(marcatore.lowercase()))
            }
        }
    }

    @Test
    fun `normalizza i livelli del profilo`() {
        assertEquals("A1", PodcastData.normalizza(null))
        assertEquals("A1", PodcastData.normalizza("A0"))
        assertEquals("A1", PodcastData.normalizza("a1"))
        assertEquals("A2", PodcastData.normalizza("A2 - Elementare"))
        assertEquals("B1", PodcastData.normalizza("B1.2"))
        assertEquals("B2", PodcastData.normalizza("B2 - Intermedio alto"))
        assertEquals("B2", PodcastData.normalizza("C1"))
        assertEquals("A1", PodcastData.normalizza("livello sconosciuto"))
    }

    @Test
    fun `il filtro combina livello tipo e piattaforma`() {
        val b1 = PodcastData.filtra("B1")
        assertEquals(PodcastData.perLivello("B1").size, b1.size)

        val soloVideo = PodcastData.filtra("B1", tipo = PodcastData.Tipo.VIDEO)
        assertTrue(soloVideo.isNotEmpty())
        assertTrue(soloVideo.all { it.tipo == PodcastData.Tipo.VIDEO && it.livello == "B1" })

        val soloSpotify = PodcastData.filtra("B1", piattaforma = PodcastData.Piattaforma.SPOTIFY)
        assertTrue(soloSpotify.isNotEmpty())
        assertTrue(soloSpotify.all { it.piattaforma == PodcastData.Piattaforma.SPOTIFY })

        val incrocio = PodcastData.filtra(
            "B1",
            tipo = PodcastData.Tipo.PODCAST,
            piattaforma = PodcastData.Piattaforma.SPOTIFY
        )
        assertTrue(incrocio.all { it.tipo == PodcastData.Tipo.PODCAST })
        assertTrue(incrocio.all { it.piattaforma == PodcastData.Piattaforma.SPOTIFY })
    }

    @Test
    fun `il consigliato appartiene al livello richiesto`() {
        PodcastData.livelli().forEach { livello ->
            val ep = PodcastData.consigliatoPer(livello)
            assertNotNull("Livello $livello senza consigliato", ep)
            assertEquals(livello, ep!!.livello)
        }
    }

    @Test
    fun `info restituisce sempre una scheda valida`() {
        PodcastData.livelli().forEach { livello ->
            val info = PodcastData.info(livello)
            assertEquals(livello, info.codice)
            assertTrue(info.titolo.isNotBlank())
            assertTrue(info.obiettivo.isNotBlank())
            assertTrue(info.consiglio.isNotBlank())
        }
        assertEquals("A1", PodcastData.info("sconosciuto").codice)
    }

    @Test
    fun `il catalogo copre tutte le piattaforme`() {
        PodcastData.Piattaforma.entries.forEach { piattaforma ->
            assertTrue(
                "Nessun contenuto per $piattaforma",
                PodcastData.perPiattaforma(piattaforma).isNotEmpty()
            )
        }
    }
}
