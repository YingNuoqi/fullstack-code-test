package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    services.put("https://www.kry.se", "UNKNOWN");
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
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
     * router.get(String path)
     * Add a route that matches a HTTP GET request and the specified path
     */
    router.get("/service").handler(req -> {
      String getAllQuery = "SELECT * FROM service";
      connector.query(getAllQuery).setHandler(res -> {
        if(res.succeeded()) {
          List<JsonObject> jsonServices = new ArrayList<>();

          List<JsonObject> results = res.result().getRows();
          for(JsonObject result: results) {
            services.put(result.getString("url"), "UNKNOWN");
            jsonServices.add(new JsonObject()
                  .put("name", result.getString("url"))
                  .put("status", "UNKNOWN"));
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
     * Add a route that matches a HTTP POST request and the specified path
     */
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      String insertQuery = "INSERT INTO service VALUES (?)";
      JsonArray params = new JsonArray().add(jsonBody.getString("url"));
      connector.update(insertQuery, params).setHandler(done -> {
        if(done.succeeded()) {
          System.out.println("insert succeeded...--------");

          services.put(jsonBody.getString("url"), "UNKNOWN");
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          done.cause().printStackTrace();
        }
      });
    });
  }

}



