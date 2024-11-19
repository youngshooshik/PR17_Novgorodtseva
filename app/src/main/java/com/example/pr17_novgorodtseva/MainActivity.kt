package com.example.pr17_novgorodtseva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.pr17_novgorodtseva.ui.theme.PR17_NovgorodtsevaTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PR17_NovgorodtsevaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000) // Задержка 3 секунды
        isLoading = false // После задержки, переключаем состояние
    }

    if (isLoading) {
        SplashScreen() // Показываем экран загрузки
    } else {
        NavHost(navController = navController, startDestination = "login_screen") {
            composable("login_screen") { LoginScreen(navController) }
            composable("signup_screen") { SignUpScreen(navController) }
            composable("home_screen") { HomeScreen(navController) }
            composable("welcome_screen") { WelcomeScreen() }
            composable("wait_timer_screen") { WaitTimerScreen(navController) }
            composable("history_screen") { HistoryScreen(navController) } // Экран History
            composable("settings_screen") { SettingsScreen(navController) } // Экран Settings
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            // Drawer Content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .background(Color(0xFF2D2942))
                    .padding(16.dp)
            ) {
                // Профиль пользователя
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_13), // Ваше изображение профиля
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Wirtz Florian", color = Color.White)
                        Text("Driver", color = Color.White)
                    }
                }

                // Меню
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("history_screen")
                }) {
                    Icon(painter = painterResource(id = R.drawable.img_11), contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("History", color = Color.White)
                }

                TextButton(onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("settings_screen")
                }) {
                    Icon(painter = painterResource(id = R.drawable.img_12), contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Settings", color = Color.White)
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Left Image (с zIndex)
            Image(
                painter = painterResource(id = R.drawable.img_10), // Ваше изображение
                contentDescription = "Open Drawer",
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .zIndex(1f)
                    .clickable {
                        // Переключение состояния ящика
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    }
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    // Карта
                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize(),
                        update = { mapView ->
                            mapView.getMapAsync(OnMapReadyCallback { map ->
                                googleMap = map
                                val novosibirsk = LatLng(55.0084, 82.9357)
                                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(novosibirsk, 10f))
                            })
                        }
                    )
                }

                // Кнопки увеличения и уменьшения масштаба карты
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
                    }) {
                        Text(text = "+")
                    }

                    Button(onClick = {
                        googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
                    }) {
                        Text(text = "-")
                    }
                }

                // Центральное изображение
                Image(
                    painter = painterResource(id = R.drawable.img_6),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            showBottomSheet = true
                        }
                )
            }

            // Bottom Sheet
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color(0xFF2D2942)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Верхняя часть с иконками
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.img_7),
                                    contentDescription = "Car",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text("Kia Rio", color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.img_8),
                                    contentDescription = "Distance",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text("1.5 km", color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.img_9),
                                    contentDescription = "Free time",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text("5 min-free", color = Color.White)
                            }
                        }

                        // Цены
                        Text("Price:", color = Color.White, modifier = Modifier.align(Alignment.Start))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFC107))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Minute", color = Color.Black)
                                Text("1 min - $1", color = Color.Black)
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Hour", color = Color.Black)
                                Text("60 min - $50", color = Color.Black)
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Day", color = Color.Black)
                                Text("1440 min - $300", color = Color.Black)
                            }
                        }

                        // Кнопка "BOOK"
                        Button(
                            onClick = {
                                showBottomSheet = false
                                navController.navigate("wait_timer_screen")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                        ) {
                            Text("BOOK", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Opt-in to the experimental API
@Composable
fun HistoryScreen(navController: NavController) {
    // Scaffold allows for convenient placement of UI elements, including the top bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.img_14), // Use icon from drawable
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF2D2942)) // Задаем цвет фонаar
            )
        }
    ) { innerPadding ->
        // Main content of the screen, considering inner padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Take padding into account for content
                .padding(16.dp)
        ) {
            // Trip history items
            TripHistoryItem(carName = "Kia Rio", time = "15 min", price = "$15")
            Spacer(modifier = Modifier.height(16.dp))
            TripHistoryItem(carName = "Kia Rio", time = "10 min", price = "$10")
            Spacer(modifier = Modifier.height(16.dp))
            TripHistoryItem(carName = "Kia Rio", time = "60 min", price = "$50")
        }
    }
}

@Composable
fun TripHistoryItem(carName: String, time: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = carName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = time,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Text(
            text = price,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2942)) // Устанавливаем фон правой половины на всю страницу
    ) {
        // Кнопка Settings и увеличенная иконка img_12 в левом верхнем углу
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart) // Размещение в верхнем левом углу
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }, // Возврат на предыдущую страницу
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_12), // Иконка настроек
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp) // Увеличиваем размер иконки
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically) // Выравнивание текста по центру рядом с иконкой
                )
            }
        }

        // Основное содержимое страницы настроек
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Основной блок информации
            Text(
                text = "Wirtz Florian", // Пример данных пользователя
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(25.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "15 hours",
                        fontSize = 25.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Drive", fontSize = 25.sp, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$1510",
                        fontSize = 25.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Paid", fontSize = 25.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "E-mail: Wirtz@gmail.com", // Пример электронной почты
                fontSize = 20.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка EXIT
            Button(
                onClick = { navController.navigate("login_screen") }, // Переход на login_screen
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE) // Цвет кнопки EXIT
                )
            ) {
                Text(text = "EXIT", fontSize = 25.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Экран таймера ожидания
@Composable
fun WaitTimerScreen(navController: NavController) {
    var timeLeft by remember { mutableStateOf(300) } // Таймер на 5 минут (300 секунд)
    val scope = rememberCoroutineScope()
    var timerRunning by remember { mutableStateOf(true) } // Флаг для управления таймером

    LaunchedEffect(Unit) {
        scope.launch {
            while (timeLeft > 0 && timerRunning) { // Уменьшение времени продолжается, пока таймер запущен
                delay(1000L) // Задержка 1 секунда
                timeLeft--
            }
            if (timeLeft == 0) {
                navController.navigate("home_screen") // Возвращаемся на HomeScreen, если таймер дошел до 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2942)), // Тёмный фон
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Wait timer",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Таймер
        Text(
            text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Кнопка "STOP"
        Button(
            onClick = {
                timerRunning = false // Останавливаем таймер
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
        ) {
            Text(text = "STOP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Кнопка "CANCEL"
        Button(
            onClick = {
                navController.popBackStack() // Возврат на предыдущий экран
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
        ) {
            Text(text = "Cancel", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Вспомогательная функция для управления жизненным циклом MapView
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // Управление жизненным циклом карты
    DisposableEffect(mapView) {
        mapView.onCreate(null)
        mapView.onResume() // Обязательно вызовите onResume для правильной работы карты
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }
    return mapView
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img), // Укажите ваше изображение для фона
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Логотип по центру
        Image(
            painter = painterResource(id = R.drawable.img_1), // Укажите ваше изображение логотипа
            contentDescription = null,
            modifier = Modifier.size(120.dp) // Задайте размер логотипа
        )
    }
}

@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Устанавливаем изображение фона с прозрачностью 50%
        Image(
            painter = painterResource(id = R.drawable.img_4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.5f) // Устанавливаем прозрачность
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .alpha(1f) // Устанавливаем 100% непрозрачность для текста
        ) {
            TextField(
                value = login,
                onValueChange = {
                    login = it
                    isLoginValid = login.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_2),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "login") },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isLoginValid // Показывать ошибку при незаполненном поле
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = password.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_3),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isPasswordValid // Показывать ошибку при незаполненном поле
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forgot Password?",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoginValid = login.isNotEmpty()
                    isPasswordValid = password.isNotEmpty()
                    if (isLoginValid && isPasswordValid) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        navController.navigate("home_screen")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2942))
            ) {
                Text(text = "SIGN IN", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create A New Account?",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                    navController.navigate("signup_screen")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoginValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Устанавливаем изображение фона с прозрачностью 50%
        Image(
            painter = painterResource(id = R.drawable.img_4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.5f) // Устанавливаем прозрачность
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .alpha(1f) // Устанавливаем 100% непрозрачность для текста
        ) {
            TextField(
                value = login,
                onValueChange = {
                    login = it
                    isLoginValid = login.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_2),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "login") },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isLoginValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = {
                    email = it
                    isEmailValid = email.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_5),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "login@mail.ru") },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isEmailValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = password.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_3),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isPasswordValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.img_3),
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "confirm password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                isError = !isConfirmPasswordValid
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoginValid = login.isNotEmpty()
                    isEmailValid = email.isNotEmpty()
                    isPasswordValid = password.isNotEmpty()
                    isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()
                    if (isLoginValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        navController.navigate("home_screen")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2942))
            ) {
                Text(text = "SIGN UP", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already Have An Account?",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                    navController.navigate("login_screen")
                }
            )
        }
    }
}