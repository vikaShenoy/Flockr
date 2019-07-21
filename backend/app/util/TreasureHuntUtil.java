package util;

import models.TreasureHunt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TreasureHuntUtil {

    public static List<TreasureHunt> validateTreasureHunts(List<TreasureHunt> treasureHunts) {
        Date currentDate = new Date(System.currentTimeMillis());
        List<TreasureHunt> validHunts = new ArrayList<>();
        for (TreasureHunt treasureHunt : treasureHunts) {
            System.out.println(treasureHunt);
            if (treasureHunt.getStartDate().compareTo(currentDate) <= 0 &&
                    treasureHunt.getEndDate().compareTo(currentDate) >= 0) {
                validHunts.add(treasureHunt);
            }
        }
        return validHunts;
    }
}
