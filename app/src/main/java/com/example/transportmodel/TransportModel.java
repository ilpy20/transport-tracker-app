package com.example.transportmodel;

import android.util.Log;

import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

public class TransportModel {
  private static final float COORDINATE_PADDING = 0.01f;
  private ApolloSubscriptionCall<TransportSubscription.Data> subscriptionInstance = null;

  private TransportSubscription initializeSubscription(LatLng farLeft, LatLng nearRight) {
    return TransportSubscription.builder()
      .minLon(farLeft.longitude + COORDINATE_PADDING)
      .minLat(nearRight.latitude + COORDINATE_PADDING)
      .maxLat(farLeft.latitude + COORDINATE_PADDING)
      .maxLon(nearRight.longitude + COORDINATE_PADDING)
      .build();
  }


  private void subscribeToEvents(TransportSubscription transportSubscription, final Callback callback) {
    subscriptionInstance = Networking.apollo().subscribe(transportSubscription);

    subscriptionInstance.execute(new ApolloSubscriptionCall.Callback<TransportSubscription.Data>() {
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
    if (subscriptionInstance != null) {
      terminateSubscription();
    }
    subscribeToEvents(
      initializeSubscription(farLeft, nearRight),
      callback
    );
  }

  public void terminateSubscription() {
    if (subscriptionInstance != null) {
      subscriptionInstance.cancel();
    }
  }

  public interface Callback {
    void onEvent(TransportSubscription.TransportEventsInArea transportEvent);

    void onError(@NotNull ApolloException e);
  }
}
