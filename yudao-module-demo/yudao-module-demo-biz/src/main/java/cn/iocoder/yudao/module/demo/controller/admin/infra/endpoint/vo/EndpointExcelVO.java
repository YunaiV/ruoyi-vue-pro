package cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 区块链节点 Excel VO
 *
 * @author ruanzh.eth
 */
@Data
public class EndpointExcelVO {

    @ExcelProperty("节点编号")
    private Long id;

    @ExcelProperty("网络编号")
    private Long netId;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("地址")
    private String url;

    @ExcelProperty("是否被墙")
    private Boolean blocked;

    @ExcelProperty("信息")
    private String info;

    @ExcelProperty("创建时间")
    private Date createTime;

}
