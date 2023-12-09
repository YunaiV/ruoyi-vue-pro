package cn.iocoder.yudao.module.crm.dal.mysql.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.util.CrmQueryWrapperUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * CRM 联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactMapper extends BaseMapperX<CrmContactDO> {

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmContactDO>()
                .eq(CrmContactDO::getId, id)
                .set(CrmContactDO::getOwnerUserId, ownerUserId));
    }

    default PageResult<CrmContactDO> selectPage(CrmContactPageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmContactDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.builderPageQuery(query, CrmBizTypeEnum.CRM_CONTACT.getType(), CrmContactDO::getId,
                userId, pageReqVO.getSceneType(), pageReqVO.getPool());
        // 拼接自身的查询条件
        query.selectAll(CrmContactDO.class)
                .eqIfPresent(CrmContactDO::getCustomerId, pageReqVO.getCustomerId()) // 指定客户编号
                .likeIfPresent(CrmContactDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmContactDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmContactDO::getTelephone, pageReqVO.getTelephone())
                .eqIfPresent(CrmContactDO::getEmail, pageReqVO.getEmail())
                .eqIfPresent(CrmContactDO::getQq, pageReqVO.getQq())
                .eqIfPresent(CrmContactDO::getWechat, pageReqVO.getWechat())
                .orderByDesc(CrmContactDO::getId);
        return selectJoinPage(pageReqVO, CrmContactDO.class, query);
    }

    default List<CrmContactDO> selectBatchIds(Collection<Long> ids, Long userId) {
        MPJLambdaWrapperX<CrmContactDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.builderListQueryBatch(query, CrmBizTypeEnum.CRM_CONTACT.getType(), ids, userId);
        return selectJoinList(CrmContactDO.class, query);
    }

}
