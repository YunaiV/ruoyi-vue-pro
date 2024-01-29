package cn.iocoder.yudao.module.crm.dal.mysql.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeQueryVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商机状态类型 Mapper
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusTypeMapper extends BaseMapperX<CrmBusinessStatusTypeDO> {

    default PageResult<CrmBusinessStatusTypeDO> selectPage(CrmBusinessStatusTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessStatusTypeDO>()
                .orderByDesc(CrmBusinessStatusTypeDO::getId));
    }

    default List<CrmBusinessStatusTypeDO> selectList(CrmBusinessStatusTypeQueryVO queryVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessStatusTypeDO>()
                .eqIfPresent(CrmBusinessStatusTypeDO::getStatus, queryVO.getStatus())
                .inIfPresent(CrmBusinessStatusTypeDO::getId, queryVO.getIdList()));
    }

    // TODO @lzxhqs：这个可以改成 selectByName。业务上基于在判断 id 匹配；这样更通用一些；mapper 尽量通用，不关注或者特别关联业务；
    /**
     * 根据ID和name查询
     *
     * @param id 商机状态类型id
     * @param name 状态类型名
     * @return result
     */
    default CrmBusinessStatusTypeDO selectByIdAndName(Long id, String name) {
        return selectOne(CrmBusinessStatusTypeDO::getId, id, CrmBusinessStatusTypeDO::getName, name);
    }

}
