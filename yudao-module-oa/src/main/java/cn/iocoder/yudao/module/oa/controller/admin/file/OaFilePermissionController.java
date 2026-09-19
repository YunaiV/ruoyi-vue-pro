package cn.iocoder.yudao.module.oa.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission.OaFilePermissionRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission.OaFilePermissionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.service.file.OaFilePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 云盘共享权限")
@RestController
@RequestMapping("/oa/file-permission")
@Validated
public class OaFilePermissionController {

    @Resource
    private OaFilePermissionService filePermissionService;

    @GetMapping("/list")
    @Operation(summary = "获得文件共享权限")
    @Parameter(name = "nodeId", description = "节点编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:share')")
    public CommonResult<List<OaFilePermissionRespVO>> getFilePermissionList(@RequestParam("nodeId") Long nodeId) {
        List<OaFilePermissionDO> permissions = filePermissionService.getFilePermissionList(nodeId, getLoginUserId());
        return success(BeanUtils.toBean(permissions, OaFilePermissionRespVO.class));
    }

    @PostMapping("/save")
    @Operation(summary = "保存文件共享权限")
    @PreAuthorize("@ss.hasPermission('oa:file:share')")
    public CommonResult<Long> saveFilePermission(@Valid @RequestBody OaFilePermissionSaveReqVO reqVO) {
        return success(filePermissionService.saveFilePermission(reqVO, getLoginUserId()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消文件共享权限")
    @Parameter(name = "id", description = "权限编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:file:share')")
    public CommonResult<Boolean> deleteFilePermission(@RequestParam("id") Long id) {
        filePermissionService.deleteFilePermission(id, getLoginUserId());
        return success(true);
    }

}
