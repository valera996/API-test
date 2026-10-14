package requests.steps;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;

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
}
