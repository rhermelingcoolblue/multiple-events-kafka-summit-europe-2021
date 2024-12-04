package io.confluent.developer.streams;

public class BrandChangedEvent {
    public String eventName;
    public int brandId;
    public String internalName;

    // Constructor
    public BrandChangedEvent(int brandId, String internalName) {
        this.eventName = "BrandChanged";
        this.brandId = brandId;
        this.internalName = internalName;
    }

    // Default Constructor
    public BrandChangedEvent() {
    }

    @Override
    public String toString() {
        return "BrandChangedEvent{" +
                "eventName='" + eventName + '\'' +
                ", brandId=" + brandId +
                ", internalName='" + internalName + '\'' +
                '}';
    }
}
