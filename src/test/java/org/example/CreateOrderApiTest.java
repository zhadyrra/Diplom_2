package org.example;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.example.serializedDatas.OrderSerialized;
import org.example.serializedDatas.UserSerialized;
import org.junit.Test;
import com.github.javafaker.Faker;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateOrderApiTest extends MainApiTest {
    Faker faker = new Faker();

    @Step("request to /api/orders")
    public Response createOrder(OrderSerialized ingredients, String accessToken) {
        Response response = given().
                header("Content-type", "application/json")
                .and()
                .body(ingredients)
                .when()
                .post("/api/orders");

        return response;
    }

    @Step("request to get ingredients")
    public ValidatableResponse getIngredients() {
        ValidatableResponse response = given().
                when().
                get("/api/ingredients").
                then();

        return response;
    }

    public ArrayList<String> getIngredientsIds() {
        return getIngredients().extract().path("data._id");
    }

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

    @Test
    public void createOrderWithoutAuthorization() {

        Response response = createOrder(new OrderSerialized(getIngredientsIds()), "");

        response.then().statusCode(200).and().assertThat().body("success", equalTo(true));
    }

    @Test
    public void createOrderWithAuthorization() {
        String accessToken = getAccessToken();
        Response response = createOrder(new OrderSerialized(getIngredientsIds()), accessToken);

        response.then().statusCode(200).and().assertThat().body("success", equalTo(true));
    }

    @Test
    public void createOrderWithIngredients() {
        Response response = createOrder(new OrderSerialized(getIngredientsIds()), "");

        response.then().statusCode(200).and().assertThat().body("success", equalTo(true));
    }

    @Test
    public void createOrderWithoutIngredients() {
        Response response = createOrder(new OrderSerialized(null), "");

        response.then().statusCode(400)
                .and()
                .assertThat().body("success", equalTo(false))
                .and()
                .assertThat().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidHash() {
        ArrayList<String> invalidHashes = new ArrayList<String>();

        invalidHashes.add("invalidhash");
        invalidHashes.add("invalidhash2");

        Response response = createOrder(new OrderSerialized(invalidHashes), "");

        response.then().statusCode(500);
    }

}
