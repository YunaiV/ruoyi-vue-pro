package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.module.infra.service.file.InfFileService;
import cn.iocoder.yudao.coreservice.modules.infra.controller.file.vo.InfFileRespVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.convert.file.InfFileConvert;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class InfFileController {

    @Resource
    private InfFileService fileService;
    @Resource
    private InfFileCoreService fileCoreService;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "path", value = "文件路径", example = "yudaoyuanma.png", dataTypeClass = String.class)
    })
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("path") String path) throws IOException {
        return success(fileCoreService.createFile(path, IoUtil.readBytes(file.getInputStream())));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") String id) {
        fileCoreService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/get/{path}")
    @ApiOperation("下载文件")
    @ApiImplicitParam(name = "path", value = "文件附件", required = true, dataTypeClass = MultipartFile.class)
    public void getFile(HttpServletResponse response, @PathVariable("path") String path) throws IOException {
        TenantContextHolder.setNullTenantId();
        InfFileDO file = fileCoreService.getFile(path);
        if (file == null) {
            log.warn("[getFile][path({}) 文件不存在]", path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, file.getContent());
    }

    @GetMapping("/page")
    @ApiOperation("获得文件分页")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<InfFileRespVO>> getFilePage(@Valid InfFilePageReqVO pageVO) {
        PageResult<InfFileDO> pageResult = fileService.getFilePage(pageVO);
        return success(InfFileConvert.INSTANCE.convertPage(pageResult));
    }

}
