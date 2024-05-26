package com.dgmltn.ded.editor

import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.PhraseLocation
import dev.snipme.highlights.model.SyntaxLanguage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HighlightsTest {

    @BeforeTest
    fun setup() {
    }

    @Test
    fun basic_getHighlights_location() {
        val highlighter = Highlights.Builder()
            .language(SyntaxLanguage.JAVASCRIPT)
            .build()

        highlighter.setCode("const foo = 'bar';")
        val highlights1 = highlighter.getHighlights().filterIsInstance<ColorHighlight>().sortedBy { it.location.start }
        highlights1.size shouldEqual 4
        highlights1[0].location shouldEqual PhraseLocation(start=0, end=5)
        highlights1[1].location shouldEqual PhraseLocation(start=10, end=11)
        highlights1[2].location shouldEqual PhraseLocation(start=12, end=17)
        highlights1[3].location shouldEqual PhraseLocation(start=17, end=18)

        highlighter.setCode("const foo = 'barrr';")
        val highlights2 = highlighter.getHighlights().filterIsInstance<ColorHighlight>().sortedBy { it.location.start }
        highlights2.size shouldEqual 4
        highlights2[0].location shouldEqual PhraseLocation(start=0, end=5)
        highlights2[1].location shouldEqual PhraseLocation(start=10, end=11)
        // FAILS HERE:
        highlights2[2].location shouldEqual PhraseLocation(start=12, end=19)
        highlights2[3].location shouldEqual PhraseLocation(start=19, end=20)
    }

    @Test
    fun basic_getHighlights_location_alt() {
        var highlighter = Highlights.Builder()
            .language(SyntaxLanguage.JAVASCRIPT)
            .build()

        highlighter = highlighter
            .getBuilder()
            .code("const foo = 'bar';")
            .build()
        val highlights1 = highlighter.getHighlights().filterIsInstance<ColorHighlight>().sortedBy { it.location.start }
        highlights1.size shouldEqual 4
        highlights1[0].location shouldEqual PhraseLocation(start=0, end=5)
        highlights1[1].location shouldEqual PhraseLocation(start=10, end=11)
        highlights1[2].location shouldEqual PhraseLocation(start=12, end=17)
        highlights1[3].location shouldEqual PhraseLocation(start=17, end=18)

        highlighter = highlighter
            .getBuilder()
            .code("const foo = 'barrr';")
            .build()
        val highlights2 = highlighter.getHighlights().filterIsInstance<ColorHighlight>().sortedBy { it.location.start }
        highlights2.size shouldEqual 4
        highlights2[0].location shouldEqual PhraseLocation(start=0, end=5)
        highlights2[1].location shouldEqual PhraseLocation(start=10, end=11)
        highlights2[2].location shouldEqual PhraseLocation(start=12, end=19)
        highlights2[3].location shouldEqual PhraseLocation(start=19, end=20)
    }



    private infix fun Any?.shouldEqual(expected: Any?) {
        assertEquals(expected, this)
    }
}
