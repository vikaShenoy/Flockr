import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;

/**
 * This class allows us to build sequenced migration files for the database to allow us to make
 * modifications to the schema without losing persisted information. You need to uncomment the
 * ddl.migration.pendingDropsFor() section to apply database drops. This will action the pending drops
 * from the previous migration version(as specified in the XML file. To give a migration a custom
 * name or version, uncomment the ddl.migration.version code or the ddl.migration.name respectively.
 *
 * Migration files are stored in /src/main/resources/dbmigration
 */

public class ApplyDbMigrations {

  /** Generate the next "DB schema DIFF" migration. */
  public static void main(String[] args) throws IOException {


    // System.setProperty("ddl.migration.version", "1.0");
    // System.setProperty("ddl.migration.name", "Flockr-Sprint-6-Initial");

    // Generate a migration using drops from a prior version's XML file
    // System.setProperty("ddl.migration.pendingDropsFor", "1.2");

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.MYSQL);

    //All migrations are performed offline
    dbMigration.generateMigration();
  }
}
