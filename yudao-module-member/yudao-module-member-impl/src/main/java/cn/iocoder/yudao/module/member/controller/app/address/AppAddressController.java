package cn.iocoder.yudao.module.member.controller.app.address;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressRespVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import cn.iocoder.yudao.module.member.service.address.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户 APP - 用户收件地址")
@RestController
@RequestMapping("/member/address")
@Validated
public class AppAddressController {

    @Resource
    private AddressService addressService;

    @PostMapping("/create")
    @ApiOperation("创建用户收件地址")

    public CommonResult<Long> createAddress(@Valid @RequestBody AppAddressCreateReqVO createReqVO) {
        return success(addressService.createAddress(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新用户收件地址")

    public CommonResult<Boolean> updateAddress(@Valid @RequestBody AppAddressUpdateReqVO updateReqVO) {
        addressService.updateAddress(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除用户收件地址")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)

    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressService.deleteAddress(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得用户收件地址")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)

    public CommonResult<AppAddressRespVO> getAddress(@RequestParam("id") Long id) {
        AddressDO address = addressService.getAddress(id);
        return success(AddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/list")
    @ApiOperation("获得用户收件地址列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)

    public CommonResult<List<AppAddressRespVO>> getAddressList(@RequestParam("ids") Collection<Long> ids) {
        List<AddressDO> list = addressService.getAddressList(ids);
        return success(AddressConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得用户收件地址分页")

    public CommonResult<PageResult<AppAddressRespVO>> getAddressPage(@Valid AppAddressPageReqVO pageVO) {
        PageResult<AddressDO> pageResult = addressService.getAddressPage(pageVO);
        return success(AddressConvert.INSTANCE.convertPage(pageResult));
    }

}
