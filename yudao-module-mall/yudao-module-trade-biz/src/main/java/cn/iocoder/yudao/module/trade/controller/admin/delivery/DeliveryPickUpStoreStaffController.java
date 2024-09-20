package cn.iocoder.yudao.module.trade.controller.admin.delivery;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup.*;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryPickUpStoreConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 自提门店店员")
@RestController
@RequestMapping("/trade/delivery/pick-up-store-staff")
@Validated
public class DeliveryPickUpStoreStaffController {


    @Resource
    private DeliveryPickUpStoreStaffService deliveryPickUpStoreStaffService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除自提门店店员")
    @Parameter(name = "userId", description = "用户编号", required = true)
    @Parameter(name = "storeId", description = "自提门店编号", required = true)
    @PreAuthorize("@ss.hasPermission('trade:delivery:pick-up-store:delete')")
    public CommonResult<Boolean> deleteDeliveryPickUpStoreStaff(@RequestParam("userId") Long id,@RequestParam("storeId") Long storeId) {
        deliveryPickUpStoreStaffService.deleteDeliveryPickUpStoreStaff(id, storeId);
        return success(true);
    }

}
