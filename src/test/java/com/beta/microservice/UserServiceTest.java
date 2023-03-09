package com.beta.microservice;

import com.beta.microservice.classes.RegisterResult;
import com.beta.microservice.classes.UserRepository;
import com.beta.microservice.classes.UserService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

@ExtendWith(VertxExtension.class)
public class UserServiceTest {
  private MessageDigest hashingAlgoritm;

  @BeforeEach
  public void setUp() {
    try {
      hashingAlgoritm = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("No such hashing alogoritm");
    }
  }
  @Test
  public void registerSuccess() throws Throwable{

    String login = "login15";
    String password = "password19";
    byte[] hash = hashingAlgoritm.digest(password.getBytes());

    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.findUserByLogin(login))
      .thenReturn(Future.succeededFuture(null));
    Mockito.when(userRepositoryMock.createUser(login, Arrays.toString(hash)))
      .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.register(login, password)
      .onSuccess(result -> {
        Assertions.assertEquals(RegisterResult.SUCCESS, result);
      })
      .onFailure(error -> {
        Assertions.fail();
      });

  }
  @Test
  public void registerLoginTaken(){
    String login = "login15";
    String password = "password19";
    byte[] hash = hashingAlgoritm.digest(password.getBytes());

    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.findUserByLogin(login))
      .thenReturn(Future.succeededFuture(new JsonObject()));
    Mockito.when(userRepositoryMock.createUser(login, Arrays.toString(hash)))
      .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.register(login, password)
      .onSuccess(result -> {
        Assertions.assertEquals(RegisterResult.USER_ALREADY_EXIST, result);
      })
      .onFailure(error -> {
        Assertions.fail();
      });
  }
  @Test
  public void registerFailed(){
    String login = "login15";
    String password = "password19";
    byte[] hash = hashingAlgoritm.digest(password.getBytes());

    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.findUserByLogin(login))
      .thenReturn(Future.failedFuture("Find error"));
    Mockito.when(userRepositoryMock.createUser(login, Arrays.toString(hash)))
      .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.register(login, password)
      .onSuccess(result -> {
        Assertions.fail();
      })
      .onFailure(error -> {
        Assertions.assertEquals("Database connection error", error.getMessage() );
      });
  }
  @Test
  public void registerFailed2(){
    String login = "login15";
    String password = "password19";
    byte[] hash = hashingAlgoritm.digest(password.getBytes());

    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.findUserByLogin(login))
      .thenReturn(Future.succeededFuture(null));
    Mockito.when(userRepositoryMock.createUser(login, Arrays.toString(hash)))
      .thenReturn(Future.failedFuture("Create error"));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.register(login, password)
      .onSuccess(result -> {
        Assertions.fail();
      })
      .onFailure(error -> {
        Assertions.assertEquals("Database connection error", error.getMessage() );
      });
  }
  @Test
  public void createItemWrongToken(){
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NzgzMzA5MjJ9.96es-T71tBL2ZtGDg_d3nqzGQA6VL7CitWuHGHTQzS8";
    String name = "Item1";
    String id = "64094c383174ee1c62397ea5";
    JsonObject query = new JsonObject()
            .put("token", token);
    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.checkToken(query))
            .thenReturn(Future.failedFuture("noToken"));
    Mockito.when(userRepositoryMock.createItem(id,name))
            .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.createItem(token, name)
            .onSuccess(result -> {
              Assertions.assertEquals("noToken", result);
            })
            .onFailure(error -> {
              Assertions.assertEquals("Database connection error", error.getMessage() );
            });
  }
  @Test
  public void createItemSuccess(){
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NzgzMzA5MjJ9.96es-T71tBL2ZtGDg_d3nqzGQA6VL7CitWuHGHTQzS8";
    String name = "Item1";
    String id = "64094c383174ee1c62397ea5";
    JsonObject query = new JsonObject()
            .put("token", token);
    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.checkToken(query))
            .thenReturn(Future.succeededFuture(new JsonObject()
                    .put("userid", id)));
    Mockito.when(userRepositoryMock.createItem(id,name))
            .thenReturn(Future.succeededFuture("Item added"));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.createItem(token, name)
            .onSuccess(result -> {
              Assertions.assertEquals("Item added", result);
            })
            .onFailure(error -> {
              Assertions.assertEquals("Database connection error", error.getMessage() );
            });
  }
  @Test
  public void getItemsSuccessButEmpty(){
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NzgzMzA5MjJ9.96es-T71tBL2ZtGDg_d3nqzGQA6VL7CitWuHGHTQzS8";
    String id = "64094c383174ee1c62397ea5";
    JsonObject query = new JsonObject()
            .put("token", token);
    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.checkToken(query))
            .thenReturn(Future.succeededFuture(new JsonObject()
                    .put("userid", id)));
    Mockito.when(userRepositoryMock.findItemsByUserId(new JsonObject()
                    .put("userid", id)))
            .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.getItems(token)
            .onSuccess(result -> {
              Assertions.assertEquals("noItems", result.getString("status"));
            })
            .onFailure(error -> {
              Assertions.assertEquals("Database connection error", error.getMessage() );
            });
  }
  @Test
  public void getItemsWrongToken(){
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NzgzMzA5MjJ9.96es-T71tBL2ZtGDg_d3nqzGQA6VL7CitWuHGHTQzS8";
    String id = "64094c383174ee1c62397ea5";
    JsonObject query = new JsonObject()
            .put("token", token);
    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.checkToken(query))
            .thenReturn(Future.succeededFuture(null));
    Mockito.when(userRepositoryMock.findItemsByUserId(new JsonObject()
                    .put("userid", id)))
            .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.getItems(token)
            .onSuccess(result -> {
              Assertions.assertEquals("noToken", result.getString("status"));
            })
            .onFailure(error -> {
              Assertions.assertEquals("Database connection error", error.getMessage() );
            });
  }
  @Test
  public void loginWrongLoginOrPassword() throws Throwable{

    String login = "login15";
    String password = "password19";
    byte[] hash = hashingAlgoritm.digest(password.getBytes());

    JWTAuth jWTAuth = mock(JWTAuth.class);
    UserRepository userRepositoryMock = mock(UserRepository.class);
    Mockito.when(userRepositoryMock.findUserByLoginAndPassword(login, Arrays.toString(hash)))
            .thenReturn(Future.succeededFuture(null));
    UserService userService = new UserService(userRepositoryMock, jWTAuth);

    userService.login(login, password)
            .onSuccess(result -> {
              Assertions.assertEquals("WRONG_LOGIN_OR_PASSWORD", result);
            })
            .onFailure(error -> {
              Assertions.fail();
            });

  }
}
