package com.somle.otto.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OttoReceiptsResp {
    private List<OttoSalesOrder> resources;
    private Links links;

    @Data
    public static class Links {
        private String href;
        private String rel;
    }
}