package cn.iocoder.yudao.module.mp.controller.admin.news;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.mp.controller.admin.news.vo.MpFreePublishPageReqVO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.freepublish.WxMpFreePublishItem;
import me.chanjar.weixin.mp.bean.freepublish.WxMpFreePublishList;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

// TODO 芋艿：权限
@Api(tags = "管理后台 - 公众号发布能力")
@RestController
@RequestMapping("/mp/free-publish")
@Validated
public class MpFreePublishController {

    @Resource
    private MpServiceFactory mpServiceFactory;

    @GetMapping("/page")
    @ApiOperation("获得已发布的图文分页")
    public CommonResult<PageResult<WxMpFreePublishItem>> getFreePublishPage(MpFreePublishPageReqVO reqVO) {
        // 从公众号查询已发布的图文列表
        WxMpService mpService = mpServiceFactory.getRequiredMpService(reqVO.getAccountId());
        WxMpFreePublishList publicationRecords;
        try {
            publicationRecords = mpService.getFreePublishService().getPublicationRecords(
                    PageUtils.getStart(reqVO), reqVO.getPageSize());
        } catch (WxErrorException e) {
            throw exception(FREE_PUBLISH_LIST_FAIL, e.getError().getErrorMsg());
        }
        // todo 芋艿：需要查询对应的缩略图，不然前端无法展示

        // 返回分页
        return success(new PageResult<>(publicationRecords.getItems(), publicationRecords.getTotalCount().longValue()));
    }

    @PostMapping("/submit")
    @ApiOperation("发布草稿")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "公众号账号的编号", required = true,
                    example = "1024", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "mediaId", value = "要发布的草稿的 media_id", required = true,
                    example = "2048", dataTypeClass = String.class)
    })
    public CommonResult<String> submitFreePublish(@RequestParam("accountId") Long accountId,
                                                  @RequestParam("mediaId") String mediaId) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        try {
            String publishId = mpService.getFreePublishService().submit(mediaId);
            return success(publishId);
        } catch (WxErrorException e) {
            throw exception(FREE_PUBLISH_SUBMIT_FAIL, e.getError().getErrorMsg());
        }
    }


}
