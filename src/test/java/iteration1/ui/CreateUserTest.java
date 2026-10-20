package iteration1.ui;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import com.codeborne.selenide.*;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.GetUserProfileResponse;
import models.comparison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
    @BeforeAll
    public static void setupSelenoid(){
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.43:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true,
                "enableLog", true));
    }
    @Test
    public void adminCanCreateUserTest(){
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeHolder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeHolder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        $(Selectors.byAttribute("placeHolder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeHolder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        Alert alert = switchTo().alert();

        assertEquals("✅ User created successfully!", alert.getText());
        alert.accept();

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);


        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);
        CreateUserResponse createdUser = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest(){
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeHolder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeHolder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        $(Selectors.byAttribute("placeHolder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeHolder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Username must be between 3 and 15 characters");
        alert.accept();

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNot(Condition.exist);


        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);
        long usersWithSameUsernameAsNewUser = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
