package mo.younis.compose.revealable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import mo.younis.compose.revealable.ui.theme.RevealSwipeTheme

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
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
    val state = rememberRevealableState()

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
    Revealable(
        state = revealableState,
        modifier = modifier,
        startContent = {
            Box(
                modifier = Modifier
                    .background(Color.Cyan)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Hello")
            }
            Box(
                modifier = Modifier
                    .background(Color.Green)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "HelloHello")
            }
            Box(
                modifier = Modifier
                    .background(Color.Yellow)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "HelloHelloHello")
            }
            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "HelloHelloHelloHello")
            }
        },
        endContent = {
            Box(
                modifier = Modifier
                    .background(Color.Red)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Hello")
            }
            Box(
                modifier = Modifier
                    .background(Color.Magenta)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "HelloHello")
            }
            Box(
                modifier = Modifier
                    .background(Color.Blue)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "HelloHelloHello")
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
