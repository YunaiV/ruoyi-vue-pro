package cn.iocoder.dashboard.modules.tool.controller.codegen;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenPreviewRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTableRespVO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "代码生成器 API")
@RestController
@RequestMapping("/tool/codegen")
@Validated
public class ToolCodegenController {

    @Resource
    private ToolCodegenService codegenService;

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
    public void downloadCodegen(@RequestParam("tableId") Long tableId,
                                HttpServletResponse response) throws IOException {
        // 生成代码
        Map<String, String> codes = codegenService.generationCodes(tableId);
        // 构建压缩包
        byte[] data;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (Map.Entry<String, String> entry : codes.entrySet()) {
//                zip.putNextEntry(new ZipEntry(entry.getKey()));
            zip.putNextEntry(new ZipEntry("123"));
//                IoUtil.write(zip, Charset.defaultCharset(), false, entry.getValue());
            zip.write(entry.getValue().getBytes());
            zip.flush();
            zip.closeEntry();
            if (true) {
                break;
            }
        }
        data = outputStream.toByteArray();
        IoUtil.close(zip);
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//             ZipOutputStream zip = new ZipOutputStream(outputStream)) {
//            for (Map.Entry<String, String> entry : codes.entrySet()) {
////                zip.putNextEntry(new ZipEntry(entry.getKey()));
//                zip.putNextEntry(new ZipEntry("123"));
////                IoUtil.write(zip, Charset.defaultCharset(), false, entry.getValue());
//                zip.write(entry.getValue().getBytes());
//                zip.flush();
//                zip.closeEntry();
//                if (true) {
//                    break;
//                }
//            }
//            data = outputStream.toByteArray();
//        }
        // 返回
//        ServletUtils.writeAttachment(response, "yudao.zip", data);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException
    {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }
}
