package com.unal.reto8

import com.unal.reto8.data.LikeEscaper
import org.junit.Assert.assertEquals
import org.junit.Test

class LikeEscaperTest {

    @Test fun escapesPercent() = assertEquals("%50\\%%", LikeEscaper.contains("50%"))

    @Test fun escapesUnderscore() = assertEquals("%a\\_b%", LikeEscaper.contains("a_b"))

    @Test fun escapesBackslash() = assertEquals("%a\\\\b%", LikeEscaper.contains("a\\b"))

    @Test fun trimsSpaces() = assertEquals("%x%", LikeEscaper.contains("  x "))
}
