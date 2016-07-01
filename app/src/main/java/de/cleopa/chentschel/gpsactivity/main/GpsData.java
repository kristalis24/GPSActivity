package de.cleopa.chentschel.gpsactivity.main;

import android.location.Location;


public class GpsData {

  public Location location;
  
  /**
   * Konstruktor zur Erzeugung eines GpsData-Objekts.
   * 
   * @param location Längengrad, Breitengrad, Höhe üNN, Zeitpunkt
   */

  public GpsData(Location location) {
    this.location = location;    
  }
  
  public double getLaengengrad() {
    return location.getLongitude();
  }

  public double getBreitengrad() {
    return location.getLatitude();
  }

  public double getHoehe() {
    return location.getAltitude();
  }

  public long getZeitstempel() {
    return location.getTime();
  }

  @Override
  public String toString() {
    return "\nGpsData:\nBreitengrad=" + location.getLatitude()
        + "\nLaengengrad=" + location.getLongitude()
        + "\nHoehe=" + location.getAltitude()
        + "\nZeitstempel=" + location.getTime()
        + "\n";
  }
}
