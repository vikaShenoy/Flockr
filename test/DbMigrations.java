
import io.ebean.dbmigration.DbMigration;
import io.ebean.migration.DbPlatformNames;


import java.lang.String;

import java.io.IOException;

import static io.ebean.migration.DbPlatformNames.*;


public class DbMigrations {

    public static void main(String[] args) throws IOException {
        System.setProperty("ddl.migration.version", "1.0"); // Can automatically set version
        System.setProperty("ddl.migration.name", "Initial");

        //Generate PendingDrops for a previous version
        //System.setProperty("ddl.migration.pendingDropsFor","1.1");

        DbMigration dbMigration;
        dbMigration.setPlatform(DbPlatformNames.MYSQL);
        dbMigration.generateMigration();
    }
}
