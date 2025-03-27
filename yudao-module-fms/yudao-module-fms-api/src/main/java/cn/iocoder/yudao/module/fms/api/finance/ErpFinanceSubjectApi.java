package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.api.finance.dto.ErpFinanceSubjectDTO;

import java.util.List;


public interface ErpFinanceSubjectApi {

    List<ErpFinanceSubjectDTO> validateFinanceSubject(List<Long> ids);
}
