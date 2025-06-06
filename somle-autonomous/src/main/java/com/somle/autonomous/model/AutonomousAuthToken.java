package com.somle.autonomous.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class AutonomousAuthToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2048)
    @JsonProperty("access_token") // Maps JSON property access_token to accessToken
    private String accessToken;

    @Column(length = 2048)
    @JsonProperty("refresh_token") // Maps JSON property refresh_token to refreshToken
    private String refreshToken;

    @JsonProperty("expire_in") // Maps JSON property expire_in to expiresIn
    private int expiresIn;

    @JsonProperty("first_time") // Maps JSON property first_time to firstTime
    private boolean firstTime;

    @JsonProperty("expire_at") // Maps JSON property expire_at to expireAt
    private long expireAt;

//    @OneToOne(mappedBy = "autonomousAuthToken")
//    private AutonomousAccount autonomousAccount;

    @Override
    public String toString() {
        return "AutonomousAuthToken{" +
            "expireAt=" + expireAt +
            ", firstTime=" + firstTime +
            ", expiresIn=" + expiresIn +
            ", refreshToken='" + refreshToken + '\'' +
            ", accessToken='" + accessToken + '\'' +
            ", id=" + id +
            '}';
    }
}
