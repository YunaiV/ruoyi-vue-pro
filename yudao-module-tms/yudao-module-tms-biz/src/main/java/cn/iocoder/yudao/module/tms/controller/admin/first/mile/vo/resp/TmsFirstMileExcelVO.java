package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 头程单 Excel VO
 */
@Data
public class TmsFirstMileExcelVO {

    // 主表信息
    @ExcelProperty("单据编号")
    private String no;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("客户联系人")
    private String customerContact;

    @ExcelProperty("客户联系电话")
    private String customerPhone;

    @ExcelProperty("客户地址")
    private String customerAddress;

    @ExcelProperty("提货地址")
    private String pickupAddress;

    @ExcelProperty("提货联系人")
    private String pickupContact;

    @ExcelProperty("提货联系电话")
    private String pickupPhone;

    @ExcelProperty("提货时间")
    private LocalDateTime pickupTime;

    @ExcelProperty("货物名称")
    private String cargoName;

    @ExcelProperty("货物数量")
    private Integer cargoQuantity;

    @ExcelProperty("货物价值")
    private BigDecimal cargoValue;

    @ExcelProperty("货物重量(kg)")
    private BigDecimal cargoWeight;

    @ExcelProperty("货物体积(m³)")
    private BigDecimal cargoVolume;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    // 最新跟踪信息
    @ExcelProperty("最新跟踪时间")
    private LocalDateTime latestTrackTime;

    //字典
    @ExcelProperty("最新跟踪状态")
    private Integer latestTrackStatus;

    @ExcelProperty("最新跟踪描述")
    private String latestTrackDescription;

    @ExcelProperty("最新跟踪位置")
    private String latestTrackLocation;
}