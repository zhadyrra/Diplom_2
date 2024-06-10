package org.example;

import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.example.serializedDatas.UserSerialized;
import io.qameta.allure.Step;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class LoginUserApiTest extends MainApiTest {
    Faker faker = new Faker();
    String mail = faker.internet().emailAddress();
    String pass = faker.internet().password();
    String name = faker.name().name();
    String accessToken;

    @Step("Send post request to /api/auth/login")
    public Response makeRequest(UserSerialized json) {
        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/login");
        return response;
    }

    @Step("check status of /api/auth/login")
    public void checkStatus(Response response) {
        response.then().statusCode(200);
    }

    public void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken) //
                .when()
                .delete("/api/auth/user/")
                .then()
                .statusCode(202)
                .body("message", equalTo("User successfully removed"));
    }
    @Before
    public void createUser() {
        UserSerialized json = new UserSerialized(
                mail,pass,name
        );
        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/register");
        if (response.getStatusCode() == 200) {
            this.accessToken = response.jsonPath().getString("accessToken");
        }
    }

    @After
    @Step("delete created user")
    public void deleteUserTest(){
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }
    @Test
    public void loginUserTest() {
        UserSerialized json = new UserSerialized(
                mail,pass
        );
        Response response = makeRequest(json);
        checkStatus(response);
    }
    @Test
    public void loginTestWithWrongEmail(){
        Faker faker = new Faker();

        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                faker.internet().password()
        );

        Response response = makeRequest(json);
        response
                .then()
                .assertThat().body("message", equalTo("email or password are incorrect"))
                .and()
                .statusCode(401);
    }

    @Test
    public void loginTestWithWrongPassword() {
        Faker faker = new Faker();

        UserSerialized json = new UserSerialized(
                faker.name().username(),
                faker.internet().password()
        );
        Response response = makeRequest(json);
        response
                .then()
                .assertThat().body("message", equalTo("email or password are incorrect"))
                .and()
                .statusCode(401);
    }
}
