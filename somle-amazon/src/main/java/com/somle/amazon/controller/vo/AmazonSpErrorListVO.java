package com.somle.amazon.controller.vo;
import lombok.Data;

import java.util.List;

@Data
public class AmazonSpErrorListVO {
    private List<ErrorDetail> errors;

    @Data
    public class ErrorDetail {
        private String code;
        private String message;
        private String details;
    }
}


