package api;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
public interface RestaurantRepository extends CrudRepository<Restaurant, UUID> {

}
