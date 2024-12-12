package com.somle.rakuten.model.pojo;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RakutenPaginationReqModel {

    @NotNull
    private Integer requestRecordsAmount;   // 每页请求结果的数量，最大可指定1000
    @NotNull
    private Integer requestPage;            // 请求的页码，从1开始

    private List<RakutenSortModel> rakutenSortModelList;  // 排序模型列表，指定如何对数据进行排序
}
