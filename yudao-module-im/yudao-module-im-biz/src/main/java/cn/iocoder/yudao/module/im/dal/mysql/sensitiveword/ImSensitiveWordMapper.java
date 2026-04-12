package cn.iocoder.yudao.module.im.dal.mysql.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 敏感词 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImSensitiveWordMapper extends BaseMapperX<ImSensitiveWordDO> {

    // TODO @AI：已改名为 selectListByStatus（要作为参数）
    default List<ImSensitiveWordDO> selectListByStatus() {
        return selectList(new LambdaQueryWrapperX<ImSensitiveWordDO>()
                .eq(ImSensitiveWordDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }

}
