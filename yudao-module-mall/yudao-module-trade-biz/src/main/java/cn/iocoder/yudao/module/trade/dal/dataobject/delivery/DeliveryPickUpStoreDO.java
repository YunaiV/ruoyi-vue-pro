package cn.iocoder.yudao.module.trade.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 自提门店 DO
 *
 * @author jason
 */
@TableName(value ="trade_delivery_pick_up_store", autoResultMap = true)
@KeySequence("trade_delivery_pick_up_store_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class DeliveryPickUpStoreDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 门店名称
     */
    private String name;

    /**
     * 门店简介
     */
    private String introduction;

    /**
     * 门店手机
     */
    private String phone;

    /**
     * 区域编号
     */
    private Integer areaId;

    /**
     * 门店详细地址
     */
    private String detailAddress;

    /**
     * 门店 logo
     */
    private String logo;

    /**
     * 营业开始时间
     */
    private LocalTime openingTime;

    /**
     * 营业结束时间
     */
    private LocalTime closingTime;

    /**
     * 纬度
     */
    private Double latitude;
    /**
     * 经度
     */
    private Double longitude;

    /**
     * 核销员工用户编号数组
     *
     * 订单自提核销时，只有对应门店的店员才能核销
     *
     * 关联 {@link AdminUserRespDTO#getId()} 管理员编号
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> verifyUserIds;

    /**
     * 门店状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
