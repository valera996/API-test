package iteration1;

import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest{

    @Test
    public void adminCanCreateUserWithCorrectData() {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                        .post( createUserRequest ).extract().as(CreateUserResponse.class);

                softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
                softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
                softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());

    }

    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                Arguments.of("   ","Kate2000#","USER","username","Username cannot be blank"),
                Arguments.of("Ka","Kate2000#","USER","username","Username must be between 3 and 15 characters"),
                Arguments.of("Kat$","Kate2000#","USER","username","Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }
    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post( createUserRequest );
    }
}
