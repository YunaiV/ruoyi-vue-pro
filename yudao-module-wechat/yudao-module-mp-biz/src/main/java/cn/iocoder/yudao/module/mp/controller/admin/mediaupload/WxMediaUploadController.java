package cn.iocoder.yudao.module.mp.controller.admin.mediaupload;

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

import cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload.WxMediaUploadDO;
import cn.iocoder.yudao.module.mp.convert.mediaupload.WxMediaUploadConvert;
import cn.iocoder.yudao.module.mp.service.mediaupload.WxMediaUploadService;

@Api(tags = "管理后台 - 微信素材上传表 ")
@RestController
@RequestMapping("/wechatMp/wx-media-upload")
@Validated
public class WxMediaUploadController {

    @Resource
    private WxMediaUploadService wxMediaUploadService;

    @PostMapping("/create")
    @ApiOperation("创建微信素材上传表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:create')")
    public CommonResult<Integer> createWxMediaUpload(@Valid @RequestBody WxMediaUploadCreateReqVO createReqVO) {
        return success(wxMediaUploadService.createWxMediaUpload(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新微信素材上传表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:update')")
    public CommonResult<Boolean> updateWxMediaUpload(@Valid @RequestBody WxMediaUploadUpdateReqVO updateReqVO) {
        wxMediaUploadService.updateWxMediaUpload(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除微信素材上传表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:delete')")
    public CommonResult<Boolean> deleteWxMediaUpload(@RequestParam("id") Integer id) {
        wxMediaUploadService.deleteWxMediaUpload(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得微信素材上传表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:query')")
    public CommonResult<WxMediaUploadRespVO> getWxMediaUpload(@RequestParam("id") Integer id) {
        WxMediaUploadDO wxMediaUpload = wxMediaUploadService.getWxMediaUpload(id);
        return success(WxMediaUploadConvert.INSTANCE.convert(wxMediaUpload));
    }

    @GetMapping("/list")
    @ApiOperation("获得微信素材上传表 列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:query')")
    public CommonResult<List<WxMediaUploadRespVO>> getWxMediaUploadList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxMediaUploadDO> list = wxMediaUploadService.getWxMediaUploadList(ids);
        return success(WxMediaUploadConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得微信素材上传表 分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:query')")
    public CommonResult<PageResult<WxMediaUploadRespVO>> getWxMediaUploadPage(@Valid WxMediaUploadPageReqVO pageVO) {
        PageResult<WxMediaUploadDO> pageResult = wxMediaUploadService.getWxMediaUploadPage(pageVO);
        return success(WxMediaUploadConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出微信素材上传表  Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-media-upload:export')")
    @OperateLog(type = EXPORT)
    public void exportWxMediaUploadExcel(@Valid WxMediaUploadExportReqVO exportReqVO,
                                         HttpServletResponse response) throws IOException {
        List<WxMediaUploadDO> list = wxMediaUploadService.getWxMediaUploadList(exportReqVO);
        // 导出 Excel
        List<WxMediaUploadExcelVO> datas = WxMediaUploadConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "微信素材上传表 .xls", "数据", WxMediaUploadExcelVO.class, datas);
    }

}
