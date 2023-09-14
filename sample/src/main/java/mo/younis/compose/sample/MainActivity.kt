@file:OptIn(ExperimentalFoundationApi::class)

package mo.younis.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mo.younis.compose.sample.ui.theme.RevealSwipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RevealSwipeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        List(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun List(modifier: Modifier) {
    val state = rememberRevealableState(
        allowMultipleReveals = false,
        coroutineScope = rememberCoroutineScope(),
    )

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 32.dp),
    ) {
        items(30) { index ->
            Greeting(name = "Android $index", revealableState = state)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, revealableState: RevealableState) {
    val density = LocalDensity.current

    val itemState = rememberRevealableItemState(
        positionalThreshold = { distance -> distance * 0.5f },
        velocityThreshold = { with(density) { 150.dp.toPx() } },
        confirmValueChange = { true },
    )

    val coroutineScope = rememberCoroutineScope()

    Revealable(
        state = revealableState,
        itemState = itemState,
        modifier = modifier,
        startContent = {
            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable { coroutineScope.launch { itemState.animateTo(RevealableValue.Initial) } },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "Add",
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(Color.Red)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable { coroutineScope.launch { itemState.animateTo(RevealableValue.EndRevealed) } },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "Delete",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        endContent = {
            Box(
                modifier = Modifier
                    .background(Color.Cyan)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable { coroutineScope.launch { itemState.animateTo(RevealableValue.StartRevealed) } },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.Archive, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "Archive",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(32.dp),
        ) {
            Text(
                text = "Hello $name!",
            )
        }
    }
}
