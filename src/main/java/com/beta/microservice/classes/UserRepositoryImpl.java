package com.beta.microservice.classes;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;


public class UserRepositoryImpl implements UserRepository {

  private MongoClient mongoClient;

  public UserRepositoryImpl(Vertx vertx, JsonObject config) {
    this.mongoClient = MongoClient.create(vertx, config);

  }

  @Override
  public Future<String> createUser(String login, String hash) {
    JsonObject user = new JsonObject()
      .put("login", login)
      .put("password", hash);
    return mongoClient.save("users", user);
  }
  @Override
  public Future<String> createToken(String userId, String token){
    JsonObject userToken = new JsonObject()
      .put("userid", userId)
      .put("token", token);
    return mongoClient.save("tokens", userToken);
  }
  @Override
  public Future<String> createItem(String userId, String name){
    JsonObject document = new JsonObject()
      .put("userid", userId)
      .put("name", name);
    return mongoClient.save("items", document);
  }
  @Override
  public void deleteToken(String oldToken){
    JsonObject token = new JsonObject()
      .put("token", oldToken);
    mongoClient.removeDocuments("tokens", token);
  }
  @Override
  public Future<JsonObject> findUserByLogin(String login) {
    JsonObject query = new JsonObject()
      .put("login", login);
        return mongoClient.findOne("users", query, null);
  }
  @Override
  public Future<JsonObject> findUserByLoginAndPassword(String login, String password) {
    JsonObject query = new JsonObject()
      .put("login", login)
      .put("password", password);
    return mongoClient.findOne("users", query, null);
  }
  @Override
  public Future<JsonObject> findTokenByUserId(JsonObject token) {

    return mongoClient.findOne("tokens", token, null);
  }
  @Override
  public Future<List<JsonObject>> findItemsByUserId(JsonObject userId){

    return mongoClient.find("items", userId);
  }
  @Override
  public Future<JsonObject> checkToken(JsonObject token) {

    return mongoClient.findOne("tokens", token, null);
  }

}
