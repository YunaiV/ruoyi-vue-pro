package cn.iocoder.yudao.module.member.api.point;

import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;

import jakarta.validation.constraints.Min;

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
    void addPoint(Long userId, @Min(value = 1L, message = "积分必须是正数") Integer point,
                  Integer bizType, String bizId);

    /**
     * 减少用户积分
     *
     * @param userId  用户编号
     * @param point   积分
     * @param bizType 业务类型 {@link MemberPointBizTypeEnum}
     * @param bizId   业务编号
     */
    void reducePoint(Long userId, @Min(value = 1L, message = "积分必须是正数") Integer point,
                     Integer bizType, String bizId);

}
