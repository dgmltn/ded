package com.dgmltn.ded.parser

import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.RawThemeSetting
import org.codroid.textmate.theme.Setting


class SettingDef {
    var name: String? = null
    var scope: Array<String>? = null
    var fontStyle: String? = null
    var foreground: String? = null
    var background: String? = null
}

class ThemeDef {
    var name: String? = null
    val settings = mutableListOf<RawThemeSetting>()
    fun setting(init: SettingDef.() -> Unit) {
        val settingDef = SettingDef()
        settingDef.init()
        settings.add(RawThemeSetting(
            name = settingDef.name,
            scope = settingDef.scope,
            settings = Setting(
                fontStyle = settingDef.fontStyle,
                foreground = settingDef.foreground,
                background = settingDef.background
            )
        ))
    }
}

fun theme(init: ThemeDef.() -> Unit): RawTheme {
    val themeDef = ThemeDef()
    themeDef.init()
    return RawTheme(
        name = themeDef.name,
        settings = themeDef.settings.toTypedArray()
    )
}

fun RawThemeSetting.toDsl(): String = """    setting {
        name = ${name.quotedStringOrNull()}
        scope = ${scope.csvOrNull()}
        fontStyle = ${settings?.fontStyle.quotedStringOrNull()}
        foreground = ${settings?.foreground.quotedStringOrNull()}
        background = ${settings?.background.quotedStringOrNull()}
    }
"""

private fun <T> Array<T>?.csvOrNull() = this?.run { "arrayOf(" + joinToString(", ") { "\"${it.toString().trim()}\"" } + ")" } ?: "null"
private fun String?.quotedStringOrNull() = this?.let { "\"$it\"" } ?: "null"

fun RawTheme.toDsl(): String = """
theme {
    name = ${name.quotedStringOrNull()}
${settings?.joinToString("") { it: RawThemeSetting -> it.toDsl() }}}
"""