package com.example.transportmodel;

public class Transport {
  private String routeId;
  private String routeName;
  private String routeDesi;
  private String nextStop;
  private String stop;

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteDesi(String routeDesi) {
    this.routeDesi = routeDesi;
  }

  public String getRouteDesi() {
    return routeDesi;
  }

  public void setNextStop(String nextStop) {
    this.nextStop = nextStop;
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
