package com.somle.kingdee.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurRequestReqVO {
    private String billCloseState;           // 单据关闭状态（所有：“”，未关闭：“C”，已关闭：“S”,手动关闭: H）
    private String billNo;                   // 单据编码
    private String billStatus;               // 单据状态（所有：“”，已审核：“C”，未审核：“Z”）
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createEndTime;            // 创建时间-结束时间的时间戳(毫秒)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createStartTime;          // 创建时间-开始时间的时间戳(毫秒)
    private List<String> deptId;             // 调入部门
    private String deptNumber;               // 部门编码
    private List<String> empId;              // 职员ID
    private LocalDate endBillDate;              // 结束日期（格式：“yyyy-MM-dd”，为空表示不过滤），单据日期
    private String materialModel;            // 商品规格型号
    private String materialName;             // 商品名称
    private String materialNumber;           // 商品编码
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp modifyEndTime;            // 修改时间-结束时间的时间戳(毫秒)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp modifyStartTime;          // 修改时间-开始时间的时间戳(毫秒)
    private String orderBy;                  // 排序；编码升序：number asc（默认），编码降序：number desc，名称升序：name asc，名称降序：name desc
    private String page;                     // 当前页，默认1
    private String pageSize;                 // 每页显示条数默认10
    private String purChaseStatus;           // 采购状态（所有：“”，全部采购：“A”，部分采购：“P”，未采购：“N”，（新增一个采购状态 B：待采购，它等于未采购+部分采购））
    private String showMaterialTotal;        // 是否统计商品种类和申请数量、批准数量、单据价税合计到单据，默认false
    private LocalDate startBillDate;            // 开始日期（格式：“yyyy-MM-dd”，为空表示不过滤），单据日期
    private String totalFields;              // 自定义合计字段，多个字段用英文逗号隔开
}
