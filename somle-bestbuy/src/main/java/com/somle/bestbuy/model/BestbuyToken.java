package com.somle.bestbuy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Wqh
 * @date: 2024/12/13 13:47
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestbuyToken {
    @Id
    private String token;
}
