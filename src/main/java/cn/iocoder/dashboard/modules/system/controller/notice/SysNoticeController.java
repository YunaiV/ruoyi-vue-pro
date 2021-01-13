package cn.iocoder.dashboard.modules.system.controller.notice;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticeCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticeRespVO;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticeUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.notice.SysNoticeConvert;
import cn.iocoder.dashboard.modules.system.service.notice.SysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "通知公告 API")
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

    @Resource
    private SysNoticeService noticeService;

    @ApiOperation("获取通知公告列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    public CommonResult<PageResult<SysNoticeRespVO>> pageNotices(@Validated SysNoticePageReqVO reqVO) {
        return success(SysNoticeConvert.INSTANCE.convertPage(noticeService.pageNotices(reqVO)));
    }

    @ApiOperation("获得通知公告")
    @ApiImplicitParam(name = "id", value = "编号", readOnly = true, example = "1024", dataTypeClass = Long.class)
//    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/get")
    public CommonResult<SysNoticeRespVO> getNotice(@RequestParam("id") Long id) {
        return success(SysNoticeConvert.INSTANCE.convert(noticeService.getNotice(id)));
    }

    @ApiOperation("新增通知公告")
//    @PreAuthorize("@ss.hasPermi('system:notice:add')")
//    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public CommonResult<Long> createNotice(@Validated @RequestBody SysNoticeCreateReqVO reqVO) {
        Long noticeId = noticeService.createNotice(reqVO);
        return success(noticeId);
    }

    @ApiOperation("修改通知公告")
//    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
//    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public CommonResult<Boolean> updateNotice(@Validated @RequestBody SysNoticeUpdateReqVO reqVO) {
        noticeService.updateNotice(reqVO);
        return success(true);
    }

    @ApiOperation("删除通知公告")
    @ApiImplicitParam(name = "id", value = "编号", readOnly = true, example = "1024", dataTypeClass = Long.class)
//    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
//    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
        noticeService.deleteNotice(id);
        return success(true);
    }

}
