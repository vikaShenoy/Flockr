package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Files;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import repository.PhotoRepository;
import util.Security;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.unauthorized;

public class PhotoController {
    private final PhotoRepository photoRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public PhotoController(PhotoRepository photoRepository, HttpExecutionContext httpExecutionContext) {
        this.photoRepository = photoRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Upload a new photo for a user
     * @param userId the id of the user we are trying to add the photo for
     * @param request the HTTP request requesting the upload
     * @return the result for the request
     */
    public CompletionStage<Result> uploadPhotoForUser(int userId, Http.Request request) {
        Optional<String> optionalContentType = request.contentType();
        ObjectNode response = Json.newObject();
        String messageKey = "message";

        // TODO: check that we are authorised to upload the photo

        // check that a content type is specified
        if (!optionalContentType.isPresent()) {
            response.put(messageKey, "Please specify the Content Type as specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        String contentType = optionalContentType.get();

        // check that the content type is allowed
        if (!contentType.equals("multipart/form-data")) {
            response.put(messageKey, "Please specify the Content Type as specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // parse request body to multipart form data
        Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart photo = multipartFormData.getFile("image");
        Map<String, String[]> textFields = multipartFormData.asFormUrlEncoded();

        // check that the body includes fields as specified in the API spec
        if (!(textFields.containsKey("isPrimary") || textFields.containsKey("isPublic"))) {
            response.put(messageKey, "Please provide the request body as specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // check that the body includes the photo
        if (photo == null) {
            response.put(messageKey, "Missing photo in the request body");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // check for the photo's content type
        String photoContentType = photo.getContentType();
        if (!(photoContentType.equals("image/png") || photoContentType.equals("image/jpeg"))) {
            response.put(messageKey, "Your photo must be a .png or .jpg file");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // initialise the text components of the request
        String isPrimaryAsText = textFields.get("isPrimary")[0];
        String isPublicAsText = textFields.get("isPublic")[0];
        boolean isPrimary = isPrimaryAsText.equals("true");
        boolean isPublic = isPublicAsText.equals("true");

        // get the photo as a file from the request
        Files.TemporaryFile temporaryPhotoFile = (Files.TemporaryFile) photo.getRef();

        // copy file to local file system
        String path = System.getProperty("user.dir") + "/storage/photos";
        Security security = new Security();
        String token = security.generateToken();
        String extension = photoContentType.equals("image/png") ? ".png" : ".jpg";
        String filename = token + extension;
        File destination = new File(path, filename);

        // if the file path already exists, generate another token
        while (destination.exists()) {
            token = security.generateToken();
            filename = token + extension;
            destination = new File(path, filename);
        }

        temporaryPhotoFile.moveFileTo(destination);

        response.put(messageKey, "Endpoint under development");
        return supplyAsync(() -> internalServerError(response), httpExecutionContext.current());
    }
}
