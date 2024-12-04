package io.confluent.developer.streams;

public class WarrantyAddedToBrandEvent {
    public String eventName;
    public int brandId;

    // Flattened JSON fields
    public int countryId;
    public int warrantyTypeId;
    public int warrantyTermInMonths;

    // Constructor
    public WarrantyAddedToBrandEvent(
            int brandId,
            int countryId,
            int warrantyTypeId,
            int warrantyTermInMonths) {
        this.eventName = "WarrantyAddedToBrand";
        this.brandId = brandId;
        this.countryId = countryId;
        this.warrantyTypeId = warrantyTypeId;
        this.warrantyTermInMonths = warrantyTermInMonths;
    }
}
