package com.unal.reto11

import com.unal.reto11.data.ImageIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageIntentTest {
    @Test fun extracts_subject_after_de() {
        assertEquals("kotlin", ImageIntent.promptFor("Genera una imagen de kotlin"))
    }

    @Test fun detects_draw_and_logo_requests() {
        assertEquals("un gato astronauta", ImageIntent.promptFor("Dibuja una ilustración de un gato astronauta"))
        assertEquals("kotlin", ImageIntent.promptFor("crea un logo de kotlin"))
    }

    @Test fun plain_questions_are_not_image_requests() {
        assertNull(ImageIntent.promptFor("Que es kotlin"))
        assertNull(ImageIntent.promptFor("Explica qué es una imagen bitmap"))
    }
}
