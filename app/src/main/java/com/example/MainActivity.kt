package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.BottomNavItems
import com.example.ui.navigation.Screen
import com.example.ui.screens.calculators.CalculatorsScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.financials.FinancialsScreen
import com.example.ui.screens.history.CalculationHistoryScreen
import com.example.ui.screens.invoices.InvoiceDetailScreen
import com.example.ui.screens.invoices.InvoicesScreen
import com.example.ui.screens.materials.MaterialsScreen
import com.example.ui.screens.projects.ProjectDetailScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ElectricianViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: ElectricianViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isDark = when (settings.darkModePreference) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                // Mandatory Arabic RTL LayoutDirection
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ElectricianApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricianApp(viewModel: ElectricianViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Determine whether to show the bottom bar (only on main 5 tabs)
    val isBottomBarVisible = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (isBottomBarVisible) {
                TopAppBar(
                    title = {
                        Text(
                            text = "محاسب الكهربائي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "بحث")
                        }
                        IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Icon(Icons.Default.History, contentDescription = "سجل العمليات")
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("العملاء CRM") },
                                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        navNavControllerClean(navController, Screen.Clients.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("المالية والأرباح") },
                                    leadingIcon = { Icon(Icons.Default.QueryStats, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        navNavControllerClean(navController, Screen.Financials.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("الإعدادات") },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        navNavControllerClean(navController, Screen.Settings.route)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    BottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                item.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = item.titleAr,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.titleAr,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCalculators = { category ->
                        navController.navigate(
                            if (category != null) "calculators?tab=$category" else Screen.Calculators.route
                        )
                    },
                    onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                    onNavigateToInvoices = { navController.navigate(Screen.Invoices.route) },
                    onNavigateToMaterials = { navController.navigate(Screen.Materials.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onSelectProject = { projectId ->
                        navController.navigate("project_detail/$projectId")
                    },
                    onNewProject = { navController.navigate(Screen.Projects.route) },
                    onNewInvoice = { navController.navigate(Screen.Invoices.route) }
                )
            }

            // Calculators Hub
            composable(
                route = "calculators?tab={tab}",
                arguments = listOf(navArgument("tab") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab")
                CalculatorsScreen(viewModel = viewModel, initialTab = tab)
            }
            composable(Screen.Calculators.route) {
                CalculatorsScreen(viewModel = viewModel, initialTab = null)
            }

            // Projects
            composable(Screen.Projects.route) {
                ProjectsScreen(
                    viewModel = viewModel,
                    onProjectClick = { projectId ->
                        navController.navigate("project_detail/$projectId")
                    }
                )
            }

            // Project Detail
            composable(
                route = "project_detail/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                ProjectDetailScreen(
                    projectId = projectId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Invoices & Quotes
            composable(Screen.Invoices.route) {
                InvoicesScreen(
                    viewModel = viewModel,
                    onInvoiceClick = { invoiceId ->
                        navController.navigate("invoice_detail/$invoiceId")
                    }
                )
            }

            // Invoice Detail
            composable(
                route = "invoice_detail/{invoiceId}",
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
                InvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Materials & Inventory
            composable(Screen.Materials.route) {
                MaterialsScreen(viewModel = viewModel)
            }

            // Clients CRM
            composable(Screen.Clients.route) {
                BackHandler { navController.popBackStack() }
                ClientsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Financials
            composable(Screen.Financials.route) {
                BackHandler { navController.popBackStack() }
                FinancialsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // History
            composable(Screen.History.route) {
                BackHandler { navController.popBackStack() }
                CalculationHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                BackHandler { navController.popBackStack() }
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Search
            composable(Screen.Search.route) {
                BackHandler { navController.popBackStack() }
                GlobalSearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSelectProject = { id -> navController.navigate("project_detail/$id") },
                    onSelectInvoice = { id -> navController.navigate("invoice_detail/$id") },
                    onSelectCalculator = { cat -> navController.navigate("calculators?tab=$cat") }
                )
            }
        }
    }
}

private fun navNavControllerClean(navController: androidx.navigation.NavController, route: String) {
    navController.navigate(route) {
        launchSingleTop = true
    }
}
