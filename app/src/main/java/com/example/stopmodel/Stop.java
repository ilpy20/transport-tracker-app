package com.example.stopmodel;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.StopDetailsQuery;
import com.hsl.StopsQuery;
import com.hsl.type.Mode;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

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
  private ArrayList<String> routeNums;
  private ArrayList<String> routeNames;
  private ArrayList<String> routeTime;
  private ArrayList<String> routeDelay;
  private Long timeArrive;
  private BigDecimal serviceDay;

  private static StopDetailsQuery initializeQuery(String id) {
    return StopDetailsQuery.builder().id(id).build();
  }

  public Stop(StopsQuery.StopsByBbox stop) {
    id = stop.id();
    gtfsId = stop.gtfsId();
    name = stop.name();
    code = stop.code();
    zoneId = stop.zoneId();
    vehicleMode = stop.vehicleMode();
    platformCode = stop.platformCode();
    location = new LatLng(stop.lat(), stop.lon());
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

  public ArrayList<String> getRouteNums() {
    return routeNums;
  }

  public ArrayList<String> getRouteNames() {
    return routeNames;
  }

  public ArrayList<String> getRouteTime() {
    return routeTime;
  }

  public ArrayList<String> getRouteDelay() {
    return routeDelay;
  }

  public LatLng getLocation() {
    return this.location;
  }

  public void makeStopDetailsArrays(@NonNull StopDetailsQuery.Data data, long unixTime) {
    if (data.stop() == null) return;
    routeNums = new ArrayList<>();
    routeNames = new ArrayList<>();
    routeTime = new ArrayList<>();
    routeDelay = new ArrayList<>();
    List<StopDetailsQuery.StoptimesWithoutPattern> nearbyRoutes = data.stop().stoptimesWithoutPatterns();
    for (int i = 0; i < nearbyRoutes.size(); i++) {
      nearbyRoutes.get(i).trip().gtfsId();
      routeNums.add(nearbyRoutes.get(i).trip().routeShortName());
      routeNames.add(nearbyRoutes.get(i).headsign());
      nearbyRoutes.get(i).scheduledArrival();
      serviceDay = (BigDecimal) nearbyRoutes.get(i).serviceDay();
      if (nearbyRoutes.get(i).realtimeArrival() != null)
        timeArrive = Long.valueOf(nearbyRoutes.get(i).realtimeArrival());
      else timeArrive = Long.valueOf(nearbyRoutes.get(i).scheduledArrival());
      routeTime.add(Long.toString((timeArrive+serviceDay.longValue()-unixTime) / 60) + " min");
      if (nearbyRoutes.get(i).arrivalDelay() > 0)
        routeDelay.add("Delayed " + Integer.toString(nearbyRoutes.get(i).arrivalDelay() / 60)+" min");
      else if (nearbyRoutes.get(i).arrivalDelay() < 0)
        routeDelay.add("Quicked " + Integer.toString(-nearbyRoutes.get(i).arrivalDelay() / 60)+" min");
      else routeDelay.add("On time");
      nearbyRoutes.get(i).scheduledDeparture();
      nearbyRoutes.get(i).realtimeDeparture();

    }
  }

  private static void makeStopDetailsQuery(StopDetailsQuery stopDetailsQuery, final Callback callback) {
    Networking.apollo().query(stopDetailsQuery).enqueue(new ApolloCall.Callback<StopDetailsQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<StopDetailsQuery.Data> response) {
        StopDetailsQuery.Data data = response.data();
        if (data == null) return;
        //stop.makeStopDetailsArrays(data);

        callback.onStop(data);
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
    void onStop(@NonNull StopDetailsQuery.Data data);

    void onError(@NotNull ApolloException e);
  }
}
