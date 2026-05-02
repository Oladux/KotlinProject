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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import org.example.project.data.ThemeStorage
import kotlinx.coroutines.launch
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.CoroutineScope
import org.example.project.network.ProductRepository

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
            text = item.description,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
}

@Composable
fun ListScreen(
    shoppingList: MutableList<ShoppingListItem>,
    onAdd: (String) -> Unit,
    onDelete: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    repository: ProductRepository,
    scope: CoroutineScope,
    onLoadingChange: (Boolean) -> Unit,
    onLastProduct: (String) -> Unit
) {

    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        Text("Список покупок", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))


        Row {

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("Продукт") }
            )

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                if (input.isNotBlank()) {
                    onAdd(input)
                    input = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Добавлено")
                    }
                }
            }) {
                Text("+")
            }
        }

        Spacer(Modifier.height(12.dp))


        Button(onClick = {
            scope.launch {
                try {
                    onLoadingChange(true)

                    val result = repository.fetchProduct()

                    onLoadingChange(false)

                    result.onSuccess {
                        onLastProduct(it.title)
                        snackbarHostState.showSnackbar("OK: ${it.title}")
                    }

                    result.onFailure {
                        snackbarHostState.showSnackbar(it.message ?: "Ошибка")
                    }

                } catch (e: Exception) {
                    onLoadingChange(false)
                    snackbarHostState.showSnackbar("Crash: ${e.message}")
                }
            }
        }) {
            Text("Загрузить продукт из интернета")
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn {

            itemsIndexed(shoppingList) { index, item ->

                ShoppingListElement(
                    item = item,
                    onBoughtChange = {
                        shoppingList[index] = item.copy(bought = it)
                    },
                    onDelete = { onDelete(index) }
                )
            }
        }
    }
}@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {


    val repository = remember { ProductRepository() }

    val shoppingList = remember {
        mutableStateListOf(
            ShoppingListItem("Milk"),
            ShoppingListItem("Flour")
        )
    }

    val scope = rememberCoroutineScope()

    var isDarkTheme by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(Screen.LIST) }

    var loading by remember { mutableStateOf(false) }
    var lastProduct by remember { mutableStateOf<String?>(null) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkScheme else LightScheme
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                Screen.LIST -> "Список"
                                Screen.ABOUT -> "О приложении"
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            isDarkTheme = !isDarkTheme
                        }) {
                            Icon(
                                imageVector = if (isDarkTheme)
                                    Icons.Default.LightMode
                                else
                                    Icons.Default.DarkMode,
                                contentDescription = null
                            )
                        }},
                    navigationIcon = {
                        if (currentScreen == Screen.ABOUT) {
                            IconButton(onClick = {
                                currentScreen = Screen.LIST
                            }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->

            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                NavigationRail {

                    NavigationRailItem(
                        selected = currentScreen == Screen.LIST,
                        onClick = { currentScreen = Screen.LIST },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Список") }
                    )

                    NavigationRailItem(
                        selected = currentScreen == Screen.ABOUT,
                        onClick = { currentScreen = Screen.ABOUT },
                        icon = { Icon(Icons.Default.Info, null) },
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

                            if (loading) {
                                CircularProgressIndicator()
                            }

                            lastProduct?.let {
                                Text("Последний продукт: $it")
                                Spacer(Modifier.height(8.dp))
                            }

                            ListScreen(
                                shoppingList = shoppingList,
                                onAdd = { shoppingList.add(ShoppingListItem(it)) },
                                onDelete = { shoppingList.removeAt(it) },
                                snackbarHostState = snackbarHostState,
                                repository = repository,
                                scope = scope,
                                onLoadingChange = { loading = it },
                                onLastProduct = { lastProduct = it }
                            )
                        }

                        Screen.ABOUT -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Простое приложение списка покупок")
                            }
                        }
                    }
                }
            }
        }
    }
}