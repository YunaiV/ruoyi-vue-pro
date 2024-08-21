package com.somle.amazon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonAccount {
    @Id
    private Integer id;
    private String spClientId;
    private String spClientSecret;
    private String adClientId;
    private String adClientSecret;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AmazonSeller> sellers;
}
