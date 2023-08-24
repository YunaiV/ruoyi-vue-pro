package cn.iocoder.yudao.module.member.controller.admin.address;

import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.member.controller.admin.address.vo.*;
import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.service.address.AddressService;

@Tag(name = "管理后台 - 用户收件地址")
@RestController
@RequestMapping("/member/address")
@Validated
public class AddressController {

    @Resource
    private AddressService addressService;

    @PostMapping("/create")
    @Operation(summary = "创建用户收件地址")
    @PreAuthorize("@ss.hasPermission('member:address:create')")
    public CommonResult<Long> createAddress(@Valid @RequestBody AddressCreateReqVO createReqVO) {
        return success(addressService.createAddress(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户收件地址")
    @PreAuthorize("@ss.hasPermission('member:address:update')")
    public CommonResult<Boolean> updateAddress(@Valid @RequestBody AddressUpdateReqVO updateReqVO) {
        addressService.updateAddress(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户收件地址")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:address:delete')")
    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressService.deleteAddress(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户收件地址")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:address:query')")
    public CommonResult<AddressRespVO> getAddress(@RequestParam("id") Long id) {
        MemberAddressDO address = addressService.getAddress(id);
        return success(AddressConvert.INSTANCE.convert2(address));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户收件地址列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:address:query')")
    public CommonResult<List<AddressRespVO>> getAddressList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberAddressDO> list = addressService.getAddressList(ids);
        return success(AddressConvert.INSTANCE.convertList2(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户收件地址分页")
    @PreAuthorize("@ss.hasPermission('member:address:query')")
    public CommonResult<PageResult<AddressRespVO>> getAddressPage(@Valid AddressPageReqVO pageVO) {
        PageResult<MemberAddressDO> pageResult = addressService.getAddressPage(pageVO);
        return success(AddressConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户收件地址 Excel")
    @PreAuthorize("@ss.hasPermission('member:address:export')")
    @OperateLog(type = EXPORT)
    public void exportAddressExcel(@Valid AddressExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MemberAddressDO> list = addressService.getAddressList(exportReqVO);
        // 导出 Excel
        List<AddressExcelVO> datas = AddressConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用户收件地址.xls", "数据", AddressExcelVO.class, datas);
    }

}
