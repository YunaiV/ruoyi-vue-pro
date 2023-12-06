package cn.iocoder.yudao.module.crm.dal.mysql.contactbusinesslink;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.CrmContactBusinessLinkPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink.CrmContactBusinessLinkDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 联系人商机关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactBusinessLinkMapper extends BaseMapperX<CrmContactBusinessLinkDO> {

    default PageResult<CrmContactBusinessLinkDO> selectPage(CrmContactBusinessLinkPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmContactBusinessLinkDO>()
                .eqIfPresent(CrmContactBusinessLinkDO::getContactId, reqVO.getContactId())
                .eqIfPresent(CrmContactBusinessLinkDO::getBusinessId, reqVO.getBusinessId())
                .betweenIfPresent(CrmContactBusinessLinkDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmContactBusinessLinkDO::getId));
    } // TODO @zyna：方法和方法之间要有空行
    default PageResult<CrmContactBusinessLinkDO> selectPageByContact(CrmContactBusinessLinkPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmContactBusinessLinkDO>()
                .eqIfPresent(CrmContactBusinessLinkDO::getContactId, reqVO.getContactId())
                .orderByDesc(CrmContactBusinessLinkDO::getId));
    }
}