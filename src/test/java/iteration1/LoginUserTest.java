package iteration1;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;

public class LoginUserTest {

    @Test
    public void adminCanGenerateAuthTokenTest(){
    LoginUserRequest userRequest = LoginUserRequest.builder()
            .username("admin")
            .password("admin")
            .build();
        new ValidatedCrudRequester<LoginUserResponse>(RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(userRequest);
    }
    @Test
    public void userCanGenerateAuthTokenTest(){

        CreateUserRequest createUserRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());
    }

}
