package com.shadowings.devfestmilano26.apod.presentation.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

@Composable
fun DetailScreen(
    date: String?,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DetailEffect.OpenShareSheet -> shareLink(context, effect.title, effect.permalink)
                is DetailEffect.OpenBrowser -> openInBrowser(context, effect.url)
            }
        }
    }

    when (val current = state) {
        DetailState.Loading -> DetailLoading()
        is DetailState.Error -> DetailError(current.message)
        is DetailState.Content -> DetailContent(
            state = current,
            onToggleSave = { viewModel.onIntent(DetailIntent.ToggleSave) },
            onShare = { viewModel.onIntent(DetailIntent.Share) },
            onOpenPermalink = { viewModel.onIntent(DetailIntent.OpenPermalink) },
        )
    }
}

@Composable
internal fun DetailContent(
    state: DetailState.Content,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onOpenPermalink: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        AsyncImage(
            model = state.picture.url,
            contentDescription = state.picture.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        )
        Text(
            text = state.picture.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = state.picture.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
        state.picture.copyright?.let { credit ->
            Text(
                text = "CREDIT: $credit",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                .padding(12.dp),
        ) {
            Text(
                text = "ARCHIVAL PERMALINK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onOpenPermalink, modifier = Modifier.padding(top = 4.dp)) {
            Text(permalinkFor(state.picture.date), style = MaterialTheme.typography.labelSmall)
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            OutlinedButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (state.favoriteStatus != null) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                )
                Text(
                    text = if (state.favoriteStatus != null) "SAVED TO LOGBOOK" else "SAVE TO FAVORITES",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedButton(onClick = onShare, modifier = Modifier.padding(start = 12.dp)) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text("SHARE", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
internal fun DetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
internal fun DetailError(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SIGNAL LOST",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private fun shareLink(context: Context, title: String, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n$url")
    }
    context.startActivity(Intent.createChooser(intent, title))
}

private fun openInBrowser(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
