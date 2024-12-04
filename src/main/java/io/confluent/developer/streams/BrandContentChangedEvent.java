package io.confluent.developer.streams;

import java.util.Optional;

public class BrandContentChangedEvent {
    public String eventName;
    public int brandId;
    public Optional<String> name;
    public int languageId;
    public Optional<String> description;
    public Optional<Integer> imageId;

    // Constructor
    public BrandContentChangedEvent(int brandId, int languageId) {
        this.eventName = "BrandContentChanged";
        this.brandId = brandId;
        this.languageId = languageId;
    }

    // Constructor with Optional Fields
    public BrandContentChangedEvent(int brandId, int languageId, String name, String description, Integer imageId) {
        this(brandId, languageId);
        this.name = Optional.ofNullable(name);
        this.description = Optional.ofNullable(description);
        this.imageId = Optional.ofNullable(imageId);
    }

    // Default Constructor
    public BrandContentChangedEvent() {
    }

    @Override
    public String toString() {
        return "BrandContentChangedEvent{" +
                "eventName='" + eventName + '\'' +
                ", brandId=" + brandId +
                ", name='" + name + '\'' +
                ", languageId=" + languageId +
                ", description='" + description + '\'' +
                ", imageId=" + imageId +
                '}';
    }
}
