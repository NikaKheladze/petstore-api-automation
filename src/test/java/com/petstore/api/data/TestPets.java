package com.petstore.api.data;

import com.petstore.api.model.Category;
import com.petstore.api.model.Pet;
import com.petstore.api.model.Tag;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public final class TestPets {

    private static final long MIN_ID = 1_000_000_000_000L;
    private static final long MAX_ID = 9_000_000_000_000L;
    private static final String PHOTO_URL = "https://example.com/photos/pet.jpg";
    private static final Tag AUTOMATION_TAG = new Tag(1L, "api-automation");

    private TestPets() {
    }

    public static Pet newPet(String name, String categoryName, String status) {
        return new Pet(uniqueId(), new Category(uniqueId(), categoryName), name,
                List.of(PHOTO_URL), List.of(AUTOMATION_TAG), status);
    }

    public static long uniqueId() {
        return ThreadLocalRandom.current().nextLong(MIN_ID, MAX_ID);
    }

    public static String unusedStatus() {
        return "no-such-status-" + UUID.randomUUID();
    }
}
