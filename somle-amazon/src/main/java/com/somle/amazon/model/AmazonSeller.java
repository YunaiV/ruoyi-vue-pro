package com.somle.amazon.model;


import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
// @IdClass(AmazonAuthId.class)
public class AmazonSeller {
    @Id
    private String id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="region_code")
    private AmazonRegion region;

    @Column(columnDefinition="LONGTEXT")
    private String adRefreshToken;
    @Column(columnDefinition="LONGTEXT")
    private String spRefreshToken;
    @Column(columnDefinition="LONGTEXT")
    private String adAccessToken;
    @Column(columnDefinition="LONGTEXT")
    private String spAccessToken;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AmazonShop> shops;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="account_id")
    private AmazonAccount account;
}

// class AmazonAuthId implements Serializable {
//     private String sellerId;

//     private AmazonRegion region;
// }