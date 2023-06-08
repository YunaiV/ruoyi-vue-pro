package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 秒杀时段创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeCreateReqVO extends SeckillTimeBaseVO {

}
