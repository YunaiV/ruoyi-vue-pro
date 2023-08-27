package cn.iocoder.yudao.module.member.api.point;

import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;

/**
 * 用户积分的 API 接口
 *
 * @author owen
 */
public interface MemberPointApi {

    /**
     * 增加用户积分
     *
     * @param userId  用户编号
     * @param point   积分
     * @param bizType 业务类型 {@link MemberPointBizTypeEnum}
     * @param bizId   业务编号
     */
    void addPoint(Long userId, Integer point, Integer bizType, String bizId);

}
