package com.dgmltn.ded.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Density

interface DedScope {
    @Composable
    fun Modifier.position(row: Int, col: Int) = this.then(
        DedChildDataElement(
            row = row,
            col = col,
            inspectorInfo = debugInspectorInfo {
                name = "Cell: row = $row, col = $col"
            }
        )
    )

    @Composable
    fun Cursor(
        row: Int,
        col: Int,
        color: Color,
    ) {
        Box(
            modifier = Modifier
                .position(row, col)
                .background(color)
        )
    }

    @Composable
    fun LineNumber(
        row: Int,
        color: Color,
    ) {
        // row+1 here because humans are used to 1-based line numbers
        CellGlyphs(row, 0, (row + 1).toString(), color)
    }

    @Composable
    fun CellGlyph(
        row: Int,
        col: Int,
        glyph: Char,
        color: Color,
    ) {
        Text(
            text = glyph.toString(),
            color = color,
            modifier = Modifier
                .position(row, col)
        )
    }

    @Composable
    fun CellGlyphs(
        row: Int,
        col: Int,
        glyphs: String,
        color: Color,
    ) {
        glyphs.toCharArray().forEachIndexed { index, c ->
            CellGlyph(
                row = row,
                col = col + index,
                glyph = c,
                color = color,
            )
        }
    }
}

class DedChildDataNode(
    var row: Int,
    var col: Int,
) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?) = this@DedChildDataNode
}

class DedChildDataElement(
    var row: Int,
    var col: Int,
    val inspectorInfo: InspectorInfo.() -> Unit

) : ModifierNodeElement<DedChildDataNode>() {
    override fun create() = DedChildDataNode(row, col)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? DedChildDataNode ?: return false
        return row == otherModifier.row &&
                col == otherModifier.col
    }

    override fun hashCode(): Int {
        var result = row.hashCode()
        result = 31 * result + col.hashCode()
        return result
    }

    override fun update(node: DedChildDataNode) {
        row = node.row
        col = node.col
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}
