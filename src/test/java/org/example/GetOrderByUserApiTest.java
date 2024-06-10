package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.example.serializedDatas.UserSerialized;
import org.junit.After;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

public class GetOrderByUserApiTest extends MainApiTest {
    Faker faker = new Faker();

    @Step("get Access token")
    public String getAccessToken() {
        UserSerialized json = new UserSerialized(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().name()
        );

        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/register");

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("accessToken");

            return accessToken;
        }
        return "";
    }

    @Step("request to get orders from /api/orders")
    public Response getOrders(String accessToken) {
        Response response = given()
                .header("Authorization", accessToken)
                .when().
                get("/api/orders").
                then().extract().response();
        return response;
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
    public void getOrderWithAuthorization() {
        String accessToken = getAccessToken();
        getOrders(accessToken).then()
                .statusCode(200)
                .and()
                .assertThat().body("success", equalTo(true))
                .and()
                .assertThat().body(containsString("order"));
    }

    @Test
    public void getOrderWithoutAuthorization() {
        getOrders("").then()
                .statusCode(401)
                .and()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUserTest() {
        if (getAccessToken() != null) {
            deleteUser(getAccessToken());
        }
    }
}
