package cn.iocoder.yudao.module.crm.convert.contact;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;

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
    List<ContactSimpleRespVO> convertAllList(List<ContactDO> list);

}
