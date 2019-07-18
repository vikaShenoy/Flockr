package controllers;

import io.ebean.Finder;
import models.TravellerType;
import models.TreasureHunt;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.TreasureHuntRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Contains all end points associated with treasure hunts.
 */
public class TreasureHuntController extends Controller {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private TreasureHuntRepository treasureHuntRepository;

    /**
     * Dependency Injection.
     *
     * @param treasureHuntRepository
     */
    @Inject
    public TreasureHuntController(TreasureHuntRepository treasureHuntRepository) {
        this.treasureHuntRepository = treasureHuntRepository;
    }

    public void addTreasureHunt(Http.Request request, int userId) {
        
    }

}
