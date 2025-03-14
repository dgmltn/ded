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
import org.codroid.textmate.theme.RawTheme
import org.codroid.textmate.theme.RawThemeSetting
import org.codroid.textmate.theme.ScopeStack
import org.codroid.textmate.theme.Setting
import org.codroid.textmate.theme.Theme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class ColorizedRange(val range: IntRange, val color: Color?)

class TextMateParser(
    override val language: LanguageType,
    override val theme: ThemeType
): Parser {

    private lateinit var tokenizer: Tokenizer
    private lateinit var tmTheme: Theme
    private lateinit var colorMap: Map<Int, Color>

    // The result of parse()
    private var parsedLines: List<TokenizeLineResult>? = null

    private fun ensureTokenizer() {
        if (!::tokenizer.isInitialized) {
            tokenizer = loadTokenizer(language)
        }
    }

    private fun ensureTheme() {
        if (!::tmTheme.isInitialized) {
            tmTheme = loadTheme(theme)

            colorMap = tmTheme
                .getColorMap()
                .map { it.key.toInt() to it.value.hexToColor() }
                .toMap()
        }
    }

    override fun parse(lines: List<String>) {
        ensureTokenizer()
        ensureTheme()

        parsedLines = tokenizer
            .parse(lines)
    }

    override fun getColorOf(lineIndex: Int, index: Int): Color? {
        if (parsedLines == null) return theme.defaultFg
        if (!::tmTheme.isInitialized) return theme.defaultFg

        val styleAttributes = parsedLines
            ?.get(lineIndex)
            ?.tokens
            ?.firstOrNull { index >= it.startIndex && index < it.endIndex } //TODO: binary search?
            ?.let { tmTheme.match(it) }
            ?.takeUnless { it.fontStyle == FontStyleConsts.NotSet }
            ?: tmTheme.getDefaults()

        return styleAttributes.foregroundId.toInt().let { colorMap[it] }
    }

    private fun Theme.match(token: Token) = match(token.scopeStack())

    private fun Token.scopeStack() = ScopeStack.from(*scopes) //TODO: cache

    fun demo(lines: List<String>) {
        ensureTokenizer()
        ensureTheme()

        println(colorMap.keys.joinToString(",") { "$it->${colorMap[it]}" })

        tokenizer
            .parse(lines)
            .map { it.toColors(tmTheme) }
            .forEachIndexed { i, line ->
                line.forEach {
                    val substr = lines[i].substring(it.range)
                    val color = it.color
                    println("Line $i - Token from ${it.range} ( $substr ) with color ( $color )")
                }
            }
    }

    fun getColors(lines: List<String>): List<List<ColorizedRange>> {
        ensureTokenizer()
        ensureTheme()

        return tokenizer
            .parse(lines)
            .map { it.toColors(tmTheme) }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun loadTokenizer(type: LanguageType): Tokenizer {
        // Create a registry that can create a grammar from a scope name.
        val bytes = Base64.decode(type.base64Theme) //Res.readBytes(type.resLocation)
        val registry = Registry(RegistryOptions(
            regexLib = OnigLib(), // You must add the oniguruma-lib dependency, or use StandardRegex()
            loadGrammar = {
                if (it == type.initialScopeName) {
                    // It only accepts json and plist file.
                    val rawGrammar = parseRawGrammar(bytes.inputStream(), type.initialScopeName)
                    return@RegistryOptions rawGrammar
                }
                return@RegistryOptions null
            }
        ))
        return registry.loadGrammar(type.initialScopeName)
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun printOutTheme(resLocation: String = "files/Bespin.tmTheme") {
        val themeBytes = Res.readBytes(resLocation)

        val rawTheme = parsePLIST<RawTheme>(themeBytes.inputStream())
        println(rawTheme.toDsl())
    }

    private fun loadTheme(type: ThemeType): Theme {
        return Theme.createFromRawTheme(source = type.rawTheme)
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


