package io.confluent.developer.streams;

public class BrandContentChangedEvent {
    public String eventName;
    public int brandId;
    public String name;
    public int languageId;
    public String description;
    public Integer imageId;

    // Constructor
    public BrandContentChangedEvent(int brandId, int languageId) {
        this.eventName = "BrandContentChanged";
        this.brandId = brandId;
        this.languageId = languageId;
    }

    // Constructor with Optional Fields
    public BrandContentChangedEvent(int brandId, int languageId, String name, String description, Integer imageId) {
        this(brandId, languageId);
        this.name = name;
        this.description = description;
        this.imageId = imageId;
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
