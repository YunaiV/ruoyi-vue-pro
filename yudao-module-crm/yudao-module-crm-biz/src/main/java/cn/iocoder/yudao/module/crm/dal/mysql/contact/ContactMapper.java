package cn.iocoder.yudao.module.crm.dal.mysql.contact;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;

/**
 * crm联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ContactMapper extends BaseMapperX<ContactDO> {

    default PageResult<ContactDO> selectPage(ContactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContactDO>()
                .likeIfPresent(ContactDO::getName, reqVO.getName())
                .betweenIfPresent(ContactDO::getNextTime, reqVO.getNextTime())
                .eqIfPresent(ContactDO::getMobile, reqVO.getMobile())
                .eqIfPresent(ContactDO::getTelephone, reqVO.getTelephone())
                .eqIfPresent(ContactDO::getEmail, reqVO.getEmail())
                .eqIfPresent(ContactDO::getPost, reqVO.getPost())
                .eqIfPresent(ContactDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ContactDO::getAddress, reqVO.getAddress())
                .eqIfPresent(ContactDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ContactDO::getOwnerUserId, reqVO.getOwnerUserId())
                .betweenIfPresent(ContactDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ContactDO::getLastTime, reqVO.getLastTime())
                .orderByDesc(ContactDO::getId));
    }

    default List<ContactDO> selectList(ContactExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ContactDO>()
                .likeIfPresent(ContactDO::getName, reqVO.getName())
                .betweenIfPresent(ContactDO::getNextTime, reqVO.getNextTime())
                .eqIfPresent(ContactDO::getMobile, reqVO.getMobile())
                .eqIfPresent(ContactDO::getTelephone, reqVO.getTelephone())
                .eqIfPresent(ContactDO::getEmail, reqVO.getEmail())
                .eqIfPresent(ContactDO::getPost, reqVO.getPost())
                .eqIfPresent(ContactDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ContactDO::getAddress, reqVO.getAddress())
                .eqIfPresent(ContactDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ContactDO::getOwnerUserId, reqVO.getOwnerUserId())
                .betweenIfPresent(ContactDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ContactDO::getLastTime, reqVO.getLastTime())
                .orderByDesc(ContactDO::getId));
    }

}
