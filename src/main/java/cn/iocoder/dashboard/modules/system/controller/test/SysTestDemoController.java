package cn.iocoder.dashboard.modules.system.controller.test;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoRespVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.test.SysTestDemoConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.test.SysTestDemoDO;
import cn.iocoder.dashboard.modules.system.service.test.SysTestDemoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "字典类型")
@RestController
@RequestMapping("/system/test-demo")
@Validated
public class SysTestDemoController {

    @Resource
    private SysTestDemoService testDemoService;

    @ApiOperation("创建字典类型")
    @PostMapping("/create")
    public CommonResult<Long> createTestDemo(@Valid SysTestDemoCreateReqVO createReqVO) {
        return success(testDemoService.createTestDemo(createReqVO));
    }

    @ApiOperation("更新字典类型")
    @PutMapping("/update")
    public CommonResult<Boolean> updateTestDemo(@Valid SysTestDemoUpdateReqVO updateReqVO) {
        testDemoService.updateTestDemo(updateReqVO);
        return success(true);
    }

	@ApiOperation("删除字典类型")
	@DeleteMapping("/delete")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    public CommonResult<Boolean> deleteTestDemo(@RequestParam("id") Long id) {
        testDemoService.deleteTestDemo(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    public CommonResult<SysTestDemoRespVO> getTestDemo(@RequestParam("id") Long id) {
        SysTestDemoDO testDemo = testDemoService.getTestDemo(id);
        return success(SysTestDemoConvert.INSTANCE.convert(testDemo));
    }

    @GetMapping("/list")
    @ApiOperation("获得字典类型列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true)
    public CommonResult<List<SysTestDemoRespVO>> getTestDemoList(@RequestParam("ids") Collection<Long> ids) {
        List<SysTestDemoDO> list = testDemoService.getTestDemoList(ids);
        return success(SysTestDemoConvert.INSTANCE.convertList(list));
    }

    @ApiOperation("获得字典类型分页")
    @GetMapping("/page")
    public CommonResult<PageResult<SysTestDemoRespVO>> getTestDemoPage(@Valid SysTestDemoPageReqVO pageVO) {
        PageResult<SysTestDemoDO> pageResult = testDemoService.getTestDemoPage(pageVO);
        return success(SysTestDemoConvert.INSTANCE.convertPage(pageResult));
    }

}
