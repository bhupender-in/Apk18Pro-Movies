package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.SocialMedia
import com.example.models.UserSession
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SuccessGreen

@Composable
fun SidebarDrawerContent(
    currentRoute: String,
    userSession: UserSession,
    socialMediaList: List<SocialMedia>,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        drawerContentColor = Color.White,
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CinemaRed.copy(alpha = 0.35f), DarkSurface)
                        )
                    )
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CinemaRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "18",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Apk18",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "pro",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CinemaRed
                                )
                            }
                            Text(
                                text = "OTT Cinema Network",
                                fontSize = 11.sp,
                                color = CinemaGold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User ID Badge
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (userSession.isAdmin) CinemaGold else SuccessGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (userSession.isAdmin) "ADMIN: ${userSession.adminName}" else "USER ID",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userSession.userId,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            // Main Navigation Items
            DrawerMenuItem(
                title = "Home",
                icon = Icons.Default.Home,
                selected = currentRoute == "home",
                testTag = "drawer_item_home",
                onClick = {
                    onNavigate("home")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Search Movies",
                icon = Icons.Default.Search,
                selected = currentRoute == "search",
                testTag = "drawer_item_search",
                onClick = {
                    onNavigate("search")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Categories",
                icon = Icons.Default.Category,
                selected = currentRoute == "categories",
                testTag = "drawer_item_categories",
                onClick = {
                    onNavigate("categories")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Latest Movies",
                icon = Icons.Default.NewReleases,
                selected = currentRoute == "latest",
                testTag = "drawer_item_latest",
                onClick = {
                    onNavigate("latest")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "Featured Movies",
                icon = Icons.Default.Star,
                selected = currentRoute == "featured",
                testTag = "drawer_item_featured",
                onClick = {
                    onNavigate("featured")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                title = "My Profile",
                icon = Icons.Default.Person,
                selected = currentRoute == "profile",
                testTag = "drawer_item_profile",
                onClick = {
                    onNavigate("profile")
                    onCloseDrawer()
                }
            )

            // Social Media section (dynamically loaded from admin settings)
            if (socialMediaList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Text(
                    text = "SOCIAL MEDIA",
                    color = CinemaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                )

                socialMediaList.forEach { social ->
                    DrawerSocialItem(
                        social = social,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(social.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link: ${social.url}", Toast.LENGTH_SHORT).show()
                            }
                            onCloseDrawer()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            DrawerMenuItem(
                title = "About Apk18pro",
                icon = Icons.Default.Info,
                selected = currentRoute == "about",
                testTag = "drawer_item_about",
                onClick = {
                    onNavigate("about")
                    onCloseDrawer()
                }
            )

            // Admin Access Item (Changes to Admin Dashboard if authenticated)
            if (userSession.isAdmin) {
                DrawerMenuItem(
                    title = "Admin Dashboard",
                    icon = Icons.Default.AdminPanelSettings,
                    selected = currentRoute == "admin_dashboard",
                    highlightColor = CinemaGold,
                    testTag = "drawer_item_admin_dashboard",
                    onClick = {
                        onNavigate("admin_dashboard")
                        onCloseDrawer()
                    }
                )
            } else {
                DrawerMenuItem(
                    title = "Admin Login",
                    icon = Icons.Default.Lock,
                    selected = currentRoute == "admin_login",
                    testTag = "drawer_item_admin_login",
                    onClick = {
                        onNavigate("admin_login")
                        onCloseDrawer()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    highlightColor: Color = CinemaRed,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) highlightColor else Color.White.copy(alpha = 0.8f)
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = highlightColor.copy(alpha = 0.18f),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = Color.White,
            unselectedTextColor = Color.White.copy(alpha = 0.85f),
            selectedIconColor = highlightColor,
            unselectedIconColor = Color.White.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag)
    )
}

@Composable
private fun DrawerSocialItem(
    social: SocialMedia,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("drawer_social_${social.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (social.iconType.lowercase()) {
                "youtube" -> Icons.Default.Movie
                "telegram", "whatsapp" -> Icons.Default.Share
                else -> Icons.Default.Language
            },
            contentDescription = null,
            tint = CinemaGold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = social.platformName,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
