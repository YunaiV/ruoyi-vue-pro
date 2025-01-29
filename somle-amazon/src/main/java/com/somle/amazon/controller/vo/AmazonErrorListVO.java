package com.somle.amazon.controller.vo;
import lombok.Data;

import java.util.List;

@Data
public class AmazonErrorListVO {
    private List<AmazonErrorDetail> errors;

    @Data
    public class AmazonErrorDetail {
        private String code;
        private String message;
        private String details;
    }
}


