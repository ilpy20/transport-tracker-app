# Transport tracker app
Application with real-time positions and time arriving of transport. Works on Android 8.0+ in Great Helsinki Area, Finland. 
The application provides real-time positions and list of next stops for bus, tram, train, metro and ferry and their stop locations and timetable with [GraphQL](https://graphql.org/).
In Great Helsinki Area the application get's data from:
1. [HSL GraphQL API](https://digitransit.fi/en/developers/apis/1-routing-api/)(positions of stops and details for stop and transport)
2. Real-time transport locations gets from [HSL MQTT vehicle position API](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) converted to GraphQL supscription (Link of GitHub repository of MQTT to GraphQL supscription converter: https://github.com/serushakov/transport-tracker-mqtt-relay)
## References
1. GraphQL playground: https://transport-tracker-graphql.herokuapp.com/
2. Javadoc: https://ilpy20.github.io/transport-tracker-app/javadoc/
## Tips to use repository
1. You can test your query in GraphQL playground
2. If you changed query in schema.graphql you need to run command in Git Bash:
```
./gradlew generateApolloSources
```
