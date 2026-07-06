package iteration2;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.DepositMoneyToAccountRequest;
import models.TransferMoneyToAnotherAccountRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheAnotherOwnerTest(double amount){
        //Создание пользователя и аккаунта
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Депосит 10000 денег на созданный аккаунт, чтобы хватило на перевод (по 5к т.к. ограничение)
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(5000)
                .build();

        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);
        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);

        //Создание нового юзера и аккаунта
        CreateUserRequest createNewUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
                .post(createNewUserRequest);
        int newAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()),ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Перевод денег на чужой аккаунт
        TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(newAccountId)
                .amount(amount)
                .build();
        new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()),ResponseSpecs.successfulTransfer())
                .post(transferMoneyToAnotherAccountRequest);


        //Проверка, что на чужой счёт денеги поступили
        double actualBalanceNewUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");

        assertEquals(amount,actualBalanceNewUser,0.01);

        //Проверка, что с текущего счета деньги списались
        double actualBalance = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");

        softly.assertThat(actualBalance).isCloseTo(10000.00 - amount, within(0.01));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void transferValidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest(double amount){
        //Создание пользователя
      CreateUserRequest createUserRequest = CreateUserRequest.builder()
              .username(RandomData.getUserName())
              .password(RandomData.getUserPassword())
              .role(UserRole.USER.toString())
              .build();

      new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
              .post(createUserRequest);

        //Получение id аккаунта(счёта) юзера
        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");
        int anotherAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Депосит 10000 денег на созданный аккаунт, чтобы хватило на перевод (по 5к т.к. ограничение)
        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(5000)
                .build();
        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);
        new DepositMoneyToAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);


        //Перевод денег между аккаунтами юзера
        TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(anotherAccountId)
                .amount(amount)
                .build();

        new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),ResponseSpecs.successfulTransfer())
                .post(transferMoneyToAnotherAccountRequest);

        //Проверка, что на другой счёт денеги поступили
        double actualBalanceAnotherAccount = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[1].balance");
        assertEquals(amount,actualBalanceAnotherAccount, 0.01);


        //Проверка, что с текущего счета деньги списались
        double actualBalanceFirstAccount = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");
        softly.assertThat(actualBalanceFirstAccount).isCloseTo(10000.00 - amount, within(0.01));
    }

    public static Stream<Arguments> invalidAmount(){
        return Stream.of(
                Arguments.of(-0.01,"Transfer amount must be at least 0.01"),
                Arguments.of(10000.01,"Transfer amount cannot exceed 10000")
        );
    }
    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void transferInvalidAmountFromOneAccountToAnotherAccountWithTheAnotherOwnerTest(double amount, String errorValue){
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Создание нового юзера и аккаунта
   CreateUserRequest createNewUserRequest = CreateUserRequest.builder()
           .username(RandomData.getUserName())
           .password(RandomData.getUserPassword())
           .role(UserRole.USER.toString())
           .build();
   new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
           .post(createNewUserRequest);


        int newUserAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()),ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");


        //Перевод денег на чужой аккаунт
TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
        .senderAccountId(accountId)
        .receiverAccountId(newUserAccountId)
        .amount(amount)
        .build();

        new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.transferRequestReturnsBadRequest(amount, errorValue))
                .post(transferMoneyToAnotherAccountRequest);


        //Проверка, что на чужой счёт денеги не поступили
        double actualBalanceNewUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");

        softly.assertThat(actualBalanceNewUser).isCloseTo(0.00, within(0.01));

        //Проверка, что с текущего счета деньги не списались
        double actualBalanceFirstUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");
        softly.assertThat(actualBalanceFirstUser).isCloseTo(0.00, within(0.01));
    }

    @MethodSource("invalidAmount")
    @ParameterizedTest
    public void transferInvalidAmountFromOneAccountToAnotherAccountWithTheTheSameOwnerTest(double amount, String errorValue){
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //Получение id аккаунта(счёта) юзера
        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");
        int anotherAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Перевод денег между аккаунтами юзера
        TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(anotherAccountId)
                .amount(amount)
                .build();

        new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),ResponseSpecs.transferRequestReturnsBadRequest(amount, errorValue))
                .post(transferMoneyToAnotherAccountRequest);


        //Проверка, что на другой счёт денеги не поступили
        double actualBalanceAnotherAccount = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[1].balance");
        softly.assertThat(actualBalanceAnotherAccount).isCloseTo(0.00, within(0.01));

        //Проверка, что с текущего счета деньги не списались
        double actualBalanceFirstUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");
        softly.assertThat(actualBalanceFirstUser).isCloseTo(0.00, within(0.01));
    }

   @Test
    public void transferFromOneAccountWithoutEnoughBalanceToAnotherAccountWithTheAnotherOwnerTest(){
//Создание пользователя
       CreateUserRequest createUserRequest = CreateUserRequest.builder()
               .username(RandomData.getUserName())
               .password(RandomData.getUserPassword())
               .role(UserRole.USER.toString())
               .build();

       new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
               .post(createUserRequest);

       int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
               .post(null).extract().path("id");

       //Создание нового юзера и аккаунта
       CreateUserRequest createNewUserRequest = CreateUserRequest.builder()
               .username(RandomData.getUserName())
               .password(RandomData.getUserPassword())
               .role(UserRole.USER.toString())
               .build();
       new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
               .post(createNewUserRequest);


       int newUserAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()),ResponseSpecs.entityWasCreated())
               .post(null).extract().path("id");


       //Перевод денег на чужой аккаунт
       TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
               .senderAccountId(accountId)
               .receiverAccountId(newUserAccountId)
               .amount(1000)
               .build();

       new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.invalidTransfer())
               .post(transferMoneyToAnotherAccountRequest);


       //Проверка, что на чужой счёт денеги не поступили
       double actualBalanceNewUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
               .get(null).extract().jsonPath().getDouble("accounts[0].balance");

       softly.assertThat(actualBalanceNewUser).isCloseTo(0.00, within(0.01));

       //Проверка, что с текущего счета деньги не списались
       double actualBalanceFirstUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
               .get(null).extract().jsonPath().getDouble("accounts[0].balance");
       softly.assertThat(actualBalanceFirstUser).isCloseTo(0.00, within(0.01));

    }

    @Test
    public void transferFromOneAccountWithoutEnoughBalanceToAnotherAccountWithTheTheSameOwnerTest(){
        //Создание пользователя
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        //Получение id аккаунта(счёта) юзера
        int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");
        int anotherAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null).extract().path("id");

        //Перевод денег между аккаунтами юзера
        TransferMoneyToAnotherAccountRequest transferMoneyToAnotherAccountRequest = TransferMoneyToAnotherAccountRequest.builder()
                .senderAccountId(accountId)
                .receiverAccountId(anotherAccountId)
                .amount(1000)
                .build();

        new TransferMoneyToAnotherAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),ResponseSpecs.invalidTransfer())
                .post(transferMoneyToAnotherAccountRequest);


        //Проверка, что на другой счёт денеги не поступили
        double actualBalanceAnotherAccount = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[1].balance");
        softly.assertThat(actualBalanceAnotherAccount).isCloseTo(0.00, within(0.01));

        //Проверка, что с текущего счета деньги не списались
        double actualBalanceFirstUser = new GetUserProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().jsonPath().getDouble("accounts[0].balance");
        softly.assertThat(actualBalanceFirstUser).isCloseTo(0.00, within(0.01));
    }
}
