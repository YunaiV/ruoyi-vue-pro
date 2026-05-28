package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;

public interface YayaImportService {

    YayaImportResultResp previewImport(String seasonKey);

    YayaImportResultResp runImport(String seasonKey);

}
