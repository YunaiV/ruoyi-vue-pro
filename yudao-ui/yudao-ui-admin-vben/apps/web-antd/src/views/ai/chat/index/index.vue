<script lang="ts" setup>
import type { AiChatConversationApi } from '#/api/ai/chat/conversation';
import type { AiChatMessageApi } from '#/api/ai/chat/message';

import { computed, nextTick, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { alert, confirm, Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Layout, message, Switch } from 'ant-design-vue';

import { getChatConversationMy } from '#/api/ai/chat/conversation';
import {
  deleteByConversationId,
  getChatMessageListByConversationId,
  sendChatMessageStream,
} from '#/api/ai/chat/message';

import ConversationList from './modules/conversation/list.vue';
import ConversationUpdateForm from './modules/conversation/update-form.vue';
import MessageFileUpload from './modules/message/file-upload.vue';
import MessageListEmpty from './modules/message/list-empty.vue';
import MessageList from './modules/message/list.vue';
import MessageLoading from './modules/message/loading.vue';
import MessageNewConversation from './modules/message/new-conversation.vue';

/** AI 聊天对话 列表 */
defineOptions({ name: 'AiChat' });

const route = useRoute();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ConversationUpdateForm,
  destroyOnClose: true,
});

// 聊天对话
const conversationListRef = ref();
const activeConversationId = ref<null | number>(null); // 选中的对话编号
const activeConversation = ref<AiChatConversationApi.ChatConversation | null>(
  null,
); // 选中的 Conversation
const conversationInProgress = ref(false); // 对话是否正在进行中。目前只有【发送】消息时，会更新为 true，避免切换对话、删除对话等操作

// 消息列表
const messageRef = ref();
const activeMessageList = ref<AiChatMessageApi.ChatMessage[]>([]); // 选中对话的消息列表
const activeMessageListLoading = ref<boolean>(false); // activeMessageList 是否正在加载中
const activeMessageListLoadingTimer = ref<any>(); // activeMessageListLoading Timer 定时器。如果加载速度很快，就不进入加载中
// 消息滚动
const textSpeed = ref<number>(50); // Typing speed in milliseconds
const textRoleRunning = ref<boolean>(false); // Typing speed in milliseconds

// 发送消息输入框
const isComposing = ref(false); // 判断用户是否在输入
const conversationInAbortController = ref<any>(); // 对话进行中 abort 控制器(控制 stream 对话)
const inputTimeout = ref<any>(); // 处理输入中回车的定时器
const prompt = ref<string>(); // prompt
const enableContext = ref<boolean>(true); // 是否开启上下文
const enableWebSearch = ref<boolean>(false); // 是否开启联网搜索
const uploadFiles = ref<string[]>([]); // 上传的文件 URL 列表
// 接收 Stream 消息
const receiveMessageFullText = ref('');
const receiveMessageDisplayedText = ref('');

// =========== 【聊天对话】相关 ===========

/** 获取对话信息 */
async function getConversation(id: null | number) {
  if (!id) {
    return;
  }
  const conversation: AiChatConversationApi.ChatConversation =
    await getChatConversationMy(id);
  if (!conversation) {
    return;
  }
  activeConversation.value = conversation;
  activeConversationId.value = conversation.id;
}

/**
 * 点击某个对话
 *
 * @param conversation 选中的对话
 * @return 是否切换成功
 */
async function handleConversationClick(
  conversation: AiChatConversationApi.ChatConversation,
) {
  // 对话进行中，不允许切换
  if (conversationInProgress.value) {
    await alert('对话中，不允许切换!');
    return false;
  }

  // 更新选中的对话 id
  activeConversationId.value = conversation.id;
  activeConversation.value = conversation;
  // 刷新 message 列表
  await getMessageList();
  // 滚动底部
  await scrollToBottom(true);
  prompt.value = '';
  // 清空输入框
  prompt.value = '';
  // 清空文件列表
  uploadFiles.value = [];
  return true;
}

/** 删除某个对话*/
async function handlerConversationDelete(
  delConversation: AiChatConversationApi.ChatConversation,
) {
  // 删除的对话如果是当前选中的，那么就重置
  if (activeConversationId.value === delConversation.id) {
    await handleConversationClear();
  }
}

/** 清空选中的对话 */
async function handleConversationClear() {
  // 对话进行中，不允许切换
  if (conversationInProgress.value) {
    await alert('对话中，不允许切换!');
    return false;
  }
  activeConversationId.value = null;
  activeConversation.value = null;
  activeMessageList.value = [];
  // 清空输入框和文件列表
  prompt.value = '';
  uploadFiles.value = [];
}

async function openChatConversationUpdateForm() {
  formModalApi.setData({ id: activeConversationId.value }).open();
}

/** 对话更新成功，刷新最新信息 */
async function handleConversationUpdateSuccess() {
  await getConversation(activeConversationId.value);
}

/** 处理聊天对话的创建成功 */
async function handleConversationCreate() {
  // 创建对话
  await conversationListRef.value.createConversation();
}

/** 处理聊天对话的创建成功 */
async function handleConversationCreateSuccess() {
  // 创建新的对话，清空输入框
  prompt.value = '';
  // 清空文件列表
  uploadFiles.value = [];
}

// =========== 【消息列表】相关 ===========

/** 获取消息 message 列表 */
async function getMessageList() {
  try {
    if (activeConversationId.value === null) {
      return;
    }
    // Timer 定时器，如果加载速度很快，就不进入加载中
    activeMessageListLoadingTimer.value = setTimeout(() => {
      activeMessageListLoading.value = true;
    }, 60);

    // 获取消息列表
    activeMessageList.value = await getChatMessageListByConversationId(
      activeConversationId.value,
    );

    // 滚动到最下面
    await nextTick();
    await scrollToBottom();
  } finally {
    // time 定时器，如果加载速度很快，就不进入加载中
    if (activeMessageListLoadingTimer.value) {
      clearTimeout(activeMessageListLoadingTimer.value);
    }
    // 加载结束
    activeMessageListLoading.value = false;
  }
}

/**
 * 消息列表
 *
 * 和 {@link #getMessageList()} 的差异是，把 systemMessage 考虑进去
 */
const messageList = computed(() => {
  if (activeMessageList.value.length > 0) {
    return activeMessageList.value;
  }
  // 没有消息时，如果有 systemMessage 则展示它
  if (activeConversation.value?.systemMessage) {
    return [
      {
        id: 0,
        type: 'system',
        content: activeConversation.value.systemMessage,
      },
    ];
  }
  return [];
});

/** 处理删除 message 消息 */
function handleMessageDelete() {
  if (conversationInProgress.value) {
    alert('回答中，不能删除!');
    return;
  }
  // 刷新 message 列表
  getMessageList();
}

/** 处理 message 清空 */
async function handlerMessageClear() {
  if (!activeConversationId.value) {
    return;
  }
  try {
    // 确认提示
    await confirm('确认清空对话消息？');
    // 清空对话
    await deleteByConversationId(activeConversationId.value);
    // 刷新 message 列表
    activeMessageList.value = [];
  } catch {}
}

/** 回到 message 列表的顶部 */
function handleGoTopMessage() {
  messageRef.value.handlerGoTop();
}

// =========== 【发送消息】相关 ===========

/** 处理来自 keydown 的发送消息 */
async function handleSendByKeydown(event: any) {
  // 判断用户是否在输入
  if (isComposing.value) {
    return;
  }
  // 进行中不允许发送
  if (conversationInProgress.value) {
    return;
  }
  const content = prompt.value?.trim() as string;
  if (event.key === 'Enter') {
    if (event.shiftKey) {
      // 插入换行
      prompt.value += '\r\n';
      event.preventDefault(); // 防止默认的换行行为
    } else {
      // 发送消息
      await doSendMessage(content);
      event.preventDefault(); // 防止默认的提交行为
    }
  }
}

/** 处理来自【发送】按钮的发送消息 */
function handleSendByButton() {
  doSendMessage(prompt.value?.trim() as string);
}

/** 处理 prompt 输入变化 */
function handlePromptInput(event: any) {
  // 非输入法 输入设置为 true
  if (!isComposing.value) {
    // 回车 event data 是 null
    if (event.data === null || event.data === 'null') {
      return;
    }
    isComposing.value = true;
  }
  // 清理定时器
  if (inputTimeout.value) {
    clearTimeout(inputTimeout.value);
  }
  // 重置定时器
  inputTimeout.value = setTimeout(() => {
    isComposing.value = false;
  }, 400);
}

function onCompositionstart() {
  isComposing.value = true;
}

function onCompositionend() {
  setTimeout(() => {
    isComposing.value = false;
  }, 200);
}

/** 真正执行【发送】消息操作 */
async function doSendMessage(content: string) {
  // 校验
  if (content.length === 0) {
    message.error('发送失败，原因：内容为空！');
    return;
  }
  if (activeConversationId.value === null) {
    message.error('还没创建对话，不能发送!');
    return;
  }

  // 准备附件 URL 数组
  const attachmentUrls = [...uploadFiles.value];

  // 清空输入框和文件列表
  prompt.value = '';
  uploadFiles.value = [];

  // 执行发送
  await doSendMessageStream({
    conversationId: activeConversationId.value,
    content,
    attachmentUrls,
  } as AiChatMessageApi.ChatMessage);
}

/** 真正执行【发送】消息操作 */
async function doSendMessageStream(userMessage: AiChatMessageApi.ChatMessage) {
  // 创建 AbortController 实例，以便中止请求
  conversationInAbortController.value = new AbortController();
  // 标记对话进行中
  conversationInProgress.value = true;
  // 设置为空
  receiveMessageFullText.value = '';

  try {
    // 1.1 先添加两个假数据，等 stream 返回再替换
    activeMessageList.value.push(
      {
        id: -1,
        conversationId: activeConversationId.value,
        type: 'user',
        content: userMessage.content,
        attachmentUrls: userMessage.attachmentUrls || [],
        createTime: new Date(),
      } as AiChatMessageApi.ChatMessage,
      {
        id: -2,
        conversationId: activeConversationId.value,
        type: 'assistant',
        content: '思考中...',
        reasoningContent: '',
        createTime: new Date(),
      } as AiChatMessageApi.ChatMessage,
    );
    // 1.2 滚动到最下面
    await nextTick();
    await scrollToBottom(); // 底部
    // 1.3 开始滚动
    textRoll().then();

    // 2. 发送 event stream
    let isFirstChunk = true; // 是否是第一个 chunk 消息段
    await sendChatMessageStream(
      userMessage.conversationId,
      userMessage.content,
      conversationInAbortController.value,
      enableContext.value,
      enableWebSearch.value,
      async (res: any) => {
        const { code, data, msg } = JSON.parse(res.data);
        if (code !== 0) {
          await alert(`对话异常! ${msg}`);
          // 如果未接收到消息，则进行删除
          if (receiveMessageFullText.value === '') {
            activeMessageList.value.pop();
          }
          return;
        }

        // 如果内容和推理内容都为空，就不处理
        if (data.receive.content === '' && !data.receive.reasoningContent) {
          return;
        }

        // 首次返回需要添加一个 message 到页面，后面的都是更新
        if (isFirstChunk) {
          isFirstChunk = false;
          // 弹出两个假数据
          activeMessageList.value.pop();
          activeMessageList.value.pop();
          // 更新返回的数据
          activeMessageList.value.push(data.send, data.receive);
          data.send.attachmentUrls = userMessage.attachmentUrls;
        }

        // 处理 reasoningContent
        if (data.receive.reasoningContent) {
          const lastMessage =
            activeMessageList.value[activeMessageList.value.length - 1];
          // 累加推理内容
          lastMessage!.reasoningContent =
            (lastMessage!.reasoningContent || '') +
            data.receive.reasoningContent;
        }

        // 处理正常内容
        if (data.receive.content !== '') {
          receiveMessageFullText.value =
            receiveMessageFullText.value + data.receive.content;
        }

        // 滚动到最下面
        await scrollToBottom();
      },
      (error: any) => {
        // 异常提示，并停止流
        alert(`对话异常!`);
        stopStream();
        // 需要抛出异常，禁止重试
        throw error;
      },
      () => {
        stopStream();
      },
      userMessage.attachmentUrls,
    );
  } catch {}
}

/** 停止 stream 流式调用 */
async function stopStream() {
  // tip：如果 stream 进行中的 message，就需要调用 controller 结束
  if (conversationInAbortController.value) {
    conversationInAbortController.value.abort();
  }
  // 设置为 false
  conversationInProgress.value = false;
}

/** 编辑 message：设置为 prompt，可以再次编辑 */
function handleMessageEdit(message: AiChatMessageApi.ChatMessage) {
  prompt.value = message.content;
}

/** 刷新 message：基于指定消息，再次发起对话 */
function handleMessageRefresh(message: AiChatMessageApi.ChatMessage) {
  doSendMessage(message.content);
}

// ============== 【消息滚动】相关 =============

/** 滚动到 message 底部 */
async function scrollToBottom(isIgnore?: boolean) {
  await nextTick();
  if (messageRef.value) {
    messageRef.value.scrollToBottom(isIgnore);
  }
}

/** 自提滚动效果 */
async function textRoll() {
  let index = 0;
  try {
    // 只能执行一次
    if (textRoleRunning.value) {
      return;
    }
    // 设置状态
    textRoleRunning.value = true;
    receiveMessageDisplayedText.value = '';
    async function task() {
      // 调整速度
      const diff =
        (receiveMessageFullText.value.length -
          receiveMessageDisplayedText.value.length) /
        10;
      if (diff > 5) {
        textSpeed.value = 10;
      } else if (diff > 2) {
        textSpeed.value = 30;
      } else if (diff > 1.5) {
        textSpeed.value = 50;
      } else {
        textSpeed.value = 100;
      }
      // 对话结束，就按 30 的速度
      if (!conversationInProgress.value) {
        textSpeed.value = 10;
      }

      if (index < receiveMessageFullText.value.length) {
        receiveMessageDisplayedText.value +=
          receiveMessageFullText.value[index];
        index++;

        // 更新 message
        const lastMessage =
          activeMessageList.value[activeMessageList.value.length - 1];
        if (lastMessage)
          lastMessage.content = receiveMessageDisplayedText.value;
        // 滚动到住下面
        await scrollToBottom();
        // 重新设置任务
        timer = setTimeout(task, textSpeed.value);
      } else {
        // 不是对话中可以结束
        if (conversationInProgress.value) {
          // 重新设置任务
          timer = setTimeout(task, textSpeed.value);
        } else {
          textRoleRunning.value = false;
          clearTimeout(timer);
        }
      }
    }
    let timer = setTimeout(task, textSpeed.value);
  } catch {}
}

/** 初始化 */
onMounted(async () => {
  // 如果有 conversationId 参数，则默认选中
  if (route.query.conversationId) {
    const id = route.query.conversationId as unknown as number;
    activeConversationId.value = id;
    await getConversation(id);
  }

  // 获取列表数据
  activeMessageListLoading.value = true;
  await getMessageList();
});
</script>

<template>
  <Page auto-content-height>
    <Layout class="absolute left-0 top-0 m-4 h-full w-full flex-1">
      <!-- 左侧：对话列表 -->
      <ConversationList
        class="!bg-card"
        :active-id="activeConversationId"
        ref="conversationListRef"
        @on-conversation-create="handleConversationCreateSuccess"
        @on-conversation-click="handleConversationClick"
        @on-conversation-clear="handleConversationClear"
        @on-conversation-delete="handlerConversationDelete"
      />

      <!-- 右侧：详情部分 -->
      <Layout class="mx-4 bg-card">
        <Layout.Header
          class="flex !h-12 items-center justify-between border-b border-border !bg-card !px-4"
        >
          <div class="text-lg font-bold">
            {{ activeConversation?.title ? activeConversation?.title : '对话' }}
            <span v-if="activeMessageList.length > 0">
              ({{ activeMessageList.length }})
            </span>
          </div>

          <div class="flex w-72 justify-end" v-if="activeConversation">
            <Button
              type="primary"
              ghost
              class="mr-2 px-2"
              size="small"
              @click="openChatConversationUpdateForm"
            >
              <span v-html="activeConversation?.modelName"></span>
              <IconifyIcon icon="lucide:settings" class="ml-2 size-4" />
            </Button>
            <Button size="small" class="mr-2 px-2" @click="handlerMessageClear">
              <IconifyIcon icon="lucide:trash-2" color="#787878" />
            </Button>
            <Button size="small" class="mr-2 px-2">
              <IconifyIcon icon="lucide:download" color="#787878" />
            </Button>
            <Button size="small" class="mr-2 px-2" @click="handleGoTopMessage">
              <IconifyIcon icon="lucide:arrow-up" color="#787878" />
            </Button>
          </div>
        </Layout.Header>

        <Layout.Content class="relative m-0 h-full w-full p-0">
          <div class="absolute inset-0 m-0 overflow-y-hidden p-0">
            <MessageLoading v-if="activeMessageListLoading" />
            <MessageNewConversation
              v-if="!activeConversation"
              @on-new-conversation="handleConversationCreate"
            />
            <MessageListEmpty
              v-if="
                !activeMessageListLoading &&
                messageList.length === 0 &&
                activeConversation
              "
              @on-prompt="doSendMessage"
            />
            <MessageList
              v-if="!activeMessageListLoading && messageList.length > 0"
              ref="messageRef"
              :conversation="activeConversation as any"
              :list="messageList as any"
              @on-delete-success="handleMessageDelete"
              @on-edit="handleMessageEdit"
              @on-refresh="handleMessageRefresh"
            />
          </div>
        </Layout.Content>

        <Layout.Footer class="flex flex-col !bg-card !p-0">
          <form
            class="mx-4 mb-8 mt-2 flex flex-col rounded-xl border border-border p-2"
          >
            <textarea
              class="box-border h-24 resize-none overflow-auto rounded-md p-2 focus:outline-none"
              v-model="prompt"
              @keydown="handleSendByKeydown"
              @input="handlePromptInput"
              @compositionstart="onCompositionstart"
              @compositionend="onCompositionend"
              placeholder="问我任何问题...（Shift+Enter 换行，按下 Enter 发送）"
            ></textarea>
            <div class="flex justify-between pb-0 pt-1">
              <div class="flex items-center gap-3">
                <MessageFileUpload
                  v-model="uploadFiles"
                  :disabled="conversationInProgress"
                />
                <div class="flex items-center">
                  <Switch v-model:checked="enableContext" size="small" />
                  <span class="ml-1 text-sm text-gray-400">上下文</span>
                </div>
                <div class="flex items-center">
                  <Switch v-model:checked="enableWebSearch" size="small" />
                  <span class="ml-1 text-sm text-gray-400">联网搜索</span>
                </div>
              </div>
              <Button
                type="primary"
                @click="handleSendByButton"
                :loading="conversationInProgress"
                v-if="conversationInProgress === false"
              >
                <IconifyIcon
                  :icon="
                    conversationInProgress
                      ? 'lucide:loader'
                      : 'lucide:send-horizontal'
                  "
                />
                {{ conversationInProgress ? '进行中' : '发送' }}
              </Button>
              <Button
                type="primary"
                danger
                @click="stopStream()"
                v-if="conversationInProgress === true"
              >
                <IconifyIcon icon="lucide:circle-stop" />
                停止
              </Button>
            </div>
          </form>
        </Layout.Footer>
      </Layout>
    </Layout>
    <FormModal @success="handleConversationUpdateSuccess" />
  </Page>
</template>
