package cn.iocoder.yudao.module.member.controller.admin.level;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogRespVO;
import cn.iocoder.yudao.module.member.convert.level.MemberLevelLogConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import cn.iocoder.yudao.module.member.service.level.MemberLevelLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @疯狂：是不是不用这个 controller；因为日志只是为了记录，db 可以查询、和审计即可，目前暂时不需要开放出来；
@Tag(name = "管理后台 - 会员等级记录")
@RestController
@RequestMapping("/member/level-log")
@Validated
public class MemberLevelLogController {

    @Resource
    private MemberLevelLogService levelLogService;

    @GetMapping("/get")
    @Operation(summary = "获得会员等级记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:level-log:query')")
    public CommonResult<MemberLevelLogRespVO> getLevelLog(@RequestParam("id") Long id) {
        MemberLevelLogDO levelLog = levelLogService.getLevelLog(id);
        return success(MemberLevelLogConvert.INSTANCE.convert(levelLog));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员等级记录分页")
    @PreAuthorize("@ss.hasPermission('member:level-log:query')")
    public CommonResult<PageResult<MemberLevelLogRespVO>> getLevelLogPage(@Valid MemberLevelLogPageReqVO pageVO) {
        PageResult<MemberLevelLogDO> pageResult = levelLogService.getLevelLogPage(pageVO);
        return success(MemberLevelLogConvert.INSTANCE.convertPage(pageResult));
    }
}
