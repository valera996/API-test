package api.requests.steps;

import api.Specs.RequestSpecs;
import api.Specs.ResponseSpecs;
import api.models.CreateAccountResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;

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
}
