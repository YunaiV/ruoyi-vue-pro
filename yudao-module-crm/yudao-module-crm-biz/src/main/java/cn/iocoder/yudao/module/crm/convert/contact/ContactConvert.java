package cn.iocoder.yudao.module.crm.convert.contact;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * crm联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ContactConvert {

    ContactConvert INSTANCE = Mappers.getMapper(ContactConvert.class);

    ContactDO convert(ContactCreateReqVO bean);

    ContactDO convert(ContactUpdateReqVO bean);

    ContactRespVO convert(ContactDO bean);

    List<ContactRespVO> convertList(List<ContactDO> list);

    PageResult<ContactRespVO> convertPage(PageResult<ContactDO> page);

    List<ContactExcelVO> convertList02(List<ContactDO> list);

    // TODO @puhui999：参考 CrmBusinessConvert 的修改建议
    default ContactDO convert(ContactDO contact, CrmContactTransferReqVO reqVO, Long userId) {
        Set<Long> rwUserIds = contact.getRwUserIds();
        rwUserIds.removeIf(item -> ObjUtil.equal(item, userId)); // 移除老负责人
        rwUserIds.add(reqVO.getOwnerUserId()); // 读写权限加入新的负人
        return new ContactDO().setId(contact.getId()).setOwnerUserId(reqVO.getOwnerUserId()) // 设置新负责人
                .setRwUserIds(rwUserIds);
    }

}
