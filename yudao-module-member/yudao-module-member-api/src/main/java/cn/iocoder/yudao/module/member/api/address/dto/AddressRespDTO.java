package cn.iocoder.yudao.module.member.api.address.dto;

import lombok.Data;

/**
 * 用户收件地址 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class AddressRespDTO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 收件人名称
     */
    private String name;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 地区编号
     */
    private Long areaId;
    /**
     * 邮编
     */
    private String postCode;
    /**
     * 收件详细地址
     */
    private String detailAddress;
    /**
     * 是否默认
     *
     * true - 默认收件地址
     */
    private Boolean defaulted;

}
