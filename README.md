---

# Revealable Compose Library

## Overview

The Revealable Compose Library is a powerful Android library that allows users to easily implement Revealable functionality in both directions (left to right and right to left) using Jetpack Compose. It is built on top of the new Compose Foundation API `AnchoredDraggableState`, providing a flexible and customizable solution for adding swipe actions to your Compose-based Android app.

## Features

- Revealable functionality in both directions (left to right and right to left).
- Built on the new Compose Foundation API `AnchoredDraggableState`.
- Highly customizable appearance and behavior.
- Supports smooth animations and gestures.
- Ideal for implementing swipe actions in lists, grids, and other UI components.

## Installation

To get started with the Revealable Compose Library, you need to include it in your project. You can do this by adding the following dependency to your app-level `build.gradle` file:

```groovy
dependencies {
    implementation 'io.github.myounis97:revealable:1.0.0'
}
```

Replace `1.0.0` with the latest version number from the [Releases](https://github.com/myounis97/revealable/releases) section.

## Usage

Here's a quick guide on how to use the library in your Compose project:

1. **Initialize the `RevealableState`**:
   ```kotlin
   val state = rememberRevealableState(
        positionalThreshold = { distance -> distance * 0.5f },
        velocityThreshold = { with(density) { 150.dp.toPx() } },
        confirmValueChange = { true },
    )
   ```
2. **Single Expansion**:
   ```kotlin
   val onExpandedUpdated by rememberUpdatedState(onExpanded)

    LaunchedEffect(expandedIndex, index, state) {
        val expanded = state.currentValue != RevealableValue.Initial ||
                state.targetValue != RevealableValue.Initial

        if (expandedIndex != index && expanded) {
            state.animateTo(RevealableValue.Initial)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.targetValue }
            .collectLatest {
                if (it != RevealableValue.Initial) {
                    onExpandedUpdated()
                }
            }
    }
   ```

3. **Compose your UI**:
   ```kotlin
    val coroutineScope = rememberCoroutineScope()

   Revealable(
        state = state,
        modifier = modifier,
        startContent = {
             Column(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable { coroutineScope.launch { state.animateTo(RevealableValue.EndRevealed) } },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "Add",
                    textAlign = TextAlign.Center,
                )
            }
        },
        endContent = {
            Column(modifier = Modifier
                .background(Color.Cyan)
                .fillMaxHeight()
                .aspectRatio(1f)
                .clickable { coroutineScope.launch { state.animateTo(RevealableValue.StartRevealed) } },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Outlined.Archive, contentDescription = null)
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "Archive",
                    textAlign = TextAlign.Center,
                )
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
   ```

## Preview

![Revealable](https://github.com/myounis97/revealable/blob/main/art/revealable.gif)

## Sample

Check out the [Sample App](sample/) included in this repository to see the Revealable Compose Library in action.

## License

This library is released under the [MIT License](LICENSE).

## Contributing

We welcome contributions from the community.

## Issues and Feedback

If you encounter any issues or have feedback, please [open an issue](https://github.com/myounis97/revealable/issues) on this repository.

## Contact

For any inquiries or questions, feel free to contact the maintainers:

- Mohammad Younis <myouniswork97@gmail.com>

---