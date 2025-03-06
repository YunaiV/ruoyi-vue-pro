package com.somle.eccang.model;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class EccangWMSResponse {
     private String ask;
     private String message;
     private String total;
     private Pagination pagination;
     private JsonNode data;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pagination {
        private int page;
        private int pageSize = 20;
    }


    public EccangResponse.EccangPage getEccangPage() {
        EccangResponse.EccangPage eccangPage = new EccangResponse.EccangPage();
        eccangPage.setTotal(Integer.valueOf(this.total));
        eccangPage.setTotalCount(Integer.valueOf(this.total));
        eccangPage.setPageSize(Integer.valueOf(this.pagination.pageSize));
        eccangPage.setPage(Integer.valueOf(this.pagination.page));
        eccangPage.setData(data);
        return eccangPage;
    }
}
