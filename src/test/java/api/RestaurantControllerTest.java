package api;/*

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */

/*@RunWith(SpringRunner.class) // @RunWith(SpringJUnit4ClassRunner.class)
// @AutoConfigureMockMvc
*//*
*/
/*@SpringApplicationConfiguration(classes = RestaurantApplication.class)
WebAppConfiguration*//*
*/
/*
@ContextConfiguration // @SpringBootTest*//*

@RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(classes=RestaurantApplication.class, loader=SpringApplicationContextLoader.class)
@SpringApplicationConfiguration(RestaurantApplication.class)
@IntegrationTest
@WebIntegrationTest
public class RestaurantControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Test
   public void test() throws Exception {
      this.mockMvc.perform()
   }

}*/

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(RestaurantApplication.class)
@ContextConfiguration(classes = RestaurantApplication.class)
@WebAppConfiguration
public class RestaurantControllerTest {

   private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
         MediaType.APPLICATION_JSON.getSubtype(),
         Charset.forName("utf8"));

   @Autowired
   private WebApplicationContext webApplicationContext;

   private MockMvc mvc;

   private final String URL_PATH_PREFIX = "/restaurants/";

   @Before
   public void setup() throws Exception {
      initMvc();
   }

   @Test
   public void testGetAllRestaurants() throws Exception {
      initMvc();

      final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(URL_PATH_PREFIX).accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String content = mvcResult.getResponse().getContentAsString();
      Assert.assertEquals(stringToList(content).size(), 3);
   }

   @Test
   public void testAddUpdateAndDropRestaurant() throws Exception {
      initMvc();

      Map<String, Double> gps1 = new HashMap<String, Double>();
      gps1.put(Defaults.Location.LATITUDE_KEY, 48.719630);
      gps1.put(Defaults.Location.LONGITUDE_KEY, 21.261410);

      // #1 add restaurant
      final Restaurant restaurant = new Restaurant("newRestaurant", gps1, Arrays.asList(new String[] { "#new", "#restaurant" }));
      final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(URL_PATH_PREFIX)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(restaurantToString(restaurant))
                                                                    .accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String uuid = mvcResult.getResponse().getContentAsString();
      Assert.assertTrue(!uuid.isEmpty() && getAllRestaurants().size() == 4);

      // #2 update restaurant
      final UUID trimmedUUID = UUID.fromString(uuid.replace("\"", ""));
      final Restaurant restaurantToUpdate = new Restaurant(trimmedUUID, "updatedName", gps1, Arrays.asList(new String[] { "#updated" }));
      final MvcResult mvcResult2 = mvc.perform(MockMvcRequestBuilders.put(URL_PATH_PREFIX + trimmedUUID.toString())
                                                                     .contentType(MediaType.APPLICATION_JSON)
                                                                     .content(restaurantToString(restaurantToUpdate))
                                                                     .accept(contentType))
                                      .andExpect(status().isOk())
                                      .andReturn();
      final String jsonString = mvcResult2.getResponse().getContentAsString();
      Assert.assertEquals(jsonString.length(), restaurantToString(restaurantToUpdate).length());
      Assert.assertEquals(getAllRestaurants().size(), 4);

      // #3 drop restaurant
      final MvcResult mvcResult3 = mvc.perform(MockMvcRequestBuilders.delete(URL_PATH_PREFIX + trimmedUUID.toString())
                                                                     .accept(contentType))
                                      .andExpect(status().isNoContent())
                                      .andReturn();
      Assert.assertEquals(getAllRestaurants().size(), 3);
   }

   @Test
   public void testGetRestaurant() throws Exception {
      initMvc();

      Map<String, Double> gps1 = new HashMap<String, Double>();
      gps1.put(Defaults.Location.LATITUDE_KEY, 48.719630);
      gps1.put(Defaults.Location.LONGITUDE_KEY, 21.261410);

      // add new restaurant
      final Restaurant restaurant = new Restaurant("newRestaurant", gps1, Arrays.asList(new String[] { "#new", "#restaurant" }));
      final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(URL_PATH_PREFIX)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(restaurantToString(restaurant))
                                                                    .accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String uuid = mvcResult.getResponse().getContentAsString();
      Assert.assertTrue(!uuid.isEmpty() && getAllRestaurants().size() == 4);

      // get the added restaurant
      final UUID trimmedUUID = UUID.fromString(uuid.replace("\"", ""));
      final MvcResult mvcResult2 = mvc.perform(MockMvcRequestBuilders.get(URL_PATH_PREFIX + trimmedUUID.toString())
                                                                     .accept(contentType))
                                      .andExpect(status().isOk())
                                      .andReturn();
      String jsonString = mvcResult2.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(jsonString);
      String addedUuid = jsonObject.getString("id");
      Assert.assertEquals(uuid.replace("\"", ""), addedUuid);

      dropRestaurant(uuid);
   }

   @Test
   public void testGetRestaurantByLocationAndTag() throws Exception {
      initMvc();

      // #1 search
      final double radius = 70; // 70km
      final List<String> tagsToFilter = Arrays.asList(new String[] { "#vranov" });
      final Query query = new Query(Defaults.User.DEFAULT_LATITUDE, Defaults.User.DEFAULT_LONGITUDE, radius, tagsToFilter);
      final String jsonQuery = (new JSONObject(query)).toString();

      final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(URL_PATH_PREFIX + "search")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(jsonQuery)
                                                                    .accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String content = mvcResult.getResponse().getContentAsString();
      Assert.assertEquals(stringToList(content).size(), 1);

      // #2 search
      final double radius2 = 70; // 70km
      final List<String> tagsToFilter2 = Arrays.asList(new String[] {});
      final Query query2 = new Query(Defaults.User.DEFAULT_LATITUDE, Defaults.User.DEFAULT_LONGITUDE, radius2, tagsToFilter2);
      final String jsonQuery2 = (new JSONObject(query2)).toString();

      final MvcResult mvcResult2 = mvc.perform(MockMvcRequestBuilders.post(URL_PATH_PREFIX + "search")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(jsonQuery2)
                                                                    .accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String content2 = mvcResult2.getResponse().getContentAsString();
      Assert.assertEquals(stringToList(content2).size(), 3);
   }

   private List<Object> getAllRestaurants() throws Exception {
      final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(URL_PATH_PREFIX).accept(contentType))
                                     .andExpect(status().isOk())
                                     .andReturn();
      String content = mvcResult.getResponse().getContentAsString();
      return stringToList(content);
   }

   private void dropRestaurant(final String uuid) throws Exception {
      final UUID trimmedUUID = UUID.fromString(uuid.replace("\"", ""));
      mvc.perform(MockMvcRequestBuilders.delete(URL_PATH_PREFIX + trimmedUUID.toString())
                                        .accept(contentType))
         .andExpect(status().isNoContent());
   }

   private Object stringToObject(final String jsonString) {
      return JSONObject.stringToValue(jsonString);
   }

   private List<Object> stringToList(final String jsonString) {
      return (new JSONArray(jsonString)).toList();
   }

   private String restaurantToString(final Restaurant restaurant) {
      return (new JSONObject(restaurant)).toString();
   }

   private void initMvc() {
      this.mvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
   }

   /* mvc.perform(get("/restaurants/")).andDo(print()).andExpect(status().isOk())
         .andExpect(jsonPath("$.content").value(restaurants));*/

   // MvcResult mvcResult = mvc.perform(get("/restaurants/")).andDo(print()).andExpect(status().isOk()).andReturn();

   /*   this.mockMvc.perform(get("/foo").accept("application/json"))
         .andExpect(status().isOk())
         .andExpect(content().mimeType("application/json"))
         .andExpect(jsonPath("$.name").value("Lee"));*/
}
