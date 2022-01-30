package cn.iocoder.yudao.module.system.convert.tenant;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantExcelVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantRespVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
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

    TenantDO convert(TenantCreateReqVO bean);

    TenantDO convert(TenantUpdateReqVO bean);

    TenantRespVO convert(TenantDO bean);

    List<TenantRespVO> convertList(List<TenantDO> list);

    PageResult<TenantRespVO> convertPage(PageResult<TenantDO> page);

    List<TenantExcelVO> convertList02(List<TenantDO> list);

}
