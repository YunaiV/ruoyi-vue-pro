package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileRespVO;
import cn.iocoder.yudao.module.infra.convert.file.FileConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "path", value = "文件路径", example = "yudaoyuanma.png", dataTypeClass = String.class)
    })
    @OperateLog(logArgs = false) // 上传文件，没有记录操作日志的必要
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "path", required = false) String path) throws Exception {
        return success(fileService.createFile(file.getOriginalFilename(), path, IoUtil.readBytes(file.getInputStream())));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) throws Exception {
        fileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/{configId}/get/{path}")
    @PermitAll
    @ApiOperation("下载文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "configId", value = "配置编号",  required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "path", value = "文件路径", required = true, dataTypeClass = String.class)
    })
    public void getFileContent(HttpServletResponse response,
                               @PathVariable("configId") Long configId,
                               @PathVariable("path") String path) throws Exception {
        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, content);
    }

    @GetMapping("/page")
    @ApiOperation("获得文件分页")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<FileRespVO>> getFilePage(@Valid FilePageReqVO pageVO) {
        PageResult<FileDO> pageResult = fileService.getFilePage(pageVO);
        return success(FileConvert.INSTANCE.convertPage(pageResult));
    }

}
