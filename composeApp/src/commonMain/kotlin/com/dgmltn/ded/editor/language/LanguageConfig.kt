package com.dgmltn.ded.editor.language


interface LanguageConfig {
    val tabSize: Int
    val wordRegex: Regex
}

class JavascriptLanguageConfig : LanguageConfig {
    override val tabSize = 2
    override val wordRegex: Regex
        get() = Regex("\\w")
}

class KotlinLanguageConfig : LanguageConfig {
    override val tabSize = 4
    override val wordRegex: Regex
        get() = Regex("\\w")
}
