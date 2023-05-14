# Gradle

[![](https://jitpack.io/v/zj565061763/eventflow.svg)](https://jitpack.io/#zj565061763/eventflow)

# Sample

```kotlin
coroutineScope.launch {
    fEventFlow<TestEvent>().collect {
        // collect event
    }
}

// post event
fEventPost(TestEvent())
```