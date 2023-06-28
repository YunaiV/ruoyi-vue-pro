package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import lombok.*;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 用户积分记录 Excel VO
 *
 * @author QingX
 */
@Data
public class MemberPointRecordExcelVO {

    @ExcelProperty("自增主键")
    private Long id;

    @ExcelProperty("业务编码")
    private String bizId;

    @ExcelProperty(value = "业务类型", converter = DictConvert.class)
    @DictFormat("biz_type") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String bizType;

    @ExcelProperty("1增加 0扣减")
    private String type;

    @ExcelProperty("积分标题")
    private String title;

    @ExcelProperty("积分描述")
    private String description;

    @ExcelProperty("积分")
    private Integer point;

    @ExcelProperty("变动后的积分")
    private Integer totalPoint;

    @ExcelProperty(value = "状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款） ", converter = DictConvert.class)
    @DictFormat("point_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("用户id")
    private Integer userId;

    @ExcelProperty("冻结时间")
    private LocalDateTime freezingTime;

    @ExcelProperty("解冻时间")
    private LocalDateTime thawingTime;

    @ExcelProperty("发生时间")
    private LocalDateTime createTime;

}
