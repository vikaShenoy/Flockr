package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.Nationality;
import models.Passport;
import play.libs.Json;
import play.mvc.Result;
import util.Security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

/**
 * Controller used for testing with H2 internal database.
 * Enables database resampling.
 */
public class InternalController {
}
