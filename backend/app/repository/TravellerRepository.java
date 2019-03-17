package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Passport;
import models.User;
import play.db.ebean.EbeanConfig;
import play.db.ebean.EbeanDynamicEvolutions;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TravellerRepository {
    private final EbeanServer ebeanServer;
    private final EbeanDynamicEvolutions ebeanDynamicEvolutions;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public TravellerRepository(EbeanConfig ebeanConfig, EbeanDynamicEvolutions ebeanDynamicEvolutions, DatabaseExecutionContext executionContext) {
        this.ebeanDynamicEvolutions = ebeanDynamicEvolutions;
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public CompletionStage<List<Passport>> getPassportById(int passportId) {
        return supplyAsync(() -> {
            List<Passport> passports = Passport.find.all();
            return passports;
        }, executionContext);
    }
}
