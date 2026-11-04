package iteration1.api;

import api.Specs.RequestSpecs;
import api.Specs.ResponseSpecs;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;

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
