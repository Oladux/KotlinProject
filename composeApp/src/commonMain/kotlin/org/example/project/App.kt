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
import kotlinx.coroutines.launch
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.text.style.TextDecoration

data class ShoppingListItem(
    val description: String,
    val bought: Boolean = false
)

val LightScheme = lightColorScheme()
val DarkScheme = darkColorScheme()

@Composable
fun ShoppingListElement(
    item: ShoppingListItem,
    onBoughtChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = item.bought,
            onCheckedChange = onBoughtChange
        )

        Text(
            item.description,
            modifier = Modifier.weight(1f),
            textDecoration = if (item.bought) TextDecoration.LineThrough else null
        )

        if (!item.bought) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val darkThemeFlag = isSystemInDarkTheme()
    val theme = if (darkThemeFlag) DarkScheme else LightScheme

    val milkText = stringResource(Res.string.milk)
    val flourText = stringResource(Res.string.flour)

    val shoppingList = remember {
        mutableStateListOf(
            ShoppingListItem(milkText),
            ShoppingListItem(flourText)
        )
    }


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var itemToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    MaterialTheme(colorScheme = theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Список покупок") }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->

            if (itemToDeleteIndex != null) {
                AlertDialog(
                    onDismissRequest = { itemToDeleteIndex = null },
                    title = { Text("Удаление") },
                    text = { Text("Удалить элемент?") },
                    confirmButton = {
                        TextButton(onClick = {
                            itemToDeleteIndex?.let {
                                shoppingList.removeAt(it)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Удалено")
                                }
                            }
                            itemToDeleteIndex = null
                        }) {
                            Text("Да")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            itemToDeleteIndex = null
                        }) {
                            Text("Нет")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .safeDrawingPadding()
                    .padding(16.dp)
                    .fillMaxSize()
            ) {

                var newItemDesc by remember { mutableStateOf("") }

                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = newItemDesc,
                            onValueChange = { newItemDesc = it },
                            modifier = Modifier.padding(8.dp),
                            label = { Text("Название продукта") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (newItemDesc.isNotBlank()) {
                                        shoppingList.add(
                                            ShoppingListItem(newItemDesc.trim())
                                        )
                                        newItemDesc = ""
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Добавлено")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                                }
                            }
                        )
                    }
                    itemsIndexed(shoppingList) { i, item ->
                        ShoppingListElement(
                            item,
                            onBoughtChange = {
                                shoppingList[i] = item.copy(bought = it)
                            },
                            onDelete = {
                                itemToDeleteIndex = i
                            }
                        )
                    }
                }
            }
        }
    }
}