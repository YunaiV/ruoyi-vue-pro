package cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 条码清单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_barcode")
@KeySequence("mes_wm_barcode_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmBarcodeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 条码配置编号
     *
     * 关联 {@link MesWmBarcodeConfigDO#getId()}
     */
    private Long configId;
    /**
     * 条码格式
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.BarcodeFormatEnum}
     */
    private Integer format;
    /**
     * 业务类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 条码内容（核心字段，前端根据此内容生成条码图片）
     */
    private String content;
    /**
     * 业务编号
     */
    private Long bizId;
    /**
     * 业务编码
     */
    private String bizCode;
    /**
     * 业务名称
     */
    private String bizName;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
