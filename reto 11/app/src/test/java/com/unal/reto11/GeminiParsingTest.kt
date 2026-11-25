package com.unal.reto11

import com.unal.reto11.data.ChatHistoryTrimmer
import com.unal.reto11.data.GeminiRequestBuilder
import com.unal.reto11.data.GeminiResponseParser
import com.unal.reto11.data.PromptTemplate
import com.unal.reto11.model.ChatError
import com.unal.reto11.model.ChatException
import com.unal.reto11.model.Message
import com.unal.reto11.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeminiParsingTest {

    private fun msg(id: Long, role: Role, text: String, err: Boolean = false) = Message(id, role, text, err)

    private fun errorOf(body: String): ChatError {
        try {
            GeminiResponseParser.parse(body)
        } catch (e: ChatException) {
            return e.error
        }
        fail("expected ChatException")
        throw IllegalStateException()
    }

    @Test fun builder_maps_roles_and_skips_errors() {
        val json = GeminiRequestBuilder.build(
            listOf(msg(1, Role.USER, "hola"), msg(2, Role.MODEL, "x", err = true), msg(3, Role.MODEL, "ok")),
            "sys"
        )
        val contents = json.getJSONArray("contents")
        assertEquals(2, contents.length())
        assertEquals("user", contents.getJSONObject(0).getString("role"))
        assertEquals("model", contents.getJSONObject(1).getString("role"))
        assertEquals("sys", json.getJSONObject("systemInstruction").getJSONArray("parts").getJSONObject(0).getString("text"))
    }

    @Test fun builder_escapes_special_text() {
        val text = "Ñandú \"comillas\"\nlínea 😀 \\"
        val json = GeminiRequestBuilder.build(listOf(msg(1, Role.USER, text)), "")
        val roundTrip = org.json.JSONObject(json.toString())
            .getJSONArray("contents").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")
        assertEquals(text, roundTrip)
        assertTrue(!json.has("systemInstruction"))
    }

    @Test fun parser_joins_parts() {
        val body = """{"candidates":[{"content":{"parts":[{"text":"Hola "},{"text":"mundo"}]},"finishReason":"STOP"}]}"""
        val r = GeminiResponseParser.parse(body)
        assertEquals("Hola mundo", r.text)
        assertEquals("STOP", r.finishReason)
    }

    @Test fun parser_block_reason() {
        val body = """{"promptFeedback":{"blockReason":"SAFETY"}}"""
        assertTrue(errorOf(body) is ChatError.Blocked)
    }

    @Test fun parser_safety_finish_without_content() {
        val body = """{"candidates":[{"finishReason":"SAFETY"}]}"""
        assertTrue(errorOf(body) is ChatError.Blocked)
    }

    @Test fun parser_api_error_429() {
        assertEquals(ChatError.RateLimited, errorOf("""{"error":{"code":429,"message":"quota"}}"""))
    }

    @Test fun parser_empty_candidates_and_bad_json() {
        assertEquals(ChatError.BadJson, errorOf("""{"candidates":[]}"""))
        assertEquals(ChatError.BadJson, errorOf("no es json"))
        assertEquals(ChatError.BadJson, errorOf("[1,2]"))
    }

    @Test fun trimmer_keeps_tail_starting_with_user() {
        val h = (1L..6L).map { msg(it, if (it % 2 == 1L) Role.USER else Role.MODEL, "m$it") }
        val t = ChatHistoryTrimmer.trim(h, 4) // m3..m6
        assertEquals(Role.USER, t.first().role)
        assertEquals("m3", t.first().text)
        assertEquals(4, t.size)
        // si el corte cae en un MODEL, se descarta
        assertEquals("m5", ChatHistoryTrimmer.trim(h, 3).first().text)
        assertEquals(6, ChatHistoryTrimmer.trim(h, 50).size)
    }

    @Test fun templates_apply_and_reject_blank() {
        assertNull(PromptTemplate.QUIZ.apply("   "))
        val p = PromptTemplate.QUIZ.apply("Intents")!!
        assertTrue(p.contains("Intents"))
        assertTrue(!p.contains("%s"))
    }
}
