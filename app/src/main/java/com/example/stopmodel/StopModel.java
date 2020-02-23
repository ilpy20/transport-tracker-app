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


public class StopModel {
  private StopsQuery initializeQuery(LatLng farLeft, LatLng nearRight) {
    return StopsQuery.builder()
        .minLon(farLeft.longitude)
        .minLat(nearRight.latitude)
        .maxLat(farLeft.latitude)
        .maxLon(nearRight.longitude)
        .build();
  }

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

  public void makeStops(LatLng farLeft, LatLng nearRight, StopModel.Callback callback) {
    makeStopsQuery(
        initializeQuery(farLeft, nearRight),
        callback
    );
  }

  public interface Callback {
    void onStops(List<Stop> stops);

    void onError(@NotNull ApolloException e);
  }
}

