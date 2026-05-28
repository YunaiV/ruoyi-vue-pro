package cn.iocoder.yudao.module.yaya.controller.admin.content.vo;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import cn.hutool.core.collection.CollUtil;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class YayaTopicQuestionsSaveReqVO {

    @Valid
    private List<YayaQuestionSaveReqVO> questions = new ArrayList<>();

    public List<YayaQuestionSaveReq> toSaveReqs() {
        if (CollUtil.isEmpty(questions)) {
            return List.of();
        }
        return questions.stream().map(YayaQuestionSaveReqVO::toSaveReq).toList();
    }

}
