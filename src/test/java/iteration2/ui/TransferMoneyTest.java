package iteration2.ui;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import com.codeborne.selenide.*;
import generators.RandomData;
import models.*;
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

public class TransferMoneyTest {
    @BeforeAll
    public static void setupSelenoid(){
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.43:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true,
                "enableLog", true));
    }
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;

    @Test
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(account.getId())
                .balance(MAX_DEPOSIT_AMOUNT)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);

        CreateAccountResponse recipientAcc  = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

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

        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(createUserRequest.getUsername());

        $(Selectors.byAttribute("placeholder","Enter recipient account number")).setValue(recipientAcc.getAccountNumber());

        double transferAmount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        $(Selectors.byAttribute("placeholder","Enter amount")).setValue(String.valueOf(transferAmount));

        $("#confirmCheck").click();

        $(Selectors.byTagAndText("button","🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();

        assertEquals("✅ Successfully transferred $" + transferAmount + " to account " + recipientAcc.getAccountNumber() + "!", alert.getText());
        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        double senderBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double recipientBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT - transferAmount);
        assertThat(recipientBalance).isEqualTo(transferAmount);
    }

    @Test
    public void transferWithoutSenderAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.requestReturnsOk())
                .post(DepositMoneyToAccountRequest.builder()
                        .id(account.getId())
                        .balance(MAX_DEPOSIT_AMOUNT)
                        .build());

        CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        String authHeader = new CrudRequester(
                RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authHeader);
        Selenide.open("/dashboard");

        $(Selectors.byText("🔄 Make a Transfer")).click();

        // Счёт отправителя НЕ выбираем
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(createUserRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(recipientAcc.getAccountNumber());

        double amount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $("#confirmCheck").click();
        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertEquals("❌ Please fill all fields and confirm.", alert.getText());
        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        double senderBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double recipientBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();
        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferWithoutRecipientAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.requestReturnsOk())
                .post(DepositMoneyToAccountRequest.builder()
                        .id(account.getId())
                        .balance(MAX_DEPOSIT_AMOUNT)
                        .build());

       CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        String authHeader = new CrudRequester(
                RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authHeader);
        Selenide.open("/dashboard");

        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue(createUserRequest.getUsername());

        // Номер счёта получателя оставляем пустым
        double amount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $("#confirmCheck").click();
        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertEquals("❌ Please fill all fields and confirm.", alert.getText());
        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        double senderBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double recipientBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferAmountAboveMaximumTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

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

        executeJavaScript(
                "localStorage.setItem('authToken', arguments[0])",
                userAuthHeader
        );

        Selenide.open("/dashboard");

        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(createUserRequest.getUsername());

        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(recipientAcc.getAccountNumber());

        double amount = RandomData.getInvalidTransferAmountAboveMax();

        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));

        $("#confirmCheck").click();

        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();

        assertEquals("❌ Error: Transfer amount cannot exceed 10000", alert.getText());

        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class);

        double firstAccountBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double secondAccountBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    public void transferAmountBelowMinimumTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

        CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);

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

        $(Selectors.byText("🔄 Make a Transfer")).click();

        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue(createUserRequest.getUsername());

        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(recipientAcc.getAccountNumber());

        double amount = RandomData.getInvalidTransferAmountBelowMin();

        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(amount));

        $("#confirmCheck").click();

        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();

        assertEquals("❌ Error: Transfer amount must be at least 0.01", alert.getText());

        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class);

        double firstAccountBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double secondAccountBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(firstAccountBalance).isZero();
        assertThat(secondAccountBalance).isZero();
    }

    @Test
    public void transferWithoutConfirmationTest() {
        CreateUserRequest user = AdminSteps.createUser();

        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.requestReturnsOk())
                .post(DepositMoneyToAccountRequest.builder()
                        .id(account.getId())
                        .balance(MAX_DEPOSIT_AMOUNT)
                        .build());

        CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        String authHeader = new CrudRequester(
                RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build())
                .extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authHeader);
        Selenide.open("/dashboard");

        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(recipientAcc.getAccountNumber());

        double amount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(amount));

        // Чек-бокс специально НЕ активируем
        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertEquals("❌ Please fill all fields and confirm.", alert.getText());
        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        double senderBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double recipientBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(senderBalance).isEqualTo(MAX_DEPOSIT_AMOUNT);
        assertThat(recipientBalance).isZero();
    }

    @Test
    public void transferWithoutEnoughBalanceTest() {
        CreateUserRequest user = AdminSteps.createUser();

        // Создаём два счёта, но ничего не пополняем
        CreateAccountResponse account = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        CreateAccountResponse recipientAcc = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post().extract().as(CreateAccountResponse.class);

        String authHeader = new CrudRequester(
                RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .build())
                .extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0])", authHeader);
        Selenide.open("/dashboard");

        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText(account.getAccountNumber())).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(recipientAcc.getAccountNumber());

        double amount = RandomData.getRandomAmount(0.01, MAX_DEPOSIT_AMOUNT);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(amount));

        $("#confirmCheck").click();
        $(Selectors.byTagAndText("button", "🚀 Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertEquals("❌ Error: Invalid transfer: insufficient funds or invalid accounts", alert.getText());
        alert.accept();

        GetUserProfileResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        double senderBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        double recipientBalance = profile.getAccounts().stream()
                .filter(acc -> acc.getId() == recipientAcc.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        assertThat(profile.getAccounts().getFirst().getBalance()).isZero();
        assertThat(profile.getAccounts().get(1).getBalance()).isZero();
    }
}
