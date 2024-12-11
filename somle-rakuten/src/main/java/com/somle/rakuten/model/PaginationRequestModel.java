package com.somle.rakuten.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PaginationRequestModel {

    @NotNull
    private Integer requestRecordsAmount;   // 每页请求结果的数量，最大可指定1000
    @NotNull
    private Integer requestPage;            // 请求的页码，从1开始
    private List<SortModel> sortModelList;  // 排序模型列表，指定如何对数据进行排序
}
