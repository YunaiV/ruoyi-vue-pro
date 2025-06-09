package cn.iocoder.yudao.module.tms.controller.admin.common.vo;

import lombok.Data;

@Data
public class TmsCompanyRespVO {

    /**
     * 主体ID
     */
    private Long id;

    /**
     * 主体名称
     */
    private String name;

    /**
     * 公司名称（英文）
     */
    private String nameEn;
    /**
     * 公司简称
     */
    private String abbr;

    /**
     * 公司地址
     */
    private String companyAddress;
    /**
     * 公司地址（英文）
     */
    private String companyAddressEn;
}
