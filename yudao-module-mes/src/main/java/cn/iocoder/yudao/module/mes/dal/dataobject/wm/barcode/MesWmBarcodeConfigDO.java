package cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 条码配置 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_barcode_config")
@KeySequence("mes_wm_barcode_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmBarcodeConfigDO extends BaseDO {

    /**
     * 内容格式模板占位符
     */
    public static final String PLACEHOLDER_BUSINESS_CODE = "{BUSINESSCODE}";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 条码格式
     *
     * 字典 {@link DictTypeConstants#MES_BARCODE_FORMAT}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.BarcodeFormatEnum}
     */
    private Integer format;
    /**
     * 业务类型
     *
     * 字典 {@link DictTypeConstants#MES_BARCODE_BIZ_TYPE}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 内容格式模板（支持 {BUSINESSCODE} 占位符）
     *
     * @see #PLACEHOLDER_BUSINESS_CODE
     */
    private String contentFormat;
    /**
     * 内容样例
     */
    private String contentExample;
    /**
     * 是否自动生成
     */
    private Boolean autoGenerateFlag;
    /**
     * 默认打印模板
     */
    private String defaultTemplate;
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
