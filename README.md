# Transport Tracker App

## Overview
Android app for the Greater Helsinki Area that shows real-time public transport positions, arrivals, stops, and timetables. Supports buses, trams, trains, metro, and ferries. Works on Android 8.0+.

## Key Features
- Real-time vehicle positions on the map
- Next stops and arrival times
- Stop locations and timetables
- GraphQL-powered data access

## Data Sources
1. HSL GraphQL Routing API (stops and transport details)
   - https://digitransit.fi/en/developers/apis/1-routing-api/
2. HSL MQTT Vehicle Positions API, converted to GraphQL subscription
   - https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/
   - Relay service: https://github.com/serushakov/transport-tracker-mqtt-relay

## References
- GraphQL playground: https://transport-tracker-graphql.herokuapp.com/
- Javadoc: https://ilpy20.github.io/transport-tracker-app/javadoc/

## Project Structure
- app/src/main/java/com/example/...: Android app source
- app/src/main/res/: layouts, strings, drawables, styles
- app/src/main/graphql/com/hsl/: GraphQL schema files for Apollo
- app/src/test/: JVM unit tests
- app/src/androidTest/: instrumented tests
- javadoc/: generated API docs

## Development
Common commands:
```
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew generateApolloSources
./gradlew clean
```

If you change `app/src/main/graphql/com/hsl/schema.graphql`, regenerate Apollo sources with:
```
./gradlew generateApolloSources
```

## Git Commit Summary
Grouped author aliases (`ilyap`, `ilpy20`) under `Ilya Pyshkin`:
- Ilya Pyshkin (incl. `ilyap`, `ilpy20`): 57 commits
- Sergey Ushakov: 23 commits
- mahamudul: 3 commits
