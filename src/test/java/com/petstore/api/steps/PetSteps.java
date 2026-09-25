package com.petstore.api.steps;

import com.petstore.api.client.PetClient;
import com.petstore.api.context.ScenarioContext;
import com.petstore.api.data.TestPets;
import com.petstore.api.model.Pet;
import com.petstore.api.support.Eventually;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;

public class PetSteps {

    private static final int OK = 200;
    private static final int NOT_FOUND = 404;

    private final PetClient petClient;
    private final ScenarioContext context;

    public PetSteps(PetClient petClient, ScenarioContext context) {
        this.petClient = petClient;
        this.context = context;
    }

    @Given("a new pet named {string} in category {string} with status {string}")
    public void aNewPet(String name, String category, String status) {
        context.setPet(TestPets.newPet(name, category, status));
    }

    @Given("an existing pet named {string} in category {string} with status {string}")
    public void anExistingPet(String name, String category, String status) {
        aNewPet(name, category, status);
        Response created = createPet(context.pet());
        assertThat(created.statusCode()).as("HTTP status when creating the test pet").isEqualTo(OK);
        Eventually.responseWithStatus(() -> petClient.getById(context.pet().id()), OK,
                "the new pet to become readable");
    }

    @Given("a pet id that does not exist in the store")
    public void aPetThatDoesNotExist() {
        context.setPet(TestPets.newPet("Ghost", "None", "available"));
    }

    @When("I add the pet to the store")
    public void iAddThePet() {
        context.setResponse(createPet(context.pet()));
    }

    @When("I add a pet with the request body:")
    public void iAddAPetWithBody(String rawBody) {
        context.setResponse(petClient.createWithRawBody(rawBody));
    }

    @When("I retrieve the pet by its id")
    public void iRetrieveThePet() {
        context.setResponse(petClient.getById(context.pet().id()));
    }

    @When("I retrieve the pet with id {string}")
    public void iRetrieveThePetWithId(String rawId) {
        context.setResponse(petClient.getById(rawId));
    }

    @When("I update the pet with name {string} and status {string}")
    public void iUpdateThePet(String name, String status) {
        context.setPet(context.pet().withName(name).withStatus(status));
        context.setResponse(petClient.update(context.pet()));
    }

    @When("I update the pet via form with name {string} and status {string}")
    public void iUpdateThePetViaForm(String name, String status) {
        Pet pet = context.pet();
        context.setPet(pet.withName(name).withStatus(status));
        context.setResponse(petClient.updateWithForm(pet.id(), name, status));
    }

    @When("I delete the pet")
    public void iDeleteThePet() {
        context.setResponse(petClient.delete(context.pet().id()));
    }

    @When("I search for pets with status {string}")
    public void iSearchByStatus(String status) {
        context.setResponse(petClient.findByStatus(status));
    }

    @When("I search for pets with a status that no pet has")
    public void iSearchByUnusedStatus() {
        context.setResponse(petClient.findByStatus(TestPets.unusedStatus()));
    }

    @Then("the response body should describe the pet")
    public void theResponseDescribesThePet() {
        assertThat(context.response().as(Pet.class))
                .as("pet returned by the API")
                .usingRecursiveComparison()
                .isEqualTo(context.pet());
    }

    @Then("the pet should be retrievable by its id")
    public void thePetIsRetrievable() {
        Response response = Eventually.responseWithStatus(() -> petClient.getById(context.pet().id()), OK,
                "the pet to be retrievable by id");
        assertThat(response.as(Pet.class))
                .as("pet read back from the API")
                .usingRecursiveComparison()
                .isEqualTo(context.pet());
    }

    @Then("retrieving the pet should return name {string} and status {string}")
    public void retrievingThePetReturns(String name, String status) {
        Response response = Eventually.response(
                () -> petClient.getById(context.pet().id()),
                r -> r.statusCode() == OK && name.equals(r.path("name")) && status.equals(r.path("status")),
                "the pet to have name '" + name + "' and status '" + status + "'");
        Pet pet = response.as(Pet.class);
        assertThat(pet.name()).as("pet name").isEqualTo(name);
        assertThat(pet.status()).as("pet status").isEqualTo(status);
    }

    @Then("the pet should no longer be retrievable")
    public void thePetIsNoLongerRetrievable() {
        Eventually.responseWithStatus(() -> petClient.getById(context.pet().id()), NOT_FOUND,
                "the deleted pet to disappear");
    }

    @Then("the response message should be the pet id")
    public void theMessageIsThePetId() {
        context.response().then().body("message", equalTo(String.valueOf(context.pet().id())));
    }

    @Then("every pet in the response should have status {string}")
    public void everyPetHasStatus(String status) {
        context.response().then()
                .body("$", not(empty()))
                .body("status", everyItem(equalTo(status)));
    }

    @Then("the search results for status {string} should include the pet")
    public void theSearchResultsIncludeThePet(String status) {
        long petId = context.pet().id();
        Response response = Eventually.response(
                () -> petClient.findByStatus(status),
                r -> r.statusCode() == OK && r.jsonPath().getList("id", Long.class).contains(petId),
                "pet " + petId + " to appear in the '" + status + "' search results");
        assertThat(response.jsonPath().getList("id", Long.class)).as("ids of pets found").contains(petId);
    }

    @Then("the response should be an empty list")
    public void theResponseIsAnEmptyList() {
        List<Object> pets = context.response().jsonPath().getList("$");
        assertThat(pets).as("pets found").isEmpty();
    }

    private Response createPet(Pet pet) {
        context.registerCreatedPet(pet.id());
        return petClient.create(pet);
    }
}
