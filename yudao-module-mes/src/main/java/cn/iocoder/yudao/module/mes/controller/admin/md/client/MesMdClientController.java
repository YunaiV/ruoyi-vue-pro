package cn.iocoder.yudao.module.mes.controller.admin.md.client;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.client.vo.MesMdClientSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - MES 客户")
@RestController
@RequestMapping("/mes/md-client")
@Validated
public class MesMdClientController {

    @Resource
    private MesMdClientService clientService;

    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('mes:md-client:create')")
    public CommonResult<Long> createClient(@Valid @RequestBody MesMdClientSaveReqVO createReqVO) {
        return success(clientService.createClient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('mes:md-client:update')")
    public CommonResult<Boolean> updateClient(@Valid @RequestBody MesMdClientSaveReqVO updateReqVO) {
        clientService.updateClient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-client:delete')")
    public CommonResult<Boolean> deleteClient(@RequestParam("id") Long id) {
        clientService.deleteClient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-client:query')")
    public CommonResult<MesMdClientRespVO> getClient(@RequestParam("id") Long id) {
        MesMdClientDO client = clientService.getClient(id);
        return success(BeanUtils.toBean(client, MesMdClientRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('mes:md-client:query')")
    public CommonResult<PageResult<MesMdClientRespVO>> getClientPage(@Valid MesMdClientPageReqVO pageReqVO) {
        PageResult<MesMdClientDO> pageResult = clientService.getClientPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesMdClientRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('mes:md-client:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportClientExcel(@Valid MesMdClientPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdClientDO> list = clientService.getClientPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "客户.xls", "数据", MesMdClientRespVO.class,
                BeanUtils.toBean(list, MesMdClientRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得客户导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<MesMdClientImportExcelVO> list = Collections.singletonList(
                MesMdClientImportExcelVO.builder().code("C001").name("示例客户").nickname("示例")
                        .type(1).telephone("13800138000").status(0).build()
        );
        // 输出
        ExcelUtils.write(response, "客户导入模板.xls", "客户列表", MesMdClientImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入客户")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('mes:md-client:import')")
    public CommonResult<MesMdClientImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "updateSupport", required = false,
                                                                      defaultValue = "false") Boolean updateSupport) throws Exception {
        List<MesMdClientImportExcelVO> list = ExcelUtils.read(file, MesMdClientImportExcelVO.class);
        return success(clientService.importClientList(list, updateSupport));
    }

}
