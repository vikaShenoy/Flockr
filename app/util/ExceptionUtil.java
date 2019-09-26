package util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;

import static play.mvc.Results.*;

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
    ObjectNode message = Json.newObject();

    try {
      throw (Exception) error.getCause();
    } catch (BadRequestException exception) {
      message.put(MESSAGE_KEY, exception.getMessage());
      return badRequest(message);
    } catch (ForbiddenRequestException exception) {
      message.put(MESSAGE_KEY, exception.getMessage());
      return forbidden(message);
    } catch (NotFoundException exception) {
      message.put(MESSAGE_KEY, exception.getMessage());
      return notFound(message);
    } catch (ConflictingRequestException exception) {
      message.put(MESSAGE_KEY, exception.getMessage());
      return status(409, message);
    } catch (UnauthorizedException exception) {
      message.put(MESSAGE_KEY, exception.getMessage());
      return unauthorized(message);
    } catch (Exception exception) {
      message.put(MESSAGE_KEY, "An unexpected error has occurred.");
      log.error("500 - Internal Server Error", exception);
      return internalServerError(message);
    }
  }

}
