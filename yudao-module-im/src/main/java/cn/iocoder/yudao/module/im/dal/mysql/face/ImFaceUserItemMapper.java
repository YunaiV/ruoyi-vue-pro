package cn.iocoder.yudao.module.im.dal.mysql.face;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 用户私有表情 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFaceUserItemMapper extends BaseMapperX<ImFaceUserItemDO> {

    /** 取用户的所有个人表情，按 sort 升序、id 倒序（最近添加在前） */
    default List<ImFaceUserItemDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eq(ImFaceUserItemDO::getUserId, userId)
                .orderByAsc(ImFaceUserItemDO::getSort)
                .orderByDesc(ImFaceUserItemDO::getId));
    }

    default Long selectCountByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eq(ImFaceUserItemDO::getUserId, userId));
    }

    default ImFaceUserItemDO selectByUserIdAndUrl(Long userId, String url) {
        return selectOne(new LambdaQueryWrapperX<ImFaceUserItemDO>()
                .eq(ImFaceUserItemDO::getUserId, userId)
                .eq(ImFaceUserItemDO::getUrl, url));
    }

}
