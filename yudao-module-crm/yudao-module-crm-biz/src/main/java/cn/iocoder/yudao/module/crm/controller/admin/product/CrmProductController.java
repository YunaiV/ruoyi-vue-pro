package cn.iocoder.yudao.module.crm.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.product.ErpProductApi;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductSimpleRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - CRM 产品")
@RestController
@RequestMapping("/crm/product")
@Validated
public class CrmProductController {


    @Resource
    private ErpProductApi erpProductApi;


    @GetMapping("/simple-list")
    @Operation(summary = "获得产品精简列表", description = "只包含被开启的产品，主要用于前端的下拉选项")
    public CommonResult<List<ErpProductSimpleRespDTO>> getProductSimpleList() {
        List<ErpProductRespDTO> list = erpProductApi.getProductDTOListByStatus(true);
        return success(convertList(list, vo -> BeanUtils.toBean(vo, ErpProductSimpleRespDTO.class)));
    }

}
