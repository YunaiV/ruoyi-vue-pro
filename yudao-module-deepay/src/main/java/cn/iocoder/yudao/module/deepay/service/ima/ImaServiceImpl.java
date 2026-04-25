package cn.iocoder.yudao.module.deepay.service.ima;

import cn.iocoder.yudao.module.deepay.client.ima.ImaClient;
import org.springframework.util.StringUtils;

/**
 * {@link ImaService} 标准实现。
 *
 * <p>由 {@link cn.iocoder.yudao.module.deepay.config.ImaAutoConfiguration} 在 ima 启用时创建。</p>
 */
public class ImaServiceImpl implements ImaService {

    private final ImaClient imaClient;

    public ImaServiceImpl(ImaClient imaClient) {
        this.imaClient = imaClient;
    }

    @Override
    public String createKnowledgeBase(String chainCode) {
        if (!StringUtils.hasText(chainCode)) {
            throw new IllegalArgumentException("ImaService: chainCode 不能为空");
        }
        return imaClient.createKnowledgeBase(
                "Deepay-" + chainCode,
                "Deepay 设计资产库，链码：" + chainCode);
    }

    @Override
    public void uploadImage(String kbId, String imageUrl) {
        if (!StringUtils.hasText(kbId)) {
            throw new IllegalArgumentException("ImaService: kbId 不能为空");
        }
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("ImaService: imageUrl 不能为空");
        }
        imaClient.uploadImage(kbId, imageUrl);
    }

}
