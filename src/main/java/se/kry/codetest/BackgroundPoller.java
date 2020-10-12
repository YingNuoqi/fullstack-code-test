package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackgroundPoller  {
  private String STATUS_OK = "OK";
  private String STATUS_FAIL = "FAIL";

  public Future<List<String>> pollServices(Map<String, String> services, WebClient webClient) {

    List<String> list = new ArrayList<>();
    Future<List<String>> resultFuture = Future.future();
    for (Map.Entry<String, String> entry: services.entrySet()) {

      webClient.getAbs(entry.getKey()).ssl(entry.getKey().contains("https")).send(res -> {
        if(res.succeeded()) {
          services.replace(entry.getKey(), entry.getValue(), STATUS_OK);
        } else {
          services.replace(entry.getKey(), entry.getValue(), STATUS_FAIL);
        }
        list.add(entry.getKey());
      });
    }
    // for test
//    for (Map.Entry<String, String> entry: services.entrySet()) {
//      System.out.println(entry.getKey() + "--" + entry.getValue());
//    }

    if(list.size() == services.size()) {
      resultFuture.complete(list);
    } else {
      resultFuture.failed();
    }
    return resultFuture;
  }
}
