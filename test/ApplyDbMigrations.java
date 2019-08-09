import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;

public class ApplyDbMigrations {

  /** Generate the next "DB schema DIFF" migration. */
  public static void main(String[] args) throws IOException {

    // System.setProperty("ddl.migration.version", "1.1");
    // System.setProperty("ddl.migration.name", "support end dating");

    // generate a migration using drops from a prior version
     System.setProperty("ddl.migration.pendingDropsFor", "1.2");

    DbMigration dbMigration = DbMigration.create();
    dbMigration.setPlatform(Platform.MYSQL);

    dbMigration.generateMigration();
  }
}
