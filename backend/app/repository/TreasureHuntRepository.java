package repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Contains all database interaction associated with treasure hunts.
 */
public class TreasureHuntRepository {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency Injection
     *
     * @param executionContext Context to run the completion stages on.
     */
    @Inject
    public TreasureHuntRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}
