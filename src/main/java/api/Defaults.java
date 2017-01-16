package api;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
public class Defaults {

   public static class User {
      // default location for Vranov nad Toplou, Slovakia, Europe
      public static final double DEFAULT_LATITUDE = 48.875448;
      public static final double DEFAULT_LONGITUDE = 21.666845;
   }

   public static class Location {
      public static final String LATITUDE_KEY = "latitude";
      public static final String LONGITUDE_KEY = "longitude";

      public static final int EARTH_RADIUS = 6371;
   }
}
