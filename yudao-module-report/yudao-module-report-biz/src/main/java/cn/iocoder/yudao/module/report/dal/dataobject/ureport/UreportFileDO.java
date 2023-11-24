package cn.iocoder.yudao.module.report.dal.dataobject.ureport;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Ureport2报表 DO
 *
 * @author 芋道源码
 */
@TableName("ureport_file")
@KeySequence("ureport_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UreportFileDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    // TODO @赤焰：是不是用 name 就可以了。
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 状态
     */
    private Integer status;
    // TODO @赤焰：是不是用 string 就可以了。然后字段名用 content？
    /**
     * 文件内容
     */
    private byte[] fileContent;
    /**
     * 备注
     */
    private String remark;

}
