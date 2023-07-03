package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 拼团活动 Excel VO
 *
 * @author HUIHUI
 */
@Data
public class CombinationActivityExcelVO {

    @ExcelProperty("活动编号")
    private Long id;

    @ExcelProperty("拼团名称")
    private String name;

    @ExcelProperty("商品 SPU 编号关联 ProductSpuDO 的 id")
    private Long spuId;

    @ExcelProperty("总限购数量")
    private Integer totalLimitCount;

    @ExcelProperty("单次限购数量")
    private Integer singleLimitCount;

    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @ExcelProperty("购买人数")
    private Integer userSize;

    @ExcelProperty("开团组数")
    private Integer totalNum;

    @ExcelProperty("成团组数")
    private Integer successNum;

    @ExcelProperty("参与人数")
    private Integer orderUserCount;

    @ExcelProperty("虚拟成团")
    private Integer virtualGroup;

    @ExcelProperty(value = "活动状态：0开启 1关闭", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("限制时长（小时）")
    private Integer limitDuration;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
