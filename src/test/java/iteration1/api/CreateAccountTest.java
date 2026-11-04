package iteration1.api;

import api.Specs.RequestSpecs;
import api.Specs.ResponseSpecs;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.GetAccountResponse;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;

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
