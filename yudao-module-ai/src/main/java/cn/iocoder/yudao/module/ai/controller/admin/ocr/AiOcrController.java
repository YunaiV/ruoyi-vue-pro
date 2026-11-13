package cn.iocoder.yudao.module.ai.controller.admin.ocr;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ocr.AiOcrDO;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.ocr.AiOcrService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - OCR 文字识别
 *
 * 【我的】接口：用户、租户都由登录态决定，不接受前端传入；【OCR 管理】接口：按权限查看、删除本租户的全部记录
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - OCR 文字识别")
@RestController
@RequestMapping("/ai/ocr")
public class AiOcrController {

    @Resource
    private AiOcrService ocrService;

    @Resource
    private AiModelService modelService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/model-list")
    @Operation(summary = "获得可用的文字识别模型列表", description = "模型、API 密钥均启用，且平台支持文字识别；不返回密钥")
    public CommonResult<List<AiModelRespVO>> getOcrModelList() {
        return success(convertList(modelService.getOcrModelList(), model -> new AiModelRespVO()
                .setId(model.getId()).setName(model.getName()).setModel(model.getModel())
                .setPlatform(model.getPlatform()).setDescription(model.getDescription())
                .setIconUrl(model.getIconUrl())));
    }

    @PostMapping("/recognize")
    @Operation(summary = "识别图片中的文字", description = "图片先上传到文件服务；同步识别，成功、失败都会新增一条记录")
    public CommonResult<AiOcrRespVO> recognizeOcr(@Valid @RequestBody AiOcrRecognizeReqVO reqVO) {
        AiOcrDO ocr = ocrService.recognizeOcr(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(ocr, AiOcrRespVO.class));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】识别记录分页")
    public CommonResult<PageResult<AiOcrRespVO>> getOcrMyPage(@Valid PageParam pageReqVO) {
        PageResult<AiOcrDO> pageResult = ocrService.getOcrMyPage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiOcrRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】识别记录")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<AiOcrRespVO> getOcrMy(@RequestParam("id") Long id) {
        AiOcrDO ocr = ocrService.getOcrMy(id, getLoginUserId());
        return success(BeanUtils.toBean(ocr, AiOcrRespVO.class));
    }

    @PutMapping("/update-my")
    @Operation(summary = "保存【我的】识别修订", description = "仅识别成功的记录；空字符串表示清空内容")
    public CommonResult<AiOcrRespVO> updateOcrMy(@Valid @RequestBody AiOcrUpdateReqVO updateReqVO) {
        AiOcrDO ocr = ocrService.updateOcrMy(updateReqVO, getLoginUserId());
        return success(BeanUtils.toBean(ocr, AiOcrRespVO.class));
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】识别记录")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<Boolean> deleteOcrMy(@RequestParam("id") Long id) {
        ocrService.deleteOcrMy(id, getLoginUserId());
        return success(true);
    }

    // ================ OCR 管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得识别记录分页")
    @PreAuthorize("@ss.hasPermission('ai:ocr:query')")
    public CommonResult<PageResult<AiOcrRespVO>> getOcrPage(@Valid AiOcrPageReqVO pageReqVO) {
        PageResult<AiOcrDO> pageResult = ocrService.getOcrPage(pageReqVO);
        return success(new PageResult<>(buildOcrList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得识别记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:ocr:query')")
    public CommonResult<AiOcrRespVO> getOcr(@RequestParam("id") Long id) {
        AiOcrDO ocr = ocrService.getOcr(id);
        if (ocr == null) {
            return success(null);
        }
        return success(buildOcrList(List.of(ocr)).get(0));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除识别记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:ocr:delete')")
    public CommonResult<Boolean> deleteOcr(@RequestParam("id") Long id) {
        ocrService.deleteOcr(id);
        return success(true);
    }

    /**
     * 构建识别记录列表：管理端拼接用户昵称
     */
    private List<AiOcrRespVO> buildOcrList(List<AiOcrDO> list) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiOcrDO::getUserId));
        return BeanUtils.toBean(list, AiOcrRespVO.class, ocr ->
                MapUtils.findAndThen(userMap, ocr.getUserId(), user -> ocr.setUserNickname(user.getNickname())));
    }

}
