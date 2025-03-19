package com.somle.eccang.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "eccang_wms_token")
public class EccangWMSToken {

    @Id
    private String appKey;
    private String appToken;
}
