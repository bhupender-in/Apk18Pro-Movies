package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.player.VideoPlayerScreen
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.Apk18proTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.viewmodels.AdminViewModel
import com.example.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Apk18proTheme {
                Apk18proApp()
            }
        }
    }
}

@Composable
fun Apk18proApp() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val adminUiState by adminViewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    // Close drawer on back press if open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Screens where sidebar drawer is available
    val isDrawerEnabled = currentRoute == "home" ||
        currentRoute == "categories" ||
        currentRoute == "latest" ||
        currentRoute == "featured"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isDrawerEnabled,
            drawerContent = {
                SidebarDrawerContent(
                    currentRoute = currentRoute,
                    userSession = homeUiState.userSession,
                    socialMediaList = homeUiState.socialMediaLinks,
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            "search" -> navController.navigate("search")
                            "categories" -> navController.navigate("categories")
                            "latest" -> {
                                homeViewModel.selectCategory("All")
                                navController.navigate("home")
                            }
                            "featured" -> {
                                navController.navigate("categories?cat=Featured")
                            }
                            "profile" -> navController.navigate("profile")
                            "about" -> navController.navigate("about")
                            "admin_login" -> navController.navigate("admin_login")
                            "admin_dashboard" -> navController.navigate("admin_dashboard")
                        }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                // 1. Splash Screen
                composable("splash") {
                    SplashScreen(
                        userSession = homeUiState.userSession,
                        onContinue = {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Home Screen
                composable("home") {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                        onMovieClick = { movieId ->
                            navController.navigate("movie_details/$movieId")
                        },
                        onPlayMovie = { movieId ->
                            navController.navigate("player/$movieId")
                        },
                        onSearchClick = {
                            navController.navigate("search")
                        },
                        onAdminClick = {
                            if (homeUiState.userSession.isAdmin) {
                                navController.navigate("admin_dashboard")
                            } else {
                                navController.navigate("admin_login")
                            }
                        }
                    )
                }

                // 3. Movie Details Screen
                composable(
                    route = "movie_details/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val movie = homeUiState.allMovies.find { it.id == movieId }
                        ?: adminUiState.movies.find { it.id == movieId }

                    if (movie != null) {
                        MovieDetailsScreen(
                            movie = movie,
                            allMovies = homeUiState.allMovies,
                            onBack = { navController.popBackStack() },
                            onPlay = {
                                navController.navigate("player/${movie.id}")
                            },
                            onSelectMovie = { newId ->
                                navController.navigate("movie_details/$newId") {
                                    popUpTo("movie_details/$movieId") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                // 4. Video Player Screen
                composable(
                    route = "player/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val movie = homeUiState.allMovies.find { it.id == movieId }
                        ?: adminUiState.movies.find { it.id == movieId }

                    if (movie != null) {
                        VideoPlayerScreen(
                            movie = movie,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                // 5. Search Screen
                composable("search") {
                    SearchScreen(
                        movies = homeUiState.allMovies,
                        categories = homeUiState.categories,
                        onBack = { navController.popBackStack() },
                        onMovieClick = { movieId ->
                            navController.navigate("movie_details/$movieId")
                        }
                    )
                }

                // 6. Categories Screen
                composable("categories") {
                    CategoriesScreen(
                        movies = homeUiState.allMovies,
                        categories = homeUiState.categories,
                        onBack = { navController.popBackStack() },
                        onMovieClick = { movieId ->
                            navController.navigate("movie_details/$movieId")
                        }
                    )
                }

                // 7. Filtered Categories Screen (Query param)
                composable(
                    route = "categories?cat={cat}",
                    arguments = listOf(navArgument("cat") {
                        type = NavType.StringType
                        defaultValue = "All"
                    })
                ) { backStackEntry ->
                    val cat = backStackEntry.arguments?.getString("cat") ?: "All"
                    val filteredMovies = if (cat == "Featured") {
                        homeUiState.featuredMovies
                    } else {
                        homeUiState.allMovies.filter { it.category.equals(cat, ignoreCase = true) }
                    }

                    CategoriesScreen(
                        movies = filteredMovies,
                        categories = homeUiState.categories,
                        initialCategory = cat,
                        onBack = { navController.popBackStack() },
                        onMovieClick = { movieId ->
                            navController.navigate("movie_details/$movieId")
                        }
                    )
                }

                // 8. Profile Screen
                composable("profile") {
                    ProfileScreen(
                        userSession = homeUiState.userSession,
                        onBack = { navController.popBackStack() },
                        onLogoutAdmin = {
                            homeViewModel.logoutAdmin()
                            adminViewModel.logout()
                        },
                        onOpenAdminDashboard = {
                            navController.navigate("admin_dashboard")
                        }
                    )
                }

                // 9. About Screen
                composable("about") {
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // 10. Admin Login Screen
                composable("admin_login") {
                    AdminLoginScreen(
                        viewModel = adminViewModel,
                        onBack = { navController.popBackStack() },
                        onLoginSuccess = {
                            navController.navigate("admin_dashboard") {
                                popUpTo("admin_login") { inclusive = true }
                            }
                        }
                    )
                }

                // 11. Admin Dashboard Screen
                composable("admin_dashboard") {
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate("home") {
                                popUpTo("admin_dashboard") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
