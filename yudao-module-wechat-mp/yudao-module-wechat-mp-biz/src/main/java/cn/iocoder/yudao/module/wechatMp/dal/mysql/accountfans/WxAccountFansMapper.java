package cn.iocoder.yudao.module.wechatMp.dal.mysql.accountfans;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans.WxAccountFansDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.*;

/**
 * 微信公众号粉丝 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountFansMapper extends BaseMapperX<WxAccountFansDO> {

    default PageResult<WxAccountFansDO> selectPage(WxAccountFansPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxAccountFansDO>()
                .eqIfPresent(WxAccountFansDO::getOpenid, reqVO.getOpenid())
                .eqIfPresent(WxAccountFansDO::getSubscribeStatus, reqVO.getSubscribeStatus())
                .betweenIfPresent(WxAccountFansDO::getSubscribeTime, reqVO.getBeginSubscribeTime(), reqVO.getEndSubscribeTime())
                .likeIfPresent(WxAccountFansDO::getNickname, reqVO.getNickname())
                .eqIfPresent(WxAccountFansDO::getGender, reqVO.getGender())
                .eqIfPresent(WxAccountFansDO::getLanguage, reqVO.getLanguage())
                .eqIfPresent(WxAccountFansDO::getCountry, reqVO.getCountry())
                .eqIfPresent(WxAccountFansDO::getProvince, reqVO.getProvince())
                .eqIfPresent(WxAccountFansDO::getCity, reqVO.getCity())
                .eqIfPresent(WxAccountFansDO::getHeadimgUrl, reqVO.getHeadimgUrl())
                .eqIfPresent(WxAccountFansDO::getRemark, reqVO.getRemark())
                .eqIfPresent(WxAccountFansDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxAccountFansDO::getWxAccountAppid, reqVO.getWxAccountAppid())
                .betweenIfPresent(WxAccountFansDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountFansDO::getId));
    }

    default List<WxAccountFansDO> selectList(WxAccountFansExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxAccountFansDO>()
                .eqIfPresent(WxAccountFansDO::getOpenid, reqVO.getOpenid())
                .eqIfPresent(WxAccountFansDO::getSubscribeStatus, reqVO.getSubscribeStatus())
                .betweenIfPresent(WxAccountFansDO::getSubscribeTime, reqVO.getBeginSubscribeTime(), reqVO.getEndSubscribeTime())
                .likeIfPresent(WxAccountFansDO::getNickname, reqVO.getNickname())
                .eqIfPresent(WxAccountFansDO::getGender, reqVO.getGender())
                .eqIfPresent(WxAccountFansDO::getLanguage, reqVO.getLanguage())
                .eqIfPresent(WxAccountFansDO::getCountry, reqVO.getCountry())
                .eqIfPresent(WxAccountFansDO::getProvince, reqVO.getProvince())
                .eqIfPresent(WxAccountFansDO::getCity, reqVO.getCity())
                .eqIfPresent(WxAccountFansDO::getHeadimgUrl, reqVO.getHeadimgUrl())
                .eqIfPresent(WxAccountFansDO::getRemark, reqVO.getRemark())
                .eqIfPresent(WxAccountFansDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxAccountFansDO::getWxAccountAppid, reqVO.getWxAccountAppid())
                .betweenIfPresent(WxAccountFansDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountFansDO::getId));
    }

}
