package cn.iocoder.yudao.module.yaya.dal.dataobject.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.yaya.dal.typehandler.YayaJsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@TableName(value = "yaya_import_batch", autoResultMap = true)
@KeySequence("yaya_import_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaImportBatchDO extends BaseDO {

    @TableId
    private Long id;
    private String seasonKey;
    private String sourceLabel;
    private String status;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> summary;
    private String createdBy;

}
