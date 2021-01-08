package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.excel.Excel;
import cn.iocoder.dashboard.framework.mybatis.core.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 字典类型表
 *
 * @author ruoyi
 */
@TableName("sys_dict_type")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictTypeDO extends BaseDO {

    /**
     * 字典主键
     */
    @TableId
    @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
    private Long id;
    /**
     * 字典名称
     */
    @Excel(name = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典类型名称长度不能超过100个字符")
    private String name;
    /**
     * 字典类型
     */
    @TableField("dict_type")
    @Excel(name = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型类型长度不能超过100个字符")
    private String type;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private Integer status;

}
