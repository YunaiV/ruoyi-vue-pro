package com.somle.kingdee.model.supplier;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * 金蝶供应商saveVO
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeSupplierSaveVO {
    // id
    private String id;

    // 开户地址
    private String accountOpenAddr;

    // 开户银行
    private String bank;

    // 银行账号
    private String bankAccount;

    // 联系人信息列表
    private List<SupplierBomentity> bomEntity;

    // 分类id
    private String groupId;

    // 分类名称
    private String groupName;

    // 分类编码
    private String groupNumber;

    // 开票名称
    private String invoiceName;

    // 名称 必填
    @NotNull(message = "供应商名称不能为空")
    private String name;

    // 编码 必填
    @NotNull(message = "供应商编码不能为空")
    private String number;

    // 税率
    private String rate;

    // 开票税号
    private String taxpayerNo;

    //详细地址
    private String addr;

    //市
    private String cityId;

    //国家/地区
    private String countryId;

    //自定义字段
    private Map<String,Object> customField;

    //区
    private String districtId;

    //是否忽略告警信息(如：单价为0)保存，true:忽略，默认false
    private Boolean ignoreWarn;

    //省
    private String provinceId;

    //备注
    private String remark;

    //部门id
    private String saleDeptId;

    //部门编码
    private String saleDeptNumber;

    //采购员id
    private String saleId;

    //采购员编码
    private String saleNumber;

}
