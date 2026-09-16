package iteration2;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositMoneyToAccountRequester;
import requests.GetUserProfileRequester;
import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000.00})
    public void depositMoneyWithValidAmountTest(double amount) {
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);


        //Получение id аккаунта(счёта) юзера

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getId();


        //Депосит денег на созданный аккаунт
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);

        //Проверка поступления денег на счёт
        double actualBalance = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        softly.assertThat(actualBalance).isEqualTo(amount);
    }

    public static Stream<Arguments> invalidAmount() {
        return Stream.of(
                Arguments.of(-0.01, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000")
        );
    }

    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void depositMoneyWithInvalidAmountTest(double amount, String errorValue) {
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //Получение id аккаунта(счёта) юзера
        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getId();


        //Депосит денег на созданный аккаунт
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.transferRequestReturnsBadRequest(amount, errorValue))
                .post(depositMoneyToAccountRequest);

        //Проверка, что на счёт денеги не поступили
        double actualBalance = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();
        softly.assertThat(actualBalance).isEqualTo(0);
    }


    @Test
    public void depositMoneyToAnotherPersonAccountTest() {
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post();


        //Создание нового пользователя и аккаунта
        CreateUserRequest createNewUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createNewUserRequest);

        int newUserAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getId();

        //Депосит денег на чужой аккаунт
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(newUserAccountId)
                .balance(300)
                .build();

        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.depositMoneyToAnotherAccount())
                .post(depositMoneyToAccountRequest);

        //Проверка, что на чужой счёт денеги не поступили
        double actualBalance = new GetUserProfileRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        softly.assertThat(actualBalance).isEqualTo(0);
    }

    @Test
    public void depositMoneyToNonexistentPersonAccountTest() {
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //Депосит денег на несуществующий аккаунт
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(Integer.MAX_VALUE)
                .balance(300)
                .build();

        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.depositMoneyToAnotherAccount())
                .post(depositMoneyToAccountRequest);
    }
}
