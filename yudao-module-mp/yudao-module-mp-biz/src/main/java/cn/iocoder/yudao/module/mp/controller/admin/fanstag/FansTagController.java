package cn.iocoder.yudao.module.mp.controller.admin.fanstag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.convert.fanstag.WxFansTagConvert;
import cn.iocoder.yudao.module.mp.service.tag.FansTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

/**
 * @author fengdan
 */
@Api(tags = "管理后台 - 粉丝标签")
@RestController
@RequestMapping("/wechatMp/fans-tag")
@Validated
public class FansTagController {

    @Resource
    private FansTagService fansTagService;

    @PostMapping("/create")
    @ApiOperation("创建粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:create')")
    public CommonResult<WxUserTag> createWxFansTag(@Valid @RequestBody FansTagCreateReqVO createReqVO) throws WxErrorException {
        return success(fansTagService.createWxFansTag(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:update')")
    public CommonResult<Boolean> updateWxFansTag(@Valid @RequestBody FansTagUpdateReqVO updateReqVO) throws WxErrorException {
        return success(fansTagService.updateWxFansTag(updateReqVO));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:delete')")
    public CommonResult<Boolean> deleteWxFansTag(@RequestParam("id") Long id,
                                                 @RequestParam("appId") String appId) throws WxErrorException {
        return success(fansTagService.deleteWxFansTag(id, appId));
    }

    @GetMapping("/list")
    @ApiOperation("获取公众号已创建的标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:query')")
    public CommonResult<List<FansTagRespVO>> getWxFansTagList(@NotEmpty(message = "公众号appId不能为空")
                                                              @RequestParam("appId") String appId) throws WxErrorException {
        List<WxUserTag> list = fansTagService.getWxFansTagList(appId);
        return success(WxFansTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获取公众号已创建的标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:query')")
    public CommonResult<PageResult<FansTagRespVO>> page() throws WxErrorException {
        PageResult<WxUserTag> page = new PageResult<>();
        return success(WxFansTagConvert.INSTANCE.convertPage(page));
    }

    @GetMapping("/tagListUser")
    @ApiOperation("获取标签下粉丝列表")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:query')")
    public CommonResult<String> tagListUser(@Valid FansTagPageReqVO pageVO) {
        return success("");
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出粉丝标签 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:fans-tag:export')")
    @OperateLog(type = EXPORT)
    public void exportWxFansTagExcel(@Valid FansTagExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<WxUserTag> list = fansTagService.getWxFansTagList(exportReqVO);
        // 导出 Excel
        List<FansTagExcelVO> datas = WxFansTagConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "粉丝标签.xls", "数据", FansTagExcelVO.class, datas);
    }

}
