package cn.iocoder.yudao.module.mes.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 产品SOP DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_product_sop")
@KeySequence("mes_md_product_sop_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdProductSopDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 物料产品编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 排列顺序
     */
    private Integer sort;
    /**
     * 工序编号
     */
    private Long processId;
    /**
     * 标题
     */
    private String title;
    /**
     * 详细描述
     */
    private String description;
    /**
     * 图片地址
     */
    private String url;
    /**
     * 备注
     */
    private String remark;

}
