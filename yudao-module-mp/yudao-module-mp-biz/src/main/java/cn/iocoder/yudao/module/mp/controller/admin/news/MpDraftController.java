package cn.iocoder.yudao.module.mp.controller.admin.news;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.mp.controller.admin.news.vo.MpDraftPageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.material.MpMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

@Tag(name = "管理后台 - 公众号草稿")
@RestController
@RequestMapping("/mp/draft")
@Validated
public class MpDraftController {

    @Resource
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpMaterialService mpMaterialService;

    @GetMapping("/page")
    @Operation(summary = "获得草稿分页")
    @PreAuthorize("@ss.hasPermission('mp:draft:query')")
    public CommonResult<PageResult<WxMpDraftItem>> getDraftPage(MpDraftPageReqVO reqVO) {
        // 从公众号查询草稿箱
        WxMpService mpService = mpServiceFactory.getRequiredMpService(reqVO.getAccountId());
        WxMpDraftList draftList;
        try {
            draftList = mpService.getDraftService().listDraft(PageUtils.getStart(reqVO), reqVO.getPageSize());
        } catch (WxErrorException e) {
            throw exception(DRAFT_LIST_FAIL, e.getError().getErrorMsg());
        }
        // 查询对应的图片地址。目的：解决公众号的图片链接无法在我们后台展示
        setDraftThumbUrl(draftList.getItems());

        // 返回分页
        return success(new PageResult<>(draftList.getItems(), draftList.getTotalCount().longValue()));
    }

    private void setDraftThumbUrl(List<WxMpDraftItem> items) {
        // 1.1 获得 mediaId 数组
        Set<String> mediaIds = new HashSet<>();
        items.forEach(item -> item.getContent().getNewsItem().forEach(newsItem -> mediaIds.add(newsItem.getThumbMediaId())));
        if (CollUtil.isEmpty(mediaIds)) {
            return;
        }
        // 1.2 批量查询对应的 Media 素材
        Map<String, MpMaterialDO> materials = CollectionUtils.convertMap(mpMaterialService.getMaterialListByMediaId(mediaIds),
                MpMaterialDO::getMediaId);

        // 2. 设置回 WxMpDraftItem 记录
        items.forEach(item -> item.getContent().getNewsItem().forEach(newsItem ->
                findAndThen(materials, newsItem.getThumbMediaId(), material -> newsItem.setThumbUrl(material.getUrl()))));
    }

    @PostMapping("/create")
    @Operation(summary = "创建草稿")
    @Parameter(name = "accountId", description = "公众号账号的编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mp:draft:create')")
    public CommonResult<String> deleteDraft(@RequestParam("accountId") Long accountId,
                                            @RequestBody WxMpAddDraft draft) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        try {
            String mediaId = mpService.getDraftService().addDraft(draft);
            return success(mediaId);
        } catch (WxErrorException e) {
            throw exception(DRAFT_CREATE_FAIL, e.getError().getErrorMsg());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新草稿")
    @Parameters({
            @Parameter(name = "accountId", description = "公众号账号的编号", required = true, example = "1024"),
            @Parameter(name = "mediaId", description = "草稿素材的编号", required = true, example = "xxx")
    })
    @PreAuthorize("@ss.hasPermission('mp:draft:update')")
    public CommonResult<Boolean> deleteDraft(@RequestParam("accountId") Long accountId,
                                             @RequestParam("mediaId") String mediaId,
                                             @RequestBody List<WxMpDraftArticles> articles) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        try {
            for (int i = 0; i < articles.size(); i++) {
                WxMpDraftArticles article = articles.get(i);
                mpService.getDraftService().updateDraft(new WxMpUpdateDraft(mediaId, i, article));
            }
            return success(true);
        } catch (WxErrorException e) {
            throw exception(DRAFT_UPDATE_FAIL, e.getError().getErrorMsg());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除草稿")
    @Parameters({
            @Parameter(name = "accountId", description = "公众号账号的编号", required = true, example = "1024"),
            @Parameter(name = "mediaId", description = "草稿素材的编号", required = true, example = "xxx")
    })
    @PreAuthorize("@ss.hasPermission('mp:draft:delete')")
    public CommonResult<Boolean> deleteDraft(@RequestParam("accountId") Long accountId,
                                             @RequestParam("mediaId") String mediaId) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        try {
            mpService.getDraftService().delDraft(mediaId);
            return success(true);
        } catch (WxErrorException e) {
            throw exception(DRAFT_DELETE_FAIL, e.getError().getErrorMsg());
        }
    }

}
