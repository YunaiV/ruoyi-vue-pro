package cn.iocoder.yudao.module.mes.dal.mysql.md.autocode;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 编码生成记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdAutoCodeRecordMapper extends BaseMapperX<MesMdAutoCodeRecordDO> {

    default MesMdAutoCodeRecordDO selectByResult(Long ruleId, String result) {
        // 查重限定在本规则内（带 rule_id 维度）：他规则生成的同值编码不再误判为本规则重复
        return selectOne(MesMdAutoCodeRecordDO::getRuleId, ruleId,
                MesMdAutoCodeRecordDO::getResult, result);
    }

}
