package cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;

import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem.WxNewsArticleItemDO;
import cn.iocoder.yudao.module.mp.convert.newsarticleitem.WxNewsArticleItemConvert;
import cn.iocoder.yudao.module.mp.service.newsarticleitem.WxNewsArticleItemService;

@Api(tags = "管理后台 - 图文消息文章列表表 ")
@RestController
@RequestMapping("/wechatMp/wx-news-article-item")
@Validated
public class WxNewsArticleItemController {

    @Resource
    private WxNewsArticleItemService wxNewsArticleItemService;

    @PostMapping("/create")
    @ApiOperation("创建图文消息文章列表表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:create')")
    public CommonResult<Integer> createWxNewsArticleItem(@Valid @RequestBody WxNewsArticleItemCreateReqVO createReqVO) {
        return success(wxNewsArticleItemService.createWxNewsArticleItem(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新图文消息文章列表表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:update')")
    public CommonResult<Boolean> updateWxNewsArticleItem(@Valid @RequestBody WxNewsArticleItemUpdateReqVO updateReqVO) {
        wxNewsArticleItemService.updateWxNewsArticleItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除图文消息文章列表表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:delete')")
    public CommonResult<Boolean> deleteWxNewsArticleItem(@RequestParam("id") Integer id) {
        wxNewsArticleItemService.deleteWxNewsArticleItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得图文消息文章列表表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:query')")
    public CommonResult<WxNewsArticleItemRespVO> getWxNewsArticleItem(@RequestParam("id") Integer id) {
        WxNewsArticleItemDO wxNewsArticleItem = wxNewsArticleItemService.getWxNewsArticleItem(id);
        return success(WxNewsArticleItemConvert.INSTANCE.convert(wxNewsArticleItem));
    }

    @GetMapping("/list")
    @ApiOperation("获得图文消息文章列表表 列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:query')")
    public CommonResult<List<WxNewsArticleItemRespVO>> getWxNewsArticleItemList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxNewsArticleItemDO> list = wxNewsArticleItemService.getWxNewsArticleItemList(ids);
        return success(WxNewsArticleItemConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得图文消息文章列表表 分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:query')")
    public CommonResult<PageResult<WxNewsArticleItemRespVO>> getWxNewsArticleItemPage(@Valid WxNewsArticleItemPageReqVO pageVO) {
        PageResult<WxNewsArticleItemDO> pageResult = wxNewsArticleItemService.getWxNewsArticleItemPage(pageVO);
        return success(WxNewsArticleItemConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出图文消息文章列表表  Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-article-item:export')")
    @OperateLog(type = EXPORT)
    public void exportWxNewsArticleItemExcel(@Valid WxNewsArticleItemExportReqVO exportReqVO,
                                             HttpServletResponse response) throws IOException {
        List<WxNewsArticleItemDO> list = wxNewsArticleItemService.getWxNewsArticleItemList(exportReqVO);
        // 导出 Excel
        List<WxNewsArticleItemExcelVO> datas = WxNewsArticleItemConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "图文消息文章列表表 .xls", "数据", WxNewsArticleItemExcelVO.class, datas);
    }

}
