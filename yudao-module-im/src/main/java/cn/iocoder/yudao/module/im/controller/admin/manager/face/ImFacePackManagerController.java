package cn.iocoder.yudao.module.im.controller.admin.manager.face;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import cn.iocoder.yudao.module.im.service.face.ImFacePackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 表情包")
@RestController
@RequestMapping("/im/manager/face-pack")
@Validated
public class ImFacePackManagerController {

    @Resource
    private ImFacePackService facePackService;

    @PostMapping("/create")
    @Operation(summary = "新增表情包")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:create')")
    public CommonResult<Long> createFacePack(@Valid @RequestBody ImFacePackSaveReqVO reqVO) {
        return success(facePackService.createFacePack(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改表情包")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:update')")
    public CommonResult<Boolean> updateFacePack(@Valid @RequestBody ImFacePackSaveReqVO reqVO) {
        facePackService.updateFacePack(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除表情包")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:delete')")
    public CommonResult<Boolean> deleteFacePack(@RequestParam("id") Long id) {
        facePackService.deleteFacePack(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除表情包")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:delete')")
    public CommonResult<Boolean> deleteFacePackList(@RequestParam("ids")
                                                    @Size(max = 100, message = "批量删除最多 100 条") List<Long> ids) {
        facePackService.deleteFacePackList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得表情包分页")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:query')")
    public CommonResult<PageResult<ImFacePackRespVO>> getFacePackPage(@Valid ImFacePackPageReqVO pageReqVO) {
        PageResult<ImFacePackDO> pageResult = facePackService.getFacePackPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImFacePackRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得表情包详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack:query')")
    public CommonResult<ImFacePackRespVO> getFacePack(@RequestParam("id") Long id) {
        ImFacePackDO pack = facePackService.getFacePack(id);
        return success(BeanUtils.toBean(pack, ImFacePackRespVO.class));
    }

}
