package com.example.transportmodel;

import android.util.Log;

import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 *
 * @author Sergey Ushakov
 * @version 1.0
 * @since 2020-03-11
 */
public class TransportModel {
  private static final float COORDINATE_PADDING = 0.001f;
  private ApolloSubscriptionCall<TransportSubscription.Data> subscriptionInstance = null;

  private HashMap<String, Transport> transportPool;

  public TransportModel() {
    transportPool = new HashMap<>();
  }

  /**
   *
   * @param northeast
   * @param southwest
   * @return
   */
  private TransportSubscription initializeSubscription(LatLng northeast, LatLng southwest) {
    return TransportSubscription.builder()
      .minLon(southwest.longitude + COORDINATE_PADDING)
      .minLat(southwest.latitude + COORDINATE_PADDING)
      .maxLat(northeast.latitude + COORDINATE_PADDING)
      .maxLon(northeast.longitude + COORDINATE_PADDING)
      .build();
  }

  /**
   *
   * @param transportSubscription
   * @param callback
   */
  private void subscribeToEvents(TransportSubscription transportSubscription, final Callback callback) {
    subscriptionInstance = Networking.apollo().subscribe(transportSubscription);

    subscriptionInstance.execute(new ApolloSubscriptionCall.Callback<TransportSubscription.Data>() {
      @Override
      public void onResponse(@NotNull Response<TransportSubscription.Data> response) {
        TransportSubscription.TransportEventsInArea event = response.data().transportEventsInArea();
        if(event == null) return;

        Transport transport = saveTransportData(response.data().transportEventsInArea());
        callback.onEvent(transport);
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

  /**
   *
   * @param transportEvent
   * @return
   */
  private Transport saveTransportData(TransportSubscription.TransportEventsInArea transportEvent) {
    Transport transport = transportPool.get(transportEvent.id());

    if(transport != null) {
      transport.updateFromEvent(transportEvent);
    } else {
      transport = new Transport(transportEvent);
    }

    return transport;
  }

  /**
   *
   * @param northeast
   * @param southwest
   * @param callback
   */
  public void subscribeToTransportEvents(LatLng northeast, LatLng southwest, Callback callback) {
    if (subscriptionInstance != null) {
      terminateSubscription();
    }
    subscribeToEvents(
      initializeSubscription(northeast, southwest),
      callback
    );
  }

  /**
   *
   */
  public void terminateSubscription() {
    if (subscriptionInstance != null) {
      subscriptionInstance.cancel();
    }
  }

  /**
   *
   */
  public interface Callback {
    void onEvent(Transport transport);

    void onError(@NotNull ApolloException e);
  }
}
