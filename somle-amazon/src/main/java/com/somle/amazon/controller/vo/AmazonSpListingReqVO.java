package com.somle.amazon.controller.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/15$
 */
@Data
@Builder
public class AmazonSpListingReqVO {

    // Path Parameter
    private String sellerId; // Required: A selling partner identifier.

    // Query Parameters
    private List<String> marketplaceIds; // Required: A comma-delimited list of Amazon marketplace identifiers.

    private String issueLocale; // Optional: A locale for localizing issues.

    private List<IncludedData> includedData = List.of(IncludedData.SUMMARIES); // Optional: Default: summaries.

    private List<String> identifiers; // Optional: A comma-delimited list of product identifiers.

    private IdentifiersType identifiersType; // Optional: Type of product identifiers.

    private String variationParentSku; // Optional: SKU of variation parent.

    private String packageHierarchySku; // Optional: SKU for package hierarchy.

    private LocalDateTime createdAfter; // Optional: Filter items created at or after this time.

    private LocalDateTime createdBefore; // Optional: Filter items created at or before this time.

    private LocalDateTime lastUpdatedAfter; // Optional: Filter items updated at or after this time.

    private LocalDateTime lastUpdatedBefore; // Optional: Filter items updated at or before this time.

    private List<WithIssueSeverity> withIssueSeverity; // Optional: Filter items with specified issue severities.

    private List<WithStatus> withStatus; // Optional: Filter items with specified statuses.

    private List<WithoutStatus> withoutStatus; // Optional: Filter items without specified statuses.

    private SortBy sortBy = SortBy.LAST_UPDATED_DATE; // Optional: Default: "lastUpdatedDate".

    private SortOrder sortOrder = SortOrder.DESC; // Optional: Default: "DESC".

    private Integer pageSize = 10; // Optional: Default: 10. Maximum: 20.

    private String pageToken; // Optional: Token for pagination.

    // Enums for specific fields
    public enum IncludedData {
        SUMMARIES,
        ATTRIBUTES,
        ISSUES,
        OFFERS,
        FULFILLMENT_AVAILABILITY,
        PROCUREMENT,
        RELATIONSHIPS,
        PRODUCT_TYPES
    }

    public String getIncludedData() {
        StringBuilder builder = new StringBuilder();
        for (IncludedData includedDatum : includedData) {
            if ("FULFILLMENT_AVAILABILITY".equals(includedDatum.name())) {
                builder.append("fulfillmentAvailability,");
                continue;
            }
            if ("PRODUCT_TYPES".equals(includedDatum.name())) {
                builder.append("productTypes,");
                continue;
            }
            builder.append(includedDatum.name().toLowerCase() + ",");
        }
        return builder.toString();
    }

    public enum IdentifiersType {
        ASIN, SKU, UPC
    }

    public enum WithIssueSeverity {
        LOW, MEDIUM, HIGH
    }

    public enum WithStatus {
        ACTIVE, INACTIVE
    }

    public enum WithoutStatus {
        DELETED, BLOCKED
    }

    public enum SortBy {
        LAST_UPDATED_DATE, CREATED_DATE
    }

    public enum SortOrder {
        ASC, DESC
    }
}

