package com.dgmltn.ded.parser


// Grammar textmate files: https://github.com/microsoft/vscode/blob/main/extensions/javascript/syntaxes/JavaScript.tmLanguage.json
// https://github.com/textmate/javascript.tmbundle/

enum class LanguageType(val resLocation: String, val initialScopeName: String) {
    Kotlin("files/Kotlin.tmLanguage.json", "source.kotlin"),
    Javascript("files/JavaScript.tmLanguage", "source.js")
}