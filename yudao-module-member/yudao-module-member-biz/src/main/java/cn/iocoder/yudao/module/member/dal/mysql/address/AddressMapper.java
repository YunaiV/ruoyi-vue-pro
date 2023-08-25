package cn.iocoder.yudao.module.member.dal.mysql.address;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.address.vo.AddressPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper extends BaseMapperX<MemberAddressDO> {

    default MemberAddressDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(MemberAddressDO::getId, id, MemberAddressDO::getUserId, userId);
    }

    default List<MemberAddressDO> selectListByUserIdAndDefaulted(Long userId, Boolean defaulted) {
        return selectList(new LambdaQueryWrapperX<MemberAddressDO>().eq(MemberAddressDO::getUserId, userId)
                .eqIfPresent(MemberAddressDO::getDefaultStatus, defaulted));
    }
    default PageResult<MemberAddressDO> selectPage(AddressPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberAddressDO>()
                .eqIfPresent(MemberAddressDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MemberAddressDO::getName, reqVO.getName())
                .eqIfPresent(MemberAddressDO::getMobile, reqVO.getMobile())
                .eqIfPresent(MemberAddressDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(MemberAddressDO::getDetailAddress, reqVO.getDetailAddress())
                .eqIfPresent(MemberAddressDO::getDefaultStatus, reqVO.getDefaultStatus())
                .betweenIfPresent(MemberAddressDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberAddressDO::getId));
    }

    default List<MemberAddressDO> selectList(AddressExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MemberAddressDO>()
                .eqIfPresent(MemberAddressDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MemberAddressDO::getName, reqVO.getName())
                .eqIfPresent(MemberAddressDO::getMobile, reqVO.getMobile())
                .eqIfPresent(MemberAddressDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(MemberAddressDO::getDetailAddress, reqVO.getDetailAddress())
                .eqIfPresent(MemberAddressDO::getDefaultStatus, reqVO.getDefaultStatus())
                .betweenIfPresent(MemberAddressDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberAddressDO::getId));
    }

}
