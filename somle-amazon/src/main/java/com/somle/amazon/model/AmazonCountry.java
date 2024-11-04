package com.somle.amazon.model;

import com.somle.framework.common.util.date.LocalDateTimeUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonCountry {
    @Id
    private String code;
    private String marketplaceId;
    private String zoneId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="region_code")
    private AmazonRegion region;

    public ZoneId getZoneId() {
        return ZoneId.of(zoneId);
    }

    public LocalDateTime localTime(LocalDateTime systemTime) {
        return LocalDateTimeUtils.leap(systemTime, getZoneId());
    }
}