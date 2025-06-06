package com.somle.rakuten.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RakutenInventoryReqVO {

    private String manageNumber;

    private String variantId;
}
