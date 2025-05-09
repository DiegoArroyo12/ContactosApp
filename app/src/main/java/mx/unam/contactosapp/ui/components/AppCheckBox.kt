package mx.unam.contactosapp.ui.components

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mx.unam.contactosapp.ui.theme.Mainbutton

@Composable
fun AppCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = CheckboxDefaults.colors(
            checkedColor = Mainbutton,
            uncheckedColor = Color.White,
            checkmarkColor = Color.Black
        )
    )
}