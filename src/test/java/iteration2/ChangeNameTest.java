package iteration2;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.ChangeUserNameRequester;


public class ChangeNameTest extends BaseTest {

    @Test
    public void changeNameWithValidWord(){

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);


        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(RandomData.getDoubleName())
                .build();

        new ChangeUserNameRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.userNameWasChanged())
                .put(changeUserNameRequest);


        GetUserProfileResponse actualName = new ChangeUserNameRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().as(GetUserProfileResponse.class);

        softly.assertThat(changeUserNameRequest.getName()).isEqualTo(actualName.getName());
    }


    @ParameterizedTest
    @ValueSource(strings = {"Kate1 Smith", "Kate", "Kate Ivanovna Smith"})
    public void changeNameWithInvalidWord(String newName){

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

       CreateUserResponse createUserResponse= new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest).extract().as(CreateUserResponse.class);

        ChangeUserNameRequest changeUserNameRequest = ChangeUserNameRequest.builder()
                .name(newName)
                .build();

        new ChangeUserNameRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.changeUserNameReturnsBadRequest())
                .put(changeUserNameRequest);


        GetUserProfileResponse actualName = new ChangeUserNameRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(),createUserRequest.getPassword()), ResponseSpecs.requestReturnsOk())
                .get(null).extract().as(GetUserProfileResponse.class);

        softly.assertThat(createUserResponse.getName()).isEqualTo(actualName.getName());
    }

}
