package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.models.AdSettings
import com.example.models.Movie
import com.example.models.SocialMedia
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingMovie by remember { mutableStateOf<Movie?>(null) }
    var isAddMovieOpen by remember { mutableStateOf(false) }
    var movieToDelete by remember { mutableStateOf<Movie?>(null) }

    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Admin Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Apk18pro Manager",
                            fontSize = 11.sp,
                            color = CinemaGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_dashboard_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("admin_dashboard_logout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        editingMovie = null
                        isAddMovieOpen = true
                    },
                    containerColor = CinemaRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_movie_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Movie")
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = CinemaGold,
                indicator = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Movies (${uiState.movies.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Social Links", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("AdMob Ads", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AdsClick, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> MoviesManagementTab(
                    movies = uiState.movies,
                    onEditMovie = { movie ->
                        editingMovie = movie
                        isAddMovieOpen = true
                    },
                    onTogglePublish = { movie ->
                        viewModel.togglePublishStatus(movie.id, movie.published)
                    },
                    onDeleteMovie = { movie ->
                        movieToDelete = movie
                    }
                )
                1 -> SocialMediaManagementTab(
                    socialList = uiState.socialMediaLinks,
                    onSave = { viewModel.saveSocialMedia(it) },
                    onDelete = { id, name -> viewModel.deleteSocialMedia(id, name) }
                )
                2 -> AdSettingsManagementTab(
                    adSettings = uiState.adSettings,
                    onSave = { viewModel.saveAdSettings(it) }
                )
            }
        }

        // Add / Edit Movie Dialog
        if (isAddMovieOpen) {
            MovieEditDialog(
                initialMovie = editingMovie,
                onDismiss = {
                    isAddMovieOpen = false
                    editingMovie = null
                },
                onSave = { movieToSave ->
                    viewModel.saveMovie(movieToSave) { success ->
                        if (success) {
                            isAddMovieOpen = false
                            editingMovie = null
                        }
                    }
                },
                onDelete = { movieId, movieName ->
                    viewModel.deleteMovie(movieId, movieName)
                    isAddMovieOpen = false
                    editingMovie = null
                }
            )
        }

        // Delete Movie Confirmation Dialog
        if (movieToDelete != null) {
            AlertDialog(
                onDismissRequest = { movieToDelete = null },
                containerColor = DarkSurfaceVariant,
                title = { Text("Delete Movie?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete '${movieToDelete?.name}'?", color = Color.White.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        onClick = {
                            movieToDelete?.let { viewModel.deleteMovie(it.id, it.name) }
                            movieToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { movieToDelete = null }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

// =============================================================================
// TAB 1: MOVIES MANAGEMENT
// =============================================================================

@Composable
private fun MoviesManagementTab(
    movies: List<Movie>,
    onEditMovie: (Movie) -> Unit,
    onTogglePublish: (Movie) -> Unit,
    onDeleteMovie: (Movie) -> Unit
) {
    var filterQuery by remember { mutableStateOf("") }

    val filtered = remember(filterQuery, movies) {
        if (filterQuery.isBlank()) movies else {
            movies.filter { it.name.contains(filterQuery, ignoreCase = true) || it.category.contains(filterQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Filter inside Admin
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Filter movies by title or category...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = CinemaRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { movie ->
                AdminMovieCard(
                    movie = movie,
                    onEdit = { onEditMovie(movie) },
                    onTogglePublish = { onTogglePublish(movie) },
                    onDelete = { onDeleteMovie(movie) }
                )
            }
        }
    }
}

@Composable
private fun AdminMovieCard(
    movie: Movie,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(55.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.displayPosterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${movie.category} • ${movie.year} • ${movie.language}",
                    color = CinemaGold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Published Badge
                    Surface(
                        color = if (movie.published) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (movie.published) "PUBLISHED" else "UNPUBLISHED",
                            color = if (movie.published) SuccessGreen else ErrorRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (movie.featured) {
                        Surface(
                            color = CinemaRed.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "FEATURED",
                                color = CinemaRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onTogglePublish,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (movie.published) Icons.Default.Public else Icons.Default.PublicOff,
                        contentDescription = "Publish toggle",
                        tint = if (movie.published) SuccessGreen else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = CinemaGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: SOCIAL MEDIA MANAGEMENT
// =============================================================================

@Composable
private fun SocialMediaManagementTab(
    socialList: List<SocialMedia>,
    onSave: (SocialMedia) -> Unit,
    onDelete: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    var editingSocial by remember { mutableStateOf<SocialMedia?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Social Media Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Button(
                onClick = {
                    editingSocial = null
                    isDialogOpen = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Link", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (socialList.isEmpty()) {
            Text("No social media links added yet.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        } else {
            socialList.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.platformName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(text = item.url, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Switch(
                            checked = item.enabled,
                            onCheckedChange = { onSave(item.copy(enabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CinemaRed,
                                checkedTrackColor = CinemaRed.copy(alpha = 0.5f)
                            )
                        )

                        IconButton(onClick = {
                            editingSocial = item
                            isDialogOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CinemaGold, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDelete(item.id, item.platformName) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (isDialogOpen) {
        SocialMediaEditDialog(
            initial = editingSocial,
            onDismiss = { isDialogOpen = false },
            onSave = {
                onSave(it)
                isDialogOpen = false
            }
        )
    }
}

// =============================================================================
// TAB 3: ADMOB ADVERTISEMENT SETTINGS
// =============================================================================

@Composable
private fun AdSettingsManagementTab(
    adSettings: AdSettings,
    onSave: (AdSettings) -> Unit
) {
    val scrollState = rememberScrollState()

    var interstitialEnabled by remember(adSettings) { mutableStateOf(adSettings.interstitialEnabled) }
    var minIntervalSeconds by remember(adSettings) { mutableStateOf(adSettings.minIntervalSeconds.toString()) }
    var adsOnMovieDetails by remember(adSettings) { mutableStateOf(adSettings.adsOnMovieDetails) }
    var adsBeforePlayback by remember(adSettings) { mutableStateOf(adSettings.adsBeforePlayback) }
    var adUnitId by remember(adSettings) { mutableStateOf(adSettings.interstitialAdUnitId) }
    var testMode by remember(adSettings) { mutableStateOf(adSettings.testMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "AdMob Policy & Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CinemaGold
                )

                // Master Interstitial Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Interstitial Ads", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Enable policy-compliant interstitial impressions", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = interstitialEnabled,
                        onCheckedChange = { interstitialEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaRed, checkedTrackColor = CinemaRed.copy(alpha = 0.5f))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Minimum Interval
                Column {
                    Text("Minimum Ad Interval (Seconds)", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Google Play UX Policy requires >= 30s minimum interval", color = CinemaGold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = minIntervalSeconds,
                        onValueChange = { minIntervalSeconds = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = CinemaRed,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Ads on Movie Details Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ads on Movie Details Screen", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Show ad when transitioning into movie details", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = adsOnMovieDetails,
                        onCheckedChange = { adsOnMovieDetails = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaRed, checkedTrackColor = CinemaRed.copy(alpha = 0.5f))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Ads before Playback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ads Before Playback", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Show natural transition ad prior to video launch", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = adsBeforePlayback,
                        onCheckedChange = { adsBeforePlayback = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaRed, checkedTrackColor = CinemaRed.copy(alpha = 0.5f))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Interstitial Ad Unit ID
                Column {
                    Text("Interstitial Ad Unit ID", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = adUnitId,
                        onValueChange = { adUnitId = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = CinemaRed,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Test Mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AdMob Test Mode", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Use Google standard test ads during development", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = testMode,
                        onCheckedChange = { testMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaGold, checkedTrackColor = CinemaGold.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Button(
            onClick = {
                val intervalLong = minIntervalSeconds.toLongOrNull() ?: 60L
                val updated = adSettings.copy(
                    interstitialEnabled = interstitialEnabled,
                    minIntervalSeconds = maxOf(30L, intervalLong),
                    adsOnMovieDetails = adsOnMovieDetails,
                    adsBeforePlayback = adsBeforePlayback,
                    interstitialAdUnitId = adUnitId.trim(),
                    testMode = testMode
                )
                onSave(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAVE ADVERTISEMENT SETTINGS", fontWeight = FontWeight.Bold)
        }
    }
}

// =============================================================================
// ADD / EDIT MOVIE DIALOG
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieEditDialog(
    initialMovie: Movie?,
    onDismiss: () -> Unit,
    onSave: (Movie) -> Unit,
    onDelete: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialMovie?.name ?: "") }
    var bannerUrl by remember { mutableStateOf(initialMovie?.bannerUrl ?: "") }
    var posterUrl by remember { mutableStateOf(initialMovie?.posterUrl ?: "") }
    var streamUrl by remember { mutableStateOf(initialMovie?.streamUrl ?: "") }
    var description by remember { mutableStateOf(initialMovie?.description ?: "") }
    var category by remember { mutableStateOf(initialMovie?.category ?: "Action") }
    var language by remember { mutableStateOf(initialMovie?.language ?: "English") }
    var year by remember { mutableStateOf(initialMovie?.year ?: "2024") }
    var featured by remember { mutableStateOf(initialMovie?.featured ?: false) }
    var published by remember { mutableStateOf(initialMovie?.published ?: true) }

    val categoriesList = listOf("Action", "Sci-Fi", "Fantasy", "Animation", "Drama", "Thriller", "Horror", "Comedy", "Documentary", "Romance")
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (initialMovie == null) "Add New Movie" else "Edit Movie",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Movie Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed,
                        focusedLabelColor = CinemaGold
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("movie_edit_name_input")
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CinemaRed
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        categoriesList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    category = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Banner URL
                OutlinedTextField(
                    value = bannerUrl,
                    onValueChange = { bannerUrl = it },
                    label = { Text("Banner URL (16:9) *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("movie_edit_banner_input")
                )

                // Poster URL (Optional)
                OutlinedTextField(
                    value = posterUrl,
                    onValueChange = { posterUrl = it },
                    label = { Text("Poster URL (2:3) (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Stream / M3U URL
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("M3U8 / Stream URL *") },
                    placeholder = { Text("https://domain.com/stream.m3u8 or .mp4") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("movie_edit_stream_input")
                )

                // Language & Year
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text("Language") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CinemaRed
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CinemaRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Storyline") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Featured Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured in Hero Carousel", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = featured,
                        onCheckedChange = { featured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CinemaRed, checkedTrackColor = CinemaRed.copy(alpha = 0.5f))
                    )
                }

                // Published Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Published to Users", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = published,
                        onCheckedChange = { published = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SuccessGreen, checkedTrackColor = SuccessGreen.copy(alpha = 0.5f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && streamUrl.isNotBlank()) {
                        val movieToSave = (initialMovie ?: Movie()).copy(
                            name = name.trim(),
                            bannerUrl = bannerUrl.trim(),
                            posterUrl = posterUrl.trim(),
                            streamUrl = streamUrl.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            language = language.trim(),
                            year = year.trim(),
                            featured = featured,
                            published = published
                        )
                        onSave(movieToSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                modifier = Modifier.testTag("movie_edit_save_button")
            ) {
                Text(if (initialMovie == null) "Add Movie" else "Update Movie", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

// =============================================================================
// SOCIAL MEDIA EDIT DIALOG
// =============================================================================

@Composable
private fun SocialMediaEditDialog(
    initial: SocialMedia?,
    onDismiss: () -> Unit,
    onSave: (SocialMedia) -> Unit
) {
    var platformName by remember { mutableStateOf(initial?.platformName ?: "") }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var iconType by remember { mutableStateOf(initial?.iconType ?: "website") }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initial == null) "Add Social Link" else "Edit Social Link", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = platformName,
                    onValueChange = { platformName = it },
                    label = { Text("Platform Name (e.g. YouTube, Telegram)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CinemaGold),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CinemaGold),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enabled in Sidebar", color = Color.White, fontSize = 13.sp)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (platformName.isNotBlank() && url.isNotBlank()) {
                        val toSave = (initial ?: SocialMedia()).copy(
                            platformName = platformName.trim(),
                            url = url.trim(),
                            iconType = iconType,
                            enabled = enabled
                        )
                        onSave(toSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        }
    )
}
