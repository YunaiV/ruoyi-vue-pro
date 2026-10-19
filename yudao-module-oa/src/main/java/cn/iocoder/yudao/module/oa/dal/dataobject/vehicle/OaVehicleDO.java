package cn.iocoder.yudao.module.oa.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OA 车辆 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_vehicle", autoResultMap = true)
@KeySequence("oa_vehicle_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaVehicleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 车牌号
     */
    private String no;
    /**
     * 车辆名称
     */
    private String name;
    /**
     * 所属部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long deptId;
    /**
     * 车型
     */
    private String type;
    /**
     * 车辆分类
     *
     * 字典 {@link DictTypeConstants#VEHICLE_CATEGORY}
     */
    private String category;
    /**
     * 品牌型号
     */
    private String brandModel;
    /**
     * 座位数
     */
    private Integer seatCount;
    /**
     * 裸车价格（元）
     */
    private BigDecimal barePrice;
    /**
     * 交强险到期时间
     */
    private LocalDateTime compulsoryInsuranceExpireTime;
    /**
     * 商业险到期时间
     */
    private LocalDateTime commercialInsuranceExpireTime;
    /**
     * 年检到期时间
     */
    private LocalDateTime inspectionExpireTime;
    /**
     * 车辆照片 URL
     */
    private String picUrl;
    /**
     * 车辆台账状态
     *
     * 枚举 {@link OaVehicleStatusEnum}
     */
    private Integer status;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
