// Keep the root build script dependency-free so `./gradlew build` succeeds in offline/restricted environments.
// Android module is included by default; disable with: `./gradlew -PenableAndroid=false build`
plugins {
  // ...

  // Add the dependency for the Google services Gradle plugin
  id("com.google.gms.google-services") version "4.4.4" apply false

}
tasks.register("build") {
    group = "build"
    description = "Assembles the root project. Android module is included by default; use -PenableAndroid=false to skip it."
}
