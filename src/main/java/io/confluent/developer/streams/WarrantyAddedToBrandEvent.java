package io.confluent.developer.streams;

public class WarrantyAddedToBrandEvent {
    public String eventName;
    public int brandId;
    public String timestamp;

    // Flattened JSON fields
    public int countryId;
    public int warrantyTypeId;
    public int warrantyTermInMonths;

    // Constructor
    public WarrantyAddedToBrandEvent(
        int brandId,
        String timestamp,
        int countryId,
        int warrantyTypeId,
        int warrantyTermInMonths
    ) {
        this.eventName = "WarrantyAddedToBrand";
        this.brandId = brandId;
        this.timestamp = timestamp;
        this.countryId = countryId;
        this.warrantyTypeId = warrantyTypeId;
        this.warrantyTermInMonths = warrantyTermInMonths;
    }
}
