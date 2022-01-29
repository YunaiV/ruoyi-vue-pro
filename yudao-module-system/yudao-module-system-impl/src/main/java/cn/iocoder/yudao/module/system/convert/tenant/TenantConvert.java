package cn.iocoder.yudao.module.system.convert.tenant;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantExcelVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantRespVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantUpdateReqVO;
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
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    SysTenantDO convert(TenantCreateReqVO bean);

    SysTenantDO convert(TenantUpdateReqVO bean);

    TenantRespVO convert(SysTenantDO bean);

    List<TenantRespVO> convertList(List<SysTenantDO> list);

    PageResult<TenantRespVO> convertPage(PageResult<SysTenantDO> page);

    List<TenantExcelVO> convertList02(List<SysTenantDO> list);

}
