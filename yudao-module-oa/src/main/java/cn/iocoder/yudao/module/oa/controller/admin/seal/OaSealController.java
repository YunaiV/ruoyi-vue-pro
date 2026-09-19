package cn.iocoder.yudao.module.oa.controller.admin.seal;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO;
import cn.iocoder.yudao.module.oa.service.seal.OaSealService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - 印章")
@RestController
@RequestMapping("/oa/seal")
@Validated
public class OaSealController {

    @Resource
    private OaSealService sealService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建印章")
    @PreAuthorize("@ss.hasPermission('oa:seal:create')")
    public CommonResult<Long> createSeal(@Valid @RequestBody OaSealSaveReqVO createReqVO) {
        return success(sealService.createSeal(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新印章")
    @PreAuthorize("@ss.hasPermission('oa:seal:update')")
    public CommonResult<Boolean> updateSeal(@Valid @RequestBody OaSealSaveReqVO updateReqVO) {
        sealService.updateSeal(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除印章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal:delete')")
    public CommonResult<Boolean> deleteSeal(@RequestParam("id") Long id) {
        sealService.deleteSeal(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得印章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal:query')")
    public CommonResult<OaSealRespVO> getSeal(@RequestParam("id") Long id) {
        OaSealDO seal = sealService.validateSealExists(id);
        return success(buildSealRespVO(seal));
    }

    @GetMapping("/page")
    @Operation(summary = "获得印章分页")
    @PreAuthorize("@ss.hasPermission('oa:seal:query')")
    public CommonResult<PageResult<OaSealRespVO>> getSealPage(@Valid OaSealPageReqVO pageReqVO) {
        PageResult<OaSealDO> pageResult = sealService.getSealPage(pageReqVO);
        return success(new PageResult<>(buildSealRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接印章详情
     *
     * @param seal 印章
     * @return 印章详情
     */
    private OaSealRespVO buildSealRespVO(OaSealDO seal) {
        if (seal == null) {
            return null;
        }
        return CollUtil.getFirst(buildSealRespVOList(Collections.singletonList(seal)));
    }

    /**
     * 拼接印章列表的保管人和部门信息
     *
     * @param seals 印章列表
     * @return 印章响应列表
     */
    private List<OaSealRespVO> buildSealRespVOList(List<OaSealDO> seals) {
        if (CollUtil.isEmpty(seals)) {
            return Collections.emptyList();
        }
        // 1. 批量查询保管人及部门
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(seals, OaSealDO::getKeeperUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(seals, seal -> Stream.of(seal.getDeptId(), seal.getKeeperDeptId())));
        // 2. 转换台账并拼接展示字段
        return convertList(seals, seal -> {
            OaSealRespVO respVO = BeanUtils.toBean(seal, OaSealRespVO.class);
            MapUtils.findAndThen(deptMap, seal.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            MapUtils.findAndThen(userMap, seal.getKeeperUserId(), user -> respVO.setKeeperName(user.getNickname()));
            MapUtils.findAndThen(deptMap, seal.getKeeperDeptId(), dept -> respVO.setKeeperDeptName(dept.getName()));
            return respVO;
        });
    }
}
