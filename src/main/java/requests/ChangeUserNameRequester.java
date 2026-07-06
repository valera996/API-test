package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;


import static io.restassured.RestAssured.given;

public class ChangeUserNameRequester extends Request<BaseModel>{

    public ChangeUserNameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse get(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
