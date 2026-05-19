package cn.iocoder.yudao.module.im.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.message.ImChannelMessagePageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IM 频道消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMessageMapper extends BaseMapperX<ImChannelMessageDO> {

    // TODO @AI：代码风格，和别的 message 一致；
    /**
     * 拉取指定用户应收的频道消息
     * <p>
     * 命中条件：id 大于游标 + (receiver_user_ids 为空表示全员 OR 逗号分隔列表里包含当前 userId)
     *
     * @param userId 当前用户编号
     * @param minId  游标；返回大于此值的消息
     * @param size   返回条数
     * @return 频道消息列表；按 id 升序
     */
    @Select("SELECT * FROM im_channel_message "
            + "WHERE deleted = 0 "
            + "AND id > #{minId} "
            + "AND (receiver_user_ids IS NULL "
            + "     OR receiver_user_ids = '' "
            + "     OR FIND_IN_SET(#{userId}, receiver_user_ids)) "
            + "ORDER BY id ASC "
            + "LIMIT #{size}")
    List<ImChannelMessageDO> selectListByUserAndMinId(@Param("userId") Long userId,
                                                     @Param("minId") Long minId,
                                                     @Param("size") Integer size);

    // TODO @AI：代码风格，和别的 message 一致；
    /**
     * 校验用户是否曾经收到过指定素材（receiver_user_ids 为空表示全员）；用于详情接口归属校验
     */
    @Select("SELECT EXISTS(SELECT 1 FROM im_channel_message "
            + "WHERE deleted = 0 AND material_id = #{materialId} "
            + "AND (receiver_user_ids IS NULL "
            + "     OR receiver_user_ids = '' "
            + "     OR FIND_IN_SET(#{userId}, receiver_user_ids)) "
            + "LIMIT 1)")
    boolean existsByUserAndMaterial(@Param("userId") Long userId,
                                    @Param("materialId") Long materialId);

    default Long selectCountByMaterialId(Long materialId) {
        return selectCount(ImChannelMessageDO::getMaterialId, materialId);
    }

    default PageResult<ImChannelMessageDO> selectPage(ImChannelMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImChannelMessageDO>()
                .eqIfPresent(ImChannelMessageDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(ImChannelMessageDO::getMaterialId, reqVO.getMaterialId())
                .betweenIfPresent(ImChannelMessageDO::getSendTime, reqVO.getSendTime())
                .orderByDesc(ImChannelMessageDO::getId));
    }

}
