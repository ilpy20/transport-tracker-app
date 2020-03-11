package com.example.stopmodel;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.StopsQuery;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * StopModel class initialize StopsQuery and convert to List<Stop>
 * @author Ilya Pyshkin
 * @version 1.0
 * @since 2020-03-11
 */
public class StopModel {
  /**
   * Initialize StopsQuery
   * @param farLeft coordinate of farLeft point
   * @param nearRight coordinate of nearRight point
   * @return StopsQuery query of stops
   */
  private StopsQuery initializeQuery(LatLng farLeft, LatLng nearRight) {
    return StopsQuery.builder()
        .minLon(farLeft.longitude)
        .minLat(nearRight.latitude)
        .maxLat(farLeft.latitude)
        .maxLon(nearRight.longitude)
        .build();
  }

  /**
   * Making StopsQuery as a list of stops(Stop as a class)
   * @param stopsQuery query of stops
   * @param callback StopsQuery callback from Apollo server
   */
  private void makeStopsQuery(StopsQuery stopsQuery, final Callback callback) {
    Networking.apollo().query(stopsQuery).enqueue(new ApolloCall.Callback<StopsQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<StopsQuery.Data> response) {
        StopsQuery.Data data = response.data();
        if(data == null) return;

        List<Stop> stops = data
          .stopsByBbox()
          .stream()
          .map(stop -> new Stop(stop))
          .collect(Collectors.toList());

        callback.onStops(stops);
      }

      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e); }

    });
  }

  /**
   * Initialize stopsQuery and transform to List<Stop> stops
   * @param farLeft coordinate of farLeft point
   * @param nearRight coordinate of nearRight point
   * @param callback StopModel callback
   */
  public void makeStops(LatLng farLeft, LatLng nearRight, StopModel.Callback callback) {
    makeStopsQuery(
        initializeQuery(farLeft, nearRight),
        callback
    );
  }

  /**
   * Interface of StopModel callback
   */
  public interface Callback {
    void onStops(List<Stop> stops);

    void onError(@NotNull ApolloException e);
  }
}

