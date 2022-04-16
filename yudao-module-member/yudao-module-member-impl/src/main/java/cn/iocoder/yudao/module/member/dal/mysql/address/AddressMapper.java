package cn.iocoder.yudao.module.member.dal.mysql.address;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.member.controller.app.address.vo.*;

/**
 * 用户收件地址 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AddressMapper extends BaseMapperX<AddressDO> {

    default PageResult<AddressDO> selectPage(AppAddressPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AddressDO>()
                .eqIfPresent(AddressDO::getUserId, reqVO.getUserId())
                .likeIfPresent(AddressDO::getName, reqVO.getName())
                .eqIfPresent(AddressDO::getMobile, reqVO.getMobile())
                .eqIfPresent(AddressDO::getAreaCode, reqVO.getAreaCode())
                .eqIfPresent(AddressDO::getDetailAddress, reqVO.getDetailAddress())
                .eqIfPresent(AddressDO::getType, reqVO.getType())
                .betweenIfPresent(AddressDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(AddressDO::getId));
    }

}
