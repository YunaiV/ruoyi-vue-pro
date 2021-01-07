package cn.iocoder.dashboard.modules.system.service.dict.impl;

import cn.iocoder.dashboard.modules.system.dal.mysql.dao.dict.SysDictDataMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * 字典数据 Service 实现类
 *
 * @author ruoyi
 */
@Service
public class SysDictDataServiceImpl implements SysDictDataService {

    private static final Comparator<SysDictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(SysDictDataDO::getDictType)
            .thenComparingInt(SysDictDataDO::getDictSort);

    @Resource
    private SysDictDataMapper dictDataMapper;

    @Override
    public List<SysDictDataDO> listDictDatas() {
        List<SysDictDataDO> list = dictDataMapper.selectList(null);
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

}
