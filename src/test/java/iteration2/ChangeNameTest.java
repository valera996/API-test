package iteration2;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;

public class ChangeNameTest extends BaseTest {

    @Test
    public void changeNameWithValidWord(){

        CreateUserRequest createUserRequest = AdminSteps.createUser();

        ChangeUserNameRequest changeUserNameRequest = RandomModelGenerator.generate(ChangeUserNameRequest.class);

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.userNameWasChanged())
                .update(changeUserNameRequest);


        GetUserProfileResponse actualName = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        softly.assertThat(changeUserNameRequest.getName()).isEqualTo(actualName.getName());
    }


    @ParameterizedTest
    @ValueSource(strings = {"Kate1 Smith", "Kate", "Kate Ivanovna Smith"})
    public void changeNameWithInvalidWord(String newName){

        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

       CreateUserResponse createUserResponse = new CrudRequester(RequestSpecs.adminSpec(),Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated())
                .post(createUserRequest).extract().as(CreateUserResponse.class);

        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(newName)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.changeUserNameReturnsBadRequest())
                .update(changeUserNameRequest);


        GetUserProfileResponse actualName = new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOk())
                .get().extract().as(GetUserProfileResponse.class);

        softly.assertThat(createUserResponse.getName()).isEqualTo(actualName.getName());
    }

}
