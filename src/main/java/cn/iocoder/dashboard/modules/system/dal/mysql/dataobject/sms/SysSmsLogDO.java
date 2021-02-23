package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信日志
 *
 * @author zzf
 * @since 2021-01-25
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName(value = "sms_log", autoResultMap = true)
public class SysSmsLogDO implements Serializable {

    /**
     * 自增编号
     */
    private Long id;

    /**
     * 短信渠道编码(来自枚举类)
     */
    private String channelCode;

    /**
     * 短信渠道id
     */
    private Long channelId;

    /**
     * 模板id
     */
    private String templateCode;

    /**
     * 手机号(数组json字符串)
     */
    private String phones;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发送状态（1异步推送中 2发送中 3失败 4成功）
     */
    private Integer sendStatus;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

}
