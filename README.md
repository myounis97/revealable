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
   val revealableState = rememberRevealableState(
        allowMultipleReveals = false,
        coroutineScope = rememberCoroutineScope(),
    )
   ```

2. **Initialize the `RevealableItemState`**:
   ```kotlin
   val itemState = rememberRevealableItemState(
        positionalThreshold = { distance -> distance * 0.5f },
        velocityThreshold = { with(density) { 150.dp.toPx() } },
        confirmValueChange = { true },
    )
   ```

3. **Compose your UI**:
   ```kotlin
    val coroutineScope = rememberCoroutineScope()

   Revealable(
        state = revealableState,
        itemState = itemState,
        modifier = modifier,
        startContent = {
            Box(
                modifier = Modifier
                    .background(Color.Cyan)
                    .fillMaxHeight()
                    .clickable { coroutineScope.launch { itemState.animateTo(RevealableValue.EndRevealed) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Hello")
            }
        },
        endContent = {
            Box(
                modifier = Modifier
                    .background(Color.Red)
                    .fillMaxHeight()
                    .clickable { coroutineScope.launch { itemState.animateTo(RevealableValue.StartRevealed) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Hello")
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