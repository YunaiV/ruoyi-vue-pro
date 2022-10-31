package cn.iocoder.yudao.module.demo.service.infra.endpoint;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.demo.convert.infra.endpoint.EndpointConvert;
import cn.iocoder.yudao.module.demo.dal.mysql.infra.endpoint.EndpointMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.demo.enums.ErrorCodeConstants.*;

/**
 * 区块链节点 Service 实现类
 *
 * @author ruanzh.eth
 */
@Service
@Validated
public class EndpointServiceImpl implements EndpointService {

    @Resource
    private EndpointMapper endpointMapper;

    @Override
    public Long createEndpoint(EndpointCreateReqVO createReqVO) {
        // 插入
        EndpointDO endpoint = EndpointConvert.INSTANCE.convert(createReqVO);
        endpointMapper.insert(endpoint);
        // 返回
        return endpoint.getId();
    }

    @Override
    public void updateEndpoint(EndpointUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateEndpointExists(updateReqVO.getId());
        // 更新
        EndpointDO updateObj = EndpointConvert.INSTANCE.convert(updateReqVO);
        endpointMapper.updateById(updateObj);
    }

    @Override
    public void deleteEndpoint(Long id) {
        // 校验存在
        this.validateEndpointExists(id);
        // 删除
        endpointMapper.deleteById(id);
    }

    private void validateEndpointExists(Long id) {
        if (endpointMapper.selectById(id) == null) {
            throw exception(ENDPOINT_NOT_EXISTS);
        }
    }

    @Override
    public EndpointDO getEndpoint(Long id) {
        return endpointMapper.selectById(id);
    }

    @Override
    public List<EndpointDO> getEndpointList(Collection<Long> ids) {
        return endpointMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<EndpointDO> getEndpointPage(EndpointPageReqVO pageReqVO) {
        return endpointMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EndpointDO> getEndpointList(EndpointExportReqVO exportReqVO) {
        return endpointMapper.selectList(exportReqVO);
    }

}
