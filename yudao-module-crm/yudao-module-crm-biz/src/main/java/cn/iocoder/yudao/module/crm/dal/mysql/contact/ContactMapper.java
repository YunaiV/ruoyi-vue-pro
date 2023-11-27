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
                .eqIfPresent(ContactDO::getMobile, reqVO.getMobile())
                .eqIfPresent(ContactDO::getTelephone, reqVO.getTelephone())
                .eqIfPresent(ContactDO::getEmail, reqVO.getEmail())
                .eqIfPresent(ContactDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ContactDO::getName, reqVO.getName())
                .eqIfPresent(ContactDO::getQq, reqVO.getQq())
                .eqIfPresent(ContactDO::getWechat, reqVO.getWechat())
                .orderByDesc(ContactDO::getId));
    }

    default List<ContactDO> selectList(ContactPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ContactDO>()
                .eqIfPresent(ContactDO::getMobile, reqVO.getMobile())
                .eqIfPresent(ContactDO::getTelephone, reqVO.getTelephone())
                .eqIfPresent(ContactDO::getEmail, reqVO.getEmail())
                .eqIfPresent(ContactDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ContactDO::getName, reqVO.getName())
                .eqIfPresent(ContactDO::getQq, reqVO.getQq())
                .eqIfPresent(ContactDO::getWechat, reqVO.getWechat())
                .orderByDesc(ContactDO::getId));
    }

}
