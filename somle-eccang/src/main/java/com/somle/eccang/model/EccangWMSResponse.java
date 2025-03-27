package com.somle.eccang.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class EccangWMSResponse {
     private String ask;
     private String message;
     //由于易仓wms返回结果的字段不统一，因此使用多个别名映射一个字段
    @JsonAlias({"total", "count"})
    private String total;
     private Pagination pagination;
     private JsonNode data;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private Integer page;
        private Integer pageSize;
    }


    public EccangResponse.EccangPage getEccangPage() {
        EccangResponse.EccangPage eccangPage = new EccangResponse.EccangPage();
        eccangPage.setTotal(Integer.valueOf(this.total));
        eccangPage.setTotalCount(Integer.valueOf(this.total));
        eccangPage.setPageSize(this.pagination.pageSize);
        eccangPage.setPage(this.pagination.page);
        eccangPage.setData(data);
        return eccangPage;
    }
}
