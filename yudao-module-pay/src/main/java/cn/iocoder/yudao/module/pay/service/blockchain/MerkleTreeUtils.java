package cn.iocoder.yudao.module.pay.service.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Merkle 树工具类
 * <p>
 * 将任意 key-value 数据转为按 key 排序的叶子哈希列表，再向上折叠成
 * 单一 Merkle Root，确保数据的不可篡改性。
 * 对应 Python EnhancedBlockchainAttestation._generate_merkle_root()
 *
 * @author deepay
 */
public final class MerkleTreeUtils {

    private MerkleTreeUtils() {
    }

    /**
     * 计算 Map 数据的 Merkle Root（SHA-256）
     *
     * @param data 待存证数据
     * @return 64 位 hex 字符串
     */
    public static String computeMerkleRoot(Map<String, Object> data) {
        // 按 key 排序，保证同等数据得到相同结果
        TreeMap<String, Object> sorted = new TreeMap<>(data);
        List<String> leaves = new ArrayList<>();
        for (Map.Entry<String, Object> e : sorted.entrySet()) {
            String leaf = e.getKey() + ":" + (e.getValue() == null ? "" : e.getValue().toString());
            leaves.add(sha256(leaf));
        }
        return buildMerkleRoot(leaves);
    }

    /**
     * 计算纯字符串的 SHA-256
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }

    // ==================== private ====================

    private static String buildMerkleRoot(List<String> leaves) {
        if (leaves.isEmpty()) {
            return "0".repeat(64);
        }
        List<String> current = new ArrayList<>(leaves);
        while (current.size() > 1) {
            if (current.size() % 2 == 1) {
                // 奇数个叶子：复制最后一个
                current.add(current.get(current.size() - 1));
            }
            List<String> next = new ArrayList<>();
            for (int i = 0; i < current.size(); i += 2) {
                next.add(sha256(current.get(i) + current.get(i + 1)));
            }
            current = next;
        }
        return current.get(0);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
