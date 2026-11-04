package api.requests.steps;

import api.Specs.RequestSpecs;
import api.Specs.ResponseSpecs;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser(){
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserRequest>(
            RequestSpecs.adminSpec(),
            Endpoint.ADMIN_USER,
            ResponseSpecs.entityWasCreated())
            .post(userRequest);

         return userRequest;
    }

    public static List<CreateUserResponse> getAllUsers(){
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOk()).getAll(CreateUserResponse[].class);
    }
}
