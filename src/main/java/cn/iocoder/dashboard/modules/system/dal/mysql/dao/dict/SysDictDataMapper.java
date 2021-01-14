package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dict;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import static com.baomidou.mybatisplus.core.metadata.OrderItem.asc;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictDataDO> {

    default SysDictDataDO selectByLabel(String label) {
        return selectOne(new QueryWrapper<SysDictDataDO>().eq("label", label));
    }

    default int selectCountByDictType(String dictType) {
        return selectCount(new QueryWrapper<SysDictDataDO>().eq("dict_type", dictType));
    }

    default IPage<SysDictDataDO> selectList(SysDictDataPageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO),
                new QueryWrapperX<SysDictDataDO>().likeIfPresent("label", reqVO.getLabel())
                        .likeIfPresent("dict_type", reqVO.getDictType())
                        .eqIfPresent("status", reqVO.getStatus()))
                .addOrder(asc("dict_type"), asc("sort"));
    }

    default List<SysDictDataDO> selectList() {
        return selectList(new QueryWrapper<>());
    }

    default List<SysDictDataDO> selectList(SysDictDataExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysDictDataDO>().likeIfPresent("label", reqVO.getLabel())
                        .likeIfPresent("dict_type", reqVO.getDictType())
                        .eqIfPresent("status", reqVO.getStatus()));
    }
}
