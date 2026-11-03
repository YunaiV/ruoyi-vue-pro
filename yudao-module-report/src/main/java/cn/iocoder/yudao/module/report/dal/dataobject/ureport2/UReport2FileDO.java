package cn.iocoder.yudao.module.report.dal.dataobject.ureport2;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * UReport2 报表模板 DO
 *
 * @author 芋道源码
 */
@TableName("report_ureport_file")
@KeySequence("report_ureport_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UReport2FileDO extends TenantBaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId
    private Long id;
    /**
     * 模板名称，不包含存储前缀
     *
     * 例如说：demo.ureport.xml
     */
    private String name;
    /**
     * 模板内容，UReport2 的 XML 格式
     */
    private String content;

}
