package cn.iocoder.yudao.module.yaya.controller.admin.content.vo;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class YayaImportResultRespVO {

    private String seasonKey;
    private Integer topics;
    private Integer questions;
    private List<String> errors = new ArrayList<>();

    public static YayaImportResultRespVO from(YayaImportResultResp resp) {
        return new YayaImportResultRespVO()
                .setSeasonKey(resp.getSeasonKey())
                .setTopics(resp.getTopics())
                .setQuestions(resp.getQuestions())
                .setErrors(resp.getErrors());
    }

}
