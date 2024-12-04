package io.confluent.developer.streams;

public class ProductLineAddedEvent {
    public String eventName;
    public int brandId;
    public String name;
    public int productLineId;

    // Constructor
    public ProductLineAddedEvent(int brandId, String name, int productLineId) {
        this.eventName = "ProductLineAdded";
        this.brandId = brandId;
        this.name = name;
        this.productLineId = productLineId;
    }

    // Default Constructor
    public ProductLineAddedEvent() {
    }

    @Override
    public String toString() {
        return "ProductLineAdded{" +
                "eventName='" + eventName + '\'' +
                ", brandId=" + brandId +
                ", name='" + name + '\'' +
                ", productLineId='" + productLineId + '\'' +
                '}';
    }
}
