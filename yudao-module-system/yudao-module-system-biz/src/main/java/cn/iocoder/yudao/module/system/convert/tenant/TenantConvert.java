package cn.iocoder.yudao.module.system.convert.tenant;

import cn.iocoder.yudao.module.system.api.user.dto.AdminUserSaveReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 租户 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    default AdminUserSaveReqDTO convert02(TenantSaveReqVO bean) {
        AdminUserSaveReqDTO reqDTO = new AdminUserSaveReqDTO();
        reqDTO.setUsername(bean.getUsername());
        reqDTO.setPassword(bean.getPassword());
        reqDTO.setNickname(bean.getContactName()).setMobile(bean.getContactMobile());
        return reqDTO;
    }

}
