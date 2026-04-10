package cn.iocoder.yudao.module.mes.dal.dataobject.pro.process;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 生产工序内容 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_process_content")
@KeySequence("mes_pro_process_content_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProProcessContentDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 顺序编号
     */
    private Integer sort;
    /**
     * 步骤说明
     */
    private String content;
    /**
     * 辅助设备
     */
    private String device;
    /**
     * 辅助材料
     */
    private String material;
    /**
     * 材料文档 URL
     */
    private String docUrl;
    /**
     * 备注
     */
    private String remark;

}
