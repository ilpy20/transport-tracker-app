# Repository Guidelines

## Project Structure & Module Organization
- `app/src/main/java/com/example/...`: Android app source (activities, fragments, models, networking).
- `app/src/main/res/`: layouts, strings, drawables, mipmaps, and styles.
- `app/src/main/graphql/com/hsl/`: GraphQL schema files used by Apollo (`schema.graphql`, `schema.json`).
- `app/src/test/`: local unit tests (JVM).
- `app/src/androidTest/`: instrumented tests (device/emulator).
- `javadoc/`: generated API docs (static HTML).

## Build, Test, and Development Commands
- `./gradlew assembleDebug`: build a debug APK.
- `./gradlew installDebug`: build and install on a connected device/emulator.
- `./gradlew test`: run JVM unit tests.
- `./gradlew connectedAndroidTest`: run instrumented tests.
- `./gradlew generateApolloSources`: regenerate Apollo models after GraphQL schema changes.
- `./gradlew clean`: remove build outputs.

## Coding Style & Naming Conventions
- Java, Android SDK (compile/target 29). Use 4-space indentation, no tabs.
- Follow Android Studio formatting and standard Java naming:
  - Classes: `PascalCase` (e.g., `StopModel`).
  - Methods/fields: `camelCase`.
  - Resources: `snake_case` (e.g., `activity_main.xml`).
- Keep packages under `com.example.transporttracker`, `com.example.transportmodel`, `com.example.stopmodel`, `com.example.network`.

## Testing Guidelines
- Unit tests use JUnit 4 (`app/src/test`).
- Instrumented tests use AndroidX Test + Espresso (`app/src/androidTest`).
- Name tests `*Test.java` (follow existing examples like `ExampleUnitTest`).
- No explicit coverage threshold; add tests for new logic where practical.

## Commit & Pull Request Guidelines
- Commit messages are short, imperative, and lowercase (e.g., “add localization”, “fix permissions crash”).
- PRs should include: a concise summary, testing notes/commands, and screenshots for UI changes.
- Link relevant issues or tickets when available.

## Configuration & Secrets
- Google Maps key is injected via Gradle property `GOOGLE_MAPS_API_KEY`.
  - Example: `./gradlew assembleDebug -PGOOGLE_MAPS_API_KEY=YOUR_KEY`.
- Do not hardcode API keys in source or resources.
