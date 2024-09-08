package com.dgmltn.ded.parser

import androidx.compose.ui.graphics.Color


interface Parser {
    val language: LanguageType
    val theme: ThemeType
    fun parse(lines: List<String>)
    fun getColorOf(lineIndex: Int, index: Int): Color?
}