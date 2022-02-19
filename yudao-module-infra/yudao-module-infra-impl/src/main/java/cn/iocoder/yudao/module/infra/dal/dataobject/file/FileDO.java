package cn.iocoder.yudao.module.infra.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.InputStream;

/**
 * 文件表
 *
 * @author 芋道源码
 */
@Data
@TableName("infra_file")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDO extends BaseDO {

    /**
     * 文件路径
     */
    @TableId(type = IdType.INPUT)
    private String id;
    /**
     * 文件类型
     *
     * 通过 {@link cn.hutool.core.io.FileTypeUtil#getType(InputStream)} 获取
     */
    @TableField(value = "`type`")
    private String type;
    /**
     * 文件内容
     */
    private byte[] content;

}
