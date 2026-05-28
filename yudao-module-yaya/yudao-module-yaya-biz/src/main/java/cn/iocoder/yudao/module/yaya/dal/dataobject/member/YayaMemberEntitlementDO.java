package cn.iocoder.yudao.module.yaya.dal.dataobject.member;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.yaya.dal.typehandler.YayaJsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "yaya_member_entitlement", autoResultMap = true)
@KeySequence("yaya_member_entitlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaMemberEntitlementDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long planId;
    private String sourceType;
    private Long sourceId;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String idempotencyKey;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
