package com.imbavchenko.bimil.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.imbavchenko.bimil.domain.model.AccountWithHint
import com.imbavchenko.bimil.domain.model.LoginType
import com.imbavchenko.bimil.domain.model.PasswordHint
import com.imbavchenko.bimil.domain.model.RequirementStatus
import com.imbavchenko.bimil.presentation.localization.strings
import com.imbavchenko.bimil.presentation.theme.BimilColors

private fun parseHexColor(hex: String): Color? {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorLong = cleanHex.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF) / 255f,
            green = ((colorLong shr 8) and 0xFF) / 255f,
            blue = (colorLong and 0xFF) / 255f
        )
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountCard(
    accountWithHint: AccountWithHint,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account = accountWithHint.account
    val category = accountWithHint.category
    val strings = strings()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Service Icon with Favicon
            ServiceIcon(
                serviceName = account.serviceName,
                websiteUrl = account.websiteUrl,
                categoryColor = category?.color
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Service name
                Text(
                    text = account.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Website URL (if available)
                if (!account.websiteUrl.isNullOrBlank()) {
                    Text(
                        text = account.websiteUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login type specific content
                if (account.loginType == LoginType.SSO) {
                    // SSO: "via Google" style
                    Text(
                        text = "${strings.via} ${accountWithHint.ssoHint?.provider?.displayName ?: "SSO"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = BimilColors.Primary
                    )
                } else {
                    // Password: Hint chips
                    PasswordHintChips(hint = accountWithHint.passwordHint, digitPlusSuffix = strings.digitPlus)
                }

                // Note section (if memo exists)
                if (!account.memo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.note,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BimilColors.Primary
                    )
                    Text(
                        text = account.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Favorite button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (account.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (account.isFavorite) strings.removeFromFavorites else strings.addToFavorites,
                    tint = if (account.isFavorite) BimilColors.StarActive else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extracts domain from URL for favicon fetching.
 * Supports: google.com, www.google.com, https://google.com, etc.
 */
private fun extractDomain(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return try {
        var cleanUrl = url.trim().lowercase()
        // Remove protocol
        cleanUrl = cleanUrl.removePrefix("https://").removePrefix("http://")
        // Remove www.
        cleanUrl = cleanUrl.removePrefix("www.")
        // Get domain part only (before path)
        val domain = cleanUrl.split("/").firstOrNull() ?: return null
        // Must have a dot and reasonable length
        if (domain.contains(".") && domain.length >= 4) domain else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Generates favicon URL using Google's favicon service.
 */
private fun getFaviconUrl(domain: String): String {
    return "https://www.google.com/s2/favicons?domain=$domain&sz=128"
}

@Composable
fun ServiceIcon(
    serviceName: String,
    websiteUrl: String? = null,
    categoryColor: String?,
    modifier: Modifier = Modifier
) {
    val color = parseHexColor(categoryColor ?: "#6B7280") ?: MaterialTheme.colorScheme.primary
    val domain = remember(websiteUrl) { extractDomain(websiteUrl) }
    val faviconUrl = remember(domain) { domain?.let { getFaviconUrl(it) } }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (faviconUrl != null) {
            SubcomposeAsyncImage(
                model = faviconUrl,
                contentDescription = "Favicon for $serviceName",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
                loading = {
                    FallbackLetter(serviceName, color)
                },
                error = {
                    FallbackLetter(serviceName, color)
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )
        } else {
            FallbackLetter(serviceName, color)
        }
    }
}

@Composable
private fun FallbackLetter(serviceName: String, color: Color) {
    Text(
        text = serviceName.firstOrNull()?.uppercase() ?: "?",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

/**
 * Password hint chips component displaying password requirements
 * Shows: digit count, numbers required, uppercase required, special chars required
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordHintChips(
    hint: PasswordHint?,
    modifier: Modifier = Modifier,
    digitPlusSuffix: String = "digit+"
) {
    val isDarkTheme = isSystemInDarkTheme()

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Minimum length chip (always show if hint exists)
        if (hint != null && hint.minLength > 0) {
            HintChip(
                text = "${hint.minLength}$digitPlusSuffix",
                isActive = true,
                isDarkTheme = isDarkTheme
            )
        }

        // Numbers required chip
        HintChip(
            text = "123",
            isActive = hint?.requiresNumber == RequirementStatus.YES,
            isDarkTheme = isDarkTheme
        )

        // Uppercase required chip
        HintChip(
            text = "ABC",
            isActive = hint?.requiresUppercase == RequirementStatus.YES,
            isDarkTheme = isDarkTheme
        )

        // Special characters required chip
        HintChip(
            text = "!@#",
            isActive = hint?.requiresSpecial == RequirementStatus.YES,
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * Individual hint chip with active/inactive styling
 */
@Composable
fun HintChip(
    text: String,
    isActive: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        if (isDarkTheme) BimilColors.ChipActiveDark else BimilColors.ChipActive
    } else {
        if (isDarkTheme) BimilColors.ChipInactiveDark else BimilColors.ChipInactive
    }

    val textColor = if (isActive) {
        if (isDarkTheme) BimilColors.ChipActiveTextDark else BimilColors.ChipActiveText
    } else {
        if (isDarkTheme) BimilColors.ChipInactiveTextDark else BimilColors.ChipInactiveText
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
