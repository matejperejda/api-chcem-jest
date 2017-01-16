package api;

import com.sun.istack.internal.NotNull;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
@Entity
public class Restaurant implements Serializable {

   // POST REQUEST posielat cez Postman-a nasledovne:
   // Vybrat "Body" -> "raw" -> "JSON(application/json)"
   //    {
   //       "name": "Med Malina",
   //          "gpsLocation": {
   //       "latitude": 48.723412,
   //             "longitude": 21.256927
   //    },
   //       "hashTags":[
   //       "korcula",
   //             "kamiony"
   //    ]
   //    }

   @Id
   @GeneratedValue(generator = "uuid")
   @GenericGenerator(name = "uuid", strategy = "uuid2")
   private UUID id;

   @Column(name = "name", nullable = false)
   @NotNull
   private String name;

   @ElementCollection
   @Lob
   @Column(name = "gpsLocation", nullable = false, length = 10000)
   @NotNull
   private Map<String, Double> gpsLocation = new HashMap<>();

   @ElementCollection
   @Lob
   @Column(name = "hashTags", nullable = false, length = 10000)
   @NotNull
   private List<String> hashTags = new ArrayList<>();

   protected Restaurant() {
      // no-args constructor required by JPA spec
      // this one is protected since it shouldn't be used directly
   }

   public Restaurant(final String name, final Map<String, Double> gpsLocation, final List<String> hashTags) {
      this.name = name;
      this.gpsLocation = gpsLocation;
      this.hashTags = hashTags;
   }

   public Restaurant(final UUID id, final String name, final Map<String, Double> gpsLocation, final List<String> hashTags) {
      this.id = id;
      this.name = name;
      this.gpsLocation = gpsLocation;
      this.hashTags = hashTags;
   }

   public UUID getId() {
      return id;
   }

   public void setId(final UUID id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public Map<String, Double> getGpsLocation() {
      return gpsLocation;
   }

   public void setGpsLocation(final Map<String, Double> gpsLocation) {
      this.gpsLocation = gpsLocation;
   }

   public List<String> getHashTags() {
      return hashTags;
   }

   public void setHashTags(final List<String> hashTags) {
      this.hashTags = hashTags;
   }

   public String toString() {
      return this.id.toString() + ": " +
            this.name + ", [" +
            this.getGpsLocation().get("latitude") + ", " + this.getGpsLocation().get("longitude") + "]" +
            ", tags: " + this.hashTags.toString();
   }
}
