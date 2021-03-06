package tasks;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import controllers.PhotoController;
import models.PersonalPhoto;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Task to create profile photos for all the users in the database.
 */
public class ExampleUserPhotoData {

  private ActorSystem actorSystem;
  private ExecutionContext executionContext;
  private PhotoController photoController;
  final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public ExampleUserPhotoData(
      ActorSystem actorSystem, ExecutionContext executionContext, PhotoController photoController) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.photoController = photoController;
    initialise();
  }

  /**
   * Define the code to be run, and when it should be run NOTE - internet enabler must be turned on.
   * Runs at start up and then every 24 hours after that.
   */
  private void initialise() {
    this.actorSystem
        .scheduler()
        .scheduleOnce(
            Duration.create(1, TimeUnit.SECONDS), // initial delay
            () ->
                runAsync(
                    () -> {
                      log.info("Started adding example photos to users.");
                      photoController.checkForAndCreatePhotosDirectory();
                      List<User> users = User.find.all();
                      final int[] fileIndex = {0};

                      File folder = new File("storage/photos");
                      File[] listOfFiles = folder.listFiles();

                      if (listOfFiles != null) {
                        for (User user : users) {
                          runAsync(
                              () -> {
                                if (user.getProfilePhoto() == null) {
                                    File photoFile = listOfFiles[fileIndex[0]];
                                    fileIndex[0] = (fileIndex[0] + 1) % listOfFiles.length;

                                    PersonalPhoto personalPhoto =
                                        new PersonalPhoto(
                                            photoFile.getName(),
                                            true,
                                            user,
                                            true,
                                            null,
                                            false);
                                    personalPhoto.save();
                                    user.setProfilePhoto(personalPhoto);
                                    user.save();
                                    log.info(
                                        String.format(
                                            "Added photo for user %s %s%n",
                                            user.getFirstName(), user.getLastName()));
                                }
                              });
                        }
                      }
                      log.info("Finished adding example photos to users");
                    }),
            executionContext);
  }
}
