package io.confluent.developer.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.LoggerFactory;

public class BrandEventParser {

    public static Object parse(GenericRecord record) {
        LoggerFactory.getLogger(BrandEventParser.class).info("Parsing record");
        // Ensure the record is not null and has the expected fields
        if (record == null || record.get("event_name") == null) {
            return null;
        }

        // Get the event name
        String eventName = record.get("event_name").toString();

        // Map to the corresponding event class based on the event name
        switch (eventName) {
            case "BrandAdded":
                return parseBrandAddedEvent(record);
            case "BrandChanged":
                return parseBrandChangedEvent(record);
            case "BrandContentAdded":
                return parseBrandContentAddedEvent(record);
            case "BrandContentChanged":
                return parseBrandContentChangedEvent(record);
            case "ProductLineAdded":
                return parseProductLineAddedEvent(record);
            case "WarrantyAddedToBrand":
                return parseWarrantyAddedToBrand(record);
            default:
                // Return null if the event name doesn't match
                return null;
        }
    }

    private static BrandAddedEvent parseBrandAddedEvent(GenericRecord record) {
        try {
            LoggerFactory.getLogger(BrandEventParser.class).info("Parsing BrandAddedEvent record");
            // Validate and extract aggregateId
            Object aggregateIdObj = record.get("aggregate_id");
            if (!(aggregateIdObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Aggregate id not found");
                LoggerFactory.getLogger(BrandEventParser.class).info(aggregateIdObj.getClass().getName());
                return null; // Return null if aggregate_id is invalid
            }
            int brandId = Integer.parseInt(aggregateIdObj.toString());

            // Validate and extract fields (stringified JSON)
            Object fieldsObj = record.get("fields");
            if (!(fieldsObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Fields id not found");
                return null; // Return null if fields is not a string
            }
            String fieldsJson = fieldsObj.toString();

            // Parse fields JSON
            JsonNode fieldsNode = new ObjectMapper().readTree(fieldsJson);
            String internalName = fieldsNode.path("internalName").asText(null);
            if (internalName == null) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Internal name not foudn in fields");
                return null; // Return null if internalName is missing
            }

            // Create and return BrandAddedEvent
            return new BrandAddedEvent(brandId, internalName);

        } catch (Exception e) {
            LoggerFactory.getLogger(BrandEventParser.class).error("Parsing record failed");
            LoggerFactory.getLogger(BrandEventParser.class).error(e.getMessage());
            LoggerFactory.getLogger(BrandEventParser.class).error(e.toString());
            e.printStackTrace(); // Log exception (use proper logging in production)
            return null; // Return null if any exception occurs
        }

    }

    private static BrandChangedEvent parseBrandChangedEvent(GenericRecord record) {
        try {
            LoggerFactory.getLogger(BrandEventParser.class).info("Parsing BranchChangedEvent record");
            // Validate and extract aggregateId
            Object aggregateIdObj = record.get("aggregate_id");
            if (!(aggregateIdObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Aggregate id not found");
                LoggerFactory.getLogger(BrandEventParser.class).info(aggregateIdObj.getClass().getName());
                return null; // Return null if aggregate_id is invalid
            }

            int brandId = Integer.parseInt(aggregateIdObj.toString());

            // Validate and extract fields (stringified JSON)
            Object fieldsObj = record.get("fields");
            if (!(fieldsObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Fields id not found");
                return null; // Return null if fields is not a string
            }
            String fieldsJson = fieldsObj.toString();

            // Parse fields JSON
            JsonNode fieldsNode = new ObjectMapper().readTree(fieldsJson);
            String internalName = fieldsNode.path("internalName").asText(null);
            if (internalName == null) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Internal name not foudn in fields");
                return null; // Return null if internalName is missing
            }

            // Create and return BrandAddedEvent
            return new BrandChangedEvent(brandId, internalName);

        } catch (Exception e) {
            LoggerFactory.getLogger(BrandEventParser.class).error("Parsing record failed");
            LoggerFactory.getLogger(BrandEventParser.class).error(e.getMessage());
            LoggerFactory.getLogger(BrandEventParser.class).error(e.toString());
            e.printStackTrace(); // Log exception (use proper logging in production)
            return null; // Return null if any exception occurs
        }

    }

    private static BrandContentAddedEvent parseBrandContentAddedEvent(GenericRecord record) {
        try {
            LoggerFactory.getLogger(BrandEventParser.class).info("Parsing BrandContentAddedEvent record");
            // Validate and extract aggregateId
            Object aggregateIdObj = record.get("aggregate_id");
            if (!(aggregateIdObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Aggregate id not found");
                LoggerFactory.getLogger(BrandEventParser.class).info(aggregateIdObj.getClass().getName());
                return null; // Return null if aggregate_id is invalid
            }

            int brandId = Integer.parseInt(aggregateIdObj.toString());

            // Validate and extract fields (stringified JSON)
            Object fieldsObj = record.get("fields");
            if (!(fieldsObj instanceof Utf8)) {
                LoggerFactory.getLogger(BrandEventParser.class).info("Fields id not found");
                return null; // Return null if fields is not a string
            }
            String fieldsJson = fieldsObj.toString();

            // Parse fields JSON
            JsonNode fieldsNode = new ObjectMapper().readTree(fieldsJson);

            String name = fieldsNode.path("name").asText(null);
            if (name == null) {
                LoggerFactory.getLogger(BrandEventParser.class).info("name not found in fields");
                return null;
            }

            Integer languageId = fieldsNode.has("languageId") && fieldsNode.get("languageId").isInt()
                    ? fieldsNode.get("languageId").asInt()
                    : null;
            if (languageId == null) {
                LoggerFactory.getLogger(BrandEventParser.class).info("languageId not found in fields or wrong type");
                return null;
            }

            Integer imageId = fieldsNode.has("imageId") && fieldsNode.get("imageId").isInt()
                    ? fieldsNode.get("imageId").asInt()
                    : null;
            if (imageId == null) {
                LoggerFactory.getLogger(BrandEventParser.class).info("imageId not found in fields or wrong type");
                return null;
            }

            String description = fieldsNode.path("description").asText(null);
            return new BrandContentAddedEvent(brandId, name, description, languageId, imageId);
        } catch (Exception e) {
            LoggerFactory.getLogger(BrandEventParser.class).error("Parsing record failed");
            LoggerFactory.getLogger(BrandEventParser.class).error(e.getMessage());
            LoggerFactory.getLogger(BrandEventParser.class).error(e.toString());
            e.printStackTrace(); // Log exception (use proper logging in production)
            return null; // Return null if any exception occurs
        }

    }

    private static BrandContentChangedEvent parseBrandContentChangedEvent(GenericRecord record) {
        if (!isFieldValid(record.get("aggregateId"), Integer.class) ||
                !isFieldValid(record.get("languageId"), Integer.class)) {
            return null;
        }

        int brandId = (Integer) record.get("aggregateId");
        int languageId = (Integer) record.get("languageId");

        BrandContentChangedEvent event = new BrandContentChangedEvent(brandId, languageId);

        if (isFieldValid(record.get("name"), String.class)) {
            event.name = (String) record.get("name");
        }

        return event;

    }

    private static ProductLineAddedEvent parseProductLineAddedEvent(GenericRecord record) {
        if (!isFieldValid(record.get("aggregateId"), Integer.class) ||
                !isFieldValid(record.get("name"), String.class) ||
                !isFieldValid(record.get("productLineId"), Integer.class)) {
            return null;
        }

        int brandId = (Integer) record.get("aggregateId");
        String name = record.get("name").toString();
        int productLineId = (Integer) record.get("productLineId");

        return new ProductLineAddedEvent(brandId, name, productLineId);
    }

    private static WarrantyAddedToBrandEvent parseWarrantyAddedToBrand(GenericRecord record) {
        if (!isFieldValid(record.get("aggregateId"), Integer.class) ||
                !isFieldValid(record.get("timestamp"), String.class) ||
                !isFieldValid(record.get("countryId"), Integer.class) ||
                !isFieldValid(record.get("warrantyTypeId"), Integer.class) ||
                !isFieldValid(record.get("warrantyTermInMonths"), Integer.class)) {
            return null;
        }

        int brandId = (Integer) record.get("aggregateId");
        String timestamp = record.get("timestamp").toString();
        int countryId = (Integer) record.get("countryId");
        int warrantyTypeId = (Integer) record.get("warrantyTypeId");
        int warrantyTermInMonths = (Integer) record.get("warrantyTermInMonths");

        return new WarrantyAddedToBrandEvent(brandId, timestamp, countryId, warrantyTypeId, warrantyTermInMonths);
    }

    private static boolean isFieldValid(Object field, Class<?> expectedType) {
        return field != null && expectedType.isInstance(field);
    }
}
