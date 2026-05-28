package cn.iocoder.yudao.module.yaya.controller.app.evaluation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.service.evaluation.YayaEvaluationService;
import jakarta.annotation.Resource;
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
@RequestMapping("/yaya/evaluations")
@Validated
public class YayaAppEvaluationController {

    @Resource
    private YayaEvaluationService evaluationService;

    @PostMapping("")
    public CommonResult<YayaAppEvaluationRespVO> createEvaluation(
            @Valid @RequestBody YayaAppEvaluationCreateReqVO reqVO) {
        return success(evaluationService.createEvaluation(getLoginUserId(), reqVO));
    }

    @GetMapping("/{evaluationId}")
    public CommonResult<YayaAppEvaluationRespVO> getEvaluation(@PathVariable("evaluationId") Long evaluationId) {
        return success(evaluationService.getEvaluation(getLoginUserId(), evaluationId));
    }

    @PostMapping("/{evaluationId}/polish-pack")
    public CommonResult<YayaAppEvaluationRespVO> createPolishPack(@PathVariable("evaluationId") Long evaluationId) {
        return success(evaluationService.createPolishPack(getLoginUserId(), evaluationId));
    }

}
