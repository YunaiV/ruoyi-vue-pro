package cn.iocoder.yudao.module.hrm.controller.admin.recruit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.type.HrmRecruitPostTypeRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostTypeDO;
import cn.iocoder.yudao.module.hrm.service.recruit.post.HrmRecruitPostTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 招聘职位类型")
@RestController
@RequestMapping("/hrm/recruit/post-type")
@Validated
public class HrmRecruitPostTypeController {

    @Resource
    private HrmRecruitPostTypeService recruitPostTypeService;

    @GetMapping("/list")
    @Operation(summary = "获得招聘职位类型列表")
    @Parameter(name = "status", description = "状态", example = "0")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:post:query')")
    public CommonResult<List<HrmRecruitPostTypeRespVO>> getRecruitPostTypeList(@RequestParam(value = "status", required = false) Integer status) {
        List<HrmRecruitPostTypeDO> postTypes = recruitPostTypeService.getRecruitPostTypeList(status);
        return success(BeanUtils.toBean(postTypes, HrmRecruitPostTypeRespVO.class));
    }

}
