package cn.iocoder.yudao.module.system.dal.mysql.dict;

import cn.iocoder.yudao.framework.mybatis.core.enums.SqlConstants;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.DictDataExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface SysDictDataMapper extends BaseMapperX<SysDictDataDO> {

    default SysDictDataDO selectByDictTypeAndValue(String dictType, String value) {
        return selectOne(new LambdaQueryWrapper<SysDictDataDO>().eq(SysDictDataDO::getDictType, dictType)
                .eq(SysDictDataDO::getValue, value));
    }

    default List<SysDictDataDO> selectByDictTypeAndValues(String dictType, Collection<String> values) {
        return selectList(new LambdaQueryWrapper<SysDictDataDO>().eq(SysDictDataDO::getDictType, dictType)
                .in(SysDictDataDO::getValue, values));
    }

    default int selectCountByDictType(String dictType) {
        return selectCount(SysDictDataDO::getDictType, dictType);
    }

    default PageResult<SysDictDataDO> selectPage(DictDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SysDictDataDO>()
                .likeIfPresent(SysDictDataDO::getLabel, reqVO.getLabel())
                .likeIfPresent(SysDictDataDO::getDictType, reqVO.getDictType())
                .eqIfPresent(SysDictDataDO::getStatus, reqVO.getStatus())
                .orderByAsc(Arrays.asList(SysDictDataDO::getDictType, SysDictDataDO::getSort)));
    }

    default List<SysDictDataDO> selectList(DictDataExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SysDictDataDO>().likeIfPresent(SysDictDataDO::getLabel, reqVO.getLabel())
                .likeIfPresent(SysDictDataDO::getDictType, reqVO.getDictType())
                .eqIfPresent(SysDictDataDO::getStatus, reqVO.getStatus()));
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new LambdaQueryWrapper<SysDictDataDO>().select(SysDictDataDO::getId)
                .gt(SysDictDataDO::getUpdateTime, maxUpdateTime).last(SqlConstants.LIMIT1)) != null;
    }

}
