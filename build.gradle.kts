// Keep the root build script dependency-free so `./gradlew build` succeeds in offline/restricted environments.
// Enable Android module resolution with: `./gradlew -PenableAndroid=true build`

tasks.register("build") {
    group = "build"
    description = "Assembles the root project. Use -PenableAndroid=true to build the Android app module."
}
