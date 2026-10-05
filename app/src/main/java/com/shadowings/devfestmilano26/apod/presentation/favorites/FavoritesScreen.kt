package com.shadowings.devfestmilano26.apod.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shadowings.devfestmilano26.apod.domain.model.Favorite
import com.shadowings.devfestmilano26.apod.domain.model.SyncStatus

@Composable
fun FavoritesScreen(
    onOpenFavorite: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        FavoritesHeader(onSyncNow = { viewModel.onIntent(FavoritesIntent.SyncNow) })
        when (val current = state) {
            FavoritesState.Loading -> FavoritesLoading()
            FavoritesState.Empty -> FavoritesEmpty()
            is FavoritesState.Error -> FavoritesErrorView(current.message)
            is FavoritesState.Content -> FavoritesGrid(current.favorites) { date ->
                viewModel.onIntent(FavoritesIntent.OpenFavorite(date))
                onOpenFavorite(date)
            }
        }
    }
}

@Composable
private fun FavoritesHeader(onSyncNow: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recorded Observations",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onSyncNow) {
            Icon(Icons.Filled.CloudSync, contentDescription = "Sync now", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
internal fun FavoritesGrid(favorites: List<Favorite>, onOpenFavorite: (String) -> Unit) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(favorites, key = { it.picture.date.toString() }) { favorite ->
            FavoriteCell(favorite, onOpenFavorite)
        }
    }
}

@Composable
private fun FavoriteCell(favorite: Favorite, onOpenFavorite: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenFavorite(favorite.picture.date.toString()) },
    ) {
        AsyncImage(
            model = favorite.picture.url,
            contentDescription = favorite.picture.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (favorite.picture.date.dayOfMonth % 2 == 0) 1f else 0.75f),
        )
        Text(
            text = favorite.picture.date.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = favorite.picture.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (favorite.syncStatus == SyncStatus.SYNCED) "SYNCED" else "LOCAL",
            style = MaterialTheme.typography.labelSmall,
            color = if (favorite.syncStatus == SyncStatus.SYNCED) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
internal fun FavoritesEmpty() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO ENTRIES YET",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Favorite a picture from Today to start your logbook.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
internal fun FavoritesLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("LOADING LOGBOOK…", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun FavoritesErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "COULD NOT LOAD FAVORITES",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}
