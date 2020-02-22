package com.example.stopmodel;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.hsl.StopDetailsQuery;
import com.hsl.StopsQuery;

import org.jetbrains.annotations.NotNull;

import okhttp3.Callback;

public class Stop {
  private static StopDetailsQuery initializeQuery(String id){
    return StopDetailsQuery.builder().id(id).build();
  }

  private static void makeStopDetailsQuery(StopDetailsQuery stopDetailsQuery, final Callback callback){
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

  public static void makeStop(String id, Callback callback){
    makeStopDetailsQuery(initializeQuery(id),callback);
  }

  public interface Callback{
    void onStop(StopDetailsQuery.Data stop);

    void onError(@NotNull ApolloException e);
  }
  /*private String stopId;
  private String stopName;
  private String stopCode;
  private String zoneId;
  private String vehicleMode;
  private int platformCode;

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;

  }

  public String getStopName() {
    return stopName;
  }

  public void setStopCode(String stopCode) {
    this.stopCode = stopCode;
  }

  public String getStopCode() {
    return stopCode;
  }

  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setVehicleMode(String vehicleMode) {
    this.vehicleMode = vehicleMode;
  }

  public String getVehicleMode() {
    return vehicleMode;
  }

  public void setPlatformCode(int platformCode) {
    this.platformCode = platformCode;
  }

  public int getPlatformCode() {
    return platformCode;
  }*/
}
