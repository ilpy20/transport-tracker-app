package com.example.transportmodel;

import com.google.android.gms.maps.model.LatLng;
import com.hsl.TransportSubscription;

public class Transport {
  private String id;
  private String routeId;
  private String routeName;
  private String routeDisplayName;
  private String nextStop;
  private String stop;
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
}
