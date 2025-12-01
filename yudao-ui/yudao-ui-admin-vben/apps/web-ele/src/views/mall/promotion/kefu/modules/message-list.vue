<script lang="ts" setup>
import type { Emoji } from './tools/emoji';

import type { MallKefuConversationApi } from '#/api/mall/promotion/kefu/conversation';
import type { MallKefuMessageApi } from '#/api/mall/promotion/kefu/message';

import { computed, reactive, ref, toRefs, unref, watch } from 'vue';

import { UserTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatDate, isEmpty, jsonParse } from '@vben/utils';

import { useScroll } from '@vueuse/core';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import {
  ElAvatar,
  ElEmpty,
  ElImage,
  ElInput,
  ElNotification,
} from 'element-plus';

import {
  getKeFuMessageList,
  sendKeFuMessage,
  updateKeFuMessageReadStatus,
} from '#/api/mall/promotion/kefu/message';
import { useMallKefuStore } from '#/store/mall/kefu';

import MessageItem from './message/message-item.vue';
import OrderItem from './message/order-item.vue';
import ProductItem from './message/product-item.vue';
import { KeFuMessageContentTypeEnum } from './tools/constants';
import { useEmoji } from './tools/emoji';
import EmojiSelectPopover from './tools/emoji-select-popover.vue';
import PictureSelectUpload from './tools/picture-select-upload.vue';

dayjs.extend(relativeTime);

const message = ref(''); // 消息弹窗
const { replaceEmoji } = useEmoji();
const messageList = ref<MallKefuMessageApi.Message[]>([]); // 消息列表
const conversation = ref<MallKefuConversationApi.Conversation>(
  {} as MallKefuConversationApi.Conversation,
); // 用户会话
const showNewMessageTip = ref(false); // 显示有新消息提示
const queryParams = reactive({
  conversationId: 0,
  createTime: undefined,
});
const total = ref(0); // 消息总条数
const refreshContent = ref(false); // 内容刷新,主要解决会话消息页面高度不一致导致的滚动功能精度失效
const kefuStore = useMallKefuStore(); // 客服缓存
const skipGetMessageList = ref(false); // 跳过消息获取
const loadHistory = ref(false); // 加载历史消息

/** 获悉消息内容 */
const getMessageContent = computed(
  () => (item: any) => jsonParse(item.content),
);

/** 获得消息列表 */
async function getMessageList() {
  const res: any = await getKeFuMessageList(queryParams as any);
  if (isEmpty(res)) {
    // 当返回的是空列表说明没有消息或者已经查询完了历史消息
    skipGetMessageList.value = true;
    return;
  }
  queryParams.createTime = formatDate(
    (res as any).at(-1).createTime,
    'YYYY-MM-DD HH:mm:ss',
  ) as any;

  // 如果 createTime 存在，说明是加载历史消息，需要追加到列表
  if (queryParams.createTime) {
    // 情况一：加载历史消息，追加到列表
    for (const item of res as any) {
      pushMessage(item);
    }
  } else {
    // 情况二：加载最新消息，直接替换列表
    messageList.value = res;
  }
  refreshContent.value = true;
}

/** 添加消息 */
function pushMessage(message: any) {
  if (messageList.value.some((val) => val.id === message.id)) {
    return;
  }
  messageList.value.push(message);
}

/** 按照时间倒序，获取消息列表 */
const getMessageList0 = computed(() => {
  // 使用展开运算符创建新数组，避免直接修改原数组
  return [...messageList.value].toSorted(
    (a: any, b: any) => a.createTime - b.createTime,
  );
});

/** 刷新消息列表 */
async function refreshMessageList(message?: any) {
  if (!conversation.value) {
    return;
  }

  if (message === undefined) {
    queryParams.createTime = undefined;
    await getMessageList();
  } else {
    // 当前查询会话与消息所属会话不一致则不做处理
    if (message.conversationId !== conversation.value.id) {
      return;
    }
    pushMessage(message);
  }

  if (loadHistory.value) {
    // 右下角显示有新消息提示
    showNewMessageTip.value = true;
  } else {
    // 滚动到最新消息处
    await handleToNewMessage();
  }
}

/** 获得新会话的消息列表, 点击切换时，读取缓存；然后异步获取新消息，merge 下； */
async function getNewMessageList(val: MallKefuConversationApi.Conversation) {
  // 1. 缓存当前会话消息列表
  kefuStore.saveMessageList(conversation.value.id, messageList.value);
  // 2.1 会话切换,重置相关参数
  messageList.value = kefuStore.getConversationMessageList(val.id) || [];
  total.value = messageList.value.length || 0;
  loadHistory.value = false;
  refreshContent.value = false;
  skipGetMessageList.value = false;
  // 2.2 设置会话相关属性
  conversation.value = val;
  queryParams.conversationId = val.id;
  queryParams.createTime = undefined;
  // 3. 获取消息
  await refreshMessageList();
}
defineExpose({ getNewMessageList, refreshMessageList });

/** 是否显示聊天区域 */
function showMessageList() {
  return !isEmpty(conversation.value);
}

/** 处理表情选择 */
function handleEmojiSelect(item: Emoji) {
  message.value += item.name;
}

/** 处理图片发送 */
async function handleSendPicture(picUrl: string) {
  // 组织发送消息
  const msg = {
    conversationId: conversation.value.id,
    contentType: KeFuMessageContentTypeEnum.IMAGE,
    content: JSON.stringify({ picUrl }),
  };
  await sendMessage(msg);
}

/** 发送文本消息 */
async function handleSendMessage(event: any) {
  // shift 不发送
  if (event.shiftKey) {
    return;
  }
  // 1. 校验消息是否为空
  if (isEmpty(unref(message.value)?.trim())) {
    ElNotification.warning({ message: '请输入消息后再发送哦！' });
    message.value = '';
    return;
  }
  // 2. 组织发送消息
  const msg = {
    conversationId: conversation.value.id,
    contentType: KeFuMessageContentTypeEnum.TEXT,
    content: JSON.stringify({ text: message.value }),
  };
  await sendMessage(msg);
}

/** 真正发送消息 【共用】*/
async function sendMessage(msg: MallKefuMessageApi.MessageSend) {
  // 发送消息
  await sendKeFuMessage(msg);
  message.value = '';
  // 加载消息列表
  await refreshMessageList();
  // 更新会话缓存
  await kefuStore.updateConversation(conversation.value.id);
}

/** 滚动到底部 */
const scrollbarRef = ref<HTMLElement | null>(null);
const { y, arrivedState } = useScroll(scrollbarRef);

const { bottom } = toRefs(arrivedState);

async function scrollToBottom() {
  if (!scrollbarRef.value) return;
  // 1. 首次加载时滚动到最新消息，如果加载的是历史消息则不滚动
  if (loadHistory.value) {
    return;
  }
  // 2.1 滚动到最新消息，关闭新消息提示
  // 使用 useScroll 监听滚动容器
  y.value = scrollbarRef.value.scrollHeight - scrollbarRef.value!.clientHeight;
  showNewMessageTip.value = false;
  // 2.2 消息已读
  await updateKeFuMessageReadStatus(conversation.value.id);
}

/** 查看新消息 */
async function handleToNewMessage() {
  loadHistory.value = false;
  await scrollToBottom();
}

watch(
  () => bottom.value,
  (newVal) => {
    if (newVal) {
      if (skipGetMessageList.value) {
        return;
      }
      // 滚动到底部了
      if (newVal) {
        loadHistory.value = false;
        refreshMessageList();
      }

      // 触顶自动加载下一页数据
      if (arrivedState.top) {
        handleOldMessage();
      }
    }
  },
);
/** 加载历史消息 */
async function handleOldMessage() {
  loadHistory.value = true;
  await getMessageList();
}

/** 是否显示时间 */
function showTime(item: MallKefuMessageApi.Message, index: number) {
  if (unref(messageList.value)[index + 1]) {
    const dateString = dayjs(
      unref(messageList.value)[index + 1]!.createTime,
    ).fromNow();
    return dateString !== dayjs(unref(item).createTime).fromNow();
  }
  return false;
}
</script>

<template>
  <div
    v-if="showMessageList()"
    class="flex h-full flex-auto flex-col bg-background p-4"
  >
    <div class="flex h-full flex-auto flex-shrink-0 flex-col">
      <div class="flex h-12 w-full flex-row items-center justify-between">
        <span class="text-lg font-bold">{{ conversation.userNickname }}</span>
      </div>
      <div
        ref="scrollbarRef"
        class="mb-4 flex h-full flex-col overflow-x-auto rounded-lg bg-gray-100 p-2"
      >
        <div class="flex flex-col">
          <!-- 消息列表 -->
          <div
            v-for="(item, index) in getMessageList0"
            :key="item.id"
            class="w-full"
          >
            <div class="mb-5 flex items-center justify-center">
              <!-- 日期 -->
              <div
                v-if="
                  item.contentType !== KeFuMessageContentTypeEnum.SYSTEM &&
                  showTime(item, index)
                "
                class="w-fit rounded-lg bg-black/10 px-2 text-xs"
              >
                {{ formatDate(item.createTime) }}
              </div>
              <!-- 系统消息 -->
              <div
                v-if="item.contentType === KeFuMessageContentTypeEnum.SYSTEM"
                class="w-fit rounded-lg bg-black/10 px-2 text-xs"
              >
                {{ item.content }}
              </div>
            </div>
            <div
              :class="[
                item.senderType === UserTypeEnum.MEMBER
                  ? 'justify-start'
                  : item.senderType === UserTypeEnum.ADMIN
                    ? 'justify-end'
                    : '',
              ]"
              class="mb-5 flex w-full"
            >
              <ElAvatar
                v-if="item.senderType === UserTypeEnum.MEMBER"
                :src="conversation.userAvatar"
                alt="avatar"
                class="size-8"
              />
              <div
                :class="{
                  'w-auto max-w-[50%] p-1 font-medium text-gray-500 transition-all duration-200 hover:scale-105':
                    KeFuMessageContentTypeEnum.TEXT === item.contentType,
                  'm-1 break-words rounded-lg bg-gray-100':
                    KeFuMessageContentTypeEnum.TEXT === item.contentType &&
                    item.senderType === UserTypeEnum.MEMBER,
                  'm-1 break-words rounded-lg bg-blue-50':
                    KeFuMessageContentTypeEnum.TEXT === item.contentType &&
                    item.senderType === UserTypeEnum.ADMIN,
                }"
              >
                <!-- 文本消息 -->
                <MessageItem :message="item">
                  <template
                    v-if="KeFuMessageContentTypeEnum.TEXT === item.contentType"
                  >
                    <div
                      v-dompurify-html="
                        replaceEmoji(
                          getMessageContent(item).text || item.content,
                        )
                      "
                      class="h-full w-full text-justify"
                    ></div>
                  </template>
                </MessageItem>
                <!-- 图片消息 -->
                <MessageItem :message="item">
                  <ElImage
                    v-if="KeFuMessageContentTypeEnum.IMAGE === item.contentType"
                    :initial-index="0"
                    :preview-src-list="[
                      getMessageContent(item).picUrl || item.content,
                    ]"
                    :src="getMessageContent(item).picUrl || item.content"
                    class="mx-2 !w-52"
                    fit="contain"
                    preview-teleported
                  />
                </MessageItem>
                <!-- 商品消息 -->
                <MessageItem :message="item">
                  <ProductItem
                    v-if="
                      KeFuMessageContentTypeEnum.PRODUCT === item.contentType
                    "
                    :pic-url="getMessageContent(item).picUrl"
                    :price="getMessageContent(item).price"
                    :sales-count="getMessageContent(item).salesCount"
                    :spu-id="getMessageContent(item).spuId"
                    :stock="getMessageContent(item).stock"
                    :title="getMessageContent(item).spuName"
                    class="mx-2 max-w-80"
                  />
                </MessageItem>
                <!-- 订单消息 -->
                <MessageItem :message="item">
                  <OrderItem
                    v-if="KeFuMessageContentTypeEnum.ORDER === item.contentType"
                    :message="item"
                    class="mx-2 max-w-full"
                  />
                </MessageItem>
              </div>
              <ElAvatar
                v-if="item.senderType === UserTypeEnum.ADMIN"
                :src="item.senderAvatar"
                alt="avatar"
              />
            </div>
          </div>
        </div>
      </div>
      <div
        v-show="showNewMessageTip"
        class="absolute bottom-9 right-9 z-10 flex cursor-pointer items-center rounded-lg p-2 text-xs shadow-md"
        @click="handleToNewMessage"
      >
        <span>有新消息</span>
        <IconifyIcon class="ml-1" icon="lucide:arrow-down-from-line" />
      </div>
      <div class="flex flex-col">
        <div class="-mt-2 flex flex-col rounded-xl border">
          <div class="flex h-10 w-full items-center">
            <EmojiSelectPopover @select-emoji="handleEmojiSelect" />
            <PictureSelectUpload
              class="ml-4 mt-1 cursor-pointer"
              @send-picture="handleSendPicture"
            />
          </div>
          <ElInput
            type="textarea"
            v-model="message"
            :rows="4"
            class="border-none p-2"
            placeholder="输入消息，Enter发送，Shift+Enter换行"
            @keyup.enter="handleSendMessage"
            :input-style="{ boxShadow: 'none' }"
          />
        </div>
      </div>
    </div>
  </div>
  <div v-else class="relative bg-background">
    <ElEmpty description="请选择左侧的一个会话后开始" class="mt-[20%]" />
  </div>
</template>
