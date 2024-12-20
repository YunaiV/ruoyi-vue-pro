package com.somle.matomo.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

/**
 * @className: MatomoToken
 * @author: Wqh
 * @date: 2024/12/20 13:14
 * @Version: 1.0
 * @description:
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatomoToken {
    @Id
    private String token;
}
