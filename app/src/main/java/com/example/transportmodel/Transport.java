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

/**
 * Transport class where stored information about transport
 * @author Ilya Pyshkin, Sergey Ushakov
 * @version 1.0
 * @since 2020-03-12
 */
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

  /**
   * Initialize Transport class(not used)
   * @param id id of the transport
   * @param lat Double latitude
   * @param lon Double longitude
   */
  public Transport(String id, Double lat, Double lon) {
    this.id = id;
    this.location = new LatLng(lat, lon);
  }

  /**
   * Get data from transport subscription and send to the Transport Class
   * @param transportEvent transport subscription
   */
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

  /**
   * initialize TransportDetailsFromStopQuery(in future)
   * @param id id of the transport
   * @return TransportDetailsFromStopQuery
   */
  private static TransportDetailsFromStopQuery initializeQueryFromStop(String id) {
    return TransportDetailsFromStopQuery.builder().id(id).build();
  }

  /**
   * initialize TransportDetailsFromMapQuery
   * @param date date of the trip
   * @param dir direction of the transport
   * @param route if of the route
   * @param time start of the trip
   * @return TransportDetailsFromMapQuery
   */
  private static TransportDetailsFromMapQuery initializeQueryFromMap(String date, int dir, String route, int time) {
    return TransportDetailsFromMapQuery.builder().date(date).dir(dir).route(route).time(time).build();
  }

  /**
   * Update transport subscription(changing location of the transport....)
   * @param transportEvent transport subscription
   */
  public void updateFromEvent(TransportSubscription.TransportEventsInArea transportEvent) {
    LatLng position;
    try {

      position =  new LatLng(transportEvent.lat(), transportEvent.lon());

    } catch (NullPointerException e) {
      position = this.location;
    }
    this.location = position;
    nextStop = transportEvent.nextStop();
    stop = transportEvent.stop();

  }

  /**
   * Get location of the transport
   * @return LatLng location
   */
  public LatLng getLocation() {
    return location;
  }

  /**
   * Get id of the transport
   * @return String id
   */
  public String getId() {
    return id;
  }

  /**
   * Set location of the transport
   * @param location LatLng
   */
  public void setLocation(LatLng location) {
    this.location = location;
  }

  /**
   * Get route display name of the transport
   * @return String routeDisplayName
   */
  public String getRouteDisplayName() {
    return routeDisplayName;
  }

  /**
   * Get route id of the transport
   * @return String id
   */
  public String getRouteId() {
    return routeId;
  }

  /**
   * Get route name of the transport
   * @return String routeName
   */
  public String getRouteName() {
    return routeName;
  }

  /**
   * Get route mode of the transport
   * @return String routeMode
   */
  public String getRouteMode() { return routeMode; }

  /**
   * Get start of the trip
   * @return String routeStart
   */
  public int getRouteStart() { return routeStart; }

  /**
   * Get date of the trip
   * @return String routeDate
   */
  public static String getRouteDate() { return routeDate; }

  /**
   * Get time arriving
   * @return Long timeArrive
   */
  public Long getTimeArrive() { return timeArrive; }

  /**
   * Get rote direction
   * @return int routeDirection
   */
  public int getRouteDirection() { return routeDirection; }

  /**
   * Get routing API compatible direction
   * @return String Integer.toString(routeDirection)
   */
  public String getRoutingApiCompatibleDirection() { return Integer.toString(routeDirection); };

  /**
   * Get service day
   * @return Long serviceDay
   */
  public Long getServiceDay() { return serviceDay; }

  /**
   * Get ArrayList<String> with stop id for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getStopId() { return stopId; }

  /**
   * Get ArrayList<String> with stop codes for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getStopCodes() {
    return stopCodes;
  }

  /**
   * Get ArrayList<String> with stop names for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getStopNames() {
    return stopNames;
  }

  /**
   * Get ArrayList<String> with stop zones for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getStopZones() {
    return stopZones;
  }

  /**
   * Get ArrayList<String> with route times for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getRouteTime() {
    return routeTime;
  }

  /**
   * Get ArrayList<String> with route delays for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getRouteDelay() {
    return routeDelay;
  }

  /**
   * Get ArrayList<String> with platform codes for setting transport bottom sheet
   * @return ArrayList<String>
   */
  public ArrayList<String> getPlatformCodes() { return platformCodes; }

  /**
   * Get next stop for transport
   * @return String
   */
  public String getNextStop() {
    return nextStop;
  }

  /**
   * Set stop in Transport class
   * @param stop stop
   */
  public void setStop(String stop) {
    this.stop = stop;
  }

  /**
   * Get stop from Transport class
   * @return String stop
   */
  public String getStop() {
    return stop;
  }


  /**
   * Set up data to Transport class from TransportDetailsFromMapQuery
   * @param transportDetailsQuery query of transport details from map
   * @param callback Apollo callback
   */
  public static void makeTransportDetailsFromMapQuery(TransportDetailsFromMapQuery transportDetailsQuery, Callback callback){
    Networking.apollo().query(transportDetailsQuery).enqueue(new ApolloCall.Callback<TransportDetailsFromMapQuery.Data>() {
      /**
       * Gets called when GraphQL response is received and parsed successfully.
       * @param response the GraphQL response
       */
      @Override
      public void onResponse(@NotNull Response<TransportDetailsFromMapQuery.Data> response) {
        TransportDetailsFromMapQuery.Data data = response.data();
        if(data==null) return;
        long unixTime = Instant.now().getEpochSecond(); //get unix time in seconds
        if (data.fuzzyTrip() != null) {
          //initialize arrays
          stopId = new ArrayList<>();
          stopCodes = new ArrayList<>();
          stopNames = new ArrayList<>();
          stopZones = new ArrayList<>();
          platformCodes = new ArrayList<>();
          routeTime = new ArrayList<>();
          routeDelay = new ArrayList<>();
          //fill data
          routeName = data.fuzzyTrip().tripHeadsign();
          //fill data into the arrays
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
                routeDelay.add("Earlier " + Integer.toString(-routeList.get(i).arrivalDelay() / 60) + " min");
              else routeDelay.add("On time");
            }
          }
        }
        callback.onTransportFromMap(data);
      }

      /**
       * Gets called when an unexpected exception occurs while creating the request or processing the response.
       * @param e ApolloException
       */
      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e);}
    });
  }

  /**
   * Set up data to Transport class from TransportDetailsFromStopQuery (in future)
   * @param transportDetailsQuery query of transport details from stop
   * @param callback Apollo callback
   */
  public static void makeTransportDetailsFromStopQuery(TransportDetailsFromStopQuery transportDetailsQuery, Callback callback){
    Networking.apollo().query(transportDetailsQuery).enqueue(new ApolloCall.Callback<TransportDetailsFromStopQuery.Data>() {
      /**
       * Gets called when GraphQL response is received and parsed successfully.
       * @param response the GraphQL response
       */
      @Override
      public void onResponse(@NotNull Response<TransportDetailsFromStopQuery.Data> response) {
        TransportDetailsFromStopQuery.Data data = response.data();
        if(data==null) return;
        long unixTime = Instant.now().getEpochSecond();//get unix time in seconds
        if(data.trip()!=null){
          stopId = new ArrayList<>();
          stopCodes = new ArrayList<>();
          stopNames = new ArrayList<>();
          stopZones = new ArrayList<>();
          platformCodes = new ArrayList<>();
          routeTime = new ArrayList<>();
          routeDelay = new ArrayList<>();
          //fill data
          routeName = data.trip().tripHeadsign();
          //fill data into the arrays
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
                routeDelay.add("Earlier " + Integer.toString(-routeList.get(i).arrivalDelay() / 60) + " min");
              else routeDelay.add("On time");
            }
          }
        }
        callback.onTransportFromStop(data);
      }

      /**
       * Gets called when an unexpected exception occurs while creating the request or processing the response.
       * @param e ApolloException
       */
      @Override
      public void onFailure(@NotNull ApolloException e) { callback.onError(e);}
    });
  }

  /**
   * initialize TransportDetailsFromStopQuery(in future) and set up data to Transport class from TransportDetailsFromStopQuery (in future)
   * @param id id of the transport
   * @param callback callback
   */
  public static void getTransportDetailsFromStop(String id, Callback callback){
    makeTransportDetailsFromStopQuery(initializeQueryFromStop(id),callback);
  }

  /**
   * initialize TransportDetailsFromMapQuery and set up data to Transport class from TransportDetailsFromMapQuery
   * @param date date of the trip
   * @param dir direction of the transport
   * @param route if of the route
   * @param time start of the trip
   * @param callback callback
   */
  public static void getTransportDetailsFromMap(String date, int dir, String route, int time, Callback callback){
    makeTransportDetailsFromMapQuery(initializeQueryFromMap(date,dir,route,time),callback);
  }

  /**
   * Interface of Transport callback
   */
  public interface Callback {
    void onTransportFromMap(@NonNull TransportDetailsFromMapQuery.Data data);

    void onTransportFromStop(@NonNull TransportDetailsFromStopQuery.Data data);

    void onError(@NotNull ApolloException e);
  }
}
