package com.example.network;


import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import okhttp3.OkHttpClient;

/**
 * Networking class works with Transport Tracker GraphQL server
 * @author Sergey Ushakov
 * @version 1.0
 * @since 2020-03-12
 */
public class Networking {
    private static final String BASE_URL = "https://transport-tracker-graphql.herokuapp.com/graphql";
    private static final String WEBSOCKET_URL = "wss://transport-tracker-graphql.herokuapp.com/graphql";

    private static ApolloClient INSTANCE = initApollo();

    /**
     * Configurate the ApolloClient
     * @return ApolloClient
     */
    private static ApolloClient initApollo() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();


        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(WEBSOCKET_URL, okHttpClient))
                .build();
    }


    /**
     * Get ApolloClient
     * @return ApolloClient INSTANCE
     */
    public static ApolloClient apollo() {
        return INSTANCE;
    }
}
