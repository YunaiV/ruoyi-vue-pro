package cn.iocoder.yudao.adminserver.modules.system.convert.tenant;

import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantExcelVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantRespVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 租户 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SysTenantConvert {

    SysTenantConvert INSTANCE = Mappers.getMapper(SysTenantConvert.class);

    SysTenantDO convert(SysTenantCreateReqVO bean);

    SysTenantDO convert(SysTenantUpdateReqVO bean);

    SysTenantRespVO convert(SysTenantDO bean);

    List<SysTenantRespVO> convertList(List<SysTenantDO> list);

    PageResult<SysTenantRespVO> convertPage(PageResult<SysTenantDO> page);

    List<SysTenantExcelVO> convertList02(List<SysTenantDO> list);

}
