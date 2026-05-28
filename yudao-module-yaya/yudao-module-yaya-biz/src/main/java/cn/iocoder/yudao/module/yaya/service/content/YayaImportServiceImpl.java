package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class YayaImportServiceImpl implements YayaImportService {

    @Override
    public YayaImportResultResp previewImport(String seasonKey) {
        return emptyResult(seasonKey);
    }

    @Override
    public YayaImportResultResp runImport(String seasonKey) {
        throw exception(NOT_IMPLEMENTED);
    }

    private static YayaImportResultResp emptyResult(String seasonKey) {
        return new YayaImportResultResp()
                .setSeasonKey(seasonKey)
                .setTopics(0)
                .setQuestions(0)
                .setErrors(List.of());
    }

}
