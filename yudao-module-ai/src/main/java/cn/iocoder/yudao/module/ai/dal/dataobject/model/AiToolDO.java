package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.tool.function.DirectoryListToolFunction;
import cn.iocoder.yudao.module.ai.tool.function.WeatherQueryToolFunction;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 工具 DO
 *
 * @author 芋道源码
 */
@TableName("ai_tool")
@KeySequence("ai_tool_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolDO extends BaseDO {

    /**
     * 工具编号
     */
    @TableId
    private Long id;
    /**
     * 工具名称
     *
     * 对应 Bean 的名字，例如说：
     * 1. {@link DirectoryListToolFunction} 的 Bean 名字是 directory_list
     * 2. {@link WeatherQueryToolFunction} 的 Bean 名字是 weather_query
     */
    private String name;
    /**
     * 工具描述
     */
    private String description;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}