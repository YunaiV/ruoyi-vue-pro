package cn.iocoder.yudao.module.iot.core.script.dto;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.Getter;

/**
 * IoT 数据解析脚本：不可变对象 {productId, hash, content}
 * <p>
 * hash = 源码 UTF-8 的完整 SHA-256（64 位 hex），作为缓存和编译身份；{@link #getShortHash()} 只用于日志和界面。
 * 源码字符串可以被多个线程只读共享，编译结果不共享
 *
 * @author 芋道源码
 */
@Getter
public final class IotScriptSource {

    /**
     * 产品编号；调试时为 null
     */
    private final Long productId;
    /**
     * 源码 UTF-8 的完整 SHA-256
     */
    private final String hash;
    /**
     * 源码
     */
    private final String content;
    /**
     * 编译缓存的 key：productId:hash；调试时为 debug:hash
     */
    private final String key;

    private IotScriptSource(Long productId, String content) {
        this.productId = productId;
        this.content = content;
        this.hash = DigestUtil.sha256Hex(content);
        this.key = (productId != null ? productId.toString() : "debug") + ":" + hash;
    }

    public static IotScriptSource of(Long productId, String content) {
        return new IotScriptSource(productId, content);
    }

    public static IotScriptSource ofDebug(String content) {
        return new IotScriptSource(null, content);
    }

    /**
     * @return hash 前 12 位：只用于日志和界面，不作为唯一标识
     */
    public String getShortHash() {
        return hash.substring(0, 12);
    }

}
