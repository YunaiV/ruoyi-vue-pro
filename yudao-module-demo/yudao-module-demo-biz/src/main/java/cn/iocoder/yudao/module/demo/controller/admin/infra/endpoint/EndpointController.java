package cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

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

import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;
import cn.iocoder.yudao.module.demo.convert.infra.endpoint.EndpointConvert;
import cn.iocoder.yudao.module.demo.service.infra.endpoint.EndpointService;

@Api(tags = "管理后台 - 区块链节点")
@RestController
@RequestMapping("/demo/endpoint")
@Validated
public class EndpointController {

    @Resource
    private EndpointService endpointService;

    @PostMapping("/create")
    @ApiOperation("创建区块链节点")
    @PreAuthorize("@ss.hasPermission('demo:endpoint:create')")
    public CommonResult<Long> createEndpoint(@Valid @RequestBody EndpointCreateReqVO createReqVO) {
        return success(endpointService.createEndpoint(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新区块链节点")
    @PreAuthorize("@ss.hasPermission('demo:endpoint:update')")
    public CommonResult<Boolean> updateEndpoint(@Valid @RequestBody EndpointUpdateReqVO updateReqVO) {
        endpointService.updateEndpoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除区块链节点")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('demo:endpoint:delete')")
    public CommonResult<Boolean> deleteEndpoint(@RequestParam("id") Long id) {
        endpointService.deleteEndpoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得区块链节点")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('demo:endpoint:query')")
    public CommonResult<EndpointRespVO> getEndpoint(@RequestParam("id") Long id) {
        EndpointDO endpoint = endpointService.getEndpoint(id);
        return success(EndpointConvert.INSTANCE.convert(endpoint));
    }

    @GetMapping("/list")
    @ApiOperation("获得区块链节点列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('demo:endpoint:query')")
    public CommonResult<List<EndpointRespVO>> getEndpointList(@RequestParam("ids") Collection<Long> ids) {
        List<EndpointDO> list = endpointService.getEndpointList(ids);
        return success(EndpointConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得区块链节点分页")
    @PreAuthorize("@ss.hasPermission('demo:endpoint:query')")
    public CommonResult<PageResult<EndpointRespVO>> getEndpointPage(@Valid EndpointPageReqVO pageVO) {
        PageResult<EndpointDO> pageResult = endpointService.getEndpointPage(pageVO);
        return success(EndpointConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出区块链节点 Excel")
    @PreAuthorize("@ss.hasPermission('demo:endpoint:export')")
    @OperateLog(type = EXPORT)
    public void exportEndpointExcel(@Valid EndpointExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<EndpointDO> list = endpointService.getEndpointList(exportReqVO);
        // 导出 Excel
        List<EndpointExcelVO> datas = EndpointConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "区块链节点.xls", "数据", EndpointExcelVO.class, datas);
    }

}
