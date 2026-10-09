package iteration1;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.GetAccountResponse;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();

        String accountNumber = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class)
                .getAccountNumber();

        //Запросить все аккаунты пользователя и проверить, что созданный аккаунт там
        GetAccountResponse[] getAccountResponse = new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),Endpoint.CUSTOMER_ACCOUNTS, ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(GetAccountResponse[].class);
        softly.assertThat(getAccountResponse[0].getAccountNumber()).isEqualTo(accountNumber);
    }
}
