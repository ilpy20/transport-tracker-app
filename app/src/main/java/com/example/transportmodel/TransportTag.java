package com.example.transportmodel;

/**
 *
 * @author Sergey Ushakov
 * @version 1.0
 * @since 2020-03-12
 */
public class TransportTag {
  public String name;
  public String direction;

  /**
   * Initialize TransportTag
   * @param name name of the route
   * @param direction direction of the route
   */
  public TransportTag(String name, String direction) {
    this.name = name;
    this.direction = direction;
  }

}
