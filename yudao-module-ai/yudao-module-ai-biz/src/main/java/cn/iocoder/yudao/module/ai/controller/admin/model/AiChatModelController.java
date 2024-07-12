package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI 聊天模型")
@RestController
@RequestMapping("/ai/chat-model")
@Validated
public class AiChatModelController {

    @Resource
    private AiChatModelService chatModelService;

    @PostMapping("/create")
    @Operation(summary = "创建聊天模型")
    @PreAuthorize("@ss.hasPermission('ai:chat-model:create')")
    public CommonResult<Long> createChatModel(@Valid @RequestBody AiChatModelSaveReqVO createReqVO) {
        return success(chatModelService.createChatModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新聊天模型")
    @PreAuthorize("@ss.hasPermission('ai:chat-model:update')")
    public CommonResult<Boolean> updateChatModel(@Valid @RequestBody AiChatModelSaveReqVO updateReqVO) {
        chatModelService.updateChatModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除聊天模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:chat-model:delete')")
    public CommonResult<Boolean> deleteChatModel(@RequestParam("id") Long id) {
        chatModelService.deleteChatModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得聊天模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:chat-model:query')")
    public CommonResult<AiChatModelRespVO> getChatModel(@RequestParam("id") Long id) {
        AiChatModelDO chatModel = chatModelService.getChatModel(id);
        return success(BeanUtils.toBean(chatModel, AiChatModelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得聊天模型分页")
    @PreAuthorize("@ss.hasPermission('ai:chat-model:query')")
    public CommonResult<PageResult<AiChatModelRespVO>> getChatModelPage(@Valid AiChatModelPageReqVO pageReqVO) {
        PageResult<AiChatModelDO> pageResult = chatModelService.getChatModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiChatModelRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得聊天模型列表")
    @Parameter(name = "status", description = "状态", required = true, example = "1")
    public CommonResult<List<AiChatModelRespVO>> getChatModelSimpleList(@RequestParam("status") Integer status) {
        List<AiChatModelDO> list = chatModelService.getChatModelListByStatus(status);
        return success(convertList(list, model -> new AiChatModelRespVO().setId(model.getId())
                .setName(model.getName()).setModel(model.getModel())));
    }

}