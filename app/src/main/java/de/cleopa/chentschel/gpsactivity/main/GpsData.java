package de.cleopa.chentschel.gpsactivity.main;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


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
        + "\nZeitstempel=" + getZeit(location.getTime())
        + "\n";
  }

  public String getZeit(long time){
//    TimeZone tz = TimeZone.getTimeZone("");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY); // Quoted "Z" to indicate UTC, no timezone offset
//    df.setTimeZone(tz);
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'", Locale.GERMAN);
    return df.format(time);
  }
}
