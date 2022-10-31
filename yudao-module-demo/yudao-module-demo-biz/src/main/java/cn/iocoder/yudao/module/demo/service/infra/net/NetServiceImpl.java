package cn.iocoder.yudao.module.demo.service.infra.net;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.net.NetDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.demo.convert.infra.net.NetConvert;
import cn.iocoder.yudao.module.demo.dal.mysql.infra.net.NetMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.demo.enums.ErrorCodeConstants.*;

/**
 * 区块链网络 Service 实现类
 *
 * @author ruanzh.eth
 */
@Service
@Validated
public class NetServiceImpl implements NetService {

    @Resource
    private NetMapper netMapper;

    @Override
    public Long createNet(NetCreateReqVO createReqVO) {
        // 插入
        NetDO net = NetConvert.INSTANCE.convert(createReqVO);
        netMapper.insert(net);
        // 返回
        return net.getId();
    }

    @Override
    public void updateNet(NetUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateNetExists(updateReqVO.getId());
        // 更新
        NetDO updateObj = NetConvert.INSTANCE.convert(updateReqVO);
        netMapper.updateById(updateObj);
    }

    @Override
    public void deleteNet(Long id) {
        // 校验存在
        this.validateNetExists(id);
        // 删除
        netMapper.deleteById(id);
    }

    private void validateNetExists(Long id) {
        if (netMapper.selectById(id) == null) {
            throw exception(NET_NOT_EXISTS);
        }
    }

    @Override
    public NetDO getNet(Long id) {
        return netMapper.selectById(id);
    }

    @Override
    public List<NetDO> getNetList(Collection<Long> ids) {
        return netMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<NetDO> getNetPage(NetPageReqVO pageReqVO) {
        return netMapper.selectPage(pageReqVO);
    }

    @Override
    public List<NetDO> getNetList(NetExportReqVO exportReqVO) {
        return netMapper.selectList(exportReqVO);
    }

}
