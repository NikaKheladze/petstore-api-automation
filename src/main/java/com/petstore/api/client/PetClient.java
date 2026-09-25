package com.petstore.api.client;

import com.petstore.api.model.Pet;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class PetClient {

    private static final String PETS = "/pet";
    private static final String PET_BY_ID = "/pet/{petId}";
    private static final String FIND_BY_STATUS = "/pet/findByStatus";
    private static final String PET_ID = "petId";

    private final RequestSpecification spec;

    public PetClient() {
        this(RequestSpecFactory.petstore());
    }

    public PetClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response create(Pet pet) {
        return given().spec(spec).body(pet).when().post(PETS);
    }

    /** Sends an arbitrary (possibly malformed) JSON body to the create endpoint. */
    public Response createWithRawBody(String body) {
        return given().spec(spec).body(body).when().post(PETS);
    }

    /** Accepts any id value so that invalid, non-numeric ids can be tested as well. */
    public Response getById(Object petId) {
        return given().spec(spec).pathParam(PET_ID, petId).when().get(PET_BY_ID);
    }

    public Response update(Pet pet) {
        return given().spec(spec).body(pet).when().put(PETS);
    }

    public Response updateWithForm(Object petId, String name, String status) {
        return given().spec(spec)
                .contentType(ContentType.URLENC)
                .pathParam(PET_ID, petId)
                .formParam("name", name)
                .formParam("status", status)
                .when().post(PET_BY_ID);
    }

    public Response delete(Object petId) {
        return given().spec(spec).pathParam(PET_ID, petId).when().delete(PET_BY_ID);
    }

    public Response findByStatus(String status) {
        return given().spec(spec).queryParam("status", status).when().get(FIND_BY_STATUS);
    }
}
