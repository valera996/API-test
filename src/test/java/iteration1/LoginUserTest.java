package iteration1;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;

public class LoginUserTest {

    @Test
    public void adminCanGenerateAuthTokenTest(){
    LoginUserRequest userRequest = LoginUserRequest.builder()
            .username("admin")
            .password("admin")
            .build();
        new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnsOk())
                .post(userRequest);
    }
    @Test
    public void userCanGenerateAuthTokenTest(){

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post( createUserRequest );

        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username(createUserRequest.getUsername())
                .password(createUserRequest.getPassword())
                .build();

        new LoginUserRequester(RequestSpecs.unauthSpec(),ResponseSpecs.requestReturnsOk())
                .post(userRequest)
                .header(RequestSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());
    }

}
