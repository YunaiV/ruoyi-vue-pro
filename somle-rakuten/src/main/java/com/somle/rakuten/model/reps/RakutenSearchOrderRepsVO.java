package com.somle.rakuten.model.reps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RakutenSearchOrderRepsVO {

    private List<String> orderNumberList;
    @JsonProperty("MessageModelList")
    private List<MessageModel> MessageModelList;
    @JsonProperty("PaginationResponseModel")
    private PaginationResponseModel PaginationResponseModel;

    @Data
    @NoArgsConstructor
    public static class MessageModel {
        private String messageType;  // 对应JSON的"INFO"类型[2,5](@ref)
        private String messageCode;  // 接口操作码(如ORDER_EXT_API_SEARCH_ORDER_INFO_101)
        private String message;      // 业务提示信息
    }

    @Data
    @NoArgsConstructor
    public static class PaginationResponseModel {
        private Integer totalRecordsAmount; // 总记录数[8](@ref)
        private Integer totalPages;         // 总页数
        private Integer requestPage;        // 当前页码(从1开始)[3,6](@ref)
    }
}
