package org.example;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import org.example.serializedDatas.UserSerialized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class EditUserApiTest extends MainApiTest{

    Faker faker = new Faker();
    String mail ;
    String pass ;
    String name ;
    String accessToken;

    @Step("Send patch request to /api/auth/user")
    public Response makeRequest(UserSerialized json, String accessToken) {
        Response response = given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)// заполни header
                .and()
                .body(json)
                .when()
                .patch("/api/auth/user");
        return response;
    }
    @Step("create user for tests")
    public void createUser() {
         this.mail = faker.internet().emailAddress();
         this.pass = faker.internet().password();
        this.name = faker.name().name();
        UserSerialized json = new UserSerialized(
                mail,pass,name
        );
        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/register");
    }
    @Step("login with created user for test")
    public void loginUser() {
        UserSerialized json = new UserSerialized(
                mail,pass
        );
        Response response = given()
                .header("Content-type", "application/json") // заполни header
                .and()
                .body(json)
                .when()
                .post("/api/auth/login");

        if (response.getStatusCode() == 200) {
            this.accessToken = response.jsonPath().getString("accessToken");
        }
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
    @After
    @Step("delete created user for tests")
    public void deleteUserTest(){
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    public void EditUserTestWithAuthorization() {
        createUser();
        loginUser();
        String editMail = faker.internet().emailAddress();
        String editPass = faker.internet().password();
        String editName = faker.name().name();
        UserSerialized json = new UserSerialized(
                editMail,editPass,editName
        );
        Response response = makeRequest(json,accessToken);

        response.then().statusCode(200)
                .and()
                .assertThat()
                .body("success", equalTo(true))
                .and()
                .assertThat()
                .body("user.email", equalTo(editMail.toLowerCase()))
                .and().assertThat()
                .body("user.name", equalTo(editName));
    }
    @Test
    public void EditUserTestWithoutAuthorization() {
        String editMail = faker.internet().emailAddress();
        String editPass = faker.internet().password();
        String editName = faker.name().name();
        UserSerialized json = new UserSerialized(
                editMail,editPass,editName
        );
        Response response = makeRequest(json,"");
        response.then().statusCode(401)
                .and()
                .assertThat()
                .body("success", equalTo(false))
                .and()
                .assertThat()
                .body("message", equalTo("You should be authorised"));
    }
}
