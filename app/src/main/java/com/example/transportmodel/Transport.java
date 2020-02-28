package com.example.transportmodel;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportDetailsQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Transport {
  private String id;
  private String routeId;
  private String routeName;
  private String routeDisplayName;
  private String nextStop;
  private String stop;
  private ArrayList<String> stopCodes;
  private ArrayList<String> stopNames;
  private ArrayList<String> stopZones;
  private ArrayList<String> routeTime;
  private ArrayList<String> routeDelay;
  private Long timeArrive;
  private BigDecimal serviceDay;
  private LatLng location;

  public Transport(String id, Double lat, Double lon) {
    this.id = id;
    this.location = new LatLng(lat, lon);
  }

  public Transport(TransportSubscription.TransportEventsInArea transportEvent) {
    id = transportEvent.id();
    routeDisplayName = transportEvent.desi();
    updateFromEvent(transportEvent);
  }

  private static TransportDetailsQuery initializeQueryFromStop(String id) {
    return TransportDetailsQuery.builder().id(id).build();
  }

  private static TransportDetailsQuery initializeQueryFromMap(String date, int dir, String route, int time) {
    return TransportDetailsQuery.builder().date(date).dir(dir).route(route).time(time).build();
  }

  public void updateFromEvent(TransportSubscription.TransportEventsInArea transportEvent) {
    location = new LatLng(transportEvent.lat(), transportEvent.lon());
    nextStop = transportEvent.nextStop();
    stop = transportEvent.stop();
  }

  public LatLng getLocation() {
    return location;
  }

  public String getId() {
    return id;
  }

  public void setLocation(LatLng location) {
    this.location = location;
  }

  public String getRouteDisplayName() {
    return routeDisplayName;
  }

  public String getRouteId() {
    return routeId;
  }

  public String getRouteName() {
    return routeName;
  }


  public String getNextStop() {
    return nextStop;
  }

  public void setStop(String stop) {
    this.stop = stop;
  }

  public String getStop() {
    return stop;
  }

  public void makeTransportDetailsArrays(@NonNull TransportDetailsQuery.Data data, long unixTime){

  }

  public static void makeTransportDetailsQuery(TransportDetailsQuery transportDetailsQuery, Callback callback){
    Networking.apollo().query(transportDetailsQuery).enqueue(new ApolloCall.Callback<TransportDetailsQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<TransportDetailsQuery.Data> response) {
        TransportDetailsQuery.Data data = response.data();
        if(data==null) return;
        callback.onTransport(data);
      }

      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e);}
    });
  }

  public static void getTransportDetailsFromStop(String id, Callback callback){
    makeTransportDetailsQuery(initializeQueryFromStop(id),callback);
  }

  public static void getTransportDetailsFromMap(String date, int dir, String route, int time, Callback callback){
    makeTransportDetailsQuery(initializeQueryFromMap(date,dir,route,time),callback);
  }

  public interface Callback {
    void onTransport(@NonNull TransportDetailsQuery.Data data);

    void onError(@NotNull ApolloException e);
  }
}
