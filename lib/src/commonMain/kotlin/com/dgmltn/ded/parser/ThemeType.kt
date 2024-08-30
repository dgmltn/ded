package com.dgmltn.ded.parser

import androidx.compose.runtime.sourceInformationMarkerEnd
import androidx.compose.ui.graphics.Color
import com.dgmltn.ded.hexToColor
import org.codroid.textmate.theme.RawTheme

// Theme textmate files: https://github.com/filmgirl/TextMate-Themes

enum class ThemeType(
    val rawTheme: RawTheme,
    val defaultFg: Color,
    val defaultBg: Color,
    val cursor: Color,
    val selection: Color,
    val guide: Color = defaultFg.copy(alpha = 0.5f),
) {
    Abyss(
        rawTheme = abyss,
        defaultFg = "#6688cc".hexToColor(),
        defaultBg = "#000c18".hexToColor(),
        cursor = "#ddbb88".hexToColor(),
        selection = "#770811".hexToColor(),
    ),
    Bespin(
        rawTheme = bespin,
        defaultFg = "#BAAE9E".hexToColor(),
        defaultBg = "#28211C".hexToColor(),
        cursor = "#A7A7A7".hexToColor(),
        selection = "#DDF0FF33".hexToColor(),
    ),
    MadeOfCode(
        rawTheme = madeOfCode,
        defaultFg = "#F8F8F8".hexToColor(),
        defaultBg = "#090A1BF2".hexToColor(),
        cursor = "#00FFFF".hexToColor(),
        selection = "#007DFF80".hexToColor(),
    )
}

val abyss = theme {
    name = "Abyss"
    setting {
        name = null
        scope = null
        fontStyle = null
        foreground = "#6688cc"
        background = "#000c18"
    }
    setting {
        name = "Comment"
        scope = arrayOf("comment")
        fontStyle = null
        foreground = "#223355"
        background = null
    }
    setting {
        name = "String"
        scope = arrayOf("string")
        fontStyle = null
        foreground = "#22aa44"
        background = null
    }
    setting {
        name = "Number"
        scope = arrayOf("constant.numeric")
        fontStyle = null
        foreground = "#f280d0"
        background = null
    }
    setting {
        name = "Built-in constant"
        scope = arrayOf("constant.language")
        fontStyle = null
        foreground = "#f280d0"
        background = null
    }
    setting {
        name = "User-defined constant"
        scope = arrayOf("constant.character", "constant.other")
        fontStyle = null
        foreground = "#f280d0"
        background = null
    }
    setting {
        name = "Variable"
        scope = arrayOf("variable")
        fontStyle = ""
        foreground = null
        background = null
    }
    setting {
        name = "Keyword"
        scope = arrayOf("keyword")
        fontStyle = null
        foreground = "#225588"
        background = null
    }
    setting {
        name = "Storage"
        scope = arrayOf("storage")
        fontStyle = ""
        foreground = "#225588"
        background = null
    }
    setting {
        name = "Storage type"
        scope = arrayOf("storage.type")
        fontStyle = "italic"
        foreground = "#9966b8"
        background = null
    }
    setting {
        name = "Class name"
        scope = arrayOf("entity.name.class", "entity.name.type")
        fontStyle = "underline"
        foreground = "#ffeebb"
        background = null
    }
    setting {
        name = "Inherited class"
        scope = arrayOf("entity.other.inherited-class")
        fontStyle = "italic underline"
        foreground = "#ddbb88"
        background = null
    }
    setting {
        name = "Function name"
        scope = arrayOf("entity.name.function")
        fontStyle = ""
        foreground = "#ddbb88"
        background = null
    }
    setting {
        name = "Function argument"
        scope = arrayOf("variable.parameter")
        fontStyle = "italic"
        foreground = "#2277ff"
        background = null
    }
    setting {
        name = "Tag name"
        scope = arrayOf("entity.name.tag")
        fontStyle = ""
        foreground = "#225588"
        background = null
    }
    setting {
        name = "Tag attribute"
        scope = arrayOf("entity.other.attribute-name")
        fontStyle = ""
        foreground = "#ddbb88"
        background = null
    }
    setting {
        name = "Library function"
        scope = arrayOf("support.function")
        fontStyle = ""
        foreground = "#9966b8"
        background = null
    }
    setting {
        name = "Library constant"
        scope = arrayOf("support.constant")
        fontStyle = ""
        foreground = "#9966b8"
        background = null
    }
    setting {
        name = "Library class/type"
        scope = arrayOf("support.type", "support.class")
        fontStyle = "italic"
        foreground = "#9966b8"
        background = null
    }
    setting {
        name = "Library variable"
        scope = arrayOf("support.other.variable")
        fontStyle = ""
        foreground = null
        background = null
    }
    setting {
        name = "Invalid"
        scope = arrayOf("invalid")
        fontStyle = ""
        foreground = "#F8F8F0"
        background = "#F92672"
    }
    setting {
        name = "Invalid deprecated"
        scope = arrayOf("invalid.deprecated")
        fontStyle = null
        foreground = "#F8F8F0"
        background = "#AE81FF"
    }
    setting {
        name = "Markup Quote"
        scope = arrayOf("markup.quote")
        fontStyle = null
        foreground = "#22aa44"
        background = null
    }
    setting {
        name = "Markup Styling"
        scope = arrayOf("markup.bold", "markup.italic")
        fontStyle = null
        foreground = "#22aa44"
        background = null
    }
    setting {
        name = "Markup Inline"
        scope = arrayOf("markup.inline.raw")
        fontStyle = ""
        foreground = "#9966b8"
        background = null
    }
    setting {
        name = "Markup Setext Header"
        scope = arrayOf("markup.heading.setext")
        fontStyle = ""
        foreground = "#ddbb88"
        background = null
    }
}

val bespin = theme {
    name = "Bespin"
    setting {
        name = null
        scope = null
        fontStyle = null
        foreground = "#BAAE9E"
        background = "#28211C"
    }
    setting {
        name = "Comment"
        scope = arrayOf("comment")
        fontStyle = "italic"
        foreground = "#666666"
        background = null
    }
    setting {
        name = "Constant"
        scope = arrayOf("constant")
        fontStyle = null
        foreground = "#CF6A4C"
        background = null
    }
    setting {
        name = "Entity"
        scope = arrayOf("entity")
        fontStyle = ""
        foreground = "#937121"
        background = null
    }
    setting {
        name = "Keyword"
        scope = arrayOf("keyword")
        fontStyle = ""
        foreground = "#5EA6EA"
        background = null
    }
    setting {
        name = "Storage"
        scope = arrayOf("storage")
        fontStyle = ""
        foreground = "#F9EE98"
        background = null
    }
    setting {
        name = "String"
        scope = arrayOf("string")
        fontStyle = ""
        foreground = "#54BE0D"
        background = null
    }
    setting {
        name = "Support"
        scope = arrayOf("support")
        fontStyle = ""
        foreground = "#9B859D"
        background = null
    }
    setting {
        name = "Variable"
        scope = arrayOf("variable")
        fontStyle = null
        foreground = "#7587A6"
        background = null
    }
    setting {
        name = "Invalid – Deprecated"
        scope = arrayOf("invalid.deprecated")
        fontStyle = "italic underline"
        foreground = "#D2A8A1"
        background = null
    }
    setting {
        name = "Invalid – Illegal"
        scope = arrayOf("invalid.illegal")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#562D56BF"
    }
    setting {
        name = "-----------------------------------"
        scope = null
        fontStyle = null
        foreground = null
        background = null
    }
    setting {
        name = "♦ Embedded Source"
        scope = arrayOf("text source")
        fontStyle = null
        foreground = null
        background = "#B0B3BA14"
    }
    setting {
        name = "♦ Embedded Source (Bright)"
        scope = arrayOf("text.html.ruby source")
        fontStyle = null
        foreground = null
        background = "#B1B3BA21"
    }
    setting {
        name = "♦ Entity inherited-class"
        scope = arrayOf("entity.other.inherited-class")
        fontStyle = "italic"
        foreground = "#9B5C2E"
        background = null
    }
    setting {
        name = "♦ String embedded-source"
        scope = arrayOf("string source")
        fontStyle = ""
        foreground = "#DAEFA3"
        background = null
    }
    setting {
        name = "♦ String constant"
        scope = arrayOf("string constant")
        fontStyle = null
        foreground = "#DDF2A4"
        background = null
    }
    setting {
        name = "♦ String.regexp"
        scope = arrayOf("string.regexp")
        fontStyle = ""
        foreground = "#E9C062"
        background = null
    }
    setting {
        name = "♦ String.regexp.«special»"
        scope = arrayOf("string.regexp constant.character.escape", "string.regexp source.ruby.embedded", "string.regexp string.regexp.arbitrary-repitition")
        fontStyle = null
        foreground = "#CF7D34"
        background = null
    }
    setting {
        name = "♦ String variable"
        scope = arrayOf("string variable")
        fontStyle = null
        foreground = "#8A9A95"
        background = null
    }
    setting {
        name = "♦ Support.function"
        scope = arrayOf("support.function")
        fontStyle = ""
        foreground = "#DAD085"
        background = null
    }
    setting {
        name = "♦ Support.constant"
        scope = arrayOf("support.constant")
        fontStyle = ""
        foreground = "#CF6A4C"
        background = null
    }
    setting {
        name = "c C/C++ Preprocessor Line"
        scope = arrayOf("meta.preprocessor.c")
        fontStyle = null
        foreground = "#8996A8"
        background = null
    }
    setting {
        name = "c C/C++ Preprocessor Directive"
        scope = arrayOf("meta.preprocessor.c keyword")
        fontStyle = null
        foreground = "#AFC4DB"
        background = null
    }
    setting {
        name = "✘ Doctype/XML Processing"
        scope = arrayOf("meta.tag.sgml.doctype", "meta.tag.sgml.doctype entity", "meta.tag.sgml.doctype string", "meta.tag.preprocessor.xml", "meta.tag.preprocessor.xml entity", "meta.tag.preprocessor.xml string")
        fontStyle = null
        foreground = "#5EA6EA"
        background = null
    }
    setting {
        name = "✘ Meta.tag.«all»"
        scope = arrayOf("declaration.tag", "declaration.tag entity", "meta.tag", "meta.tag entity")
        fontStyle = null
        foreground = "#AC885B"
        background = null
    }
    setting {
        name = "§ css tag-name"
        scope = arrayOf("meta.selector.css entity.name.tag")
        fontStyle = ""
        foreground = "#CDA869"
        background = null
    }
    setting {
        name = "§ css:pseudo-class"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.tag.pseudo-class")
        fontStyle = null
        foreground = "#8F9D6A"
        background = null
    }
    setting {
        name = "§ css#id"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.id")
        fontStyle = null
        foreground = "#8B98AB"
        background = null
    }
    setting {
        name = "§ css.class"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.class")
        fontStyle = null
        foreground = "#9B703F"
        background = null
    }
    setting {
        name = "§ css property-name:"
        scope = arrayOf("support.type.property-name.css")
        fontStyle = null
        foreground = "#C5AF75"
        background = null
    }
    setting {
        name = "§ css property-value;"
        scope = arrayOf("meta.property-group support.constant.property-value.css", "meta.property-value support.constant.property-value.css")
        fontStyle = null
        foreground = "#F9EE98"
        background = null
    }
    setting {
        name = "§ css @at-rule"
        scope = arrayOf("meta.preprocessor.at-rule keyword.control.at-rule")
        fontStyle = null
        foreground = "#8693A5"
        background = null
    }
    setting {
        name = "§ css additional-constants"
        scope = arrayOf("meta.property-value support.constant.named-color.css", "meta.property-value constant")
        fontStyle = null
        foreground = "#CA7840"
        background = null
    }
    setting {
        name = "§ css constructor.argument"
        scope = arrayOf("meta.constructor.argument.css")
        fontStyle = null
        foreground = "#8F9D6A"
        background = null
    }
    setting {
        name = "⎇ diff.header"
        scope = arrayOf("meta.diff", "meta.diff.header", "meta.separator")
        fontStyle = "italic"
        foreground = "#F8F8F8"
        background = "#0E2231"
    }
    setting {
        name = "⎇ diff.deleted"
        scope = arrayOf("markup.deleted")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#420E09"
    }
    setting {
        name = "⎇ diff.changed"
        scope = arrayOf("markup.changed")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#4A410D"
    }
    setting {
        name = "⎇ diff.inserted"
        scope = arrayOf("markup.inserted")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#253B22"
    }
    setting {
        name = "Markup: List"
        scope = arrayOf("markup.list")
        fontStyle = null
        foreground = "#F9EE98"
        background = null
    }
    setting {
        name = "Markup: Heading"
        scope = arrayOf("markup.heading")
        fontStyle = null
        foreground = "#CF6A4C"
        background = null
    }
    setting {
        name = "tag name"
        scope = arrayOf("entity.name.tag")
        fontStyle = null
        foreground = "#5EA6EA"
        background = null
    }
}

val madeOfCode = theme {
    name = "Made of Code"
    setting {
        name = null
        scope = null
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#090A1BF2"
    }
    setting {
        name = "Comment"
        scope = arrayOf("comment")
        fontStyle = "italic"
        foreground = "#C050C2"
        background = "#000000"
    }
    setting {
        name = "Constant"
        scope = arrayOf("constant")
        fontStyle = ""
        foreground = "#0A9CFF"
        background = null
    }
    setting {
        name = "Entity"
        scope = arrayOf("entity")
        fontStyle = ""
        foreground = "#6FD3FF"
        background = null
    }
    setting {
        name = "Keyword"
        scope = arrayOf("keyword")
        fontStyle = ""
        foreground = "#FF3854"
        background = null
    }
    setting {
        name = "Storage"
        scope = arrayOf("storage")
        fontStyle = ""
        foreground = "#99CF50"
        background = null
    }
    setting {
        name = "String"
        scope = arrayOf("string")
        fontStyle = ""
        foreground = "#8FFF58"
        background = "#102622FA"
    }
    setting {
        name = "Support"
        scope = arrayOf("support")
        fontStyle = ""
        foreground = "#00FFBC"
        background = null
    }
    setting {
        name = "Variable"
        scope = arrayOf("variable")
        fontStyle = ""
        foreground = "#588AFF"
        background = null
    }
    setting {
        name = "Invalid – Deprecated"
        scope = arrayOf("invalid.deprecated")
        fontStyle = "italic underline"
        foreground = "#FD5FF1"
        background = null
    }
    setting {
        name = "Invalid – Illegal"
        scope = arrayOf("invalid.illegal")
        fontStyle = null
        foreground = "#FD5FF1"
        background = "#562D56BF"
    }
    setting {
        name = "-----------------------------------"
        scope = null
        fontStyle = null
        foreground = null
        background = null
    }
    setting {
        name = "♦ Embedded Source (Bright)"
        scope = arrayOf("text source")
        fontStyle = ""
        foreground = null
        background = "#B1B3BA08"
    }
    setting {
        name = "♦ Entity inherited-class"
        scope = arrayOf("entity.other.inherited-class")
        fontStyle = "italic"
        foreground = "#FF9749"
        background = null
    }
    setting {
        name = "♦ String embedded-source"
        scope = arrayOf("string.quoted source")
        fontStyle = ""
        foreground = "#D972DE"
        background = null
    }
    setting {
        name = "♦ String constant"
        scope = arrayOf("string constant")
        fontStyle = null
        foreground = "#D972DE"
        background = null
    }
    setting {
        name = "♦ String.regexp"
        scope = arrayOf("string.regexp")
        fontStyle = null
        foreground = "#E9C062"
        background = null
    }
    setting {
        name = "♦ String.regexp.«special»"
        scope = arrayOf("string.regexp constant.character.escape", "string.regexp source.ruby.embedded", "string.regexp string.regexp.arbitrary-repitition")
        fontStyle = null
        foreground = "#CF7D34"
        background = null
    }
    setting {
        name = "♦ String variable"
        scope = arrayOf("string variable")
        fontStyle = null
        foreground = "#8A9A95"
        background = null
    }
    setting {
        name = "♦ Support.function"
        scope = arrayOf("support.function")
        fontStyle = ""
        foreground = "#F1D950"
        background = null
    }
    setting {
        name = "♦ Support.constant"
        scope = arrayOf("support.constant")
        fontStyle = ""
        foreground = "#CF6A4C"
        background = null
    }
    setting {
        name = "c C/C++ Preprocessor Line"
        scope = arrayOf("meta.preprocessor.c")
        fontStyle = null
        foreground = "#8996A8"
        background = null
    }
    setting {
        name = "c C/C++ Preprocessor Directive"
        scope = arrayOf("meta.preprocessor.c keyword")
        fontStyle = null
        foreground = "#AFC4DB"
        background = null
    }
    setting {
        name = "j Entity Name Type"
        scope = arrayOf("entity.name.type")
        fontStyle = "underline"
        foreground = null
        background = null
    }
    setting {
        name = "j Cast"
        scope = arrayOf("meta.cast")
        fontStyle = "italic"
        foreground = "#676767"
        background = null
    }
    setting {
        name = "✘ Doctype/XML Processing"
        scope = arrayOf("meta.sgml.html meta.doctype", "meta.sgml.html meta.doctype entity", "meta.sgml.html meta.doctype string", "meta.xml-processing", "meta.xml-processing entity", "meta.xml-processing string")
        fontStyle = null
        foreground = "#494949"
        background = null
    }
    setting {
        name = "✘ Meta.tag.all"
        scope = arrayOf("meta.tag", "meta.tag entity")
        fontStyle = null
        foreground = "#45C1EA"
        background = null
    }
    setting {
        name = "✘ Meta.tag.inline"
        scope = arrayOf("source entity.name.tag", "source entity.other.attribute-name", "meta.tag.inline", "meta.tag.inline entity")
        fontStyle = ""
        foreground = "#45C1EA"
        background = null
    }
    setting {
        name = "✘ Namespaces"
        scope = arrayOf("entity.name.tag.namespace", "entity.other.attribute-name.namespace")
        fontStyle = ""
        foreground = "#E18964"
        background = null
    }
    setting {
        name = "§ css tag-name"
        scope = arrayOf("meta.selector.css entity.name.tag")
        fontStyle = ""
        foreground = "#8B98AB"
        background = null
    }
    setting {
        name = "§ css:pseudo-class"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.tag.pseudo-class")
        fontStyle = ""
        foreground = "#8B98AB"
        background = null
    }
    setting {
        name = "§ css#id"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.id")
        fontStyle = ""
        foreground = "#8B98AB"
        background = null
    }
    setting {
        name = "§ css.class"
        scope = arrayOf("meta.selector.css entity.other.attribute-name.class")
        fontStyle = ""
        foreground = "#8B98AB"
        background = null
    }
    setting {
        name = "§ css property-name:"
        scope = arrayOf("support.type.property-name.css")
        fontStyle = ""
        foreground = "#C5AF75"
        background = null
    }
    setting {
        name = "§ css property-value;"
        scope = arrayOf("meta.property-group support.constant.property-value.css", "meta.property-value support.constant.property-value.css")
        fontStyle = ""
        foreground = "#F9EE98"
        background = null
    }
    setting {
        name = "§ css @at-rule"
        scope = arrayOf("meta.preprocessor.at-rule keyword.control.at-rule")
        fontStyle = ""
        foreground = "#8693A5"
        background = null
    }
    setting {
        name = "§ css additional-constants"
        scope = arrayOf("meta.property-value support.constant.named-color.css", "meta.property-value constant")
        fontStyle = ""
        foreground = "#FF6A4B"
        background = null
    }
    setting {
        name = "§ css constructor.argument"
        scope = arrayOf("meta.constructor.argument.css")
        fontStyle = null
        foreground = "#8F9D6A"
        background = null
    }
    setting {
        name = "⎇ diff.header"
        scope = arrayOf("meta.diff", "meta.diff.header")
        fontStyle = "italic"
        foreground = "#F8F8F8"
        background = "#00184F"
    }
    setting {
        name = "⎇ diff.deleted"
        scope = arrayOf("markup.deleted")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#74052A"
    }
    setting {
        name = "⎇ diff.changed"
        scope = arrayOf("markup.changed")
        fontStyle = ""
        foreground = "#F8F8F8"
        background = "#A96A06"
    }
    setting {
        name = "⎇ diff.inserted"
        scope = arrayOf("markup.inserted")
        fontStyle = null
        foreground = "#F8F8F8"
        background = "#008A41"
    }
    setting {
        name = "--------------------------------"
        scope = null
        fontStyle = null
        foreground = null
        background = null
    }
    setting {
        name = "Markup: Italic"
        scope = arrayOf("markup.italic")
        fontStyle = "italic"
        foreground = "#E9C062"
        background = null
    }
    setting {
        name = "Markup: Bold"
        scope = arrayOf("markup.bold")
        fontStyle = ""
        foreground = "#E9C062"
        background = null
    }
    setting {
        name = "Markup: Underline"
        scope = arrayOf("markup.underline")
        fontStyle = "underline"
        foreground = "#E18964"
        background = null
    }
    setting {
        name = "Markup: Quote"
        scope = arrayOf("markup.quote")
        fontStyle = "italic"
        foreground = "#E1D4B9"
        background = "#FEE09C12"
    }
    setting {
        name = "Markup: Heading"
        scope = arrayOf("markup.heading", "markup.heading entity")
        fontStyle = ""
        foreground = "#FEDCC5"
        background = "#632D04"
    }
    setting {
        name = "Markup: List"
        scope = arrayOf("markup.list")
        fontStyle = null
        foreground = "#E1D4B9"
        background = null
    }
    setting {
        name = "Markup: Raw"
        scope = arrayOf("markup.raw")
        fontStyle = ""
        foreground = "#578BB3"
        background = "#B1B3BA08"
    }
    setting {
        name = "Markup: Comment"
        scope = arrayOf("markup comment")
        fontStyle = "italic"
        foreground = "#F67B37"
        background = null
    }
    setting {
        name = "Markup: Separator"
        scope = arrayOf("meta.separator")
        fontStyle = null
        foreground = "#60A633"
        background = "#242424"
    }
    setting {
        name = "Log Entry"
        scope = arrayOf("meta.line.entry.logfile", "meta.line.exit.logfile")
        fontStyle = null
        foreground = null
        background = "#EEEEEE29"
    }
    setting {
        name = "Log Entry Error"
        scope = arrayOf("meta.line.error.logfile")
        fontStyle = null
        foreground = null
        background = "#751012"
    }
}
