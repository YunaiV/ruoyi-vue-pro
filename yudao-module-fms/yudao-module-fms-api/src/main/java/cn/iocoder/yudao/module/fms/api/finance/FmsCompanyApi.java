package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface FmsCompanyApi {

    List<FmsCompanyDTO> validateCompany(Set<Long> ids);

    List<FmsCompanyDTO> getCompanyList(Set<Long> ids);

    Map<Long,FmsCompanyDTO> getCompanyMap(Set<Long> ids);

    Map<String, FmsCompanyDTO> getCompanyMapByNames(Set<String> companyNames);
}
