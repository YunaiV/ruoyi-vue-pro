package cn.iocoder.yudao.module.oa.dal.mysql.reimbursement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement.OaReimbursementDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 费用报销 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaReimbursementMapper extends BaseMapperX<OaReimbursementDO> {

    default PageResult<OaReimbursementDO> selectPage(Long userId, OaReimbursementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaReimbursementDO>()
                .eq(OaReimbursementDO::getCreator, userId.toString())
                .likeIfPresent(OaReimbursementDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaReimbursementDO::getStatus, reqVO.getStatus())
                .orderByDesc(OaReimbursementDO::getCreateTime, OaReimbursementDO::getId));
    }

}
