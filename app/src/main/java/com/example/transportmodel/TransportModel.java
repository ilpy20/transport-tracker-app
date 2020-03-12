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
import java.util.List;
import java.util.Optional;

/**
 * TransportModel class initialize and communicate with transport subscription
 * @author Sergey Ushakov
 * @version 1.0
 * @since 2020-03-12
 */
public class TransportModel {
  private static final float COORDINATE_PADDING = 0.001f;
  private ApolloSubscriptionCall<TransportSubscription.Data> subscriptionInstance = null;

  private HashMap<String, Transport> transportPool;

  /**
   *Initializing hashmap for transport data
   */
  public TransportModel() {
    transportPool = new HashMap<>();
  }

  /**
   * Initialize subscription for transport
   * @param northeast coordinate of northeast point
   * @param southwest coordinate of southwest point
   * @return TransportSubscription
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
   * Subscribe to events
   * @param transportSubscription transport subscription
   * @param callback TransportModel callback
   */
  private void subscribeToEvents(TransportSubscription transportSubscription, final Callback callback) {
    subscriptionInstance = Networking.apollo().subscribe(transportSubscription);

    subscriptionInstance.execute(new ApolloSubscriptionCall.Callback<TransportSubscription.Data>() {
      /**
       * Gets called when GraphQL response is received and parsed successfully.
       * @param response the GraphQL response
       */
      @Override
      public void onResponse(@NotNull Response<TransportSubscription.Data> response) {
        TransportSubscription.TransportEventsInArea event = response.data().transportEventsInArea();
        if (event == null) return;

        Transport transport = saveTransportData(response.data().transportEventsInArea());

        if(transport == null) return;
        callback.onEvent(transport);
      }

      /**
       * Gets called when an unexpected exception occurs while creating the request or processing the response.
       * @param e ApolloException
       */
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

      /**
       * Print "Connected to subscription" then connection is successful
       */
      @Override
      public void onConnected() {
        System.out.println("Connected to subscription");
      }
    });
  }

  /**
   * Save data about transport
   * @param transportEvent transport subscription
   * @return transport class which contains data about transport
   */
  private Transport saveTransportData(TransportSubscription.TransportEventsInArea transportEvent) {
    Transport transport = transportPool.get(transportEvent.id());
    if (transportEvent.lat() == null || transportEvent.lon() == null) return null;

    if (transport != null) {
      try {
      transport.updateFromEvent(transportEvent);

      } catch (NullPointerException e) {
        // Do nothing
      }
    } else {
      transport = new Transport(transportEvent);
      transportPool.put(transportEvent.id(), transport);
    }

    return transport;
  }

  /**
   * Remove items from list keysToRemove(removing transport data from transportPool)
   * @param keysToRemove List<String>
   */
  public void removeItems(List<String> keysToRemove) {
    keysToRemove
      .forEach(key -> transportPool.remove(key));
  }

  /**
   * Subscribe to transport events
   * @param northeast coordinate of northeast point
   * @param southwest coordinate of southwest point
   * @param callback TransportModel callback
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
   *Terminating transport subscription
   */
  public void terminateSubscription() {
    if (subscriptionInstance != null) {
      subscriptionInstance.cancel();
    }
  }

  /**
   * Set some data about transport
   * @param tag Transport tag(stored name and direction of the transport)
   * @return Optional<Transport>
   */
  public Optional<Transport> findTransportByTag(TransportTag tag) {
    return transportPool
      .values()
      .stream()
      .filter((item) -> item.getRouteDisplayName().equals(tag.name)
        && item.getRoutingApiCompatibleDirection().equals(tag.direction))
      .findFirst();
  }

  /**
   *Callback interface
   */
  public interface Callback {
    void onEvent(Transport transport);

    void onError(@NotNull ApolloException e);
  }
}
