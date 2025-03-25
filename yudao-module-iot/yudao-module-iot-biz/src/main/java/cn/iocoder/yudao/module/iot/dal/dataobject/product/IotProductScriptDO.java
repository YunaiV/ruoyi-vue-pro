package cn.iocoder.yudao.module.iot.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

// TODO @haohao：类似阿里云的脚本，貌似是一个？这个可以简化么？【微信讨论哈】类似阿里云，貌似是加了个 topic？
/**
 * IoT 产品脚本信息 DO
 *
 * @author 芋道源码
 */
@TableName("iot_product_script")
@KeySequence("iot_product_script_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotProductScriptDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 产品唯一标识符
     */
    private String productKey;
    /**
     * 脚本类型(property_parser=属性解析,event_parser=事件解析,command_encoder=命令编码)
     */
    private String scriptType;
    /**
     * 脚本内容
     */
    private String scriptContent;
    /**
     * 脚本语言
     */
    private String scriptLanguage;
    /**
     * 状态(0=禁用 1=启用)
     */
    private Integer status;
    /**
     * 备注说明
     */
    private String remark;
    /**
     * 最后测试时间
     */
    private LocalDateTime lastTestTime;
    /**
     * 最后测试结果(0=失败 1=成功)
     */
    private Integer lastTestResult;
    /**
     * 脚本版本号
     */
    private Integer version;

}