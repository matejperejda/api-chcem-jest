package api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;

import exception.BadRequestException;
import exception.RadiusNegativeException;
import exception.RestaurantNotFoundException;
import exception.UserLocationNotFoundException;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

   @Autowired
   private RestaurantRepository restaurantRepository;

   public RestaurantController() {
   }

   @PostConstruct
   private void fillDummyEntries() {
      Map<String, Double> gps1 = new HashMap<String, Double>();
      gps1.put(Defaults.Location.LATITUDE_KEY, 48.719630);
      gps1.put(Defaults.Location.LONGITUDE_KEY, 21.261410);

      Map<String, Double> gps2 = new HashMap<String, Double>();
      gps2.put(Defaults.Location.LATITUDE_KEY, 48.890216);
      gps2.put(Defaults.Location.LONGITUDE_KEY, 21.682171);

      Map<String, Double> gps3 = new HashMap<String, Double>();
      gps3.put(Defaults.Location.LATITUDE_KEY, 48.725127);
      gps3.put(Defaults.Location.LONGITUDE_KEY, 21.255022);

      // two pubs in Kosice, one in Vranov
      restaurantRepository.save(new Restaurant("Dobré časy", gps1, Arrays.asList(new String[] { "#kosice", "#pivo", "#dobreJedlo" }))); // Kosice
      restaurantRepository.save(new Restaurant("LaCosa Nostra", gps2, Arrays.asList(new String[] { "#whiskey", "#vranov", "#bar" }))); // Vranov
      restaurantRepository.save(new Restaurant("Red Nose Pub", gps3, Arrays.asList(new String[] { "#skvelePivo", "#superLudia", "#kosice" }))); // Kosice
   }

   /**
    * Returns a list of the stored restaurants in the database.
    *
    * @return the list of the stored restaurants
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
   @ResponseStatus(HttpStatus.OK)
   public Iterable<Restaurant> getAllRestaurants() {
      return restaurantRepository.findAll();
   }

   /**
    * Adds new restaurant to database.
    *
    * @param restaurant
    *       Restaurant object in the reuqest body
    * @return UUID of the inserted restaurant
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
   @ResponseStatus(HttpStatus.OK)
   public UUID addRestaurant(final @RequestBody Restaurant restaurant) throws BadRequestException {
      if (restaurant.getName() == null || restaurant.getGpsLocation().isEmpty()) {
         throw new BadRequestException("Wrong request body.");
      }

      restaurantRepository.save(restaurant);
      System.out.println("[LOG>> ]" + restaurant.toString());
      return restaurant.getId();
   }

   /**
    * Updates the given restaurant by its id. The request body contains updated restaurant.
    *
    * @param id
    *       the id of the given restaurant to update
    * @param restaurant
    *       the updated restaurant
    * @return updated restaurant
    * @throws RestaurantNotFoundException
    *       When the given restaurant does not exist.
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
   @ResponseStatus(HttpStatus.OK)
   public Restaurant updateRestaurant(final @PathVariable("id") UUID id, final @RequestBody Restaurant restaurant) throws RestaurantNotFoundException, BadRequestException {
      if (restaurant.getName() == null || restaurant.getGpsLocation().isEmpty()) {
         throw new BadRequestException("Wrong request body.");
      }

      if (!restaurantRepository.exists(id)) {
         throw new RestaurantNotFoundException(id);
      }

      // potrebne definovat v JSONe aj kluc "id": s rovnakym uuid ake posielame v PathVariable"!!!
      // musia byt vsetky vyplnene kluce v JSONe ako dostavame v GET requeste
            /* {
                    "id": "f0a509cd-4f4a-484d-a77c-68214c3b577c",
                    "nazov": "nazov receptu",
                    "autor": "Tomas",
                    "ingrediencie": {},
                    "postup": "ošúp jablká"
            }*/
      restaurantRepository.save(restaurant);
      return restaurant;
   }

   /**
    * Drops the given restaurant by its id.
    *
    * @param id
    *       the id of the given restaurant to drop
    * @throws RestaurantNotFoundException
    *       When the given restaurant does not exist.
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void dropRestaurant(final @PathVariable("id") UUID id) throws RestaurantNotFoundException {
      if (id == null) {
         throw new IllegalArgumentException();
      }

      if (!restaurantRepository.exists(id)) {
         throw new RestaurantNotFoundException(id);
      }
      restaurantRepository.delete(id);
   }

   /**
    * Finds and return the given restaurant by its id.
    *
    * @param id
    *       the id of the given restaurant
    * @return restaurant with the given id
    * @throws RestaurantNotFoundException
    *       When the given restaurant does not exist.
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
   @ResponseStatus(HttpStatus.OK)
   public Restaurant getRestaurant(final @PathVariable UUID id) throws RestaurantNotFoundException {
      if (id == null) {
         throw new IllegalArgumentException();
      }

      if (!restaurantRepository.exists(id)) {
         throw new RestaurantNotFoundException(id);
      }
      return restaurantRepository.findOne(id);
   }

   /**
    * Returns a list of restaurants based on the same hashtags within the given radius.
    *
    * @param query
    *       query including user gps location, radius and tag list.
    * @return the list of restaurants that fit the search
    */
   @CrossOrigin(origins = "http://localhost:3000")
   @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
   @ResponseStatus(HttpStatus.OK)
   public List<Restaurant> getRestaurantByLocationAndTag(final @RequestBody Query query) throws RadiusNegativeException, UserLocationNotFoundException, BadRequestException {
      if (query == null) {
         throw new IllegalArgumentException();
      }

      if (query.getRadius() < 0) {
         throw new RadiusNegativeException("Radius can not be nagative value.");
      }

      final double userLat = query.getUserLatitude();
      final double userLong = query.getUserLongitude();

      if (userLat == 0 || userLong == 0) {
         throw new UserLocationNotFoundException("Missing user's gps coordinates.");
      }

      List<Restaurant> restaurantsWithTags = getRestaurantsWithTags(query.getTags());

      if (query.getRadius() == 0 && query.getTags().isEmpty()) {
         return restaurantsWithTags;
      }

      List<Restaurant> restaurantsWithTagsAndLocation = new ArrayList<>();
      for (Restaurant restaurant : restaurantsWithTags) {
         final double restLat = restaurant.getGpsLocation().get(Defaults.Location.LATITUDE_KEY);
         final double restLong = restaurant.getGpsLocation().get(Defaults.Location.LONGITUDE_KEY);

         if (isInRadius(userLat, userLong, restLat, restLong, query.getRadius())) {
            restaurantsWithTagsAndLocation.add(restaurant);
         }
      }
      return restaurantsWithTagsAndLocation;
   }

   private List<Restaurant> getRestaurantsWithTags(final List<java.lang.String> tags) {
      final Iterable<Restaurant> restaurants = restaurantRepository.findAll();
      List<Restaurant> restaurantsWithTags = new ArrayList<>();

      for (Restaurant restaurant : restaurants) {
         final List<java.lang.String> currentHashTags = restaurant.getHashTags();

         if (currentHashTags.containsAll(tags)) {
            restaurantsWithTags.add(restaurant);
         }
      }
      return restaurantsWithTags;
   }

   private boolean isInRadius(final double userLat, final double userLong, final double restLat, final double restLong, final double radius) {
      if (radius == 0) {
         return true;
      }
      final double usrLatRad = getRadians(userLat);
      final double usrLongRad = getRadians(userLong);
      final double restLatRad = getRadians(restLat);
      final double restLongRad = getRadians(restLong);

      boolean isInRadius = (Math.acos(Math.sin(usrLatRad)
            * Math.sin(restLatRad) + Math.cos(usrLatRad)
            * Math.cos(restLatRad)
            * Math.cos(restLongRad - (usrLongRad)))
            * Defaults.Location.EARTH_RADIUS <= radius);

      if (isInRadius) {
         return true;
      }
      return false;
   }

   private double getRadians(double value) {
      return (value * Math.PI) / 180;
   }

}
