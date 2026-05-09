package cn.iocoder.yudao.module.im.service.rtc.bo;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * IM 通话会话内存仓储；MVP 阶段不落库，全量存内存
 * <p>
 * 双层索引：
 * - sessions: roomName -> 通话会话；invite 时 computeIfAbsent 保证同一好友对 / 群只有一个进行中通话；
 * - userActiveRoom: userId -> 当前所在 roomName；用于"对方正忙"提示与冷启动恢复。
 * 通话结束时 {@link #remove} 一次性清两个索引；同一用户的并发挂断走 ConcurrentHashMap 原子操作收敛。
 *
 * @author 芋道源码
 */
@Component
public class ImCallSessionStore {

    private final Map<String, ImCallSessionBO> sessions = new ConcurrentHashMap<>();

    private final Map<Long, String> userActiveRoom = new ConcurrentHashMap<>();

    public ImCallSessionBO get(String roomName) {
        if (roomName == null) {
            return null;
        }
        return sessions.get(roomName);
    }

    /**
     * 原子获取或创建会话；在并发 invite 下保证同一 roomName 只 build 一次
     *
     * @param roomName 房间名（已由 service 派生）
     * @param builder  仅当不存在时调用；返回已组装好的 ImCallSessionBO
     * @return 会话实例（新建或已有）
     */
    public ImCallSessionBO getOrCreate(String roomName, Function<String, ImCallSessionBO> builder) {
        return sessions.computeIfAbsent(roomName, builder);
    }

    /**
     * 移除会话并清理参与者的 active 索引；调用方应在会话状态切到 ENDED 后调用
     */
    public void remove(String roomName) {
        if (roomName == null) {
            return;
        }
        ImCallSessionBO session = sessions.remove(roomName);
        if (session == null) {
            return;
        }
        for (Long userId : session.getAllUserIds()) {
            // 仅当该用户的 active room 仍是当前 room 时才解绑；避免误清用户后续新建的通话
            userActiveRoom.remove(userId, roomName);
        }
    }

    /**
     * 绑定用户到房间；invite / accept 时调用
     */
    public void bindUser(Long userId, String roomName) {
        if (userId == null || roomName == null) {
            return;
        }
        userActiveRoom.put(userId, roomName);
    }

    /**
     * 仅在用户当前 active room 等于给定 roomName 时解绑；用于 reject / leave 单方退出
     */
    public void unbindUserFromRoom(Long userId, String roomName) {
        if (userId == null || roomName == null) {
            return;
        }
        userActiveRoom.remove(userId, roomName);
    }

    /**
     * 用户是否处于通话中；用于 invite 时判定"对方正忙"
     */
    public boolean isBusy(Long userId) {
        return userId != null && userActiveRoom.containsKey(userId);
    }

    /**
     * 用户当前所在的 roomName；冷启动恢复用
     */
    public String getActiveRoom(Long userId) {
        return userId == null ? null : userActiveRoom.get(userId);
    }

    /**
     * 列出当前所有进行中会话；用于运维 / 单元测试观察；线上调用频次需谨慎
     */
    public List<ImCallSessionBO> listAll() {
        return new ArrayList<>(sessions.values());
    }

}
