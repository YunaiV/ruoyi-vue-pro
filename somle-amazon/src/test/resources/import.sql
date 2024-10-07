

-- SET NAMES utf8mb4;
-- SET FOREIGN_KEY_CHECKS = 0;
SET REFERENTIAL_INTEGRITY FALSE;



-- ----------------------------
-- Records of amazon_account
-- ----------------------------
INSERT INTO `amazon_account` (`sp_client_id`, `ad_client_id`, `ad_client_secret`, `sp_client_secret`, `id`) VALUES ('spid', 'adid', 'adsec', 'spsec', 1);



-- ----------------------------
-- Records of amazon_country
-- ----------------------------
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('AE', 'A2VIGQ35RCS4UG', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('AU', 'A39IBJ37TRP1C6', 'FE');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('BE', 'AMEN7PMS3EDWL', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('BR', 'A2Q3Y263D00KWC', 'NA');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('CA', 'A2EUQ1WTGCTBG2', 'NA');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('DE', 'A1PA6795UKMFR9', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('EG', 'ARBP9OOSHTCHU', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('ES', 'A1RKKUPIHCS9HS', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('FR', 'A13V1IB3VIYZZH', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('IN', 'A21TJRUUN4KGV', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('IT', 'APJ6JRA9NG5V4', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('JP', 'A1VC38T7YXB528', 'FE');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('MX', 'A1AM78C64UM0Y8', 'NA');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('NL', 'A1805IZSGTT6HS', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('PL', 'A1C3SOZRARQ6R3', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('SA', 'A17E79C6D8DWNP', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('SE', 'A2NODRKZP88ZB9', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('SG', 'A19VAU5U5O7RUS', 'FE');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('TR', 'A33AVAJ2PDY3EV', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('UK', 'A1F83G8C2ARO7P', 'EU');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('US', 'ATVPDKIKX0DER', 'NA');
INSERT INTO `amazon_country` (`code`, `marketplace_id`, `region_code`) VALUES ('ZA', 'AE08WJ6YKNBMC', 'EU');



-- ----------------------------
-- Records of amazon_region
-- ----------------------------
INSERT INTO `amazon_region` (`code`, `ad_end_point`, `sp_end_point`) VALUES ('EU', 'https://advertising-api-eu.amazon.com', 'https://sellingpartnerapi-eu.amazon.com');
INSERT INTO `amazon_region` (`code`, `ad_end_point`, `sp_end_point`) VALUES ('FE', 'https://advertising-api-fe.amazon.com', 'https://sellingpartnerapi-fe.amazon.com');
INSERT INTO `amazon_region` (`code`, `ad_end_point`, `sp_end_point`) VALUES ('NA', 'https://advertising-api.amazon.com', 'https://sellingpartnerapi-na.amazon.com');



-- ----------------------------
-- Records of amazon_seller
-- ----------------------------
INSERT INTO `amazon_seller` (`id`, `ad_access_token`, `ad_refresh_token`, `sp_access_token`, `sp_refresh_token`, `region_code`, `account_id`) VALUES ('A1IRXLRDFT847J', 'adaccess', 'adrefresh', 'spaccess', 'sprefresh', 'EU', 1);
INSERT INTO `amazon_seller` (`id`, `ad_access_token`, `ad_refresh_token`, `sp_access_token`, `sp_refresh_token`, `region_code`, `account_id`) VALUES ('A264T81AP1XSP3', 'adaccess', 'adrefresh', 'spaccess', 'sprefresh', 'FE', 1);
INSERT INTO `amazon_seller` (`id`, `ad_access_token`, `ad_refresh_token`, `sp_access_token`, `sp_refresh_token`, `region_code`, `account_id`) VALUES ('A2T4RI5JPP2USG', 'adaccess', 'adrefresh', 'spaccess', 'sprefresh', 'NA', 1);



-- ----------------------------
-- Records of amazon_shop
-- ----------------------------
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('DE', '1550061378277601', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('FR', '79600241658251', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('IT', '3500035971481865', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('NL', '2769129935802806', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('PL', '1090043889947136', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('SE', '1978628245199896', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('TR', '3845654133398847', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('UK', '815148712718788', 'A1IRXLRDFT847J');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('JP', '1783908340437520', 'A264T81AP1XSP3');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('CA', '4306702640475887', 'A2T4RI5JPP2USG');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('MX', '4071896250495704', 'A2T4RI5JPP2USG');
INSERT INTO `amazon_shop` (`country_code`, `profile_id`, `seller_id`) VALUES ('US', '3199368120306884', 'A2T4RI5JPP2USG');


SET FOREIGN_KEY_CHECKS = 1;
