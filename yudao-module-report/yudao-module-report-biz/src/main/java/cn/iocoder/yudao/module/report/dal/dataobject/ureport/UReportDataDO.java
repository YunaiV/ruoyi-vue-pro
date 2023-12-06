package cn.iocoder.yudao.module.report.dal.dataobject.ureport;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @赤焰：这个是不是可以支持多租户？
/**
 * Ureport2 报表 DO
 *
 * @author 芋道源码
 */
@TableName("report_ureport_data")
@KeySequence("report_ureport_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UReportDataDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link  CommonStatusEnum#getStatus()}
     */
    private Integer status;
    /**
     * 文件内容
     */
    private String content;
    /**
     * 备注
     */
    private String remark;

}
