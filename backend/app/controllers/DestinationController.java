package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ConflictingRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import models.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;
import repository.DestinationRepository;

import javax.annotation.processing.Completion;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;
import repository.PhotoRepository;
import util.DestinationUtil;


/**
 * Controller to manage endpoints related to destinations.
 */
public class DestinationController extends Controller {
    private final DestinationRepository destinationRepository;
    private final PhotoRepository photoRepository;
    private HttpExecutionContext httpExecutionContext;
    private final DestinationUtil destinationUtil;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DestinationController(DestinationRepository destinationRepository, HttpExecutionContext httpExecutionContext, PhotoRepository photoRepository, DestinationUtil destinationUtil) {
        this.photoRepository = photoRepository;
        this.destinationRepository = destinationRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.destinationUtil = destinationUtil;
    }

    /**
     * A function that gets a list of all the destinations and returns it with a 200 ok code to the HTTP client
     *
     * @param request Http.Request the http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDestinations(Http.Request request) {
        return destinationRepository.getDestinations()
                .thenApplyAsync(destinations -> {
                    List<Destination> publicDestinations = destinations
                                                            .stream()
                                                            .filter(Destination::getIsPublic)
                                                            .collect(Collectors.toList());
                    return ok(Json.toJson(publicDestinations));
                }, httpExecutionContext.current());
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
                .thenApplyAsync((optionalDestination) -> {
                    if (!optionalDestination.isPresent()) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "No destination exists with the specified ID");
                        return notFound(message);
                    }

                    Destination destination = optionalDestination.get();

                    try {
                        if (!user.isAdmin() && destination.getDestinationOwner() != null && destination.getDestinationOwner() != user.getUserId()) {
                            ObjectNode message = Json.newObject();
                            message.put("message", "You are not authorised to get this destination");
                            return forbidden(message);
                        }
                    } catch (NullPointerException e) {
                        log.error(e.getMessage());
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
                                .filter(destination -> {
                                    Integer ownerId = destination.getDestinationOwner();
                                    return ownerId != null && ownerId == userId;
                                }).
                                        collect(Collectors.toList());
                        return ok(Json.toJson(userDestinations));
                    } else {
                        List<Destination> userDestinations = destinations.stream()
                                .filter(destination -> {
                                    Integer ownerId = destination.getDestinationOwner();
                                    if (ownerId != null && ownerId == userId) {
                                        return loggedInUser.isAdmin() || destination.getIsPublic();
                                    }
                                    return false;
                                }).
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
        User user = request.attrs().get(ActionState.USER);
        boolean createDestForUser = user.isAdmin() && jsonRequest.has("userId");

        try {
            int userId = createDestForUser ? jsonRequest.get("userId").asInt() : user.getUserId();

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
            JsonNode travellerTypeIds;

            try {
                destinationName = jsonRequest.get("destinationName").asText();
                destinationTypeId = jsonRequest.get("destinationTypeId").asInt();
                districtId = jsonRequest.get("districtId").asInt();
                latitude = jsonRequest.get("latitude").asDouble();
                longitude = jsonRequest.get("longitude").asDouble();
                countryId = jsonRequest.get("countryId").asInt();
                travellerTypeIds = jsonRequest.get("travellerTypeIds");

            } catch (NullPointerException exception) {
                throw new BadRequestException("One or more fields are missing.");
            }

            DestinationType destinationTypeAdd = DestinationType.find.byId(destinationTypeId);
            District districtAdd = District.find.byId(districtId);
            Country countryAdd = Country.find.byId(countryId);
            if (destinationTypeAdd == null || districtAdd == null || countryAdd == null) {
                throw new BadRequestException("One of the fields you have selected does not exist.");
            }

            List<TravellerType> allTravellerTypes = TravellerType.find.all();
            List<TravellerType> travellerTypes = destinationUtil.transformTravellerTypes(travellerTypeIds, allTravellerTypes);

            Destination destinationToAdd = new Destination(destinationName, destinationTypeAdd, districtAdd,
                    latitude, longitude, countryAdd, userId, travellerTypes, false);

            return destinationRepository.getDestinations()
                    .thenComposeAsync(destinations -> {
                        for (Destination dest : destinations) {
                            boolean ownsDestination = dest.getDestinationOwner() != null
                                    && dest.getDestinationOwner() == userId;
                            if (dest.equals(destinationToAdd) &&
                                    (dest.getIsPublic() || ownsDestination)) {
                                throw new CompletionException(new ConflictingRequestException(
                                        "Destination already exists."));
                            }
                        }
                        return destinationRepository.insert(destinationToAdd);
                    }, httpExecutionContext.current())
                    .thenApplyAsync(insertedDestination -> created(Json.toJson(insertedDestination)))
                    .exceptionally(e -> {
                        try {
                            throw e.getCause();
                        } catch (ConflictingRequestException conflictingException) {
                            return status(409, conflictingException.getMessage());
                        } catch (Throwable generalException) {
                            generalException.printStackTrace();
                            return internalServerError("Error creating destinations");
                        }

                    });
        } catch (BadRequestException e) {
            ObjectNode message = Json.newObject();
            message.put("message", "Please provide a valid Destination with complete data");
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
        ObjectNode response = Json.newObject();
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestinationById(destinationId)
                .thenComposeAsync(optionalDest -> {
                    if (!optionalDest.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    // Checks that the user is either the admin or the owner of the photo to change permission groups
                    if (!user.isAdmin() && user.getUserId() != optionalDest.get().getDestinationOwner()) {
                        throw new CompletionException(new ForbiddenRequestException("You are unauthorised to update " +
                                "this destination"));
                    }

                    JsonNode jsonBody = request.body().asJson();

                    String destinationName = jsonBody.get("destinationName").asText();
                    int destinationTypeId = jsonBody.get("destinationTypeId").asInt();
                    int countryId = jsonBody.get("countryId").asInt();
                    int districtId = jsonBody.get("districtId").asInt();
                    double latitude = jsonBody.get("latitude").asDouble();
                    double longitude = jsonBody.get("longitude").asDouble();
                    boolean isPublic = jsonBody.get("isPublic").asBoolean();
                    JsonNode travellerTypeIds = jsonBody.get("travellerTypeIds");
                    List<TravellerType> allTravellerTypes = TravellerType.find.all();
                    List<TravellerType> travellerTypes = destinationUtil.transformTravellerTypes(travellerTypeIds, allTravellerTypes);


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
                    destination.setTravellerTypes(travellerTypes);

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
                        List<DestinationPhoto> photoDest = DestinationPhoto.find.query()
                                .where().eq("destination_destination_id", destId).findList();
                        destinationRepository.deleteDestination(destId);

                        for (DestinationPhoto photo : photoDest) {
                            photo.setDestination(destination);
                            destinationRepository.insertDestinationPhoto(photo);
                        }
                    }

                    return destinationRepository.update(destination);
                }, httpExecutionContext.current())
                .thenApplyAsync(destination -> (Result) ok(), httpExecutionContext.current())
                .exceptionally(error -> {
                    try {
                        throw error.getCause();
                    } catch (NotFoundException notFoundE) {
                        response.put("message", "There is no destination with the given ID found");
                        return notFound(response);
                    } catch (ForbiddenRequestException forbiddenRequest) {
                        response.put("message", forbiddenRequest.getMessage());
                        return forbidden(response);
                    } catch (BadRequestException badRequestE) {
                        response.put("message", badRequestE.getMessage());
                        return badRequest(response);
                    } catch (Throwable ee) {
                        ee.printStackTrace();
                        response.put("message", "Endpoint under development");
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
                    if (destination.getDestinationOwner() == null && !user.isAdmin()) {
                        throw new CompletionException(new ForbiddenRequestException("You are not permitted to delete this destination"));
                    }
                    if (destination.getDestinationOwner() != null && !user.isAdmin() && user.getUserId() != destination.getDestinationOwner()) {
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

    /**
     * Adds a photo to a destination
     *
     * @param destinationId the destination to add a photo to
     * @param request the request object
     * @return - 400 If request body fields don't exist
     *         - 404 If the destination or photo could not be found
     *         - 403 If the user is not the owner of the destination
     *         - 201 Photo has been added successfully
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addPhoto(int destinationId, Http.Request request) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);
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

                    return photoRepository.getPhotoById(photoId)
                            .thenComposeAsync(optionalPhoto -> {
                                if (!optionalPhoto.isPresent()) {
                                    throw new CompletionException(new NotFoundException("Could not find photo"));
                                }

                                if (!userFromMiddleware.isAdmin() && optionalPhoto.get().getOwnerId() != userFromMiddleware.getUserId()) {
                                    throw new CompletionException(new ForbiddenRequestException("User does not own the image"));
                                }


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

    /**
     * Get all the photos linked to this destination
     *
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

            return ok(Json.toJson(photosToReturn));
        }), httpExecutionContext.current());
    }

    /**
     * Remove the association between a personal photo and a destination
     *
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

    /**
     * Allows a user to add a proposal
     *
     * @param request Request object to get user object
     * @param destinationId ID of destination to propose to
     * @return A response that complies with the API spec
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addProposal(Http.Request request, int destinationId) {
        return destinationRepository.getDestinationById(destinationId)
        .thenComposeAsync(optionalDestination -> {
            if (!optionalDestination.isPresent()) {
                throw new CompletionException(new BadRequestException("Destination not found"));
            }

            Destination destination = optionalDestination.get();

            // Cannot make proposals for destinations that are not public
            if (!destination.getIsPublic()) {
                throw new CompletionException(new ForbiddenRequestException("Destination is not public"));
            }

            // Get traveller type objects from ID's
            JsonNode travellerTypeIds = request.body().asJson().get("travellerTypeIds");
            List<TravellerType> allTravellerTypes = TravellerType.find.all();
            List<TravellerType> travellerTypes = destinationUtil.transformTravellerTypes(travellerTypeIds, allTravellerTypes);

            DestinationProposal proposal = new DestinationProposal(destination, travellerTypes);
            return destinationRepository.createProposal(proposal);
        })
        .thenApplyAsync(proposal -> (Result) ok())
        .exceptionally(e -> {
            try {
                throw e.getCause();
            } catch (BadRequestException badRequestException) {
                return badRequest(badRequestException.getMessage());
            } catch (ForbiddenRequestException forbiddenException) {
                return forbidden(forbiddenException.getMessage());
            }
            catch (Throwable throwableException) {
                return internalServerError(throwableException.getMessage());
            }
        });
    }

    /**
     * Allows an admin to accept a traveller type proposal change
     *
     * @param destinationProposalId Id of destination prosposal to accept
     * @return A response that complies with the API spec
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> acceptProposal(int destinationProposalId) {
        return destinationRepository.getDestinationProposalById(destinationProposalId)
        .thenApplyAsync(optionalDestinationProposal -> {
            if (!optionalDestinationProposal.isPresent()) {
                return notFound("Proposal could not be found");
            }
            DestinationProposal destinationProposal = optionalDestinationProposal.get();

            Destination destination = destinationProposal.getDestination();
            destination.setTravellerTypes(destinationProposal.getTravellerTypes());
            destination.update();
            destinationProposal.delete();
            return ok();
        });
    }

    /**
     * Allows an admin to reject a destination proposal
     *
     * @param destinationProposalId the ID of the proposal to reject
     * @return A response that complies with the API spec
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> rejectProposal(int destinationProposalId) {
        return destinationRepository.deleteDestinationProposalById(destinationProposalId)
                .thenApplyAsync((Void) -> ok());
    }

    /**
     * Allows an admin to get all the destination proposals
     *
     * @return A response that complies with the API spec
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> getProposals() {
        return destinationRepository.getDestinationProposals()
        .thenApplyAsync(destinationProposals -> ok(Json.toJson(destinationProposals)));
    }

}

