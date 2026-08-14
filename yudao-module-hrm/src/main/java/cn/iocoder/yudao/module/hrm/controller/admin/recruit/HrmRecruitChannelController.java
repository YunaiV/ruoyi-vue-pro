package cn.iocoder.yudao.module.hrm.controller.admin.recruit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelDeleteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - HRM 招聘渠道")
@RestController
@RequestMapping("/hrm/recruit/channel")
@Validated
public class HrmRecruitChannelController {

    @Resource
    private HrmRecruitChannelService recruitChannelService;

    @PostMapping("/create")
    @Operation(summary = "创建招聘渠道")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:create')")
    public CommonResult<Long> createRecruitChannel(@Valid @RequestBody HrmRecruitChannelSaveReqVO createReqVO) {
        return success(recruitChannelService.createRecruitChannel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新招聘渠道")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:update')")
    public CommonResult<Boolean> updateRecruitChannel(@Valid @RequestBody HrmRecruitChannelSaveReqVO updateReqVO) {
        recruitChannelService.updateRecruitChannel(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新招聘渠道状态")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:update')")
    public CommonResult<Boolean> updateRecruitChannelStatus(
            @Valid @RequestBody HrmRecruitChannelStatusReqVO statusReqVO) {
        recruitChannelService.updateRecruitChannelStatus(statusReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除招聘渠道")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:delete')")
    public CommonResult<Boolean> deleteRecruitChannel(@Valid @RequestBody HrmRecruitChannelDeleteReqVO deleteReqVO) {
        recruitChannelService.deleteRecruitChannel(deleteReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得招聘渠道")
    @Parameter(name = "id", description = "招聘渠道编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:query')")
    public CommonResult<HrmRecruitChannelRespVO> getRecruitChannel(@RequestParam("id") Long id) {
        HrmRecruitChannelDO recruitChannel = recruitChannelService.getRecruitChannel(id);
        return success(BeanUtils.toBean(recruitChannel, HrmRecruitChannelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得招聘渠道分页")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:channel:query')")
    public CommonResult<PageResult<HrmRecruitChannelRespVO>> getRecruitChannelPage(@Validated HrmRecruitChannelPageReqVO pageReqVO) {
        PageResult<HrmRecruitChannelDO> pageResult = recruitChannelService.getRecruitChannelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HrmRecruitChannelRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得招聘渠道精简列表", description = "用于招聘职位、候选人等表单的下拉选项")
    public CommonResult<List<HrmRecruitChannelRespVO>> getRecruitChannelSimpleList() {
        return success(convertList(recruitChannelService.getRecruitChannelSimpleList(), channel ->
                new HrmRecruitChannelRespVO().setId(channel.getId()).setName(channel.getName()).setStatus(channel.getStatus())));
    }

}
