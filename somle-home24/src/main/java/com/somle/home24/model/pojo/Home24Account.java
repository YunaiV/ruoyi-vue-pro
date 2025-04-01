package com.somle.home24.model.pojo;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "home24_account")
public class Home24Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String apiKey;

    public String description;

    //List<Integer> shopIds,一个账户一个店铺,暂且使用默认的店铺id,不考虑一对多关系
    Integer shopId;

    String shopName;
}
