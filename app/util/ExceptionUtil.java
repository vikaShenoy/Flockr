package util;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.forbidden;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;
import static play.mvc.Results.status;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ConflictingRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;

/**
 * Class containing utilities for handling exceptions used in HTTP responses.
 */
public class ExceptionUtil {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String MESSAGE_KEY = "message";


  /**
   * Gets a result based on an error that has been thrown.
   *
   * @param error the error that has been thrown.
   * @return the result to reply in the http response.
   */
  public Result getResultFromError(Throwable error) {
    try {
      throw (Exception) error.getCause();
    } catch (BadRequestException exception) {
      ObjectNode message = Json.newObject();
      message.put(MESSAGE_KEY, exception.getMessage());
      return badRequest(message);
    } catch (ForbiddenRequestException exception) {
      ObjectNode message = Json.newObject();
      message.put(MESSAGE_KEY, exception.getMessage());
      return forbidden(message);
    } catch (NotFoundException exception) {
      ObjectNode message = Json.newObject();
      message.put(MESSAGE_KEY, exception.getMessage());
      return notFound(message);
    } catch (ConflictingRequestException exception) {
      ObjectNode message = Json.newObject();
      message.put(MESSAGE_KEY, exception.getMessage());
      return status(409, message);
    } catch (Exception exception) {
      ObjectNode message = Json.newObject();
      message.put(MESSAGE_KEY, "An unexpected error has occurred.");
      exception.printStackTrace();
      return internalServerError(message);
    }
  }

}
