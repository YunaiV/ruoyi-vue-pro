package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment.PmsKnowledgeDocumentCommentRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment.PmsKnowledgeDocumentCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentCommentDO;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentCommentService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文档评论")
@RestController
@RequestMapping("/pms/kb/document-comment")
@Validated
public class PmsKnowledgeDocumentCommentController {

    @Resource
    private PmsKnowledgeDocumentCommentService commentService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建知识库文档评论")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Long> createDocumentComment(@Valid @RequestBody PmsKnowledgeDocumentCommentSaveReqVO saveReqVO) {
        return success(commentService.createDocumentComment(saveReqVO, getLoginUserId()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库文档评论")
    @Parameter(name = "id", description = "评论编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> deleteDocumentComment(@RequestParam("id") Long id) {
        commentService.deleteDocumentComment(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得知识库文档评论列表")
    @Parameter(name = "documentId", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<List<PmsKnowledgeDocumentCommentRespVO>> getDocumentCommentList(
            @RequestParam("documentId") Long documentId) {
        List<PmsKnowledgeDocumentCommentDO> comments = commentService.getDocumentCommentList(documentId, getLoginUserId());
        return success(buildCommentRespVOList(comments));
    }

    // ==================== 拼接 VO ====================

    private List<PmsKnowledgeDocumentCommentRespVO> buildCommentRespVOList(
            List<PmsKnowledgeDocumentCommentDO> comments) {
        if (CollUtil.isEmpty(comments)) {
            return Collections.emptyList();
        }
        // 1. 批量查询评论人和回复对象
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(comments,
                comment -> Stream.of(comment.getUserId(), comment.getReplyUserId())));

        // 2. 批量转换评论，避免回复组装依赖查询顺序
        Map<Long, PmsKnowledgeDocumentCommentRespVO> commentMap = convertMap(comments,
                PmsKnowledgeDocumentCommentDO::getId, comment -> {
                    PmsKnowledgeDocumentCommentRespVO commentVO = BeanUtils.toBean(comment,
                            PmsKnowledgeDocumentCommentRespVO.class).setChildren(new ArrayList<>());
                    findAndThen(userMap, comment.getUserId(), user -> commentVO.setUserName(user.getNickname()));
                    findAndThen(userMap, comment.getReplyUserId(),
                            user -> commentVO.setReplyUserName(user.getNickname()));
                    return commentVO;
                });

        // 3. 按主评论组织回复列表
        List<PmsKnowledgeDocumentCommentRespVO> result = new ArrayList<>();
        for (PmsKnowledgeDocumentCommentDO comment : comments) {
            PmsKnowledgeDocumentCommentRespVO commentVO = commentMap.get(comment.getId());
            if (PmsKnowledgeDocumentCommentDO.MAIN_ID_ROOT.equals(comment.getMainId())) {
                result.add(commentVO);
                continue;
            }
            findAndThen(commentMap, comment.getMainId(), mainComment -> mainComment.getChildren().add(commentVO));
        }
        return result;
    }

}
