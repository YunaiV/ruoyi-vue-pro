package com.wangdian.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WangdianToken {
    private String sid = "smst2";
    @Id
    private String appkey = "smst2-ot";
	private String appsecret = "1b5f382668b9b1c74b3ef8e3e4403315";
}
