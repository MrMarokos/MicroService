package com.beta.microservice.classes;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface UserRepository {

  Future<String> createUser(String login, String hash);

  Future<String> createToken(String userid, String token);
  Future<String> createItem(String userId, String name);
  void deleteToken(String token);

  Future<JsonObject> findUserByLogin(String login);
  Future<JsonObject> findUserByLoginAndPassword(String login, String password);
  Future<JsonObject> findTokenByUserId(JsonObject token);

  Future<List<JsonObject>> findItemsByUserId(JsonObject userId);

  Future<JsonObject> checkToken(JsonObject token);


}
