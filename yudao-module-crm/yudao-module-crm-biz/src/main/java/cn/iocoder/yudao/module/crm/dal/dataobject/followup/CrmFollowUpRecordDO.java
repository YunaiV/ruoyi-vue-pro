package cn.iocoder.yudao.module.crm.dal.dataobject.followup;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跟进记录 DO
 *
 * 用于记录客户、联系人的每一次跟进
 *
 * @author 芋道源码
 */
@TableName(value = "crm_follow_up_record", autoResultMap = true)
@KeySequence("crm_follow_up_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmFollowUpRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 数据类型
     *
     * 枚举 {@link CrmBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 数据编号
     *
     * 关联 {@link CrmBizTypeEnum} 对应模块 DO 的 id 字段
     */
    private Long bizId;

    /**
     * 跟进类型
     *
     * 关联 {@link DictTypeConstants#CRM_FOLLOW_UP_TYPE} 字典
     */
    private Integer type;
    /**
     * 跟进内容
     */
    private String content;
    /**
     * 下次联系时间
     */
    private LocalDateTime nextTime;

    /**
     * 图片
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> picUrls;
    /**
     * 附件
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> fileUrls;

    /**
     * 关联的商机编号数组
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> businessIds;
    /**
     * 关联的联系人编号数组
     *
     * 关联 {@link CrmContactDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> contactIds;


}
