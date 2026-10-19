package cn.iocoder.yudao.module.oa.dal.mysql.travel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.OaTravelReimbursementPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelReimbursementDO;
import org.apache.ibatis.annotations.Mapper;


/**
 * OA 出差报销 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaTravelReimbursementMapper extends BaseMapperX<OaTravelReimbursementDO> {

    default PageResult<OaTravelReimbursementDO> selectPage(Long userId, OaTravelReimbursementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaTravelReimbursementDO>()
                .eq(OaTravelReimbursementDO::getCreator, userId.toString())
                .likeIfPresent(OaTravelReimbursementDO::getNo, reqVO.getNo())
                .eqIfPresent(OaTravelReimbursementDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaTravelReimbursementDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(OaTravelReimbursementDO::getPayStatus, reqVO.getPayStatus())
                .betweenIfPresent(OaTravelReimbursementDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OaTravelReimbursementDO::getId));
    }

    default OaTravelReimbursementDO selectByNo(String no) {
        return selectOne(OaTravelReimbursementDO::getNo, no);
    }

}
