package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - CRM 模块的机构/公司创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InstitutionCreateReqVO extends InstitutionBaseVO {

}
