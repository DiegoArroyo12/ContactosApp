package mx.unam.contactosapp.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mx.unam.contactosapp.ui.theme.Mainbutton

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Mainbutton,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        content()
    }
}