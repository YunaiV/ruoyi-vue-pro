package com.somle.amazon.model;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
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

    @Transient
    private LocalDateTime spExpireTime;
    @Transient
    private LocalDateTime adExpireTime;

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