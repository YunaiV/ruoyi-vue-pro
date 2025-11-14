<template>
  <!--  聊天列表使用scroll-view原生组件，整体倒置  -->
  <scroll-view
    :scroll-top="scroll.top"
    class="chat-scroll-view"
    scroll-y
    :refresher-enabled="false"
    @scroll="onScroll"
    @scrolltolower="loadMoreHistory"
    style="transform: scaleY(-1)"
  >
    <!-- 消息列表容器 -->
    <view class="message-container">
      <!-- 加载更多提示 -->
      <view v-if="isLoading" class="loading-more" style="transform: scaleY(-1)">
        <text>加载中...</text>
      </view>
      <!-- 消息列表 -->
      <view class="message-list">
        <view
          v-for="(item, index) in messageList"
          :key="item.id"
          class="message-item"
          style="transform: scaleY(-1)"
        >
          <!--  消息渲染  -->
          <MessageListItem
            :message="item"
            :message-index="index"
            :message-list="messageList"
          ></MessageListItem>
        </view>
      </view>
    </view>
  </scroll-view>

  <!-- 底部聊天输入框 -->
  <su-fixed bottom>
    <view v-if="showTip" class="back-top ss-flex ss-row-center ss-m-b-10" @tap="scrollToTop">
      <text class="back-top-item ss-flex ss-row-center">
        {{ showNewMessageTip ? '有新消息' : '回到底部' }}
      </text>
    </view>
    <slot name="bottom"></slot>
  </su-fixed>
</template>

<script setup>
  import MessageListItem from '@/pages/chat/components/messageListItem.vue';
  import { onMounted, reactive, ref, computed } from 'vue';
  import KeFuApi from '@/sheep/api/promotion/kefu';
  import { isEmpty } from '@/sheep/helper/utils';
  import { formatDate } from '@/sheep/helper/utils';
  import sheep from '@/sheep';

  const { safeAreaInsets } = sheep.$platform.device;
  const safeAreaInsetsBottom = safeAreaInsets.bottom + 'px'; // 底部安全区域
  const messageList = ref([]); // 消息列表
  const showTip = ref(false); // 显示提示
  const showNewMessageTip = ref(false); // 显示有新消息提示
  const refreshMessage = ref(false); // 更新消息列表
  const isLoading = ref(false); // 是否正在加载更多
  const hasMore = ref(true); // 是否还有更多数据
  const keyboardHeight = ref(0); // 键盘高度
  const scroll = ref({
    top: 0,
    oldTop: 0,
  }); // 滚动位置记录
  const queryParams = reactive({
    no: 1,
    limit: 20,
    createTime: undefined,
  }); // 查询参数

  // 计算聊天窗口高度
  const chatScrollHeight = computed(() => {
    const baseHeight = 'calc(100vh - 150px - ' + safeAreaInsetsBottom + ')';
    if (keyboardHeight.value > 0) {
      // 键盘弹起状态，减去键盘高度
      return `calc(${baseHeight} - ${keyboardHeight.value}px)`;
    }
    return baseHeight;
  });

  // 获得消息分页列表
  const getMessageList = async () => {
    isLoading.value = true;
    try {
      const { data } = await KeFuApi.getKefuMessageList(queryParams);
      if (isEmpty(data)) {
        hasMore.value = false;
        return;
      }
      if (queryParams.no > 1 && refreshMessage.value) {
        const newMessageList = [];
        for (const message of data) {
          if (messageList.value.some((val) => val.id === message.id)) {
            continue;
          }
          newMessageList.push(message);
        }
        // 新消息追加到开头
        messageList.value = [...newMessageList, ...messageList.value];
        refreshMessage.value = false; // 更新好后重置状态
        return;
      }

      if (queryParams.no > 1) {
        // 加载更多历史消息，追加到现有列表末尾（因为是倒置的，所以旧消息在底部/列表末尾）
        if (data.length < queryParams.limit) {
          hasMore.value = false; // 如果返回的数据少于请求的数量，说明没有更多数据了
        }

        // 过滤掉已存在的消息
        const historyMessages = data.filter(
          (msg) => !messageList.value.some((existing) => existing.id === msg.id),
        );

        if (historyMessages.length > 0) {
          messageList.value = [...messageList.value, ...historyMessages];
        }
      } else {
        // 首次加载
        messageList.value = data;

        if (data.length < queryParams.limit) {
          hasMore.value = false;
        }
      }

      if (data.slice(-1).length > 0) {
        // 设置最后一次历史查询的最后一条消息的 createTime
        queryParams.createTime = formatDate(data.slice(-1)[0].createTime);
      }
    } finally {
      isLoading.value = false;
    }
  };

  /** 加载更多历史数据 */
  const loadMoreHistory = async () => {
    if (isLoading.value || !hasMore.value) return;

    // 增加页码
    queryParams.no += 1;
    await getMessageList();
  };

  /** 刷新消息列表 */
  const refreshMessageList = async (message = undefined) => {
    if (typeof message !== 'undefined') {
      // 追加数据到列表开头（因为是倒置的，所以新消息在顶部/列表开头）
      messageList.value.unshift(message);
      showNewMessageTip.value = true;
    } else {
      queryParams.createTime = undefined;
      refreshMessage.value = true;
      await getMessageList();
    }

    // 若已是第一页则不做处理
    if (queryParams.no > 1) {
      showTip.value = true;
    } else {
      scrollToTop();
    }
  };

  /** 滚动到顶部（倒置后相当于滚动到最新消息） */
  const scrollToTop = () => {
    scroll.value.top = scroll.value.oldTop;
    setTimeout(() => {
      scroll.value.top = 0;
    }, 200); // 等待 view 层同步
    showTip.value = false;
  };

  /** 设置键盘高度 */
  const setKeyboardHeight = (height) => {
    keyboardHeight.value = height;
    // 键盘弹起时，滚动到最新消息
    if (height > 0) {
      scrollToTop();
    }
  };

  defineExpose({ getMessageList, refreshMessageList });

  /** 监听消息列表滚动 */
  const onScroll = (e) => {
    const { scrollTop } = e.detail;
    scroll.value.oldTop = scrollTop;
    // 当滚动位置超过一定值时，显示"新消息"提示
    if (scrollTop > 100) {
      showTip.value = true;
    } else {
      showTip.value = false;
    }
  };

  // 监听键盘弹起和收起事件
  const setupKeyboardListeners = () => {
    // #ifdef H5
    // H5环境
    window.addEventListener('resize', () => {
      // 窗口大小变化可能是由键盘引起的
      if (
        document.activeElement &&
        (document.activeElement.tagName === 'INPUT' ||
          document.activeElement.tagName === 'TEXTAREA')
      ) {
        // 估算键盘高度，实际上是窗口高度变化
        const currentHeight = window.innerHeight;
        const viewportHeight = window.visualViewport
          ? window.visualViewport.height
          : window.innerHeight;
        const keyboardHeight = currentHeight - viewportHeight;
        setKeyboardHeight(keyboardHeight > 0 ? keyboardHeight : 0);
      } else {
        setKeyboardHeight(0);
      }
    });
    // #endif

    // #ifdef MP-WEIXIN
    // TODO puhui999: 小程序键盘弹起还有点问题，看看怎么适配
    // 微信小程序环境
    uni.onKeyboardHeightChange((res) => {
      setKeyboardHeight(res.height);
    });
    // #endif
  };

  onMounted(() => {
    queryParams.no = 1; // 确保首次加载是第一页
    scroll.value = {
      top: 0,
      oldTop: 0,
    };
    getMessageList();
    setupKeyboardListeners();
  });
</script>

<style lang="scss" scoped>
  .chat-scroll-view {
    height: v-bind(chatScrollHeight);
    width: 100%;
    position: relative;
    background-color: #f8f8f8;
    z-index: 1;
  }

  .message-container {
    width: 100%;
    /* 确保容器至少有一屏高度 */
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
  }

  .message-list {
    width: 100%;
    display: flex;
    flex-direction: column;
    padding-bottom: 20px;
  }

  .message-item {
    margin-bottom: 10px;
  }

  .loading-more {
    width: 100%;
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #999;
    font-size: 14px;
  }

  .back-top {
    .back-top-item {
      height: 30px;
      width: 100px;
      background-color: #fff;
      border-radius: 30px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
</style>
