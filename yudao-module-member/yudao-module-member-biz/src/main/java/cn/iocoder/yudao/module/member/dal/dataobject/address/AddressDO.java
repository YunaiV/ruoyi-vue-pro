package cn.iocoder.yudao.module.member.dal.dataobject.address;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.member.enums.AddressTypeEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 用户收件地址 DO
 *
 * @author 芋道源码
 */
@TableName("member_address")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
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
     * 地区编码
     */
    private Integer areaCode;
    /**
     * 收件详细地址
     */
    private String detailAddress;
    /**
     * 地址类型
     *
     * 枚举 {@link AddressTypeEnum}
     */
    private Integer type;

}
