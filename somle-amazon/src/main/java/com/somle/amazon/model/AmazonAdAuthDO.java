package com.somle.amazon.model;


import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "amazonad_auth")
public class AmazonAdAuthDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;


    private String clientId;

    @Column(columnDefinition="TEXT")
    private String accessToken;

    @Column(columnDefinition="TEXT")
    private String refreshToken;
}
