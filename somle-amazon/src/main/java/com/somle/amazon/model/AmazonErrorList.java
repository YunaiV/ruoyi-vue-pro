package com.somle.amazon.model;
import lombok.Data;

import java.util.List;

@Data
public class AmazonErrorList {
    private List<AmazonErrorDetail> errors;

    @Data
    public class AmazonErrorDetail {
        private String code;
        private String message;
        private String details;
    }
}


