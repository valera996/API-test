package iteration1;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.GetAccountResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetUserAccountRequester;


public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        String accountNumber = new CreateAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class)
                .getAccountNumber();

        //Запросить все аккаунты пользователя и проверить, что созданный аккаунт там
        GetAccountResponse[] getAccountResponse = new GetUserAccountRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get()
                .extract().as(GetAccountResponse[].class);
        softly.assertThat(getAccountResponse[0].getAccountNumber()).isEqualTo(accountNumber);
    }
}
