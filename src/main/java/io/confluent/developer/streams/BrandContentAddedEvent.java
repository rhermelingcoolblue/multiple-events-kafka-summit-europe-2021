package io.confluent.developer.streams;

import java.util.Optional;

public class BrandContentAddedEvent {
    public String eventName;
    public int brandId;
    public String name;
    public int languageId;
    public Optional<String> description;
    public Integer imageId;

    // Constructor
    public BrandContentAddedEvent(int brandId, String name, String description, int languageId, Integer imageId) {
        this.eventName = "BrandContentAdded";
        this.brandId = brandId;
        this.name = name;
        this.languageId = languageId;
        this.description = Optional.ofNullable(description);
        this.imageId = imageId;
    }

    // Default Constructor
    public BrandContentAddedEvent() {
    }

    @Override
    public String toString() {
        return "BrandContentAddedEvent{" +
                "eventName='" + eventName + '\'' +
                ", brandId=" + brandId +
                ", name='" + name + '\'' +
                ", languageId=" + languageId +
                ", description='" + description + '\'' +
                ", imageId=" + imageId +
                '}';
    }
}
