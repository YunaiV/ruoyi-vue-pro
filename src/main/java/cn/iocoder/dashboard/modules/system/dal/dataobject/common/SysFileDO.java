package cn.iocoder.dashboard.modules.system.dal.dataobject.common;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件表
 *
 * @author 芋道源码
 */
@Data
@TableName("sys_file")
@EqualsAndHashCode(callSuper = true)
public class SysFileDO extends BaseDO {

    /**
     * 文件路径
     */
    @TableId(type = IdType.INPUT)
    private String id;
    /**
     * 文件内容
     */
    private byte[] content;

}
