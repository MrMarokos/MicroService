package com.beta.microservice;

import com.beta.microservice.classes.RegisterResult;
import com.beta.microservice.classes.UserRepository;
import com.beta.microservice.classes.UserRepositoryImpl;
import com.beta.microservice.classes.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  UserService userService;
  private JWTAuth jwtAuth;
  private UserRepository userRepository;

  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  public void start() {
    JsonObject config = new JsonObject()
      .put("connection_string", "mongodb://localhost:27017/MicroDB")
      .put("db_name", "MicroDB");

    setUserRepository(new UserRepositoryImpl(vertx, config));
    jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));

    userService = new UserService(userRepository, jwtAuth);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.post("/login").consumes("*/json").handler(this::handleLogin);
    router.post("/register").consumes("*/json").handler(this::handleRegistration);
    router.post("/items").consumes("*/json").handler(this::handleCreateItem);
    router.get("/items").consumes("*/json").handler(this::handleGetItems);
    router.post("/logout").handler(this::handleLogout);

    vertx.createHttpServer().requestHandler(router).listen(8080);
  }
  public void handleRegistration(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();
    String login = requestBody.getString("login");
    String password = requestBody.getString("password");

    if (login == null || password == null ) {
      sendError(routingContext, "Invalid username or password");
    }
    else {
      userService.register(login, password)
        .onSuccess(result -> {
          if(result == RegisterResult.SUCCESS){
            routingContext.response()
              .setStatusCode(204)
              .end("User created");
          }
          else if(result == RegisterResult.USER_ALREADY_EXIST) {
            routingContext.response()
              .setStatusCode(407)
              .end("User already exists");
          }
        })
        .onFailure(error -> {
          System.err.println("ERROR: " + error.getMessage());
          routingContext.response()
            .setStatusCode(500)
            .end();
        });
    }
  }
  public void handleLogin(RoutingContext routingContext){
    JsonObject requestBody = routingContext.body().asJsonObject();

    String login = requestBody.getString("login");
    String password = requestBody.getString("password");

    if ((login == null || login.isEmpty()) || (password == null || password.isEmpty())) {
      sendError(routingContext, "Invalid username or password");
    }
    else{
      userService.login(login, password)
        .onSuccess(result -> {
          if(result.equals("WRONG_LOGIN_OR_PASSWORD")) {
            routingContext.response()
              .setStatusCode(407)
              .end("Wrong login or password");
          }
          else{
            JsonObject responseBody = new JsonObject().put("token", result);
            routingContext.response()
              .setStatusCode(200)
              .end(responseBody.encode());
          }
        })
        .onFailure(error -> {
          System.err.println("ERROR: " + error.getMessage());
          routingContext.response()
            .setStatusCode(500)
            .end();
        });
    }
  }

  public void handleLogout(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();

    String token = requestBody.getString("token");

    userRepository.deleteToken(token);
    sendResponse(routingContext,"User logged out","Success",200);
  }
  public void handleGetItems(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();

    String token = requestBody.getString("token");

    if (token == null || token.isEmpty()) {
      sendError(routingContext, "Invalid token");
    }
    else {
      userService.getItems(token)
        .onSuccess(result -> {
          if(result.getString("status").equals("noToken")) {
            routingContext.response()
              .setStatusCode(401)
              .end("You have not provided an authentication token, the one provided has expired, was revoked or is not authentic.");
          }
          else if(result.getString("status").equals("noItems")) {
            routingContext.response()
              .setStatusCode(204)
              .end("You do not have items");
          }
          else{
            routingContext.response()
              .setStatusCode(200)
              .end(result.encode());
          }
        })
        .onFailure(error -> {
          System.err.println("ERROR: " + error.getMessage());
          routingContext.response()
            .setStatusCode(500)
            .end();
        });
    }
  }
  public void handleCreateItem(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();

    String name = requestBody.getString("name");
    String token = requestBody.getString("token");

    if ((name == null || name.isEmpty()) || (token == null || token.isEmpty())) {
      sendError(routingContext, "Invalid name or token");
    }
    else {
      userService.createItem(token, name)
        .onSuccess(result -> {
          if(result.equals("noToken")) {
            routingContext.response()
              .setStatusCode(401)
              .end("You have not provided an authentication token, the one provided has expired, was revoked or is not authentic.");
          }
          else{
            routingContext.response()
              .setStatusCode(204)
              .end("Item created");
          }
        })
        .onFailure(error -> {
          System.err.println("ERROR: " + error.getMessage());
          routingContext.response()
            .setStatusCode(500)
            .end();
        });
    }
  }

  public static void sendError(RoutingContext context, String message) {
    JsonObject responseBody = new JsonObject().put("status", "error").put("message", message);
    context.response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(400)
      .end(responseBody.encode());
  }
  public static void sendResponse(RoutingContext context, String message, String status, int numberOfResponse) {
    JsonObject responseBody = new JsonObject().put("status", status).put("message", message);
    context.response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(numberOfResponse)
      .end(responseBody.encode());
  }
}
