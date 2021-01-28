package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 短信渠道
 *
 * @author zzf
 * @since 2021-01-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sms_channel", autoResultMap = true)
public class SmsChannelDO extends BaseDO {

    /**
     * 自增编号
     */
    private Long id;

    /**
     * 编码(来自枚举类 阿里、华为、七牛等)
     */
    private String code;

    /**
     * 渠道账号id
     */
    private String apiKey;

    /**
     * 渠道账号秘钥
     */
    private String apiSecret;

    /**
     * 实际渠道签名唯一标识
     */
    private String apiSignatureId;

    /**
     * 名称
     */
    private String name;

    /**
     * 签名值
     */
    private String signature;

    /**
     * 备注
     */
    private String remark;

    /**
     * 启用状态（0正常 1停用）
     */
    private Integer status;

}
