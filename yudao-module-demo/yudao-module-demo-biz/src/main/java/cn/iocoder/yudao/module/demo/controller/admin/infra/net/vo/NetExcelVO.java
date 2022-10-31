package cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 区块链网络 Excel VO
 *
 * @author ruanzh.eth
 */
@Data
public class NetExcelVO {

    @ExcelProperty("网络编号")
    private Long id;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("区块浏览器")
    private String explorer;

    @ExcelProperty("原生代币")
    private String symbol;

    @ExcelProperty("节点")
    private String endpoint;

    @ExcelProperty("网络类型")
    private String type;

    @ExcelProperty("信息")
    private String info;

    @ExcelProperty("创建时间")
    private Date createTime;

}
