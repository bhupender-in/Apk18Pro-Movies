package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ads.AdManager
import com.example.models.Movie
import com.example.ui.components.MoviePosterCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit,
    onMovieClick: (String) -> Unit,
    onPlayMovie: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val adManager = AdManager.getInstance(context)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenDrawer() }
                    ) {
                        Surface(
                            color = CinemaRed,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "18",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apk18",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "pro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = CinemaRed
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("home_hamburger_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (uiState.userSession.isAdmin) {
                        IconButton(
                            onClick = onAdminClick,
                            modifier = Modifier.testTag("home_admin_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = CinemaGold
                            )
                        }
                    }
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CinemaRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Featured Movies Hero Banner Carousel
                if (uiState.featuredMovies.isNotEmpty()) {
                    item {
                        FeaturedHeroCarousel(
                            movies = uiState.featuredMovies,
                            onMovieClick = { movieId ->
                                adManager.showInterstitialIfAllowed(activity, isBeforePlayback = false) {
                                    onMovieClick(movieId)
                                }
                            },
                            onPlayMovie = { movieId ->
                                adManager.showInterstitialIfAllowed(activity, isBeforePlayback = true) {
                                    onPlayMovie(movieId)
                                }
                            }
                        )
                    }
                }

                // 2. Categories Filter Chips
                if (uiState.categories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.categories) { category ->
                                val isSelected = category == uiState.selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(category) },
                                    label = {
                                        Text(
                                            text = category,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CinemaRed,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) CinemaRed else Color.White.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("category_chip_$category")
                                )
                            }
                        }
                    }
                }

                // 3. Filtered Movies Section (if category selected other than All)
                if (uiState.selectedCategory != "All") {
                    item {
                        SectionHeader(title = "${uiState.selectedCategory} Movies (${uiState.filteredMovies.size})")
                    }

                    if (uiState.filteredMovies.isEmpty()) {
                        item {
                            EmptyStateNotice(message = "No movies found in '${uiState.selectedCategory}'")
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.filteredMovies, key = { it.id }) { movie ->
                                    MoviePosterCard(
                                        movie = movie,
                                        onClick = {
                                            adManager.showInterstitialIfAllowed(activity, isBeforePlayback = false) {
                                                onMovieClick(movie.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Latest Movies Carousel
                if (uiState.latestMovies.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Latest Releases")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.latestMovies, key = { it.id }) { movie ->
                                MoviePosterCard(
                                    movie = movie,
                                    onClick = {
                                        adManager.showInterstitialIfAllowed(activity, isBeforePlayback = false) {
                                            onMovieClick(movie.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Featured Movies Horizontal Carousel
                if (uiState.featuredMovies.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Featured & Trending")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.featuredMovies, key = { it.id }) { movie ->
                                MoviePosterCard(
                                    movie = movie,
                                    onClick = {
                                        adManager.showInterstitialIfAllowed(activity, isBeforePlayback = false) {
                                            onMovieClick(movie.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. All Movies Grid List
                item {
                    SectionHeader(title = "All Movies")
                }

                if (uiState.allMovies.isEmpty()) {
                    item {
                        EmptyStateNotice(message = "Movie catalog is currently empty. Check back soon!")
                    }
                } else {
                    val chunkedMovies = uiState.allMovies.chunked(3)
                    items(chunkedMovies) { rowMovies ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (movie in rowMovies) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MoviePosterCard(
                                        movie = movie,
                                        cardWidth = 110,
                                        onClick = {
                                            adManager.showInterstitialIfAllowed(activity, isBeforePlayback = false) {
                                                onMovieClick(movie.id)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            // Fill empty slots if row has fewer than 3
                            for (i in 0 until (3 - rowMovies.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroCarousel(
    movies: List<Movie>,
    onMovieClick: (String) -> Unit,
    onPlayMovie: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { movies.size })

    // Auto-scroll hero banner gently every 6s
    LaunchedEffect(pagerState) {
        while (true) {
            delay(6000)
            if (movies.size > 1) {
                val nextPage = (pagerState.currentPage + 1) % movies.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) { page ->
            val movie = movies[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onMovieClick(movie.id) }
            ) {
                // Background Banner Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.displayBannerUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Cinematic Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkBackground.copy(alpha = 0.6f),
                                    DarkBackground
                                )
                            )
                        )
                )

                // Banner Details & CTAs
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = CinemaRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "FEATURED",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${movie.category} • ${movie.year}",
                                color = CinemaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = movie.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onPlayMovie(movie.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("hero_play_button_${movie.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Watch Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onMovieClick(movie.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Black.copy(alpha = 0.4f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.2f)))
                            ),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("hero_details_button_${movie.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Details",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Pager indicator dots
        if (movies.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(movies.size) { iteration ->
                    val isCurrent = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isCurrent) 8.dp else 6.dp)
                            .background(
                                color = if (isCurrent) CinemaRed else Color.White.copy(alpha = 0.25f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(CinemaRed, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}
