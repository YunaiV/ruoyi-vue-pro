package cn.iocoder.dashboard.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体对象
 */
@Data
public class BaseDO implements Serializable {

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最后更新时间
     */
    private Date updateTime;
    /**
     * 创建者 TODO 芋艿：迁移成编号
     */
    private String createBy;
    /**
     * 更新者 TODO 芋艿：迁移成编号
     */
    private String updateBy;
    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;

}
