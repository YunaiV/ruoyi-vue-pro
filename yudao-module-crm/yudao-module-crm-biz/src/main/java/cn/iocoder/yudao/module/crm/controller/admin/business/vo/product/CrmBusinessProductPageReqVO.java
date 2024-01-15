package cn.iocoder.yudao.module.crm.controller.admin.business.vo.product;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * @author lzxhqs
 */
@Schema(description = "管理后台 - 商机产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessProductPageReqVO extends PageParam {
}
