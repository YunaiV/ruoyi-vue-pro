package cn.iocoder.yudao.module.system.controller.admin.dict.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 字典数据创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataCreateReqVO extends DictDataBaseVO {

}
