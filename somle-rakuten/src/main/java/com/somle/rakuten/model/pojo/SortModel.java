package com.somle.rakuten.model.pojo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SortModel {

    @NotNull
    private Integer sortColumn;             // 排序字段，指定按什么字段排序（如：1: 订单日期）
    @NotNull
    private Integer sortDirection;          // 排序方向，1表示升序（最小到最大），2表示降序（最大到最小）
}
