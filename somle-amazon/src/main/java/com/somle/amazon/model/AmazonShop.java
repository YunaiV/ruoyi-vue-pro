package com.somle.amazon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonShop {

    @Id
    private String profileId;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="seller_id")
    private AmazonSeller seller;


    // @OneToOne(fetch = FetchType.EAGER)
    // @JoinColumns({
    //     @JoinColumn(name = "seller_id", referencedColumnName = "id"),
    //     @JoinColumn(name = "region_code", referencedColumnName = "region_code")
    // })
    // private AmazonAuth seller;


    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="country_code")
    private AmazonCountry country;


}