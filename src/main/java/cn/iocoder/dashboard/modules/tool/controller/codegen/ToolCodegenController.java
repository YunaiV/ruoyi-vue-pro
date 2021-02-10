package cn.iocoder.dashboard.modules.tool.controller.codegen;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenPreviewRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTableRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolSchemaTableRespVO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import cn.iocoder.dashboard.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "代码生成器 API")
@RestController
@RequestMapping("/tool/codegen")
@Validated
public class ToolCodegenController {

    @Resource
    private ToolCodegenService codegenService;

    @ApiOperation(value = "获得数据库自带的表定义列表", notes = "会过滤掉已经导入 Codegen 的表")
    @GetMapping("/db/table/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableName", required = true, example = "yudao", dataTypeClass = String.class),
            @ApiImplicitParam(name = "tableComment", required = true, example = "芋道", dataTypeClass = Long.class)
    })
//    @PreAuthorize("@ss.hasPermi('tool:gen:list')") TODO 权限
    public CommonResult<List<ToolSchemaTableRespVO>> getSchemaTableList(
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "tableComment", required = false) String tableComment) {
        // 获得数据库自带的表定义列表
        List<ToolSchemaTableDO> schemaTables = codegenService.getSchemaTableList(tableName, tableComment);
        // 移除在 Codegen 中，已经存在的

        return null;
    }

    @ApiOperation("获得表定义分页")
    @GetMapping("/table/page")
    // TODO 权限 @PreAuthorize("@ss.hasPermi('tool:gen:list')")
    public CommonResult<PageResult<ToolCodegenTableRespVO>> getCodeGenTablePage(@Valid ToolCodegenTablePageReqVO pageReqVO) {
        PageResult<ToolCodegenTableDO> pageResult = codegenService.getCodeGenTablePage(pageReqVO);
        return success(ToolCodegenConvert.INSTANCE.convertPage(pageResult));
    }

    @ApiOperation("获得表和字段的明细")
    @GetMapping("/detail")
//   todo @PreAuthorize("@ss.hasPermi('tool:gen:query')")
    public CommonResult<ToolCodegenDetailRespVO> getCodeGenDetail(@RequestParam("tableId") Long tableId) {
        ToolCodegenTableDO table = codegenService.getCodeGenTablePage(tableId);
        List<ToolCodegenColumnDO> columns = codegenService.getCodegenColumnListByTableId(tableId);
        // 拼装返回
        return success(ToolCodegenConvert.INSTANCE.convert(table, columns));
    }

    @ApiOperation("基于数据库的表结构，创建代码生成器的表定义")
    @PostMapping("/create")
    // TODO 权限
    public CommonResult<Long> createCodeGen(@RequestParam("tableName") String tableName) {
        return success(codegenService.createCodegen(tableName));
    }

    @ApiOperation("更新数据库的表和字段定义")
    @PutMapping("/update")
//    @PreAuthorize("@ss.hasPermi('tool:gen:edit')") TODO 权限
    public CommonResult<Boolean> updateCodegen(@Valid @RequestBody ToolCodegenUpdateReqVO updateReqVO) {
        codegenService.updateCodegen(updateReqVO);
        return success(true);
    }

    @ApiOperation("预览生成代码")
    @GetMapping("/preview")
    @ApiImplicitParam(name = "tableId", required = true, example = "表编号", dataTypeClass = Long.class)
//    @PreAuthorize("@ss.hasPermi('tool:gen:preview')") TODO 权限
    public CommonResult<List<ToolCodegenPreviewRespVO>> previewCodegen(@RequestParam("tableId") Long tableId) {
        Map<String, String> codes = codegenService.generationCodes(tableId);
        return success(ToolCodegenConvert.INSTANCE.convert(codes));
    }

    @ApiOperation("下载生成代码")
    @GetMapping("/download")
    @ApiImplicitParam(name = "tableId", required = true, example = "表编号", dataTypeClass = Long.class)
    // @PreAuthorize("@ss.hasPermi('tool:gen:code')") todo 权限
    public void downloadCodegen(@RequestParam("tableId") Long tableId,
                                HttpServletResponse response) throws IOException {
        // 生成代码
        Map<String, String> codes = codegenService.generationCodes(tableId);
        // 构建 zip 包
        String[] paths = codes.keySet().toArray(new String[0]);
        ByteArrayInputStream[] ins = codes.values().stream().map(IoUtil::toUtf8Stream).toArray(ByteArrayInputStream[]::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtil.zip(outputStream, paths, ins);
        // 输出
        ServletUtils.writeAttachment(response, "codegen.zip", outputStream.toByteArray());
    }

}
