package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferMoneyToAnotherAccountRequest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public class TransferMoneyToAnotherAccountRequester extends Request<TransferMoneyToAnotherAccountRequest> {
    public TransferMoneyToAnotherAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(TransferMoneyToAnotherAccountRequest model) {
        return   given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(TransferMoneyToAnotherAccountRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse put(TransferMoneyToAnotherAccountRequest model) {
        return null;
    }
}
