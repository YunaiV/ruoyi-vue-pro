package cn.iocoder.yudao.module.member.controller.admin.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.point.MemberPointRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.service.point.MemberPointRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户积分记录")
@RestController
@RequestMapping("/point/record")
@Validated
public class MemberPointRecordController {

    @Resource
    private MemberPointRecordService recordService;

    // TODO @xiaqing：积分应该没有更新操作呀？可以删除哈；
    @PutMapping("/update")
    @Operation(summary = "更新用户积分记录")
    @PreAuthorize("@ss.hasPermission('point:record:update')")
    public CommonResult<Boolean> updateRecord(@Valid @RequestBody MemberPointRecordUpdateReqVO updateReqVO) {
        recordService.updateRecord(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户积分记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<MemberPointRecordRespVO> getRecord(@RequestParam("id") Long id) {
        MemberPointRecordDO record = recordService.getRecord(id);
        return success(MemberPointRecordConvert.INSTANCE.convert(record));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<PageResult<MemberPointRecordRespVO>> getRecordPage(@Valid MemberPointRecordPageReqVO pageVO) {
        PageResult<MemberPointRecordDO> pageResult = recordService.getRecordPage(pageVO);
        return success(MemberPointRecordConvert.INSTANCE.convertPage(pageResult));
    }

}
