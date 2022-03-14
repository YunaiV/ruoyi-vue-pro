package cn.iocoder.yudao.framework.file.core.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.file.core.enums.FileStorageEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件客户端的工厂实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class FileClientFactoryImpl implements FileClientFactory {

    /**
     * 文件客户端 Map
     * key：配置编号
     */
    private final ConcurrentMap<Long, AbstractFileClient<?>> clients = new ConcurrentHashMap<>();

    @Override
    public FileClient getFileClient(Long channelId) {
        AbstractFileClient<?> client = clients.get(channelId);
        if (client == null) {
            log.error("[getFileClient][配置编号({}) 找不到客户端]", channelId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends FileClientConfig> void createOrUpdateFileClient(Long configId, Integer storage, Config config) {
        AbstractFileClient<Config> client = (AbstractFileClient<Config>) clients.get(configId);
        if (client == null) {
            client = this.createFileClient(configId, storage, config);
            client.init();
            clients.put(client.getId(), client);
        } else {
            client.refresh(config);
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends FileClientConfig> AbstractFileClient<Config> createFileClient(
            Long configId, Integer storage, Config config) {
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(storage);
        Assert.notNull(storageEnum, String.format("文件配置(%s) 为空", storageEnum));
        // 创建客户端
//        switch (storageEnum) {
//            case WX_PUB: return (AbstractFileClient<Config>) new WXPubFileClient(channelId, (WXFileClientConfig) config);
//            case WX_LITE: return (AbstractFileClient<Config>) new WXPubFileClient(channelId, (WXFileClientConfig) config);
//            case WX_APP: return (AbstractFileClient<Config>) new WXPubFileClient(channelId, (WXFileClientConfig) config);
//            case ALIPAY_WAP: return (AbstractFileClient<Config>) new AlipayWapFileClient(channelId, (AlipayFileClientConfig) config);
//            case ALIPAY_QR: return (AbstractFileClient<Config>) new AlipayQrFileClient(channelId, (AlipayFileClientConfig) config);
//            case ALIPAY_APP: return (AbstractFileClient<Config>) new AlipayQrFileClient(channelId, (AlipayFileClientConfig) config);
//            case ALIPAY_PC: return (AbstractFileClient<Config>) new AlipayQrFileClient(channelId, (AlipayFileClientConfig) config);
//        }
        return (AbstractFileClient<Config>) ReflectUtil.newInstance(storageEnum.getClientClass(), configId, config);
//        storageEnum.getClientClass().newInstance()
//        // 创建失败，错误日志 + 抛出异常
//        log.error("[createSmsClient][配置({}) 找不到合适的客户端实现]", config);
//        throw new IllegalArgumentException(String.format("配置(%s) 找不到合适的客户端实现", config));
    }

}
