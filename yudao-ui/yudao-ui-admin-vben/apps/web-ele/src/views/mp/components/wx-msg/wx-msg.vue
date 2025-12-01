<script lang="ts" setup>
import type { MpUserApi } from '#/api/mp/user/index';

import { nextTick, onMounted, reactive, ref, unref } from 'vue';

import { preferences } from '@vben/preferences';

import { ElButton, ElMessage } from 'element-plus';

import { getMessagePage, sendMessage } from '#/api/mp/message';
import { getUser } from '#/api/mp/user';
import { WxReply } from '#/views/mp/components';

import MsgList from './msg-list.vue';

defineOptions({ name: 'WxMsg' });

const props = defineProps<{
  userId: number;
}>();

const accountId = ref(-1); // 公众号ID，需要通过userId初始化
const loading = ref(false); // 消息列表是否正在加载中
const hasMore = ref(true); // 是否可以加载更多
const list = ref<any[]>([]); // 消息列表
const queryParams = reactive({
  accountId,
  pageNo: 1, // 当前页数
  pageSize: 14, // 每页显示多少条
});

const user: Partial<MpUserApi.User> = reactive({
  accountId, // 公众号账号编号
  avatar: preferences.app.defaultAvatar,
  nickname: '用户', // 由于微信不再提供昵称，直接使用"用户"展示
});

// ========= 消息发送 =========
const sendLoading = ref(false); // 发送消息是否加载中
const reply = ref<any>({
  accountId: -1,
  articles: [],
  type: 'text',
}); // 微信发送消息

const replySelectRef = ref<InstanceType<typeof WxReply> | null>(null); // WxReply组件ref，用于消息发送成功后清除内容
const msgDivRef = ref<HTMLDivElement | null>(null); // 消息显示窗口ref，用于滚动到底部

/** 完成加载 */
onMounted(async () => {
  const data = await getUser(props.userId);
  user.nickname = data.nickname?.length > 0 ? data.nickname : user.nickname;
  user.avatar = data.avatar?.length > 0 ? data.avatar : user.avatar;
  accountId.value = data.accountId;
  reply.value.accountId = data.accountId;

  refreshChange();
});

/** 执行发送 */
async function sendMsg() {
  if (!unref(reply)) {
    return;
  }
  // 公众号限制：客服消息，公众号只允许发送一条
  if (
    reply.value.type === 'news' &&
    reply.value.articles &&
    reply.value.articles.length > 1
  ) {
    reply.value.articles = [reply.value.articles[0]];
    ElMessage.success('图文消息条数限制在 1 条以内，已默认发送第一条');
  }

  const data = await sendMessage({
    ...reply.value,
    userId: props.userId,
  } as any);
  sendLoading.value = false;

  list.value = [...list.value, data];
  await scrollToBottom();

  // 发送后清空数据
  replySelectRef.value?.clear();
}

function loadMore() {
  queryParams.pageNo++;
  getPage(queryParams, null);
}

async function getPage(page: any, params: any = null) {
  loading.value = true;
  const dataTemp = await getMessagePage(
    Object.assign(
      {
        accountId: page.accountId,
        pageNo: page.pageNo,
        pageSize: page.pageSize,
        userId: props.userId,
      },
      params,
    ),
  );

  const scrollHeight = msgDivRef.value?.scrollHeight ?? 0;
  // 处理数据
  const data = dataTemp.list.toReversed();
  list.value = [...data, ...list.value];
  loading.value = false;
  if (data.length < queryParams.pageSize || data.length === 0) {
    hasMore.value = false;
  }
  queryParams.pageNo = page.pageNo;
  queryParams.pageSize = page.pageSize;
  // 滚动到原来的位置
  if (queryParams.pageNo === 1) {
    // 定位到消息底部
    await scrollToBottom();
  } else if (data.length > 0) {
    // 定位滚动条
    await nextTick();
    if (scrollHeight !== 0 && msgDivRef.value) {
      msgDivRef.value.scrollTop =
        msgDivRef.value.scrollHeight - scrollHeight - 100;
    }
  }
}

function refreshChange() {
  getPage(queryParams);
}

/** 定位到消息底部 */
async function scrollToBottom() {
  await nextTick();
  if (msgDivRef.value) {
    msgDivRef.value.scrollTop = msgDivRef.value.scrollHeight;
  }
}
</script>

<template>
  <div class="flex h-full flex-col">
    <div ref="msgDivRef" class="mx-2.5 flex-1 overflow-auto bg-[#eaeaea]">
      <!-- 加载更多 -->
      <div v-loading="loading"></div>
      <div v-if="!loading">
        <div
          v-if="hasMore"
          class="cursor-pointer rounded p-3 text-center text-sm text-[#409eff] transition-colors duration-300 hover:bg-[#f5f7fa]"
          @click="loadMore"
        >
          <span>点击加载更多</span>
        </div>
        <div
          v-else
          class="cursor-not-allowed rounded p-3 text-center text-sm text-[#909399] hover:bg-transparent"
        >
          <span>没有更多了</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <MsgList :list="list" :account-id="accountId" :user="user" />
    </div>

    <div class="p-2.5" v-loading="sendLoading">
      <WxReply ref="replySelectRef" v-model="reply" />
      <ElButton type="primary" class="float-right mb-2 mt-2" @click="sendMsg">
        发送(S)
      </ElButton>
    </div>
  </div>
</template>
