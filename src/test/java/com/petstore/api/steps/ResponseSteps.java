package com.petstore.api.steps;

import com.petstore.api.context.ScenarioContext;
import com.petstore.api.model.ApiMessage;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ResponseSteps {

    private final ScenarioContext context;

    public ResponseSteps(ScenarioContext context) {
        this.context = context;
    }

    @Then("the response status code should be {int}")
    public void theStatusCodeIs(int expectedStatus) {
        context.response().then().statusCode(expectedStatus);
    }

    @Then("the response content type should be JSON")
    public void theContentTypeIsJson() {
        context.response().then().contentType(ContentType.JSON);
    }

    @Then("the response header {string} should be {string}")
    public void theHeaderIs(String header, String expectedValue) {
        context.response().then().header(header, equalTo(expectedValue));
    }

    @Then("the response body should match the {string} JSON schema")
    public void theBodyMatchesSchema(String schemaName) {
        context.response().then().body(matchesJsonSchemaInClasspath("schemas/" + schemaName + ".json"));
    }

    @Then("the error response should have code {int}, type {string} and message {string}")
    public void theErrorResponseIs(int code, String type, String message) {
        assertThat(context.response().as(ApiMessage.class))
                .as("error response body")
                .isEqualTo(new ApiMessage(code, type, message));
    }

    @Then("the error response code should be {int}")
    public void theErrorCodeIs(int code) {
        context.response().then().body("code", equalTo(code));
    }
}
