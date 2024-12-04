package io.confluent.developer.streams;

public class BrandAddedEvent {
    public String eventName;
    public int brandId;
    public String internalName;

    // Constructor
    public BrandAddedEvent(int brandId, String internalName) {
        this.eventName = "BrandAdded";
        this.brandId = brandId;
        this.internalName = internalName;
    }

    // Default Constructor
    public BrandAddedEvent() {
    }

    @Override
    public String toString() {
        return "BrandAddedEvent{" +
                "eventName='" + eventName + '\'' +
                ", brandId=" + brandId +
                ", internalName='" + internalName + '\'' +
                '}';
    }
}
