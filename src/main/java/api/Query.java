package api;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
public class Query implements Serializable {

   private double userLatitude;

   private double userLongitude;

   private double radius;

   private List<String> tags;

   protected Query() {
      // no-args constructor required by JPA spec
      // this one is protected since it shouldn't be used directly
   }

   public Query(final double userLatitude, final double userLongitude, final double radius, final List<String> tags) {
      this.userLatitude = userLatitude;
      this.userLongitude = userLongitude;
      this.radius = radius;
      this.tags = tags;
   }

   public double getRadius() {
      return radius;
   }

   public void setRadius(final double radius) {
      this.radius = radius;
   }

   public double getUserLatitude() {
      return userLatitude;
   }

   public void setUserLatitude(final double userLatitude) {
      this.userLatitude = userLatitude;
   }

   public double getUserLongitude() {
      return userLongitude;
   }

   public void setUserLongitude(final double userLongitude) {
      this.userLongitude = userLongitude;
   }

   public List<String> getTags() {
      return tags;
   }

   public void setTags(final List<String> tags) {
      this.tags = tags;
   }
}
