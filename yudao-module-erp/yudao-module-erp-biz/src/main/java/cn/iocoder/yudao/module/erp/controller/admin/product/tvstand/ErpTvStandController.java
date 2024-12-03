package cn.iocoder.yudao.module.erp.controller.admin.product.tvstand;

import cn.iocoder.yudao.module.erp.service.product.tvstand.ErpProductTvStandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Wqh
 * @date: 2024/12/3 17:18
 */
@Tag(name = "管理后台 - ERP 电视机架信息")
@RestController
@RequestMapping("/erp/tvstand")
@Validated
public class ErpTvStandController {
    @Resource
    private ErpProductTvStandService erpProductTvStandService;
}
