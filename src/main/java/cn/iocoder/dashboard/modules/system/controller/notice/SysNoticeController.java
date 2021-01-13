package cn.iocoder.dashboard.modules.system.controller.notice;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "通知公告 API")
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

//    /**
//     * 获取通知公告列表
//     */
//    @PreAuthorize("@ss.hasPermi('system:notice:list')")
//    @GetMapping("/list")
//    public TableDataInfo list(SysNotice notice) {
//        startPage();
//        List<SysNotice> list = noticeService.selectNoticeList(notice);
//        return getDataTable(list);
//    }
//
//    /**
//     * 根据通知公告编号获取详细信息
//     */
//    @PreAuthorize("@ss.hasPermi('system:notice:query')")
//    @GetMapping(value = "/{noticeId}")
//    public AjaxResult getInfo(@PathVariable Long noticeId) {
//        return AjaxResult.success(noticeService.selectNoticeById(noticeId));
//    }
//
//    /**
//     * 新增通知公告
//     */
//    @PreAuthorize("@ss.hasPermi('system:notice:add')")
//    @Log(title = "通知公告", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody SysNotice notice) {
//        notice.setCreateBy(SecurityUtils.getUsername());
//        return toAjax(noticeService.insertNotice(notice));
//    }
//
//    /**
//     * 修改通知公告
//     */
//    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
//    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
//        notice.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(noticeService.updateNotice(notice));
//    }
//
//    /**
//     * 删除通知公告
//     */
//    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
//    @Log(title = "通知公告", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{noticeIds}")
//    public AjaxResult remove(@PathVariable Long[] noticeIds) {
//        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
//    }

}
