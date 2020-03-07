package com.example.transportmodel;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportDetailsFromMapQuery;
import com.hsl.TransportDetailsFromStopQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transport {
  private String id;
  private String routeId;
  private static String routeName;
  private String routeMode;
  private int routeDirection;
  private String routeDisplayName;
  private int routeStart;
  private static String routeDate;
  private String nextStop;
  private String stop;
  private static ArrayList<String> stopId;
  private static ArrayList<String> stopCodes;
  private static ArrayList<String> stopNames;
  private static ArrayList<String> stopZones;
  private static ArrayList<String> platformCodes;
  private static ArrayList<String> routeTime;
  private static ArrayList<String> routeDelay;
  private static Long timeArrive;
  private static Long serviceDay;
  private LatLng location;

  public Transport(String id, Double lat, Double lon) {
    this.id = id;
    this.location = new LatLng(lat, lon);
  }

  public Transport(TransportSubscription.TransportEventsInArea transportEvent) {
    id = transportEvent.id();
    routeDisplayName = transportEvent.desi();
    routeMode = transportEvent.mode();
    routeId = "HSL:"+transportEvent.route();
    routeDirection = Integer.parseInt(transportEvent.dir())-1;
    String time = transportEvent.start();
    String[] units = time.split(":");
    int hours = Integer.parseInt(units[0]); //first element
    int minutes = Integer.parseInt(units[1]); //second element
    routeStart = 3600 * hours + 60*minutes; //add up our values
    routeDate = transportEvent.oday();
    updateFromEvent(transportEvent);
  }

  private static TransportDetailsFromStopQuery initializeQueryFromStop(String id) {
    return TransportDetailsFromStopQuery.builder().id(id).build();
  }

  private static TransportDetailsFromMapQuery initializeQueryFromMap(String date, int dir, String route, int time) {
    return TransportDetailsFromMapQuery.builder().date(date).dir(dir).route(route).time(time).build();
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

  public String getRouteMode() { return routeMode; }

  public int getRouteStart() { return routeStart; }

  public static String getRouteDate() { return routeDate; }

  public Long getTimeArrive() { return timeArrive; }

  public int getRouteDirection() { return routeDirection; }

  public Long getServiceDay() { return serviceDay; }

  public ArrayList<String> getStopId() { return stopId; }

  public ArrayList<String> getStopCodes() {
    return stopCodes;
  }

  public ArrayList<String> getStopNames() {
    return stopNames;
  }

  public ArrayList<String> getStopZones() {
    return stopZones;
  }

  public ArrayList<String> getRouteTime() {
    return routeTime;
  }

  public ArrayList<String> getRouteDelay() {
    return routeDelay;
  }

  public ArrayList<String> getPlatformCodes() { return platformCodes; }

  public String getNextStop() {
    return nextStop;
  }

  public void setStop(String stop) {
    this.stop = stop;
  }

  public String getStop() {
    return stop;
  }


  public static void makeTransportDetailsFromMapQuery(TransportDetailsFromMapQuery transportDetailsQuery, Callback callback){
    Networking.apollo().query(transportDetailsQuery).enqueue(new ApolloCall.Callback<TransportDetailsFromMapQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<TransportDetailsFromMapQuery.Data> response) {
        TransportDetailsFromMapQuery.Data data = response.data();
        if(data==null) return;
        long unixTime = Instant.now().getEpochSecond();
        if (data.fuzzyTrip() != null) {
          stopId = new ArrayList<>();
          stopCodes = new ArrayList<>();
          stopNames = new ArrayList<>();
          stopZones = new ArrayList<>();
          platformCodes = new ArrayList<>();
          routeTime = new ArrayList<>();
          routeDelay = new ArrayList<>();
          routeName = data.fuzzyTrip().tripHeadsign();
          List<TransportDetailsFromMapQuery.Stoptime> routeList = data.fuzzyTrip().stoptimes();
          for (int i = 0; i < routeList.size(); i++) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
              Date date = format.parse(getRouteDate() + " 00:00:00");
              serviceDay = date.getTime() / 1000;
            } catch (ParseException e) {
              e.printStackTrace();
            }
            if (routeList.get(i).realtimeArrival() != null)
              timeArrive = Long.valueOf(routeList.get(i).realtimeArrival());
            else timeArrive = Long.valueOf(routeList.get(i).scheduledArrival());
            if (timeArrive + serviceDay >= unixTime) {
              stopId.add(routeList.get(i).stop().gtfsId());
              stopNames.add(routeList.get(i).stop().name());
              stopCodes.add(routeList.get(i).stop().code());
              stopZones.add(routeList.get(i).stop().zoneId());
              platformCodes.add(routeList.get(i).stop().platformCode());
              routeTime.add(Long.toString((timeArrive + serviceDay - unixTime) / 60) + " min");
              //else routeTime.add(Long.toString((unixTime-timeArrive-serviceDay)/60)+" min"+" ago");
              if (routeList.get(i).arrivalDelay() > 0)
                routeDelay.add("Delayed " + Integer.toString(routeList.get(i).arrivalDelay() / 60) + " min");
              else if (routeList.get(i).arrivalDelay() < 0)
                routeDelay.add("Quicked " + Integer.toString(-routeList.get(i).arrivalDelay() / 60) + " min");
              else routeDelay.add("On time");
            }
          }
        }
        callback.onTransportFromMap(data);
      }

      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e);}
    });
  }

  public static void makeTransportDetailsFromStopQuery(TransportDetailsFromStopQuery transportDetailsQuery, Callback callback){
    Networking.apollo().query(transportDetailsQuery).enqueue(new ApolloCall.Callback<TransportDetailsFromStopQuery.Data>() {
      @Override
      public void onResponse(@NotNull Response<TransportDetailsFromStopQuery.Data> response) {
        TransportDetailsFromStopQuery.Data data = response.data();
        if(data==null) return;
        long unixTime = Instant.now().getEpochSecond();
        if(data.trip()!=null){
          stopId = new ArrayList<>();
          stopCodes = new ArrayList<>();
          stopNames = new ArrayList<>();
          stopZones = new ArrayList<>();
          platformCodes = new ArrayList<>();
          routeTime = new ArrayList<>();
          routeDelay = new ArrayList<>();
          routeName = data.trip().tripHeadsign();
          List<TransportDetailsFromStopQuery.Stoptime> routeList = data.trip().stoptimes();
          for(int i = 0; i<routeList.size();i++) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
              Date date = format.parse(getRouteDate() + " 00:00:00");
              serviceDay = date.getTime() / 1000;
            } catch (ParseException e) {
              e.printStackTrace();
            }
            if (routeList.get(i).realtimeArrival() != null)
              timeArrive = Long.valueOf(routeList.get(i).realtimeArrival());
            else timeArrive = Long.valueOf(routeList.get(i).scheduledArrival());
            if (timeArrive + serviceDay >= unixTime) {
              stopId.add(routeList.get(i).stop().gtfsId());
              stopNames.add(routeList.get(i).stop().name());
              stopCodes.add(routeList.get(i).stop().code());
              stopZones.add(routeList.get(i).stop().zoneId());
              platformCodes.add(routeList.get(i).stop().platformCode());
              routeTime.add(Long.toString((timeArrive + serviceDay - unixTime) / 60) + " min");
              //else routeTime.add(Long.toString((unixTime-timeArrive-serviceDay)/60)+" min"+" ago");
              if (routeList.get(i).arrivalDelay() > 0)
                routeDelay.add("Delayed " + Integer.toString(routeList.get(i).arrivalDelay() / 60) + " min");
              else if (routeList.get(i).arrivalDelay() < 0)
                routeDelay.add("Quicked " + Integer.toString(-routeList.get(i).arrivalDelay() / 60) + " min");
              else routeDelay.add("On time");
            }
          }
        }
        callback.onTransportFromStop(data);
      }

      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e);}
    });
  }

  public static void getTransportDetailsFromStop(String id, Callback callback){
    makeTransportDetailsFromStopQuery(initializeQueryFromStop(id),callback);
  }

  public static void getTransportDetailsFromMap(String date, int dir, String route, int time, Callback callback){
    makeTransportDetailsFromMapQuery(initializeQueryFromMap(date,dir,route,time),callback);
  }

  public interface Callback {
    void onTransportFromMap(@NonNull TransportDetailsFromMapQuery.Data data);

    void onTransportFromStop(@NonNull TransportDetailsFromStopQuery.Data data);

    void onError(@NotNull ApolloException e);
  }
}
