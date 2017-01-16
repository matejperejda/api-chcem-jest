package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author <a href="mailto:mat.per.vt@gmail.com">Matej Perejda</a>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserLocationNotFoundException extends Exception {

   public UserLocationNotFoundException(final String message) {
      super(message);
   }

}