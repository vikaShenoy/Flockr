import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;

/**
 * The ApplyDbMigrations class builds sequenced migration files for the database to allow us to make
 * modifications to the schema without losing persisted information. You need to uncomment the
 * ddl.migration.pendingDropsFor() section to apply database drops. This will action the pending
 * drops from the previous migration version(as specified in the XML file. To give a migration file
 * a custom name or version, uncomment the ddl.migration.version() code or the ddl.migration.name()
 * respectively. Migration files are stored in /src/main/resources/dbmigration
 *
 * <p>To run the class, right-click on the file in the IntelliJ project window and select Run from
 * the drop down menu.
 */
public class ApplyDbMigrations {

  /**
   * Generate the next "DB schema DIFF" migration.
   */
  public static void main(String[] args) throws IOException {

    /**
     * The following lines can be used to give a custom name and version number to a migration file
     * if needed.
     */
    // System.setProperty("ddl.migration.version", "1.0");
    System.setProperty("ddl.migration.name", "Migration-File");

    /**
     * You can uncomment the following lines to action any pending drops from a previous version XML
     */
    // generate a migration using drops from a prior version
    // System.setProperty("ddl.migration.pendingDropsFor", "1.2");

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.MYSQL);

    dbMigration.generateMigration();
  }
}
