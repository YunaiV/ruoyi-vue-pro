package cn.iocoder.yudao.module.huawei.smarthome.util;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Slf4j
public class HuaweiSmartHomeAuthUtils {

    private static final String X_SIGN_HEADER_PREFIX_FORMAT = "x-access-key,x-project-id,x-request-id,x-timestamp;%s";

    /**
     * 计算华为智能家居 API 的签名 (x-sign)
     *
     * @param accessKey   AK
     * @param secretKey   SK (需要是16进制字符串形式，如果不是，需要提前转换)
     * @param projectId   项目 ID
     * @param requestId   请求 ID (UUID)
     * @param timestamp   时间戳 (毫秒)
     * @param messageBody 请求体字符串，如果请求没有body，则为空字符串 ""
     * @return 完整的 x-sign header 值
     */
    public static String calculateSign(String accessKey, String secretKey, String projectId,
                                       String requestId, String timestamp, String messageBody) {
        try {
            // 1. 构造待签名的字符串
            // headStr = x-access-key + x-project-id + x-request-id + x-timestamp (注意顺序)
            String headStr = accessKey + projectId + requestId + timestamp;
            String stringToSign = headStr + messageBody;

            // 2. 计算哈希值：hashValue = Base64(HMAC-SHA256(stringToSign, secretKeyBytes))
            // 文档要求 secretKey 为16进制字符串，然后转字节数组。
            // 这里假设传入的 secretKey 已经是原始密钥字符串，直接转UTF-8字节。
            // 如果 SK 是16进制表示，需要先用 Hex.decodeHex(secretKey) 转为 byte[]
            // byte[] secretKeyBytes = Hex.decodeHex(secretKey.toCharArray()); // 如果secretKey是Hex字符串
            byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8); // 如果secretKey是原始密钥

            HMac hmacSha256 = new HMac(HmacAlgorithm.HmacSHA256, secretKeyBytes);
            byte[] hashByteArray = hmacSha256.digest(stringToSign.getBytes(StandardCharsets.UTF_8));

            String base64String = Base64.getEncoder().encodeToString(hashByteArray);

            // 3. 转小写格式
            String hashValue = base64String.toLowerCase(Locale.ENGLISH);

            // 4. 构造 x-sign header
            // x-sign: x-access-key,x-project-id,x-request-id,x-timestamp;hashValue
            return String.format(X_SIGN_HEADER_PREFIX_FORMAT, hashValue);

        } catch (Exception e) {
            log.error("[calculateSign][计算华为 API 签名失败]", e);
            // 根据实际错误处理策略，可以抛出自定义异常或返回null/空字符串
            throw new RuntimeException("计算华为 API 签名失败", e);
        }
    }

    /**
     * 示例代码来自华为文档，用于对比和调试。
     * 注意：华为文档中的 secretKey 转字节数组方式可能与 Hutool 的 HMac 实现略有不同，
     * Hutool 的 HMac 构造函数直接接收 byte[] key。
     * 华为文档示例: SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey转成字节数组, "HmacSHA256");
     * 这里的 secretKey转成字节数组 指的是将16进制字符串表示的密钥转换为字节数组。
     * 例如，如果SK是 "0123456789abcdef"，则需要 Hex.decodeHex("0123456789abcdef")。
     * 如果我们配置的 SK 就是原始密钥字符串，则直接 getBytes(StandardCharsets.UTF_8) 即可。
     *
     * 为了与文档提供的Java Demo (caleHash) 尽可能一致，如果传入的 secretKey 是16进制字符串，
     * 使用下面的方法进行签名。
     */
    public static String calculateSignWithHexSecret(String accessKey, String hexSecretKey, String projectId,
                                                    String requestId, String timestamp, String messageBody) {
        try {
            String headStr = accessKey + projectId + requestId + timestamp;
            String stringToSign = headStr + messageBody;

            byte[] secretKeyBytes = Hex.decodeHex(hexSecretKey.toCharArray());

            HMac hmacSha256 = new HMac(HmacAlgorithm.HmacSHA256, secretKeyBytes);
            byte[] hashByteArray = hmacSha256.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
            String base64String = Base64.getEncoder().encodeToString(hashByteArray);
            String hashValue = base64String.toLowerCase(Locale.ENGLISH);

            return String.format(X_SIGN_HEADER_PREFIX_FORMAT, hashValue);
        } catch (Exception e) {
            log.error("[calculateSignWithHexSecret][计算华为 API 签名失败，使用HEX SecretKey]", e);
            throw new RuntimeException("计算华为 API 签名失败 (Hex SK)", e);
        }
    }

    // 可以添加一个main方法进行快速测试
    public static void main(String[] args) {
        // 示例参数 (需要替换为真实或有效的测试值)
        String accessKey = "101867757";
        // 重要：华为文档中 caleHash 方法的 secretKey 是指16进制字符串的密钥
        // 如果你的 SK 是 "mySecretKey", 你需要先将其转换为16进制字符串，或者直接使用原始密钥并调整HMac初始化
        // 假设这里的 "your_hex_secret_key" 是已经转换好的16进制密钥字符串
        String hexSecretKey = "74434e7830303031"; // 这是一个示例HEX，你需要用真实的
        String projectId = "123456112355442";
        String requestId = "4871cc67-a2ec-41f7-b4e7-a5c1b236ee1b";
        String timestamp = String.valueOf(System.currentTimeMillis()); //"1640759898914";
        String messageBody = "{\"spaceName\": \"8601\", \"desc\": \"客房\"}"; // POST/PUT请求的body
        // String messageBodyForGet = ""; // GET/DELETE 请求的body为空字符串

        // 使用 calculateSignWithHexSecret (如果SK是16进制字符串)
        String xSignWithHex = calculateSignWithHexSecret(accessKey, hexSecretKey, projectId, requestId, timestamp, messageBody);
        System.out.println("x-sign (with Hex Secret): " + xSignWithHex);

        // 如果你的SK是原始字符串，比如 "myActualSecretKey"
        String originalSecretKey = "myActualSecretKey"; // 假设这是原始密钥
        // 注意：华为文档的示例似乎期望 secretKey 是 16 进制字符串形式，然后解码成字节数组。
        // 如果我们直接使用原始密钥字符串，并用 .getBytes(UTF-8) 作为 HMac 的 key，
        // 其结果可能与华为期望的不一致，除非华为的 "secretKey转成字节数组" 就是指 getBytes(UTF-8)。
        // 通常，API密钥会直接提供原始形式或Base64编码形式，较少直接提供16进制字符串形式让客户端转换。
        // 为保险起见，最好确认华为提供的SK是原始形式还是16进制形式。
            // 此处假设我们配置的 secretKey 是原始密钥字符串, 但华为文档明确指出其Demo中的secretKey是16进制字符串
        // String xSignOriginal = calculateSign(accessKey, originalSecretKey, projectId, requestId, timestamp, messageBody);
        // System.out.println("x-sign (with Original Secret): " + xSignOriginal);

            // 打印请求参数，方便调试
            System.out.println("Access Key: " + accessKey);
            System.out.println("Hex Secret Key: " + hexSecretKey);
            System.out.println("Project ID: " + projectId);
            System.out.println("Request ID: " + requestId);
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Message Body: " + messageBody);
            String headStr = accessKey + projectId + requestId + timestamp;
            String stringToSign = headStr + messageBody;
            System.out.println("String to Sign: " + stringToSign);

        // 对比华为文档中的示例 x-sign (注意，其 hashValue 部分是 MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE)
        // 文档中的示例签名: x-access-key,x-project-id,x-request-id,xtimestamp;MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE
        // 这个 hashValue "MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE" 看起来像是 Base64 编码的，但它不是标准的 HMAC-SHA256 后再 Base64 的结果
        // 它的大小写混合，且包含非标准Base64字符（如 L 、 p 、 O 、 I 、 Y 、 T 、 H 、 P 、 W 、 N 、 U 、 T 、 G 、 F 、 E 等）
        // 这提示华为文档中的x-sign示例可能不是直接通过标准HMAC-SHA256+Base64+toLowerCase得到的，或者其原始输入/密钥与我们假设的不同。
        // **重要更新**：再次阅读文档中的Java Demo，`caleHash` 方法返回的是 `return "x-access-key,x-project-id,x-request-id,x-timestamp;" + base64String;`
        // 而 `base64String` 是 `toLowerCase(Locale.ENGLISH)` 过的。
        // 华为文档的示例 `x-sign: x-access-key,x-project-id,x-request-id,xtimestamp;MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE`
        // 其中的 `MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE` 如果是 `base64String.toLowerCase(Locale.ENGLISH)` 的结果，
        // 那么原始的 Base64 编码的 HMAC-SHA256 哈希值应该是全大写或混合大小写的。
        // 假设华为文档中的示例 `x-sign` 是正确的，那么我们需要精确匹配其生成过程。

        // 重新审视华为文档的签名示例
        // x-sign: x-access-key,x-project-id,x-request-id,xtimestamp;MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE
        // headStr (来自文档示例参数) = 101867757 (x-access-key)
        //                             + 123456112355442 (x-project-id)
        //                             + 4871cc67-a2ec-41f7-b4e7-a5c1b236ee1b (x-request-id)
        //                             + 1640759898914 (x-timestamp)
        // bodyStr (来自创建空间示例) = {"scenarioType" : "0"}  <- 文档签名总览处的 body
        // bodyStr (来自创建空间接口详情) = { "spaceName": "8601", "desc": "客房" } <- 创建空间接口处的 body
        // 假设使用创建空间接口处的 body:
        // String stringToSignExample = "1018677571234561123554424871cc67-a2ec-41f7-b4e7-a5c1b236ee1b1640759898914" +
        //                            "{\"spaceName\": \"8601\", \"desc\": \"客房\"}";
        // 如果 secretKey 是 "MTIzMTIzMTI..." (这本身看起来像base64，不太可能是原始密钥)
        // 华为文档的签名计算步骤：
        // 1. stringToSign = headStr + bodyStr
        // 2. hashValue = Base64(HMAC-SHA256(stringToSign, secretKeyBytes))
        // 3. hashValue = hashValue.toLowerCase(Locale.ENGLISH)
        // 4. x-sign = "x-access-key,x-project-id,x-request-id,x-timestamp;" + hashValue

        // 结论：华为文档中的 x-sign 示例的 hashValue `MTIzMTIzMTLpmLAOIYTkjYTTHpPIWNUTGFEEE` 应该就是 `base64String.toLowerCase(Locale.ENGLISH)` 的结果。
        // 这个值本身不是标准的Base64，因为它被转成了小写。这意味着原始的Base64编码的HMAC-SHA256值在转换前可能是混合大小写的。
        // 我们的 `calculateSign` 和 `calculateSignWithHexSecret` 方法是遵循这个逻辑的。
        // 关键在于 `secretKey` 的正确形式（原始密钥还是16进制表示的密钥）。
        // 华为的Java Demo `caleHash` 方法中 `new SecretKeySpec(secretKey转成字节数组, "HmacSHA256");`
        // 注释 `secretKey为16进制的字符串，建议用hex转成字节数组（Apache 的Hex.decodeHex）` 表明了 SK 应为16进制字符串。
        // 因此，`HuaweiSmartHomeProperties` 中配置的 `secretKey` 应该配置为16进制字符串，
        // 并在调用签名方法时使用 `calculateSignWithHexSecret` 或在 `calculateSign` 内部进行 Hex 解码。
        // 为方便，我们将统一 `calculateSign` 方法，使其接受原始 secretKey，并在内部判断是否需要Hex解码，
        // 或者更简单的是，强制要求配置的 `secretKey` 就是可以直接用于HMAC的原始密钥字节的某种字符串表示（如直接是原始密钥，或base64编码的原始密钥）。
        // 鉴于华为文档明确指出其Java Demo中的secretKey是16进制字符串，我们应该遵循此约定。
        // 所以，`HuaweiSmartHomeProperties.secretKey` 应该存储16进制字符串形式的SK。
        // `calculateSignWithHexSecret` 是正确的实现。我们将默认使用它，并将 `HuaweiSmartHomeProperties.secretKey` 视为 Hex 字符串。
    }
}
