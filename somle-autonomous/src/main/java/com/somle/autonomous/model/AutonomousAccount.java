package com.somle.autonomous.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AutonomousAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    //授权类型
    @JsonProperty(value = "grant_type")
    private String grantType;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "autonomous_token_id")
    private AutonomousAuthToken autonomousAuthToken;
}
