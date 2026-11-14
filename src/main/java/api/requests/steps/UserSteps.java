package api.requests.steps;

import api.Specs.RequestSpecs;
import api.Specs.ResponseSpecs;
import api.models.CreateAccountResponse;
import api.models.DepositMoneyToAccountRequest;
import api.models.GetUserProfileResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import io.restassured.response.ValidatableResponse;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password){
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccount(){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username,password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOk()).getAll(CreateAccountResponse[].class);
    }

    public GetUserProfileResponse getUserProfile(){
        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);
    }
    public double getBalance(String accountNumber){
        return getUserProfile().getAccounts().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow()
                .getBalance();
    }
    public CreateAccountResponse createAccount(){
        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        )
                .post()
                .extract()
                .as(CreateAccountResponse.class);
    }
    public ValidatableResponse depositMoney(int accountId, double amount) {
        DepositMoneyToAccountRequest depositMoneyToAccountRequest =
                DepositMoneyToAccountRequest.builder()
                        .id(accountId)
                        .balance(amount)
                        .build();

        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOk()
        )
                .post(depositMoneyToAccountRequest);
    }
}
