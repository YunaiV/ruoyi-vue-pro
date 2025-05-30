package com.somle.manomano.model.req;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OffersInfoReqVO {
    /**
     * 卖家合同ID（必填）
     * Seller Contract ID (mandatory)
     */
    private String sellerContractId;

    private String createdAtStart;

    private String createdAtEnd;

    /**
     * 请求追踪ID（可选）
     * Request tracking ID (optional)
     */
    private String requestId;

    /**
     * SKU过滤列表（逗号分隔，可选）
     * SKU filter list (comma-separated, optional)
     * 示例值: "1234ABC,155SRF"
     */
    private String skus;

    /**
     * 页码（默认1）
     * Page number (default 1)
     */

    private Integer page;

    /**
     * 每页数量（默认10）
     * Items per page (default 10)
     */
    private Integer limit;
}
