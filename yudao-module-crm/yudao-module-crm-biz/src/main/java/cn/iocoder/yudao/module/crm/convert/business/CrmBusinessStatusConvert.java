package cn.iocoder.yudao.module.crm.convert.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商机状态 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusConvert {

    CrmBusinessStatusConvert INSTANCE = Mappers.getMapper(CrmBusinessStatusConvert.class);

    List<CrmBusinessStatusRespVO> convertList(List<CrmBusinessStatusDO> list);

    PageResult<CrmBusinessStatusRespVO> convertPage(PageResult<CrmBusinessStatusDO> page);

}
