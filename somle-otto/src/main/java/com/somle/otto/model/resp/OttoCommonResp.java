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
public class OttoCommonResp<T> {
    public List<T> resources;
    public List<OttoLinks> links;

    @Data
    public static class OttoLinks {
        private String href;
        private String rel;
    }
}