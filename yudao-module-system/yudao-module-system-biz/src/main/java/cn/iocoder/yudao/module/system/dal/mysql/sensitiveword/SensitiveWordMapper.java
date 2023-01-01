package cn.iocoder.yudao.module.system.dal.mysql.sensitiveword;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sensitiveword.SensitiveWordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词 Mapper
 *
 * @author 永不言败
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapperX<SensitiveWordDO> {

    default PageResult<SensitiveWordDO> selectPage(SensitiveWordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SensitiveWordDO>()
                .likeIfPresent(SensitiveWordDO::getName, reqVO.getName())
                .likeIfPresent(SensitiveWordDO::getTags, reqVO.getTag())
                .eqIfPresent(SensitiveWordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SensitiveWordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SensitiveWordDO::getId));
    }

    default List<SensitiveWordDO> selectList(SensitiveWordExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SensitiveWordDO>()
                .likeIfPresent(SensitiveWordDO::getName, reqVO.getName())
                .likeIfPresent(SensitiveWordDO::getTags, reqVO.getTag())
                .eqIfPresent(SensitiveWordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SensitiveWordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SensitiveWordDO::getId));
    }

    default SensitiveWordDO selectByName(String name) {
        return selectOne(SensitiveWordDO::getName, name);
    }

    @Select("SELECT COUNT(*) FROM system_sensitive_word WHERE update_time > #{maxUpdateTime}")
    Long selectCountByUpdateTimeGt(LocalDateTime maxUpdateTime);

}
