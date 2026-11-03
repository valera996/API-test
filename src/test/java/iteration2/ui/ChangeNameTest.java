package iteration2.ui;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import com.codeborne.selenide.*;
import generators.RandomData;
import models.CreateUserRequest;
import models.GetUserProfileResponse;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import java.util.Map;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeNameTest {
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
        public void changeNameWithValidWord() {
            CreateUserRequest createUserRequest = AdminSteps.createUser();

            String userAuthHeader =  new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOk())
                    .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                    .extract()
                    .header("Authorization");

            Selenide.open("/");

            executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

            Selenide.open("/dashboard");

            $(Selectors.byClassName("user-info")).click();

            String newName = RandomData.getValidName();
            SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                    .shouldBe(Condition.visible, Condition.enabled);

            input.click();
            input.setValue(newName);
            input.clear();
            input.setValue(newName);

            input.shouldHave(Condition.exactValue(newName));

            $(Selectors.byTagAndText("button","💾 Save Changes")).click();

            Alert alert = switchTo().alert();

            assertEquals("✅ Name updated successfully!", alert.getText());
            alert.accept();

            GetUserProfileResponse actualName = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                    .get().extract().as(GetUserProfileResponse.class);

            assertThat(newName).isEqualTo(actualName.getName());
    }

    @Test
    public void changeNameWithInvalidWord() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0])", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-info")).click();

        String invalidName = RandomData.getValidName() + "1";

        SelenideElement input = $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible, Condition.enabled);

        input.click();
        input.setValue(invalidName);
        input.clear();
        input.setValue(invalidName);

        input.shouldHave(Condition.exactValue(invalidName));

        $(Selectors.byTagAndText("button", "💾 Save Changes")).click();

        Alert alert = switchTo().alert();

        assertEquals("Name must contain two words with letters only", alert.getText());
        alert.accept();

        GetUserProfileResponse actualName = new CrudRequester(
                RequestSpecs.authAsUser(
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class);

        assertThat(actualName.getName()).isNull();
    }

}
