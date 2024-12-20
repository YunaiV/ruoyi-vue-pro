package com.somle.otto.model.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OttoAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String clientId;
    public String clientSecret;
    //yyyy-mm-dd 到期日期：2025 年 6 月 16 日
    public Date expirationDate;
    public String description;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "token_id")
    private OttoAuthToken oauthToken;


}
