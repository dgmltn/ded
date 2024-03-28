package com.dgmltn.ded.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
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
        col: Int
    ) {
        Box(modifier = Modifier.position(row, col).background(Color.Black))
    }

    @Composable
    fun CellGlyph(
        row: Int,
        col: Int,
        glyph: Char
    ) {
        Text(
            text = glyph.toString(),
            modifier = Modifier
                .position(row, col)
        )
    }

    @Composable
    fun CellGlyphs(
        row: Int,
        col: Int,
        glyphs: String,
    ) {
        glyphs.toCharArray().forEachIndexed { index, c ->
            CellGlyph(
                row = row,
                col = col + index,
                glyph = c
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
