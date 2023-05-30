package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryPickUpStoreExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("门店名称")
    private String name;

    @ExcelProperty("门店简介")
    private String introduction;

    @ExcelProperty("门店手机")
    private String phone;

    @ExcelProperty("门店所在区域")
    private String areaName;

    @ExcelProperty("门店详细地址")
    private String detailAddress;

    @ExcelProperty("门店logo")
    private String logo;

    /**
     * easy-excel 好像暂时不支持 LocalTime. 转成string
     */
    @ExcelProperty("营业开始时间")
    private String openingTime;

    @ExcelProperty("营业结束时间")
    private String closingTime;

    @ExcelProperty("纬度")
    private String latitude;

    @ExcelProperty("经度")
    private String longitude;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
