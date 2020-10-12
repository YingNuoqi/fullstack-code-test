package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, String> services = new HashMap<>();
  //TODO use this
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
//    services.put("https://www.kry.se", "UNKNOWN");

    WebClient webClient = WebClient.create(vertx);
    vertx.setPeriodic(1000 * 10, timerId -> poller.pollServices(services, webClient));

    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    /**
     * Get services
     */
    router.get("/service").handler(req -> {
      String getAllQuery = "SELECT * FROM service";
      // get data from db
      connector.query(getAllQuery).setHandler(res -> {
        if(res.succeeded()) {
          List<JsonObject> jsonServices = new ArrayList<>();

          List<JsonObject> results = res.result().getRows();
          for(JsonObject result: results) {
            if(services.get(result.getString("url")) == null) {
              services.put(result.getString("url"), "UNKNOWN");
            }
            jsonServices.add(new JsonObject()
                  .put("name", result.getString("url"))
                  .put("status", services.get(result.getString("url"))).put("addTime", result.getString("addTime")));
          }

          req.response()
                  .putHeader("content-type", "application/json")
                  .end(new JsonArray(jsonServices).encode());
        } else {
          res.cause().printStackTrace();
        }
      });

//      List<JsonObject> jsonServices = services
//          .entrySet()
//          .stream()
//          .map(service ->
//              new JsonObject()
//                  .put("name", service.getKey())
//                  .put("status", service.getValue()))
//          .collect(Collectors.toList());

    });
    /**
     * Insert a service
     */
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      String insertQuery = "INSERT INTO service VALUES (?,?,?)";
      // insert time
      Date time = Calendar.getInstance().getTime();
      DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm");
      String insertTimeStr = format.format(time);
      JsonArray params = new JsonArray().add(jsonBody.getString("url")).add(insertTimeStr);
      connector.update(insertQuery, params).setHandler(done -> {
        if(done.succeeded()) {
          services.put(jsonBody.getString("url"), "UNKNOWN");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          done.cause().printStackTrace();
        }
      });
    });

    /**
     * Delete a service
     */
    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      String deleteQuery = "DELETE FROM service WHERE url=?";
      JsonArray params = new JsonArray().add(jsonBody.getString("url"));
      connector.update(deleteQuery, params).setHandler(done -> {
        if(done.succeeded()) {
          services.remove(jsonBody.getString("url"));
          req.response().end("OK");
        } else {
          done.cause().printStackTrace();
        }
      });
    });
  }

}



