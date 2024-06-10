package org.example;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;

public class MainApiTest {
    @Before
    @Step("Opening the web site")
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
}

}
