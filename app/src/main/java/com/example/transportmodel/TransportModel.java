package com.example.transportmodel;

import android.telecom.Call;

import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

public class TransportModel {
    private TransportSubscription initializeSubscription(LatLng farLeft, LatLng nearRight) {
        return TransportSubscription.builder()
                .minLon(farLeft.longitude)
                .minLat(nearRight.latitude)
                .maxLat(farLeft.latitude)
                .maxLon(nearRight.longitude)
                .build();
    }

    private void subscribeToEvents(TransportSubscription transportSubscription, final Callback callback) {
        Networking.apollo().subscribe(transportSubscription).execute(new ApolloSubscriptionCall.Callback<TransportSubscription.Data>() {
            @Override
            public void onResponse(@NotNull Response<TransportSubscription.Data> response) {
                callback.onEvent(response.data().transportEventsInArea());
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                callback.onError(e);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onTerminated() {

            }

            @Override
            public void onConnected() {
                System.out.println("Connected to subscription");
            }
        });
    }

    public void subscribeToTransportEvents(LatLng farLeft, LatLng nearRight, Callback callback) {
        subscribeToEvents(
                initializeSubscription(farLeft, nearRight),
                callback
        );
    }

    interface Callback {
        void onEvent(TransportSubscription.TransportEventsInArea transportEvent);
        void onError(@NotNull ApolloException e);
    }
}
