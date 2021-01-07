package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.excel.Excel;
import cn.iocoder.dashboard.framework.mybatis.core.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 字典数据表
 *
 * @author ruoyi
 */
@TableName("sys_dict_data")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictDataDO extends BaseDO {

    /**
     * 字典编码
     */
    @TableId
    @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
    private Long dictCode;
    /**
     * 字典排序
     */
    @Excel(name = "字典排序", cellType = Excel.ColumnType.NUMERIC)
    private Integer dictSort;
    /**
     * 字典标签
     */
    @Excel(name = "字典标签")
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100个字符")
    private String dictLabel;
    /**
     * 字典键值
     */
    @Excel(name = "字典键值")
    @NotBlank(message = "字典键值不能为空")
    @Size(max = 100, message = "字典键值长度不能超过100个字符")
    private String dictValue;
    /**
     * 字典类型
     *
     * 外键 {@link SysDictDataDO#getDictType()}
     */
    @Excel(name = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
