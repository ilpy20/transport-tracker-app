package com.example.stopmodel;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.StopDetailsQuery;
import com.hsl.StopsQuery;
import com.hsl.type.Mode;

import org.jetbrains.annotations.NotNull;

import okhttp3.Callback;

public class Stop {
  private String id;
  private String gtfsId;
  private LatLng location;
  private String name;
  private String code;
  private String zoneId;
  private Mode vehicleMode;
  private String platformCode;

  private static StopDetailsQuery initializeQuery(String id) {
    return StopDetailsQuery.builder().id(id).build();
  }

  public Stop(StopsQuery.StopsByBbox stops) {
    id = stops.id();
    gtfsId = stops.gtfsId();
    name = stops.name();
    //code = stop.stop().code();
    zoneId = stops.zoneId();
    vehicleMode = stops.vehicleMode();
    platformCode = stops.platformCode();
    location = new LatLng(stops.lat(), stops.lon());
  }
  public Stop(StopDetailsQuery.Data stop){
    code = stop.stop().code();
  }

  public String getId() {
    return id;
  }

  public String getZoneId() {
    return zoneId;
  }

  public Mode getVehicleMode() {
    return vehicleMode;
  }

  public String getPlatformCode() {
    return platformCode;
  }

  public String getCode() {
    return code;
  }

  public String getGtfsId() {
    return gtfsId;
  }

  public String getName() {
    return name;
  }

  public LatLng getLocation() {
    return this.location;
  }

  private static void makeStopDetailsQuery(StopDetailsQuery stopDetailsQuery, final Callback callback) {
    Networking.apollo().query(stopDetailsQuery).enqueue(new ApolloCall.Callback<StopDetailsQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<StopDetailsQuery.Data> response) {
        callback.onStop(response.data());
      }

      @Override
      public void onFailure(@NotNull ApolloException e) {
        callback.onError(e);
      }
    });
  }

  public static void makeStop(String id, Callback callback) {
    makeStopDetailsQuery(initializeQuery(id), callback);
  }


  public interface Callback {
    void onStop(StopDetailsQuery.Data stop);

    void onError(@NotNull ApolloException e);
  }
}
