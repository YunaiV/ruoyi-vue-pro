package cn.iocoder.yudao.module.yaya.controller.app.practice;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppFavoriteCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.service.practice.YayaPracticeService;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/yaya/practice")
@Validated
public class YayaAppPracticeController {

    @Resource
    private YayaPracticeService practiceService;

    @GetMapping("/topics")
    @PermitAll
    @TenantIgnore
    public CommonResult<PageResult<YayaAppPracticeTopicRespVO>> getTopicPage(
            @Valid YayaAppPracticeTopicPageReqVO pageReqVO) {
        return success(practiceService.getTopicPage(pageReqVO, getLoginUserId()));
    }

    @GetMapping("/topics/{id}")
    @PermitAll
    @TenantIgnore
    public CommonResult<YayaAppPracticeTopicDetailRespVO> getTopic(@PathVariable("id") Long id) {
        return success(practiceService.getTopicDetail(id, getLoginUserId()));
    }

    @PostMapping("/favorites")
    public CommonResult<Long> createFavorite(@Valid @RequestBody YayaAppFavoriteCreateReqVO reqVO) {
        return success(practiceService.createFavorite(getLoginUserId(), reqVO.getTopicId()));
    }

    @PostMapping("/attempts")
    public CommonResult<Long> createAttempt(@Valid @RequestBody YayaAppPracticeAttemptCreateReqVO reqVO) {
        return success(practiceService.createAttempt(getLoginUserId(), reqVO));
    }

}
