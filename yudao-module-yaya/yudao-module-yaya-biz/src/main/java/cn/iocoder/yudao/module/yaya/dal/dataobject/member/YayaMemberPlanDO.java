package cn.iocoder.yudao.module.yaya.dal.dataobject.member;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.yaya.dal.typehandler.YayaJsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@TableName(value = "yaya_member_plan", autoResultMap = true)
@KeySequence("yaya_member_plan_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaMemberPlanDO extends BaseDO {

    @TableId
    private Long id;
    private String planKey;
    private String name;
    private String description;
    private Long priceCent;
    private String currency;
    private Integer durationDays;
    private Integer active;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> benefits;

}
