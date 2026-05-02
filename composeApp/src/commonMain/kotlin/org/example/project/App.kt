package org.example.project

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding

data class ShoppingListItem(
    val description: String,
    val bought: Boolean = false
)

val LightScheme = lightColorScheme()
val DarkScheme = darkColorScheme()

@Composable
fun ShoppingListElement(item: ShoppingListItem, onBoughtChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = item.bought,
            onCheckedChange = onBoughtChange
        )
        Text(item.description, Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
}

@Composable
fun App() {
    val darkThemeFlag = isSystemInDarkTheme()
    val theme = if (darkThemeFlag)
        DarkScheme
    else
        LightScheme
    val milkText = stringResource(Res.string.milk)
    val flourText = stringResource(Res.string.flour)
    val shoppingList = remember {
        mutableStateListOf(ShoppingListItem(milkText), ShoppingListItem(flourText))
    }

    MaterialTheme(colorScheme = theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(16.dp)
            ) {

                Text(
                    text = "🛒 Список покупок",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                var newItemDesc by remember { mutableStateOf("") }
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = newItemDesc, onValueChange = { newItemDesc = it },
                            modifier = Modifier.padding(8.dp),
                            label = {
                                Text("Название продукта")
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (newItemDesc.isNotBlank()) {
                                        shoppingList.add(ShoppingListItem(newItemDesc.trim()))
                                        newItemDesc = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                                }
                            })
                    }
                    itemsIndexed(shoppingList) { i, item ->
                        ShoppingListElement(
                            item,
                            onBoughtChange = {
                                shoppingList[i] = item.copy(bought = it)
                            },
                            onDelete = {
                                shoppingList.removeAt(i)
                            }
                        )
                    }
                }
            }
        }
    }
}