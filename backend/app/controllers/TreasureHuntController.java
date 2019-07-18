package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import repository.TreasureHuntRepository;

import javax.inject.Inject;

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
}
