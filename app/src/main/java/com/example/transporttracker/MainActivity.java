package com.example.transporttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.hsl.TestQuery;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();


        TestQuery testQuery = TestQuery.builder().build();

        apolloClient.query(testQuery).enqueue(new ApolloCall.Callback<TestQuery.Data>() {

            @Override
            public void onResponse(@NotNull Response<TestQuery.Data> response) {
                final StringBuffer buffer = new StringBuffer();
                for (TestQuery.Edge edge : response.data().stopsByRadius().edges()) {
                    System.out.println(edge.node().stop().name());
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println(e);
            }
        });

    }
}
