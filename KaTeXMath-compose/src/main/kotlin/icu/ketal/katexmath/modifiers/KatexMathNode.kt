package icu.ketal.katexmath.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import icu.ketal.katexmath.MTFontManager
import icu.ketal.katexmath.MTMathView
import icu.ketal.katexmath.MTMathView.MTMathViewMode.KMTMathViewModeDisplay
import icu.ketal.katexmath.MTMathView.MTMathViewMode.KMTMathViewModeText
import icu.ketal.katexmath.parse.MTLineStyle
import icu.ketal.katexmath.parse.MTMathList
import icu.ketal.katexmath.parse.MTMathListBuilder
import icu.ketal.katexmath.parse.MTParseError
import icu.ketal.katexmath.parse.MTParseErrors
import icu.ketal.katexmath.render.MTMathListDisplay
import icu.ketal.katexmath.render.MTTypesetter
import kotlin.math.max

internal class KatexMathNode(
  private var latex: String,
  private var style: TextStyle,
  private var labelMode: MTMathView.MTMathViewMode = KMTMathViewModeDisplay,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode, CompositionLocalConsumerModifierNode, ObserverModifierNode {
  private var displayList: MTMathListDisplay? = null
  private val lastError = MTParseError()
  private var _mathList: MTMathList? = null

  private val currentStyle: MTLineStyle = when (labelMode) {
    KMTMathViewModeDisplay -> MTLineStyle.KMTLineStyleDisplay
    KMTMathViewModeText -> MTLineStyle.KMTLineStyleText
  }

  init {
    setLatex()
  }

  private fun setLatex() {
    _mathList = MTMathListBuilder.buildFromString(latex, lastError)
    displayList = null
  }

  override fun MeasureScope.measure(
    measurable: Measurable,
    constraints: Constraints
  ): MeasureResult {
    // defatlt font Size 20.sp
    val fontSize = style.fontSize.takeOrElse { 20.sp }.toPx()
    val font = MTFontManager.defaultFont().copyFontWithSize(fontSize)
    if (_mathList == null) {
      return layout(0, 0) {}
    }
    val dl = MTTypesetter.createLineForMathList(_mathList!!, font, currentStyle)
    val height: Float = dl.ascent + dl.descent
    val width: Float = dl.width
    val maxWidth = max(width.toInt(), constraints.maxWidth)
    val placeable = measurable.measure(constraints.copy(maxWidth = maxWidth, maxHeight = height.toInt()))
    return layout(placeable.width, placeable.height) {
      placeable.placeRelative(0, 0)
    }
  }

  override fun ContentDrawScope.draw() {
    if (displayError()) return
    val fontSize = style.fontSize.takeOrElse { 20.sp }.toPx()
    val font = MTFontManager.defaultFont().copyFontWithSize(fontSize)
    val dl = MTTypesetter.createLineForMathList(_mathList!!, font, currentStyle)
    dl.textColor = style.color.toArgb()
    var eqheight = dl.ascent + dl.descent
    if (eqheight < fontSize / 2) {
      // Set the height to the half the size of the font
      eqheight = fontSize / 2
    }
    // This will put center of vertical bounds to vertical center
    val textY = (size.height - eqheight) / 2 + dl.descent
    dl.position.y = textY
    drawIntoCanvas { canvas ->
      canvas.translate(0f, size.height)
      canvas.scale(1.0f, -1.0f)
      dl.draw(canvas.nativeCanvas)
    }
  }

  private fun displayError(): Boolean {
    return lastError.errorCode != MTParseErrors.ErrorNone
  }

  override fun onAttach() {
    updateDefaultValues()
  }

  override fun onObservedReadsChanged() {
    updateDefaultValues()
  }

  private fun updateDefaultValues() {
    observeReads {
      val context = currentValueOf(LocalContext)
      MTFontManager.setContext(context)
    }
  }
}