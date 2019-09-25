package util;

import models.TreasureHunt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for helping with treasure hunts.
 */
public class TreasureHuntUtil {

  private TreasureHuntUtil() {}

  /**
   * Gets treasure hunts with valid dates from a list of treasure hunts.
   *
   * @param treasureHunts the hunts to compare.
   * @return the valid hunts.
   */
    public static List<TreasureHunt> validateTreasureHunts(List<TreasureHunt> treasureHunts) {
        Date currentDate = new Date(System.currentTimeMillis());
        List<TreasureHunt> validHunts = new ArrayList<>();
        for (TreasureHunt treasureHunt : treasureHunts) {
            if (treasureHunt.getStartDate().compareTo(currentDate) <= 0 &&
                    treasureHunt.getEndDate().compareTo(currentDate) >= 0) {
                validHunts.add(treasureHunt);
            }
        }
        return validHunts;
    }
}
