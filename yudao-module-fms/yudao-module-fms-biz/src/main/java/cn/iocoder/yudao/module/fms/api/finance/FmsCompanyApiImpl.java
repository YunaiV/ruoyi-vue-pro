package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import cn.iocoder.yudao.module.fms.service.finance.subject.FmsCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.module.fms.api.enums.FmsErrorCodeConstants.FINANCE_SUBJECT_NOT_EXISTS;

@Service
public class FmsCompanyApiImpl implements FmsCompanyApi {

    @Autowired
    FmsCompanyService subjectService;


    @Override
    public List<FmsCompanyDTO> validateCompany(Set<Long> ids) {
        List<FmsCompanyDTO> dtoList = BeanUtils.toBean(subjectService.listCompany(ids), FmsCompanyDTO.class);
        //如果ids长度不等于dtoList长度，则说明有id不存在
        if (ids.size() != dtoList.size()) {
            List<Long> idList = dtoList.stream().map(FmsCompanyDTO::getId).toList();
            List<Long> notExistIds = ids.stream().filter(id -> !idList.contains(id)).toList();
            ThrowUtil.ifThrow(!notExistIds.isEmpty(), FINANCE_SUBJECT_NOT_EXISTS, notExistIds);
        }
        return dtoList;
    }


    @Override
    public List<FmsCompanyDTO> getCompanyList(Set<Long> ids) {
        return BeanUtils.toBean(subjectService.listCompany(ids), FmsCompanyDTO.class);
    }

    @Override
    public Map<Long,FmsCompanyDTO> getCompanyMap(Set<Long> ids) {
         return StreamX.from(getCompanyList(ids)).toMap(FmsCompanyDTO::getId);
    }

    @Override
    public Map<String, FmsCompanyDTO> getCompanyMapByNames(Set<String> companyNames) {

        if(CollectionUtils.isEmpty(companyNames)) {
            return Map.of();
        }
        List<FmsCompanyDO> listCompanyByNames =  subjectService.listCompanyByNames(companyNames);
        return StreamX.from(listCompanyByNames).toMap(FmsCompanyDO::getName,e->BeanUtils.toBean(e,FmsCompanyDTO.class));

    }
}
