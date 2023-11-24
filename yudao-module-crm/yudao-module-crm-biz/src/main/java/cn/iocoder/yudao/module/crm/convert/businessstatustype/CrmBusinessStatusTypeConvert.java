package cn.iocoder.yudao.module.crm.convert.businessstatustype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessStatusTypeRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessStatusTypeSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商机状态类型 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusTypeConvert {

    CrmBusinessStatusTypeConvert INSTANCE = Mappers.getMapper(CrmBusinessStatusTypeConvert.class);

    CrmBusinessStatusTypeDO convert(CrmBusinessStatusTypeSaveReqVO bean);

    CrmBusinessStatusTypeRespVO convert(CrmBusinessStatusTypeDO bean);

    PageResult<CrmBusinessStatusTypeRespVO> convertPage(PageResult<CrmBusinessStatusTypeDO> page);

    default PageResult<CrmBusinessStatusTypeRespVO> convertPage(PageResult<CrmBusinessStatusTypeDO> page, List<DeptRespDTO> deptList) {
        PageResult<CrmBusinessStatusTypeRespVO> pageResult = convertPage(page);
        Map<Long, String> deptMap = convertMap(deptList, DeptRespDTO::getId, DeptRespDTO::getName);
        pageResult.getList().stream().forEach(r -> {
            r.setDeptNames(convertList(r.getDeptIds(), deptMap::get));
        });
        return pageResult;
    }

    default CrmBusinessStatusTypeRespVO convert(CrmBusinessStatusTypeDO bean, List<CrmBusinessStatusDO> statusList) {
        CrmBusinessStatusTypeRespVO result = convert(bean);
        result.setStatusList(statusList);
        return result;
    }


}
