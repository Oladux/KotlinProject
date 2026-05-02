package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.text.style.TextDecoration

enum class Screen {
    LIST,
    ABOUT
}

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

@Composable
fun ListScreen(
    shoppingList: MutableList<ShoppingListItem>,
    onDeleteRequest: (Int) -> Unit,
    onAdd: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var newItemDesc by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            "Список покупок",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            OutlinedTextField(
                value = newItemDesc,
                onValueChange = { newItemDesc = it },
                modifier = Modifier.weight(1f),
                label = { Text("Название продукта") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (newItemDesc.isNotBlank()) {
                    onAdd(newItemDesc)
                    newItemDesc = ""

                    scope.launch {
                        snackbarHostState.showSnackbar("Добавлено")
                    }
                }
            }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(shoppingList) { index, item ->

                ShoppingListElement(
                    item = item,
                    onBoughtChange = { checked ->
                        shoppingList[index] = item.copy(bought = checked)
                    },
                    onDelete = {
                        onDeleteRequest(index)
                    }
                )
                
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

    var currentScreen by remember { mutableStateOf(Screen.LIST) }


    MaterialTheme(colorScheme = theme) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                Screen.LIST -> "Список покупок"
                                Screen.ABOUT -> "О приложении"
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentScreen == Screen.ABOUT) {
                            IconButton(onClick = {
                                currentScreen = Screen.LIST
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.LIST) {
                            IconButton(onClick = {
                                currentScreen = Screen.ABOUT
                            }) {
                                Icon(Icons.Default.Info, contentDescription = "О приложении")
                            }
                        }
                    }
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
                        TextButton(onClick = { itemToDeleteIndex = null }) {
                            Text("Нет")
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {


                NavigationRail {

                    NavigationRailItem(
                        selected = currentScreen == Screen.LIST,
                        onClick = { currentScreen = Screen.LIST },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Список") }
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.ABOUT,
                        onClick = { currentScreen = Screen.ABOUT },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("О программе") }
                    )
                }


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    when (currentScreen) {

                        Screen.LIST -> {
                            ListScreen(
                                shoppingList = shoppingList,
                                onDeleteRequest = { itemToDeleteIndex = it },
                                onAdd = {
                                    shoppingList.add(ShoppingListItem(it.trim()))
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        Screen.ABOUT -> {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("Это простое приложение\nдля списка покупок")
                            }
                        }
                    }
                }
            }
        }
    }
}