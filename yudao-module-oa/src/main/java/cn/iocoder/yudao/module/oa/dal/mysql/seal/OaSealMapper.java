package cn.iocoder.yudao.module.oa.dal.mysql.seal;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.OaSealPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 印章 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaSealMapper extends BaseMapperX<OaSealDO> {

    default PageResult<OaSealDO> selectPage(OaSealPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaSealDO>()
                .likeIfPresent(OaSealDO::getNo, reqVO.getNo())
                .likeIfPresent(OaSealDO::getName, reqVO.getName())
                .eqIfPresent(OaSealDO::getCategory, reqVO.getCategory())
                .eqIfPresent(OaSealDO::getType, reqVO.getType())
                .eqIfPresent(OaSealDO::getKeeperUserId, reqVO.getKeeperUserId())
                .eqIfPresent(OaSealDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(OaSealDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(OaSealDO::getPurchaseTime, reqVO.getPurchaseTime())
                .betweenIfPresent(OaSealDO::getEnableTime, reqVO.getEnableTime())
                .betweenIfPresent(OaSealDO::getDisableTime, reqVO.getDisableTime())
                .orderByAsc(OaSealDO::getSort)
                .orderByDesc(OaSealDO::getId));
    }

    default OaSealDO selectByNo(String no) {
        return selectOne(OaSealDO::getNo, no);
    }

}
