package cn.iocoder.yudao.module.deepay.service.ima;

/**
 * ima 知识库业务服务。
 *
 * <p>屏蔽底层 HTTP 细节，供 {@link cn.iocoder.yudao.module.deepay.agent.ImaAgent} 调用。</p>
 */
public interface ImaService {

    /**
     * 为指定链码创建 ima 知识库。
     *
     * @param chainCode 链码（用作知识库名称和描述）
     * @return ima 知识库 ID
     */
    String createKnowledgeBase(String chainCode);

    /**
     * 向指定知识库上传图片。
     *
     * @param kbId     知识库 ID
     * @param imageUrl 图片公网 URL
     */
    void uploadImage(String kbId, String imageUrl);

}
