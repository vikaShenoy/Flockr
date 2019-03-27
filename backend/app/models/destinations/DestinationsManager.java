package models.destinations;

import exceptions.DestinationNotFound;

import java.util.List;

/**
 * Interface that defines how other parts of the application expect a class managing destinations to behave
 */
interface DestinationsManager {

    /**
     * Get all destinations so this can be shown in the front end.
     * @return a list of all the destinations in the database
     */
    public List<Destination> getAllDestinations();

    /**
     * Get a destination by name so that it can be shown in the front end.
     * @param name the name of the destination we are after
     * @return the destination we are after
     * @throws Exception when the destination is not found
     */

    /**
     * Get a destination by name so that it can be shown in the front end.
     * @param name the name of the destination we are after
     * @return the destination we are after
     * @throws DestinationNotFound when the destination could not be found
     * @throws Exception when something else went wrong internally
     */
    public Destination getDestinationByName(String name) throws DestinationNotFound, Exception;
}
