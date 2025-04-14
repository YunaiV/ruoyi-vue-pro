package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;

import java.util.List;


public interface FmsCompanyApi {

    List<FmsCompanyDTO> validateCompany(List<Long> ids);
}
