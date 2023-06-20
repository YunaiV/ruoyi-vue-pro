package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductBaseVO;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

/**
 * 管理后台 - 秒杀参与商品创建 Request VO
 *
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 秒杀参与商品创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillProductCreateReqVO extends SeckillProductBaseVO {

}
