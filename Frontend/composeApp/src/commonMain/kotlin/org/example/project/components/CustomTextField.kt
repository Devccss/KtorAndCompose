package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import frontend.composeapp.generated.resources.Res
import frontend.composeapp.generated.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String? = null,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val jetbrainsMono = FontFamily(Font(Res.font.jetbrains_mono_regular))

    Column{


        if (label != null) {
            Text(
                text = label,
                style = textStyle.copy(
                    fontFamily = jetbrainsMono,
                    fontSize = 12.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .background(Color.Transparent)
                .border(
                    width = 0.5.dp, // Borde muy delgado
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp) // Redondeado
                )
                .padding(vertical = 4.dp, horizontal = 6.dp), // Padding general
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(
                fontFamily = jetbrainsMono, // Tipografía solicitada
                fontSize = if (textStyle.fontSize.isSp) textStyle.fontSize else 14.sp,
                color = if (textStyle.color != Color.Unspecified) textStyle.color else Color.Black
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(Color.Gray), // Cursor gris
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(Color.Transparent),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholderText != null) {
                        Text(
                            text = placeholderText,
                            style = textStyle.copy(
                                fontFamily = jetbrainsMono,
                                fontSize = if (textStyle.fontSize.isSp) textStyle.fontSize else 14.sp,
                                color = Color.Gray
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
