package com.somle.amazon.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */
@AllArgsConstructor
@Getter
public enum AmazonCountry {
    AE("AE","A2VIGQ35RCS4UG", AmazonRegion.EU, "Asia/Dubai"),
    AU("AU","A39IBJ37TRP1C6", AmazonRegion.FE, "Australia/Sydney"),
    BE("BE","AMEN7PMS3EDWL", AmazonRegion.EU, "Europe/Brussels"),
    BR("BR","A2Q3Y263D00KWC", AmazonRegion.NA, "America/Sao_Paulo"),
    CA("CA","A2EUQ1WTGCTBG2", AmazonRegion.NA, "America/Toronto"),
    DE("DE","A1PA6795UKMFR9", AmazonRegion.EU, "Europe/Berlin"),
    EG("EG","ARBP9OOSHTCHU", AmazonRegion.EU, "Africa/Cairo"),
    ES("ES","A1RKKUPIHCS9HS", AmazonRegion.EU, "Europe/Madrid"),
    FR("FR","A13V1IB3VIYZZH", AmazonRegion.EU, "Europe/Paris"),
    IN("IN","A21TJRUUN4KGV", AmazonRegion.EU, "Asia/Kolkata"),
    IT("IT","APJ6JRA9NG5V4", AmazonRegion.EU, "Europe/Rome"),
    JP("JP","A1VC38T7YXB528", AmazonRegion.FE, "Asia/Tokyo"),
    MX("MX","A1AM78C64UM0Y8", AmazonRegion.NA, "America/Mexico_City"),
    NL("NL","A1805IZSGTT6HS", AmazonRegion.EU, "Europe/Amsterdam"),
    PL("PL","A1C3SOZRARQ6R3", AmazonRegion.EU, "Europe/Warsaw"),
    SA("SA","A17E79C6D8DWNP", AmazonRegion.EU, "Asia/Riyadh"),
    SE("SE","A2NODRKZP88ZB9", AmazonRegion.EU, "Europe/Stockholm"),
    SG("SG","A19VAU5U5O7RUS", AmazonRegion.FE, "Asia/Singapore"),
    TR("TR","A33AVAJ2PDY3EV", AmazonRegion.EU, "Europe/Istanbul"),
    UK("UK","A1F83G8C2ARO7P", AmazonRegion.EU, "Europe/London"),
    US("US","ATVPDKIKX0DER", AmazonRegion.NA, "America/Los_Angeles"),
    ZA("ZA","AE08WJ6YKNBMC", AmazonRegion.EU, "Africa/Johannesburg");

    private final String code;
    private final String marketplaceId;
    private final AmazonRegion region;
    private final String zoneId;

    public static AmazonCountry findByCode(String code) {
        return Arrays.stream(values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
    }


}


