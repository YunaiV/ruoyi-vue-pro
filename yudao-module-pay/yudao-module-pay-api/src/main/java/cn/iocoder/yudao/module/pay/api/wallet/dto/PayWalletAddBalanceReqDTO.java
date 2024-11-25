package cn.iocoder.yudao.module.pay.api.wallet.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 钱包余额增加 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class PayWalletAddBalanceReqDTO {

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 属性，或者 AdminUserDO 的 id 属性
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     *
     * 关联 {@link  UserTypeEnum}
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 关联业务分类
     */
    @NotNull(message = "关联业务分类不能为空")
    private Integer bizType;
    /**
     * 关联业务编号
     */
    @NotNull(message = "关联业务编号不能为空")
    private String bizId;

    /**
     * 交易金额，单位分
     *
     * 正值表示余额增加，负值表示余额减少
     */
    @NotNull(message = "交易金额不能为空")
    private Integer price;

}
