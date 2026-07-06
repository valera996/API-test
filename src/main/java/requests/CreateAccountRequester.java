package requests;


import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.CreateUserRequest;


import static io.restassured.RestAssured.given;

public class CreateAccountRequester extends Request{

    public CreateAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(BaseModel model) {
        // TODO: реализовать метод на беке
        return null;
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        // TODO: реализовать метод на беке
        return null;
    }

}
