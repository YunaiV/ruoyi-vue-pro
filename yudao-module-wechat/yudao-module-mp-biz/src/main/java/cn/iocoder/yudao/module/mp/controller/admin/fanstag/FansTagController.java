package cn.iocoder.yudao.module.mp.controller.admin.fanstag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.convert.fanstag.WxFansTagConvert;
import cn.iocoder.yudao.module.mp.service.tag.FansTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

/**
 * @author fengdan
 */
@Api(tags = "管理后台 - 粉丝标签")
@RestController
@RequestMapping("/wechat-mp/fans-tag")
@Validated
public class FansTagController {

    @Resource
    private FansTagService fansTagService;

    @PostMapping("/create")
    @ApiOperation("创建粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:create')")
    public CommonResult<WxUserTag> createWxFansTag(@Valid @RequestBody FansTagCreateReqVO createReqVO) {
        return success(fansTagService.createWxFansTag(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:update')")
    public CommonResult<Boolean> updateWxFansTag(@Valid @RequestBody FansTagUpdateReqVO updateReqVO) {
        fansTagService.updateWxFansTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除粉丝标签")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:delete')")
    public CommonResult<Boolean> deleteWxFansTag(@RequestParam("id") Integer id) {
        fansTagService.deleteWxFansTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得粉丝标签")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<FansTagRespVO> getWxFansTag(@RequestParam("id") Integer id) {
        WxUserTag wxFansTag = fansTagService.getWxFansTag(id);
        return success(WxFansTagConvert.INSTANCE.convert(wxFansTag));
    }

    @GetMapping("/list")
    @ApiOperation("获得粉丝标签列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<List<FansTagRespVO>> getWxFansTagList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxUserTag> list = fansTagService.getWxFansTagList(ids);
        return success(WxFansTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得粉丝标签分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<PageResult<FansTagRespVO>> getWxFansTagPage(@Valid FansTagPageReqVO pageVO) {
        PageResult<WxUserTag> pageResult = fansTagService.getWxFansTagPage(pageVO);
        return success(WxFansTagConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出粉丝标签 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:export')")
    @OperateLog(type = EXPORT)
    public void exportWxFansTagExcel(@Valid FansTagExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<WxUserTag> list = fansTagService.getWxFansTagList(exportReqVO);
        // 导出 Excel
        List<FansTagExcelVO> datas = WxFansTagConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "粉丝标签.xls", "数据", FansTagExcelVO.class, datas);
    }

}
