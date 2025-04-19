package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;

import java.util.List;
import java.util.Map;


public interface FmsCompanyApi {

    List<FmsCompanyDTO> validateCompany(List<Long> ids);

    List<FmsCompanyDTO> getCompanyList(List<Long> ids);

    Map<Long,FmsCompanyDTO> getCompanyMap(List<Long> ids);
}
