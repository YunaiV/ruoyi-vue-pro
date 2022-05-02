package cn.iocoder.yudao.module.infra.dal.dataobject.test;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 字典类型 DO
 *
 * @author 芋道源码
 */
@TableName("infra_test_demo")
@KeySequence("infra_test_demo_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDemoDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 名字
     */
    private String name;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 分类
     */
    private Integer category;
    /**
     * 备注
     */
    private String remark;

}
