package cn.iocoder.yudao.module.oms.service.impl;


import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OmsShopServiceImpl extends ServiceImpl<OmsShopMapper, OmsShopDO> implements OmsShopService {

    @Resource
    private OmsShopMapper omsShopMapper;

    @Override
    public List<OmsShopDO> getByPlatformCode(String platformCode) {
        return omsShopMapper.getByPlatformCode(platformCode);
    }
}
