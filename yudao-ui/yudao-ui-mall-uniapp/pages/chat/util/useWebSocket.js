import { onBeforeUnmount, reactive, ref } from 'vue';
import { baseUrl, websocketPath } from '@/sheep/config';
import { copyValueToTarget } from '@/sheep/helper/utils';
import { getRefreshToken } from '@/sheep/request';

/**
 * WebSocket 创建 hook
 * @param opt 连接配置
 * @return {{options: *}}
 */
export function useWebSocket(opt) {
  const options = reactive({
    url: (baseUrl + websocketPath).replace('http', 'ws') + '?token=' + getRefreshToken(), // ws 地址
    isReconnecting: false, // 正在重新连接
    reconnectInterval: 3000, // 重连间隔，单位毫秒
    heartBeatInterval: 5000, // 心跳间隔，单位毫秒
    pingTimeoutDuration: 1000, // 超过这个时间，后端没有返回pong，则判定后端断线了。
    heartBeatTimer: null, // 心跳计时器
    destroy: false, // 是否销毁
    pingTimeout: null, // 心跳检测定时器
    reconnectTimeout: null, // 重连定时器ID的属性
    onConnected: () => {}, // 连接成功时触发
    onClosed: () => {}, // 连接关闭时触发
    onMessage: (data) => {}, // 收到消息
  });
  const SocketTask = ref(null); // SocketTask 由 uni.connectSocket() 接口创建

  const initEventListeners = () => {
    // 监听 WebSocket 连接打开事件
    SocketTask.value.onOpen(() => {
      console.log('WebSocket 连接成功');
      // 连接成功时触发
      options.onConnected();
      // 开启心跳检查
      startHeartBeat();
    });
    // 监听 WebSocket 接受到服务器的消息事件
    SocketTask.value.onMessage((res) => {
      try {
        if (res.data === 'pong') {
          // 收到心跳重置心跳超时检查
          resetPingTimeout();
        } else {
          options.onMessage(JSON.parse(res.data));
        }
      } catch (error) {
        console.error(error);
      }
    });
    // 监听 WebSocket 连接关闭事件
    SocketTask.value.onClose((event) => {
      // 情况一：实例销毁
      if (options.destroy) {
        options.onClosed();
      } else {
        // 情况二：连接失败重连
        // 停止心跳检查
        stopHeartBeat();
        // 重连
        reconnect();
      }
    });
  };

  // 发送消息
  const sendMessage = (message) => {
    if (SocketTask.value && !options.destroy) {
      SocketTask.value.send({ data: message });
    }
  };
  // 开始心跳检查
  const startHeartBeat = () => {
    options.heartBeatTimer = setInterval(() => {
      sendMessage('ping');
      options.pingTimeout = setTimeout(() => {
        // 如果在超时时间内没有收到 pong，则认为连接断开
        reconnect();
      }, options.pingTimeoutDuration);
    }, options.heartBeatInterval);
  };
  // 停止心跳检查
  const stopHeartBeat = () => {
    clearInterval(options.heartBeatTimer);
    resetPingTimeout();
  };

  // WebSocket 重连
  const reconnect = () => {
    if (options.destroy || !SocketTask.value) {
      // 如果WebSocket已被销毁或尚未完全关闭，不进行重连
      return;
    }

    // 重连中
    options.isReconnecting = true;

    // 清除现有的重连标志，以避免多次重连
    if (options.reconnectTimeout) {
      clearTimeout(options.reconnectTimeout);
    }

    // 设置重连延迟
    options.reconnectTimeout = setTimeout(() => {
      // 检查组件是否仍在运行和WebSocket是否关闭
      if (!options.destroy) {
        // 重置重连标志
        options.isReconnecting = false;
        // 初始化新的WebSocket连接
        initSocket();
      }
    }, options.reconnectInterval);
  };

  const resetPingTimeout = () => {
    if (options.pingTimeout) {
      clearTimeout(options.pingTimeout);
      options.pingTimeout = null; // 清除超时ID
    }
  };

  const close = () => {
    options.destroy = true;
    stopHeartBeat();
    if (options.reconnectTimeout) {
      clearTimeout(options.reconnectTimeout);
    }
    if (SocketTask.value) {
      SocketTask.value.close();
      SocketTask.value = null;
    }
  };

  const initSocket = () => {
    options.destroy = false;
    copyValueToTarget(options, opt);
    SocketTask.value = uni.connectSocket({
      url: options.url,
      complete: () => {},
      success: () => {},
    });
    initEventListeners();
  };

  initSocket();

  onBeforeUnmount(() => {
    close();
  });
  return { options };
}
