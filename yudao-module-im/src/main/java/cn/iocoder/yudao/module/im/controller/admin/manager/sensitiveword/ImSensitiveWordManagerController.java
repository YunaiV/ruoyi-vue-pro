package cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IM 敏感词")
@RestController
@RequestMapping("/im/manager/sensitive-word")
@Validated
public class ImSensitiveWordManagerController {

    @Resource
    private ImSensitiveWordService sensitiveWordService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "新增敏感词")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:create')")
    public CommonResult<Long> createSensitiveWord(@Valid @RequestBody ImSensitiveWordSaveReqVO reqVO) {
        return success(sensitiveWordService.createSensitiveWord(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改敏感词")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:update')")
    public CommonResult<Boolean> updateSensitiveWord(@Valid @RequestBody ImSensitiveWordSaveReqVO reqVO) {
        sensitiveWordService.updateSensitiveWord(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除敏感词")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:delete')")
    public CommonResult<Boolean> deleteSensitiveWord(@RequestParam("id") Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除敏感词")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:delete')")
    public CommonResult<Boolean> deleteSensitiveWordList(
            @RequestParam("ids")
            @Size(max = 100, message = "批量删除最多 100 条") List<Long> ids) {
        sensitiveWordService.deleteSensitiveWordList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得敏感词分页")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:query')")
    public CommonResult<PageResult<ImSensitiveWordRespVO>> getSensitiveWordPage(
            @Valid ImSensitiveWordPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<ImSensitiveWordDO> pageResult = sensitiveWordService.getSensitiveWordPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2.1 批量查询创建人昵称
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(pageResult.getList(),
                word -> NumberUtils.parseLong(word.getCreator())));
        // 2.2 转换为 VO，填充创建人昵称
        return success(BeanUtils.toBean(pageResult, ImSensitiveWordRespVO.class, vo ->
                MapUtils.findAndThen(userMap, NumberUtils.parseLong(vo.getCreator()),
                        user -> vo.setCreatorName(user.getNickname()))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得敏感词详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:query')")
    public CommonResult<ImSensitiveWordRespVO> getSensitiveWord(@RequestParam("id") Long id) {
        ImSensitiveWordDO word = sensitiveWordService.getSensitiveWord(id);
        return success(BeanUtils.toBean(word, ImSensitiveWordRespVO.class));
    }

}
