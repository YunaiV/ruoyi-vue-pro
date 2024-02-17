package cn.iocoder.yudao.module.crm.service.followup.bo;

import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 跟进信息 Create Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmFollowUpCreateReqBO {

    /**
     * 数据类型
     *
     * 枚举 {@link CrmBizTypeEnum}
     */
    @NotNull(message = "数据类型不能为空")
    private Integer bizType;
    /**
     * 数据编号
     *
     * 关联 {@link CrmBizTypeEnum} 对应模块 DO 的 id 字段
     */
    @NotNull(message = "数据编号不能为空")
    private Long bizId;

    /**
     * 跟进类型
     *
     * 关联 {@link DictTypeConstants#CRM_FOLLOW_UP_TYPE} 字典
     */
    @NotNull(message = "跟进类型不能为空")
    private Integer type;
    /**
     * 跟进内容
     */
    @NotEmpty(message = "跟进内容不能为空")
    private String content;
    /**
     * 下次联系时间
     */
    @NotNull(message = "下次联系时间不能为空")
    private LocalDateTime nextTime;

    /**
     * 图片
     */
    private List<String> picUrls;
    /**
     * 附件
     */
    private List<String> fileUrls;

    /**
     * 关联的商机编号数组
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    private List<Long> businessIds;

    /**
     * 关联的联系人编号数组
     *
     * 关联 {@link CrmContactDO#getId()}
     */
    private List<Long> contactIds;

}
