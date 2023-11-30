package cn.iocoder.yudao.module.crm.dal.mysql.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactMapper extends BaseMapperX<CrmContactDO> {

    // TODO @puhui999：数据权限
    default PageResult<CrmContactDO> selectPage(CrmContactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmContactDO>()
                .eqIfPresent(CrmContactDO::getMobile, reqVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, reqVO.getTelephone())
                .eqIfPresent(CrmContactDO::getEmail, reqVO.getEmail())
                .eqIfPresent(CrmContactDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(CrmContactDO::getName, reqVO.getName())
                .eqIfPresent(CrmContactDO::getQq, reqVO.getQq())
                .eqIfPresent(CrmContactDO::getWechat, reqVO.getWechat())
                .orderByDesc(CrmContactDO::getId));
    }

    default PageResult<CrmContactDO> selectPageByCustomer(CrmContactPageReqVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<CrmContactDO>()
                .eq(CrmContactDO::getCustomerId, pageVO.getCustomerId()) // 必须传递
                .likeIfPresent(CrmContactDO::getName, pageVO.getName())
                .eqIfPresent(CrmContactDO::getMobile, pageVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, pageVO.getTelephone())
                .eqIfPresent(CrmContactDO::getEmail, pageVO.getEmail())
                .eqIfPresent(CrmContactDO::getQq, pageVO.getQq())
                .eqIfPresent(CrmContactDO::getWechat, pageVO.getWechat())
                .orderByDesc(CrmContactDO::getId));
    }

}
