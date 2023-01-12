package cn.iocoder.yudao.module.mp.controller.admin.news;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.mp.controller.admin.news.vo.MpDraftPageReqVO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.WxMpAddDraft;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftItem;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftList;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.DRAFT_CREATE_FAIL;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.DRAFT_LIST_FAIL;

// TODO 芋艿：权限
@Api(tags = "管理后台 - 公众号草稿")
@RestController
@RequestMapping("/mp/draft")
@Validated
public class MpDraftController {

    @Resource
    private MpServiceFactory mpServiceFactory;

    @GetMapping("/page")
    @ApiOperation("获得草稿分页")
    public CommonResult<PageResult<WxMpDraftItem>> getDraftPage(MpDraftPageReqVO reqVO) {
        // 从公众号查询已发布的图文列表
        WxMpService mpService = mpServiceFactory.getRequiredMpService(reqVO.getAccountId());
        WxMpDraftList draftList;
        try {
            draftList = mpService.getDraftService().listDraft(PageUtils.getStart(reqVO), reqVO.getPageSize());
        } catch (WxErrorException e) {
            throw exception(DRAFT_LIST_FAIL, e.getError().getErrorMsg());
        }

        // 返回分页
        return success(new PageResult<>(draftList.getItems(), draftList.getTotalCount().longValue()));
    }

    @PostMapping("/create")
    @ApiOperation("创建草稿")
    @ApiImplicitParam(name = "accountId", value = "公众号账号的编号", required = true,
            example = "1024", dataTypeClass = Long.class)
    public CommonResult<String> createDraft(@RequestParam("accountId") Long accountId,
                                            @RequestBody WxMpAddDraft reqVO) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        try {
            String mediaId = mpService.getDraftService().addDraft(reqVO);
            return success(mediaId);
        } catch (WxErrorException e) {
            throw exception(DRAFT_CREATE_FAIL, e.getError().getErrorMsg());
        }
    }

}
