package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import models.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import play.libs.Json;
import play.mvc.*;
import repository.DestinationRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;
import repository.PhotoRepository;


/**
 * Controller to manage endpoints related to destinations.
 */
public class DestinationController  extends Controller{
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
     * @param request Http.Request the http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getDestinations(Http.Request request) {
        return destinationRepository.getDestinations()
                .thenApplyAsync(destinations -> ok(Json.toJson(destinations)), httpExecutionContext.current());
    }

    /**
     * A function that retrieves a destination details based on the destination ID given
     * @param destinationId the destination Id of the destination to retrieve
     * @param request request Object
     * @return a completion stage and a Status code of 200 and destination details as a Json object if successful,
     * otherwise returns status code 404 if the destination can't be found in the db.
     */

    public CompletionStage<Result> getDestination(int destinationId, Http.Request request) {

        return destinationRepository.getDestinationById(destinationId)
                .thenApplyAsync((destination) -> {
                    if (!destination.isPresent()) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "No destination exists with the specified ID");
                        return notFound(message);
                    }

                    JsonNode destAsJson = Json.toJson(destination);

                    return ok(destAsJson);

                }, httpExecutionContext.current());
    }

    /**
     * Function to add destinations to the database
     * @param request the HTTP post request.
     * @return a completion stage with a 200 status code and the new json object or a status code 400.
     */

    public CompletionStage<Result> addDestination(Http.Request request) {
        JsonNode jsonRequest = request.body().asJson();

        // check that the request has a body
        if (jsonRequest == null) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid request body according to the API spec");
                return badRequest(message);
            });
        }

        try {
            String destinationName = jsonRequest.get("destinationName").asText();
            int destinationType = jsonRequest.get("destinationTypeId").asInt();
            int district = jsonRequest.get("districtId").asInt();
            Double latitude = jsonRequest.get("latitude").asDouble();
            Double longitude = jsonRequest.get("longitude").asDouble();
            int country = jsonRequest.get("countryId").asInt();

            DestinationType destinationTypeAdd = new DestinationType(null);
            destinationTypeAdd.setDestinationTypeId(destinationType);
            District districtAdd = new District(null, null);
            districtAdd.setDistrictId(district);
            Country countryAdd = new Country(null);
            countryAdd.setCountryId(country);
            Destination destination = new Destination(destinationName,destinationTypeAdd,districtAdd,
                    latitude,longitude,countryAdd);

            return destinationRepository.insert(destination)
                    .thenApplyAsync(insertedDestination -> created(Json.toJson(insertedDestination)), httpExecutionContext.current());
        } catch (Exception e) {
            ObjectNode message = Json.newObject();
            message.put("message", "Please provide a valid Destination with complete data");
            return supplyAsync(() -> badRequest(message));
        }

    }

    /**
     * Endpoint to update a destination's details
     * @param request Request body to get json body from
     * @param destinationId The destination ID to update
     * @return Returns status code 200 if successful, 404 if the destination isn't found, 500 for other errors.
     *
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateDestination(Http.Request request, int destinationId) {
       return destinationRepository.getDestinationById(destinationId)
       .thenComposeAsync(optionalDest -> {
        if (!optionalDest.isPresent()) {
            throw new CompletionException(new NotFoundException());
        }
        JsonNode jsonBody = request.body().asJson();

        String destinationName =  jsonBody.get("destinationName").asText();
        int destinationTypeId = jsonBody.get("destinationTypeId").asInt();
        int countryId = jsonBody.get("countryId").asInt();
        int districtId = jsonBody.get("districtId").asInt();
        double latitude = jsonBody.get("latitude").asDouble();
        double longitude = jsonBody.get("longitude").asDouble();

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

        return destinationRepository.update(destination);
        }, httpExecutionContext.current())
       .thenApplyAsync(destination -> (Result) ok(), httpExecutionContext.current())
       .exceptionally(error -> {
            try {
                throw error.getCause();
            } catch (NotFoundException notFoundE) {
                return notFound();
            } catch (Throwable ee) {
                return internalServerError();
            }
       });
    }

    /**
     * Endpoint to delete a destination given its id
     * @param destinationId the id of the destination that we want to delete
     * @param request the request sent by the routes file
     * @return Status code 200 if successful, 404 if not found, 500 otherwise.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteDestination(int destinationId, Http.Request request) {
        return destinationRepository.getDestinationById(destinationId)
                .thenComposeAsync(optionalDestination -> {
                    if(!optionalDestination.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    Destination destination = optionalDestination.get();
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
                    } catch (Throwable serverError) {
                        return internalServerError();
                    }
                });
    }

    /**
     * Endpoint to get all countries.
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
                    // TODO: Add check that user owns destination when destination types get merged in
                    return photoRepository.getPhotoByIdAndUser(photoId, userFromMiddleware.getUserId())
                            .thenComposeAsync(optionalPhoto -> {
                                if (!optionalPhoto.isPresent()) {
                                    throw new CompletionException(new NotFoundException("Could not find photo"));
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

    /**
     * Get all the photos linked to this destination
     * @param destinationId the id of the destination
     * @param request the HTTP request trying to get the destination photos
     * @return a response according to the API spec
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhotos(int destinationId, Http.Request request) {
        // TODO: check that the destination is not private once Story 13 is done
        // TODO: if destination is private, check that the user has permission once Story 13 is done

        ObjectNode res = Json.newObject();
        String messageKey = "message";
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationById(destinationId).thenApplyAsync((optionalDestination -> {
            if (!optionalDestination.isPresent()) {
                res.put(messageKey, "Destination " + destinationId + " does not exist");
                return notFound(res);
            }
            Destination destination = optionalDestination.get();
            List<DestinationPhoto> destinationPhotos = destination.getDestinationPhotos();

            if (user.isAdmin() || user.isDefaultAdmin()) {
                return ok(Json.toJson(destinationPhotos)); // TODO: find out why "isPrimary" is serialised as "primary" in JSON
            }

            List<DestinationPhoto> photosToReturn = new ArrayList<>();
            List<DestinationPhoto> publicDestinationPhotos = destination.getPublicDestinationPhotos();
            List<DestinationPhoto> privatePhotosForUser = destination.getPrivatePhotosForUserWithId(user.getUserId());
            photosToReturn.addAll(publicDestinationPhotos);
            photosToReturn.addAll(privatePhotosForUser);

            JsonNode photos = Json.toJson(photosToReturn);
            System.out.println(photos);

            return ok(Json.toJson(photosToReturn));
        }), httpExecutionContext.current());
    }

    /**
     * Remove the association between a personal photo and a destination
     * @param photoId the id of the photo
     * @param request the HTTP request
     * @return a response that complies with the API spec
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> unlinkPhoto(int destinationId, int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        ObjectNode res = Json.newObject();
        String messageKey = "message";

        return destinationRepository.getDestinationPhotoById(destinationId, photoId).thenApplyAsync(optionalDestinationPhoto -> {
            // return 404 if destination photo is not found
            if (!optionalDestinationPhoto.isPresent()) {
                res.put(messageKey, String.format("Photo %d is not linked to destination %d",photoId, destinationId));
                return notFound(res);
            }

            // allow deletion of the destination photo to any admin
            DestinationPhoto destinationPhoto  = optionalDestinationPhoto.get();
            if (user.isDefaultAdmin() || user.isAdmin()) {
                destinationPhoto.delete();
                res.put(messageKey, "The destination photo link was deleted");
                return ok(res);
            }

            // allow the photo's owner to delete the destination photo
            PersonalPhoto personalPhoto = destinationPhoto.getPersonalPhoto();
            if (personalPhoto.getUser().equals(user)) {
                destinationPhoto.delete();
                res.put(messageKey, "The destination photo link was deleted");
                return ok(res);
            }

            // else tell the user they are not allowed :P
            res.put(messageKey, String.format("User %d is not allowed to delete destination photo %d",user.getUserId(), photoId));
            return forbidden(res);
        }, httpExecutionContext.current());
    }

}

