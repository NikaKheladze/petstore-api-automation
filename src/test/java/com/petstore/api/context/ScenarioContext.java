package com.petstore.api.context;

import com.petstore.api.model.Pet;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScenarioContext {

    private Pet pet;
    private Response response;
    private final List<Long> createdPetIds = new ArrayList<>();

    public Pet pet() {
        if (pet == null) {
            throw new IllegalStateException("No pet has been prepared in this scenario; add a 'Given ... pet' step first");
        }
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public Response response() {
        if (response == null) {
            throw new IllegalStateException("No request has been sent in this scenario yet");
        }
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void registerCreatedPet(long petId) {
        createdPetIds.add(petId);
    }

    public List<Long> createdPetIds() {
        return Collections.unmodifiableList(createdPetIds);
    }
}
