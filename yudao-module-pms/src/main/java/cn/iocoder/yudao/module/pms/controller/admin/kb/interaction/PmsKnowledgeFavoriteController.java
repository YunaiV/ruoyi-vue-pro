package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoritePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoriteSaveReqVO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识关注")
@RestController
@RequestMapping("/pms/kb/favorite")
@Validated
public class PmsKnowledgeFavoriteController {

    @Resource
    private PmsKnowledgeFavoriteService favoriteService;

    @PostMapping("/create")
    @Operation(summary = "关注知识对象")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> createFavorite(@Valid @RequestBody PmsKnowledgeFavoriteSaveReqVO saveReqVO) {
        favoriteService.createFavorite(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消关注知识对象")
    @Parameter(name = "type", description = "对象类型", required = true, example = "3")
    @Parameter(name = "entityId", description = "对象编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> deleteFavorite(
            @RequestParam("type") @InEnum(PmsKnowledgeObjectTypeEnum.class) Integer type,
            @RequestParam("entityId") Long entityId) {
        favoriteService.deleteFavorite(type, entityId, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得我的关注分页")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PageResult<PmsKnowledgeInteractionItemRespVO>> getFavoritePage(
            @Valid PmsKnowledgeFavoritePageReqVO pageReqVO) {
        return success(favoriteService.getFavoritePage(pageReqVO, getLoginUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得知识库内我的关注列表")
    @Parameter(name = "libraryId", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<List<PmsKnowledgeInteractionItemRespVO>> getFavoriteListByLibraryId(
            @RequestParam("libraryId") Long libraryId) {
        return success(favoriteService.getFavoriteListByLibraryId(libraryId, getLoginUserId()));
    }

}
