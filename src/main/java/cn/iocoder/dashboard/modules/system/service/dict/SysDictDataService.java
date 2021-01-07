package cn.iocoder.dashboard.modules.system.service.dict;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;

import java.util.List;

/**
 * 字典数据 Service 接口
 *
 * @author ruoyi
 */
public interface SysDictDataService {

    List<SysDictDataDO> listDictDatas();

}
