package com.example.network;


import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import okhttp3.OkHttpClient;

public class GraphQLClient {
    private static final String BASE_URL = "https://transport-tracker-graphql.herokuapp.com/graphql";
    private static final String WEBSOCKET_URL = "wss://transport-tracker-graphql.herokuapp.com/graphql";

    private static ApolloClient INSTANCE = null;

    private GraphQLClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();


        INSTANCE = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(WEBSOCKET_URL, okHttpClient))
                .build();
    };

    public static ApolloClient
}
