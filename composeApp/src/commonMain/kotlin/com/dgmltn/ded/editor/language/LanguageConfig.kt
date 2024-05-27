package com.dgmltn.ded.editor.language


interface LanguageConfig {
    val tabSize: Int
}

class JavascriptLanguageConfig : LanguageConfig {
    override val tabSize = 2
}

class KotlinLanguageConfig : LanguageConfig {
    override val tabSize = 4
}
