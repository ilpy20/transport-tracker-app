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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import okhttp3.Callback;

/**
 * Stop class where stored information about stop
 *  @author Ilya Pyshkin, Sergey Ushakov
 *  @version 1.0
 *  @since 2020-03-12
 */
public class Stop {
  private String id;
  private String gtfsId;
  private LatLng location;
  private String name;
  private String code;
  private String zoneId;
  private String vehicleMode;
  private String platformCode;
  private static ArrayList<String> tripId;
  private static ArrayList<String> routeNums;
  private static ArrayList<String> routeNames;
  private static ArrayList<String> routeTime;
  private static ArrayList<String> routeDelay;
  private static ArrayList<String> routeDirections;
  private static Long timeArrive;
  private static BigDecimal serviceDay;

  /**
   * initialize StopDetailsQuery
   * @param id id of the stop
   * @return StopDetailsQuery
   */
  private static StopDetailsQuery initializeQuery(String id) {
    return StopDetailsQuery.builder().id(id).build();
  }

  /**
   * Set data to Stop class from StopsQuery
   * @param stop StopsQuery.StopsByBbox
   */
  public Stop(StopsQuery.StopsByBbox stop) {
    id = stop.id();
    gtfsId = stop.gtfsId();
    name = stop.name();
    code = stop.code();
    zoneId = stop.zoneId();
    vehicleMode = String.valueOf(stop.vehicleMode());
    platformCode = stop.platformCode();
    location = new LatLng(stop.lat(), stop.lon());
  }

  /**
   * Get id of the stop
   * @return String id
   */
  public String getId() {
    return id;
  }

  /**
   * Get zone id of the stop
   * @return String zoneId
   */
  public String getZoneId() {
    return zoneId;
  }

  /**
   * Get vehicle mode of the stop
   * @return String zoneId
   */
  public String getVehicleMode() {
    return vehicleMode;
  }

  /**
   * Get platform code of the stop
   * @return String platformCode
   */
  public String getPlatformCode() {
    return platformCode;
  }

  /**
   * Get code of the stop
   * @return String code
   */
  public String getCode() {
    return code;
  }

  /**
   * Get gtfs id of the stop
   * @return String gtfsId
   */
  public String getGtfsId() {
    return gtfsId;
  }

  /**
   * Get name of the stop
   * @return String name
   */
  public String getName() {
    return name;
  }

  /**
   * Get ArrayList<String> with trip id for setting stop bottom sheet
   * @return ArrayList<String> tripId
   */
  public ArrayList<String> getTripId() {
    return tripId;
  }

  /**
   * Get ArrayList<String> with route nums for setting stop bottom sheet
   * @return ArrayList<String> routeNums
   */
  public ArrayList<String> getRouteNums() {
    return routeNums;
  }

  /**
   * Get ArrayList<String> with route names for setting stop bottom sheet
   * @return ArrayList<String> routeNames
   */
  public ArrayList<String> getRouteNames() {
    return routeNames;
  }

  /**
   * Get ArrayList<String> with route time for setting stop bottom sheet
   * @return ArrayList<String> routeTime
   */
  public ArrayList<String> getRouteTime() {
    return routeTime;
  }

  /**
   * Get ArrayList<String> with route delay for setting stop bottom sheet
   * @return ArrayList<String> routeDelay
   */
  public ArrayList<String> getRouteDelay() {
    return routeDelay;
  }

  /**
   * Get ArrayList<String> with route directions for setting stop bottom sheet
   * @return ArrayList<String> routeDirections
   */
  public ArrayList<String> getRouteDirections() {
    return routeDirections;
  }

  /**
   * Get location of the stop
   * @return LanLng location
   */
  public LatLng getLocation() {
    return this.location;
  }

  /**
   * Set up data to Stop class from StopDetailsQuery
   * @param stopDetailsQuery query of stop details
   * @param callback Apollo callback
   */
  private static void makeStopDetailsQuery(StopDetailsQuery stopDetailsQuery, final Callback callback) {
    Networking.apollo().query(stopDetailsQuery).enqueue(new ApolloCall.Callback<StopDetailsQuery.Data>() {
      /**
       * Gets called when GraphQL response is received and parsed successfully.
       * @param response the GraphQL response
       */
      @Override
      public void onResponse(@NotNull Response<StopDetailsQuery.Data> response) {
        StopDetailsQuery.Data data = response.data();
        if (data == null) return;
        long unixTime = Instant.now().getEpochSecond();
        tripId = new ArrayList<>();
        routeNums = new ArrayList<>();
        routeNames = new ArrayList<>();
        routeTime = new ArrayList<>();
        routeDelay = new ArrayList<>();
        routeDirections = new ArrayList<>();

        List<StopDetailsQuery.StoptimesWithoutPattern> nearbyRoutes = data.stop().stoptimesWithoutPatterns();
        for (int i = 0; i < nearbyRoutes.size(); i++) {
          tripId.add(nearbyRoutes.get(i).trip().gtfsId());
          routeNums.add(nearbyRoutes.get(i).trip().routeShortName());
          routeNames.add(nearbyRoutes.get(i).headsign());
          routeDirections.add(nearbyRoutes.get(i).trip().directionId());
          nearbyRoutes.get(i).scheduledArrival();
          serviceDay = (BigDecimal) nearbyRoutes.get(i).serviceDay();
          if (nearbyRoutes.get(i).realtimeArrival() != null)
            timeArrive = Long.valueOf(nearbyRoutes.get(i).realtimeArrival());
          else timeArrive = Long.valueOf(nearbyRoutes.get(i).scheduledArrival());
          routeTime.add(Long.toString((timeArrive + serviceDay.longValue() - unixTime) / 60) + " min");
          if (nearbyRoutes.get(i).arrivalDelay() > 0)
            routeDelay.add("Delayed " + Integer.toString(nearbyRoutes.get(i).arrivalDelay() / 60) + " min");
          else if (nearbyRoutes.get(i).arrivalDelay() < 0)
            routeDelay.add("Earlier " + Integer.toString(-nearbyRoutes.get(i).arrivalDelay() / 60) + " min");

          else routeDelay.add("On time");
          nearbyRoutes.get(i).scheduledDeparture();
          nearbyRoutes.get(i).realtimeDeparture();

        }
        callback.onStop(data);
      }

      /**
       * Gets called when an unexpected exception occurs while creating the request or processing the response.
       * @param e ApolloException
       */
      @Override
      public void onFailure(@NotNull ApolloException e) {
        callback.onError(e);
      }
    });
  }

  /**
   * initialize StopDetailsQuery and set up data to Stop class from StopDetailsQuery
   * @param id gtfs id of the stop
   * @param callback callback
   */
  public static void makeStop(String id, Callback callback) {
    makeStopDetailsQuery(initializeQuery(id), callback);
  }


  /**
   * Interface of Stop callback
   */
  public interface Callback {
    void onStop(@NonNull StopDetailsQuery.Data data);

    void onError(@NotNull ApolloException e);
  }
}
