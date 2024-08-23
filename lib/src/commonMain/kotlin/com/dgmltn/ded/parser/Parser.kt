package com.dgmltn.ded.parser

import androidx.compose.ui.graphics.Color
import com.dgmltn.ded.hexToColor
import ded.lib.generated.resources.Res
import oniguruma.OnigLib
import org.codroid.textmate.Registry
import org.codroid.textmate.RegistryOptions
import org.codroid.textmate.Token
import org.codroid.textmate.TokenizeLineResult
import org.codroid.textmate.Tokenizer
import org.codroid.textmate.parsePLIST
import org.codroid.textmate.parseRawGrammar
import org.codroid.textmate.theme.FontStyleConsts
import org.codroid.textmate.theme.ScopeStack
import org.codroid.textmate.theme.Theme
import org.jetbrains.compose.resources.ExperimentalResourceApi

data class ColorizedRange(val range: IntRange, val color: Color?)

class Parser(val langType: LanguageType, val themeType: ThemeType) {

    init {
        println("theme = $themeType")
    }

    private lateinit var tokenizer: Tokenizer
    private lateinit var theme: Theme
    private lateinit var colorMap: Map<Int, Color>

    // The result of parse()
    var parsedLines: List<TokenizeLineResult>? = null

    private suspend fun ensureTokenizer() {
        if (!::tokenizer.isInitialized) {
            tokenizer = loadTokenizer(langType)
        }
    }

    private suspend fun ensureTheme() {
        if (!::theme.isInitialized) {
            theme = loadTheme(themeType)

            colorMap = theme
                .getColorMap()
                .map { it.key.toInt() to it.value.hexToColor() }
                .toMap()
        }
    }

    suspend fun parse(lines: List<String>) {
        ensureTokenizer()
        ensureTheme()

        parsedLines = tokenizer
            .parse(lines)
    }

    fun getColorOf(lineIndex: Int, index: Int): Color? {
        if (parsedLines == null) return themeType.defaultFg
        if (!::theme.isInitialized) return themeType.defaultFg

        val styleAttributes = parsedLines
            ?.get(lineIndex)
            ?.tokens
            ?.firstOrNull { index >= it.startIndex && index < it.endIndex } //TODO: binary search?
            ?.let { theme.match(it) }
            ?.takeUnless { it.fontStyle == FontStyleConsts.NotSet }
            ?: theme.getDefaults()

        return styleAttributes.foregroundId.toInt().let { colorMap[it] }
    }

    private fun Theme.match(token: Token) = match(token.scopeStack())

    private fun Token.scopeStack() = ScopeStack.from(*scopes) //TODO: cache

    suspend fun demo(lines: List<String>) {
        ensureTokenizer()
        ensureTheme()

        println(colorMap.keys.joinToString(",") { "$it->${colorMap[it]}" })

        tokenizer
            .parse(lines)
            .map { it.toColors(theme) }
            .forEachIndexed { i, line ->
                line.forEach {
                    val substr = lines[i].substring(it.range)
                    val color = it.color
                    println("Line $i - Token from ${it.range} ( $substr ) with color ( $color )")
                }
            }
    }

    suspend fun getColors(lines: List<String>): List<List<ColorizedRange>> {
        ensureTokenizer()
        ensureTheme()

        return tokenizer
            .parse(lines)
            .map { it.toColors(theme) }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadTokenizer(type: LanguageType): Tokenizer {
        // Create a registry that can create a grammar from a scope name.
        val bytes = Res.readBytes(type.resLocation)
        val registry = Registry(RegistryOptions(
            regexLib = OnigLib(), // You must add the oniguruma-lib dependency, or use StandardRegex()
            loadGrammar = {
                if (it == type.initialScopeName) {
                    // It only accepts json and plist file.
                    return@RegistryOptions parseRawGrammar(bytes.inputStream(), type.resLocation)
                }
                return@RegistryOptions null
            }
        ))
        return registry.loadGrammar(type.initialScopeName)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadTheme(type: ThemeType): Theme {
        val themeBytes = Res.readBytes(type.resLocation)
        return Theme.createFromRawTheme(
            source = parsePLIST(themeBytes.inputStream())
        )
    }

    private fun TokenizeLineResult.toColors(theme: Theme) =
        tokens.map { token ->
            val scopePath = ScopeStack.from(token.scopes.last())
            val styleAttributes = theme.match(scopePath)
            val color = styleAttributes?.foregroundId?.toInt()?.let { colorMap[it] }
            val range = token.startIndex until token.endIndex
            ColorizedRange(range, color)
        }

    private fun Tokenizer.parse(lines: List<String>): List<TokenizeLineResult> =
        lines.scan(null) { previous: TokenizeLineResult?, line ->
            tokenizeLine(line, previous?.ruleStack, 0)
        }.filterNotNull()

}


