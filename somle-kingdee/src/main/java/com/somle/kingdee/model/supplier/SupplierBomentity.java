package com.somle.kingdee.model.supplier;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @className: SupplierBomentity
 * @author: Wqh
 * @date: 2024/11/6 9:30
 * @Version: 1.0
 * @description:
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SupplierBomentity {
    // 生日
    private String birthday;

    // 地址
    private String contactAddress;

    // 市-id
    private String contactCityId;

    // 市-名称
    private String contactCityName;

    // 市-编码
    private String contactCityNumber;

    // 国家-id
    private String contactCountryId;

    // 国家-名称
    private String contactCountryName;

    // 国家-编码
    private String contactCountryNumber;

    // 区-id
    private String contactDistrictId;

    // 区-名称
    private String contactDistrictName;

    // 区-编码
    private String contactDistrictNumber;

    // 联系人名称
    private String contactPerson;

    // 省-id
    private String contactProvinceId;

    // 省-名称
    private String contactProvinceName;

    // 省-编码
    private String contactProvinceNumber;

    // 邮箱
    private String email;

    // 性别1-男2-女
    private String gender;

    // 分类编码
    private String groupNumber;

    // 分录id
    private String id;

    // 是否首要联系人
    private Boolean isDefaultLinkman;

    // 手机
    private String mobile;

    // 座机
    private String phone;

    // QQ
    private String qq;

    // 税率
    private String rate;

    // 联系人序号
    private String seq;

    // 微信
    private String wechat;
}
