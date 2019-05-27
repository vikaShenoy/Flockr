package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import models.*;
import models.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import play.libs.Json;
import play.mvc.*;
import repository.DestinationRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;
import repository.PhotoRepository;


/**
 * Controller to manage endpoints related to destinations.
 */
public class DestinationController extends Controller {
    private final DestinationRepository destinationRepository;
    private final PhotoRepository photoRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public DestinationController(DestinationRepository destinationRepository, HttpExecutionContext httpExecutionContext, PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
        this.destinationRepository = destinationRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * A function that gets a list of all the destinations and returns it with a 200 ok code to the HTTP client
     *
     * @param request Http.Request the http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDestinations(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationsbyUserId(user.getUserId())
                .thenApplyAsync(destinations -> ok(Json.toJson(destinations)), httpExecutionContext.current());
    }

    /**
     * A function that retrieves a destination details based on the destination ID given
     *
     * @param destinationId the destination Id of the destination to retrieve
     * @param request request Object
     * @return a completion stage and a Status code of 200 and destination details as a Json object if successful,
     * otherwise returns status code 404 if the destination can't be found in the db.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDestination(int destinationId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationById(destinationId)
                .thenApplyAsync((destination) -> {
                    if (!destination.isPresent()) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "No destination exists with the specified ID");
                        return notFound(message);
                    }

                    if (destination.get().getDestinationOwner() != user.getUserId()) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "You are not authorised to get this destination");
                        return forbidden(message);
                    }

                    JsonNode destAsJson = Json.toJson(destination);

                    return ok(destAsJson);

                }, httpExecutionContext.current());
    }

    /**
     * Get the destinations owned by a user.
     * If the user is viewing their own destinations, return all their destinations.
     * If the user is viewing someone elses destinations, return just the public destinations of the
     * specified user.
     *
     * @param userId  user to find destinations for.
     * @param request HTTP request.
     * @return 200 status code with the data for the user's destinations in body
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getUserDestinations(int userId, Http.Request request) {
        User loggedInUser = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinations()
                .thenApplyAsync(destinations -> {
                    if (loggedInUser.getUserId() == userId) {
                        List<Destination> userDestinations = destinations.stream()
                                .filter(destination -> destination.getDestinationOwner() == userId).
                                        collect(Collectors.toList());
                        return ok(Json.toJson(userDestinations));
                    } else {
                        List<Destination> userDestinations = destinations.stream()
                                .filter(destination -> destination.getDestinationOwner() == userId
                                        && destination.getIsPublic()).
                                        collect(Collectors.toList());
                        return ok(Json.toJson(userDestinations));
                    }
                }, httpExecutionContext.current());
    }


    /**
     * Function to add destinations to the database
     *
     * @param request the HTTP post request.
     * @return a completion stage with a 200 status code and the new json object or a status code 400.
     */

    @With(LoggedIn.class)
    public CompletionStage<Result> addDestination(Http.Request request) {
        JsonNode jsonRequest = request.body().asJson();

        int user = request.attrs().get(ActionState.USER).getUserId();

        try {
            // check that the request has a body
            if (jsonRequest.isNull()) {
                throw new BadRequestException("No details received, please send a valid request.");
            }

            String destinationName;
            int destinationTypeId;
            int districtId;
            Double latitude;
            Double longitude;
            int countryId;

            try {
                destinationName = jsonRequest.get("destinationName").asText();
                destinationTypeId = jsonRequest.get("destinationTypeId").asInt();
                districtId = jsonRequest.get("districtId").asInt();
                latitude = jsonRequest.get("latitude").asDouble();
                longitude = jsonRequest.get("longitude").asDouble();
                countryId = jsonRequest.get("countryId").asInt();
            } catch (NullPointerException exception) {
                throw new BadRequestException("One or more fields are missing.");
            }

            DestinationType destinationTypeAdd = DestinationType.find.byId(destinationTypeId);
            District districtAdd = District.find.byId(districtId);
            Country countryAdd = Country.find.byId(countryId);
            if (destinationTypeAdd == null || districtAdd == null || countryAdd == null) {
                throw new BadRequestException("One of the fields you have selected does not exist.");
            }

            Destination destination = new Destination(destinationName, destinationTypeAdd, districtAdd,
                    latitude, longitude, countryAdd, user, false);

            return destinationRepository.insert(destination)
                    .thenApplyAsync(insertedDestination -> created(Json.toJson(insertedDestination)), httpExecutionContext.current());
        } catch (BadRequestException e) {
            ObjectNode message = Json.newObject();
            message.put("message", e.getMessage());
            return supplyAsync(() -> badRequest(message));
        }

    }

    /**
     * Endpoint to update a destination's details
     *
     * @param request       Request body to get json body from
     * @param destinationId The destination ID to update
     * @return Returns status code 200 if successful, 404 if the destination isn't found, 500 for other errors.
     *
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateDestination(Http.Request request, int destinationId) {
        JsonNode response = Json.newObject();
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationById(destinationId)
                .thenComposeAsync(optionalDest -> {
                    if (!optionalDest.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    // Checks that the user is either the admin or the owner of the photo to change permission groups
                    if (!user.isAdmin() && user.getUserId() != optionalDest.get().getDestinationOwner()) {
                        throw new CompletionException(new ForbiddenRequestException("You are unauthorised to update this destination"));
                    }

                    JsonNode jsonBody = request.body().asJson();

                    String destinationName = jsonBody.get("destinationName").asText();
                    int destinationTypeId = jsonBody.get("destinationTypeId").asInt();
                    int countryId = jsonBody.get("countryId").asInt();
                    int districtId = jsonBody.get("districtId").asInt();
                    double latitude = jsonBody.get("latitude").asDouble();
                    double longitude = jsonBody.get("longitude").asDouble();
                    boolean isPublic = jsonBody.get("isPublic").asBoolean();

                    Destination destination = optionalDest.get();

                    DestinationType destType = new DestinationType(null);
                    destType.setDestinationTypeId(destinationTypeId);

                    Country country = new Country(null);
                    country.setCountryId(countryId);

                    District district = new District(null, null);
                    district.setDistrictId(districtId);

                    destination.setDestinationName(destinationName);
                    destination.setDestinationType(destType);
                    destination.setDestinationCountry(country);
                    destination.setDestinationDistrict(district);
                    destination.setDestinationLat(latitude);
                    destination.setDestinationLon(longitude);
                    destination.setIsPublic(isPublic);

                    List<Integer> duplicatedDestinationIds = new ArrayList<>();
                    // Checks if destination is a duplicate destination
                    boolean exists, duplicate = false;
                    List<Destination> destinations = Destination.find.query().findList();
                    for (Destination dest : destinations) {
                        if (dest.getDestinationId() != destinationId && destination.getIsPublic()) {
                            exists = destination.equals(dest);
                            // Checks if the destination found is private
                            if (exists && !dest.getIsPublic()) {
                                duplicatedDestinationIds.add(dest.getDestinationId());
                            } // Checks if the destination is a duplicate (both public)
                            else if (exists && (dest.getIsPublic() && optionalDest.get().getIsPublic())) {
                                duplicate = true;
                            }
                        }
                    }

                    if (duplicate) {
                        throw new CompletionException(new BadRequestException("There is already a Destination with the following information"));
                    }

                    for (int destId : duplicatedDestinationIds) {
                        Optional<Destination> optDest = Destination.find.query().
                                where().eq("destination_id", destId).findOneOrEmpty();
                        destinationRepository.deleteDestination(optDest.get().getDestinationId());

                        // TODO: Transfer the destinationId to the public destinationId
                    }

                    return destinationRepository.update(destination);
                }, httpExecutionContext.current())
                .thenApplyAsync(destination -> (Result) ok(), httpExecutionContext.current())
                .exceptionally(error -> {
                    try {
                        throw error.getCause();
                    } catch (NotFoundException notFoundE) {
                        ((ObjectNode) response).put("message", "There is no destination with the given ID found");
                        return notFound(response);
                    } catch (ForbiddenRequestException forbiddenRequest) {
                        ((ObjectNode) response).put("message", forbiddenRequest.getMessage());
                        return forbidden(response);
                    } catch (BadRequestException badRequestE) {
                        ((ObjectNode) response).put("message", badRequestE.getMessage());
                        return badRequest(response);
                    } catch (Throwable ee) {
                        ((ObjectNode) response).put("message", "Endpoint under development");
                        return internalServerError(response);
                    }
                });
    }

    /**
     * Endpoint to delete a destination given its id
     *
     * @param destinationId the id of the destination that we want to delete
     * @param request       the request sent by the routes file
     * @return Status code 200 if successful, 404 if not found, 500 otherwise.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteDestination(int destinationId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationById(destinationId)
                .thenComposeAsync(optionalDestination -> {
                    if (!optionalDestination.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    Destination destination = optionalDestination.get();
                    System.out.println(destination.getDestinationOwner());
                    System.out.println(user.toString());
                    if (destination.getDestinationOwner() != null && destination.getDestinationOwner() != user.getUserId()) {
                        throw new CompletionException(new ForbiddenRequestException("You are not permitted to delete this destination"));
                    }
                    ObjectNode success = Json.newObject();
                    success.put("message", "Successfully deleted the given destination id");
                    return this.destinationRepository.deleteDestination(destination.getDestinationId());
                }, httpExecutionContext.current())
                .thenApplyAsync(destId -> (Result) ok(), httpExecutionContext.current())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundE) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "The given destination id is not found");
                        return notFound(message);
                    } catch (ForbiddenRequestException forbiddenReqE) {
                        ObjectNode message = Json.newObject();
                        message.put("message", forbiddenReqE.getMessage());
                        return forbidden(message);
                    } catch (Throwable serverError) {
                        serverError.printStackTrace();
                        return internalServerError();
                    }
                });
    }

    /**
     * Endpoint to get all countries.
     *
     * @return A completion stage and status code 200 with countries in JSON body if successful, 500 for errors.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getCountries() {
        return destinationRepository.getCountries()
                .thenApplyAsync(countries -> {
                    JsonNode countriesJson = Json.toJson(countries);
                    return ok(countriesJson);
                }, httpExecutionContext.current())
                .exceptionally(e -> internalServerError());
    }

    /**
     * Endpoint to get destination types.
     *
     * @return A completion stage and status code 200 with destination types in body if successful, 500 for errors.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDestinationTypes() {
        return destinationRepository.getDestinationTypes()
                .thenApplyAsync(destinationTypes -> {
                    JsonNode destinationTypesJson = Json.toJson(destinationTypes);
                    return ok(destinationTypesJson);
                }, httpExecutionContext.current())
                .exceptionally(e -> internalServerError());
    }

    /**
     * Endpoint to get all districts for a country.
     *
     * @param countryId country to get districts for.
     * @return A completion stage and status code of 200 with districts in the json body if successful.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDistricts(int countryId) {
        return destinationRepository.getDistricts(countryId)
                .thenApplyAsync(districts -> {
                    JsonNode districtsJson = Json.toJson(districts);
                    return ok(districtsJson);
                }, httpExecutionContext.current());
    }

    @With(LoggedIn.class)
    public CompletionStage<Result> addPhoto(int destinationId, Http.Request request) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);
        System.out.println("destination id is: " + destinationId);
        JsonNode requestJson = request.body().asJson();
        int photoId;
        try {
            photoId = requestJson.get("photoId").asInt(0);
        } catch (NullPointerException e) {
            return supplyAsync(() -> badRequest("Please make sure to add the photoId"));
        }

        return destinationRepository.getDestinationById(destinationId)
                .thenComposeAsync(optionalDestination -> {

                    if (!optionalDestination.isPresent()) {
                        throw new CompletionException(new NotFoundException("Could not found destination"));
                    }

                    if (
                            !optionalDestination.get().getIsPublic() &&
                            !optionalDestination.get().getDestinationOwner().equals(userFromMiddleware.getUserId()) &&
                            !userFromMiddleware.isAdmin()
                    ) {
                        throw new CompletionException(new ForbiddenRequestException("Forbidden"));
                    }

                    return photoRepository.getPhotoByIdAndUser(photoId, userFromMiddleware.getUserId())
                            .thenComposeAsync(optionalPhoto -> {
                                if (!optionalPhoto.isPresent()) {
                                    throw new CompletionException(new NotFoundException("Could not find photo"));
                                }

                                //TODO test this

                                if (    optionalDestination.get().getIsPublic() &&                          // The destination is public
                                        optionalDestination.get().getDestinationOwner() != null &&          // The owner is not already null
                                        !optionalDestination.get().getDestinationOwner().equals(userFromMiddleware.getUserId())) {  // The user doesn't own the destination
                                    optionalDestination.get().setDestinationOwner(null);
                                    destinationRepository.update(optionalDestination.get());
                                }

                                Destination destination = optionalDestination.get();
                                PersonalPhoto personalPhoto = optionalPhoto.get();
                                DestinationPhoto destinationPhoto = new DestinationPhoto(destination, personalPhoto);

                                return destinationRepository.savePhoto(destinationPhoto);
                            })
                            .thenApplyAsync(destinationPhoto -> {
                                JsonNode destinationPhotoJson = Json.toJson(destinationPhoto);
                                return created(destinationPhotoJson);
                            });
                })
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException badReqException) {
                        return notFound(badReqException.getMessage());
                    } catch (Throwable throwableException) {
                        throwableException.printStackTrace();
                        return internalServerError("dfd");
                    }
                });
    }

}

