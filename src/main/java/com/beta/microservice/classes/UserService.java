package com.beta.microservice.classes;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class UserService {
  private UserRepository userRepository;
  private MessageDigest hashingAlgoritm;

  private JWTAuth jwtAuth;
  public UserService(UserRepository userRepository, JWTAuth jwtAuth){
    this.userRepository = userRepository;
    try {
      hashingAlgoritm = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("No such hashing alogoritm");
    }
    this.jwtAuth = jwtAuth;
  }
  public Future<RegisterResult> register(String login, String password){
    if(login.isEmpty() || password.isEmpty()){
      return Future.failedFuture("Login and password can not be empty");
    }

    return Future.future(promise -> {
      userRepository.findUserByLogin(login)
        .onSuccess(json -> {
          byte[] hash = hashingAlgoritm.digest(password.getBytes());

          if(json != null){
            promise.complete(RegisterResult.USER_ALREADY_EXIST);
            return;
          }

          userRepository.createUser(login, Arrays.toString(hash))
            .onSuccess(message -> promise.complete(RegisterResult.SUCCESS))
            .onFailure(error -> promise.fail("Database connection error"));
        })
        .onFailure(error -> promise.fail("Database connection error"));
    });
  }
  public Future<String> login(String login, String password){
    if(login.isEmpty() || password.isEmpty()){
      return Future.failedFuture("Login and password can not be empty");
    }
    byte[] hash = hashingAlgoritm.digest(password.getBytes());
    return Future.future(promise -> {
      userRepository.findUserByLoginAndPassword(login, Arrays.toString(hash))
        .onSuccess(json -> {
          if (json == null) {
            promise.complete("WRONG_LOGIN_OR_PASSWORD");
            return;
          }
          String userId = json.getString("_id");

          JsonObject query2 = new JsonObject()
            .put("userid", userId);
          Future<JsonObject> future2 = userRepository.findTokenByUserId(query2);
          future2
            .onSuccess(json2 -> {
              if(json2 != null) {
                String oldToken = json2.getString("token");
                userRepository.deleteToken(oldToken);
              }
            });
          String token = jwtAuth.generateToken(new JsonObject());

          userRepository.createToken(userId, token)
            .onSuccess(message -> promise.complete(token))
            .onFailure(error -> promise.fail("Database connection error"));
        })
        .onFailure(error -> promise.fail("Database connection error"));
    });
  }
  public Future<JsonObject> getItems(String token){
    JsonObject query = new JsonObject()
      .put("token", token);
    return Future.future(promise -> {
      userRepository.checkToken(query)
        .onSuccess(json -> {
          if (json == null) {
            promise.complete(new JsonObject().put("status", "noToken"));
            return;
          }
          String userId = json.getString("userid");
          JsonObject query2 = new JsonObject()
            .put("userid", userId);
          Future<List<JsonObject>> future2 = userRepository.findItemsByUserId(query2);
          future2
            .onSuccess(json2 -> {
              if (json2 != null) {
                JsonObject responseBody = new JsonObject().put("status", "Items found successfully");

                for ( JsonObject json3 : json2) {
                  responseBody.put(json3.getString("_id"), json3.getString("name"));
                }
                promise.complete(responseBody);
              }
              else{
                promise.complete(new JsonObject().put("status", "noItems"));
              }
            })
          .onFailure(error -> promise.fail("Database connection error"));
        })
        .onFailure(error -> promise.fail("Database connection error"));
    });
  }
  public Future<String> createItem(String token, String name){
    JsonObject query = new JsonObject()
      .put("token", token);
    return Future.future(promise -> {
      userRepository.checkToken(query)
        .onSuccess(json -> {
          if (json == null) {
            promise.complete("noToken");
            return;
          }
          String userId = json.getString("userid");
          userRepository.createItem(userId, name)
            .onSuccess(message -> promise.complete("Item added"))
            .onFailure(error -> promise.fail("Database connection error"));
        })
        .onFailure(error -> promise.fail("Database connection error"));
    });
  }
}
