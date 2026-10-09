package iteration2;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;

import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000.00})
    public void depositMoneyWithValidAmountTest(double amount) {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        int accountId = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class)
                .getId();

        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.requestReturnsOk())
                .post(depositMoneyToAccountRequest);

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
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

        CreateUserRequest createUserRequest = AdminSteps.createUser();

        int accountId = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class)
                .getId();

        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.transferRequestReturnsBadRequest(amount, errorValue))
                .post(depositMoneyToAccountRequest);

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();
        softly.assertThat(actualBalance).isEqualTo(0);
    }


    @Test
    public void depositMoneyToAnotherPersonAccountTest() {

        CreateUserRequest createUserRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post(null);

        CreateUserRequest createNewUserRequest = AdminSteps.createUser();

        int newUserAccountId = new CrudRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class)
                .getId();

        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(newUserAccountId)
                .balance(300)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.depositMoneyToAnotherAccount())
                .post(depositMoneyToAccountRequest);

        double actualBalance = new CrudRequester(RequestSpecs.authAsUser(createNewUserRequest.getUsername(), createNewUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get()
                .extract()
                .as(GetUserProfileResponse.class)
                .getAccounts().getFirst().getBalance();

        softly.assertThat(actualBalance).isEqualTo(0);
    }

    @Test
    public void depositMoneyToNonexistentPersonAccountTest() {

        CreateUserRequest createUserRequest = AdminSteps.createUser();

        DepositMoneyToAccountRequest depositMoneyToAccountRequest = DepositMoneyToAccountRequest.builder()
                .id(Integer.MAX_VALUE)
                .balance(300)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), Endpoint.ACCOUNTS_DEPOSIT, ResponseSpecs.depositMoneyToAnotherAccount())
                .post(depositMoneyToAccountRequest);
    }
}
