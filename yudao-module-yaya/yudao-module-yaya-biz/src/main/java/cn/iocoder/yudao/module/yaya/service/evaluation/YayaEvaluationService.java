package cn.iocoder.yudao.module.yaya.service.evaluation;

import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;

public interface YayaEvaluationService {

    YayaAppEvaluationRespVO createEvaluation(Long memberUserId, YayaAppEvaluationCreateReqVO reqVO);

    YayaAppEvaluationRespVO getEvaluation(Long memberUserId, Long evaluationId);

    YayaAppEvaluationRespVO createPolishPack(Long memberUserId, Long evaluationId);

}
