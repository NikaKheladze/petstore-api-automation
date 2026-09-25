package com.petstore.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Pet(
        Long id,
        Category category,
        String name,
        List<String> photoUrls,
        List<Tag> tags,
        String status) {

    public Pet withName(String newName) {
        return new Pet(id, category, newName, photoUrls, tags, status);
    }

    public Pet withStatus(String newStatus) {
        return new Pet(id, category, name, photoUrls, tags, newStatus);
    }
}
