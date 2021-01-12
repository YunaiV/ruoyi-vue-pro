package cn.iocoder.dashboard.modules.system.service.dict;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;

/**
 * 字典类型 Service 接口
 *
 * @author 芋道源码
 */
public interface SysDictTypeService {

    PageResult<SysDictTypeDO> pageDictTypes(SysDictTypePageReqVO reqVO);

    SysDictTypeDO getDictType(Long id);

    Long createDictType(SysDictTypeCreateReqVO reqVO);

    void updateDictType(SysUserUpdateReqVO reqVO);

    void deleteDictType(Long id);

}
