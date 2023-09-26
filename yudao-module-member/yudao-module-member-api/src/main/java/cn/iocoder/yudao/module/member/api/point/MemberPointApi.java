package cn.iocoder.yudao.module.member.api.point;

import cn.iocoder.yudao.module.member.api.point.dto.MemberPointConfigRespDTO;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;

import javax.validation.constraints.Min;

/**
 * 用户积分的 API 接口
 *
 * @author owen
 */
public interface MemberPointApi {

    // TODO @疯狂：这个我们要不要搞成通用的会员配置？MemberConfig？
    /**
     * 获得积分配置
     *
     * @return 积分配置
     */
    MemberPointConfigRespDTO getConfig();

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
