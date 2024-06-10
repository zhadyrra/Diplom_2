package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import org.example.serializedDatas.UserSerialized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateUserApiTest extends MainApiTest {

    Faker faker = new Faker();

    @Step("Send GET request to api/auth/register")
    public Response makeRequest(UserSerialized json) {
        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/register");

        return response;
    }

    @Step("check status of api/auth/register")
    public void checkCreatedStatus(Response response) {
        response.then().statusCode(200);
    }

    @Step("deleting user by api/auth/user")
    public void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken) //
                .when()
                .delete("/api/auth/user/")
                .then()
                .statusCode(202)
                .body("message", equalTo("User successfully removed"));
    }

    @Test
    public void createUserSuccessTest() {
        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().name()
        );
        Response response = makeRequest(json);
        checkCreatedStatus(response);

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("accessToken");

            deleteUser(accessToken);
        }
    }

    @Test
    public void createExistUserTest() {
        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().name()
        );

        Response response = makeRequest(json);
        checkCreatedStatus(response);

        Response responseSecond = makeRequest(json);

        responseSecond.then()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("User already exists"))
                .and()
                .statusCode(403);

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("accessToken");

            deleteUser(accessToken);
        }
    }

    @Test
    public void createUserTestWithoutEmail() {
        UserSerialized json = new UserSerialized(
                "",
                faker.internet().password(),
                faker.name().name()
        );

        Response response = makeRequest(json);
        response.then()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("Email, password and name are required fields"))
                .and()
                .statusCode(403);
    }

    @Test
    public void createUserTestWithoutPassword() {
        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                "",
                faker.name().name()
        );

        Response response = makeRequest(json);
        response.then()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("Email, password and name are required fields"))
                .and()
                .statusCode(403);
    }

    @Test
    public void createUserTestWithoutName() {
        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                faker.internet().password(),
                ""
        );

        Response response = makeRequest(json);
        response.then()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("Email, password and name are required fields"))
                .and()
                .statusCode(403);
    }
}
