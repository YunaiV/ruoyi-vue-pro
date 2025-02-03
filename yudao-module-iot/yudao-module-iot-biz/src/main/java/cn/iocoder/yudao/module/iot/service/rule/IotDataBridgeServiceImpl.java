package cn.iocoder.yudao.module.iot.service.rule;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataBridgeMapper;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * IoT 数据桥梁的 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDataBridgeServiceImpl implements IotDataBridgeService {

    @Resource
    private IotDataBridgeMapper dataBridgeMapper;

    // TODO @芋艿：临时测试
    @Override
    public IotDataBridgeDO getIotDataBridge(Long id) {
        if (Objects.equals(id, 1L)) {
            IotDataBridgeDO.HttpConfig config = new IotDataBridgeDO.HttpConfig()
                    .setUrl("http://127.0.0.1:48080/test")
//                    .setMethod("POST")
                    .setMethod("GET")
                    .setQuery(MapUtil.of("aaa", "bbb"))
                    .setHeaders(MapUtil.of("ccc", "ddd"))
                    .setBody(JsonUtils.toJsonString(MapUtil.of("eee", "fff")));
            return IotDataBridgeDO.builder().id(1L).name("芋道").description("芋道源码").status(0).direction(1)
                    .type(IotDataBridgTypeEnum.HTTP.getType()).config(config).build();
        }
        return dataBridgeMapper.selectById(id);
    }

}
