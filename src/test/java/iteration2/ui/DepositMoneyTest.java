package iteration2.ui;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import com.codeborne.selenide.*;
import generators.RandomData;
import models.CreateAccountResponse;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest {
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
    public void depositMoneyWithValidAmountTest(){
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        String account = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getAccountNumber();

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

        $(Selectors.byText("💰 Deposit Money")).click();

        $(Selectors.byText(account)).click();

        double amount = RandomData.getValidDepositAmount();

        $(Selectors.byAttribute("placeholder","Enter amount")).setValue(String.valueOf(amount));

        $(Selectors.byText("💵 Deposit")).click();

        Alert alert = switchTo().alert();

        assertEquals("✅ Successfully deposited $" + amount + " to account " + account + "!", alert.getText());
        alert.accept();

        Selenide.open("/transfer");

        $(Selectors.byText("🔁 Transfer Again")).click();

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        assertThat(actualBalance).isEqualTo(amount);
    }

    @Test
    public void depositMoneyWithinSelectedAccount(){
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post();

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

        $(Selectors.byText("💰 Deposit Money")).click();

        double amount = RandomData.getValidDepositAmount();

        $(Selectors.byAttribute("placeholder","Enter amount")).setValue(String.valueOf(amount));

        $(Selectors.byText("💵 Deposit")).click();

        Alert alert = switchTo().alert();

        assertEquals("❌ Please select an account.", alert.getText());
        alert.accept();

        Selenide.open("/transfer");

        $(Selectors.byText("🔁 Transfer Again")).click();

        ElementsCollection allUsersTransactions = $(Selectors.byText("Matching Transactions")).parent().findAll("li");
        allUsersTransactions.findBy(Condition.text("DEPOSIT - $" + String.valueOf(amount))).shouldNotBe(Condition.visible);

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        assertThat(actualBalance).isZero();
    }

    @Test
    public void depositMoneyWithInvalidAmountTest(){
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        String account = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getAccountNumber();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post();

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

        $(Selectors.byText("💰 Deposit Money")).click();

        $(Selectors.byText(account)).click();

        double amount = RandomData.getInvalidDepositAmount();

        $(Selectors.byAttribute("placeholder","Enter amount")).setValue(String.valueOf(amount));

        $(Selectors.byText("💵 Deposit")).click();

        Alert alert = switchTo().alert();

        assertEquals("❌ Please deposit less or equal to 5000$.", alert.getText());
        alert.accept();

        Selenide.open("/transfer");

        $(Selectors.byText("🔁 Transfer Again")).click();

        ElementsCollection allUsersTransactions = $(Selectors.byText("Matching Transactions")).parent().findAll("li");
        allUsersTransactions.shouldBe(CollectionCondition.empty);

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        assertThat(actualBalance).isZero();
    }
}