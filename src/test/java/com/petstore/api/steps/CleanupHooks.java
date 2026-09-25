package com.petstore.api.steps;

import com.petstore.api.client.PetClient;
import com.petstore.api.context.ScenarioContext;
import io.cucumber.java.After;

public class CleanupHooks {

    private final PetClient petClient;
    private final ScenarioContext context;

    public CleanupHooks(PetClient petClient, ScenarioContext context) {
        this.petClient = petClient;
        this.context = context;
    }

    @After
    public void deleteCreatedPets() {
        context.createdPetIds().forEach(petClient::delete);
    }
}
