package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.api.finance.dto.FmsFinanceSubjectDTO;

import java.util.List;


public interface FmsFinanceSubjectApi {

    List<FmsFinanceSubjectDTO> validateFinanceSubject(List<Long> ids);
}
