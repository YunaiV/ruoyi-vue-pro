<script setup lang="ts">
import type { PropType } from 'vue';

import type { AiChatConversationApi } from '#/api/ai/chat/conversation';

import { h, onMounted, ref, toRefs, watch } from 'vue';

import { confirm, prompt, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, SvgGptIcon } from '@vben/icons';

import { Avatar, Button, Empty, Input, Layout, message } from 'ant-design-vue';

import {
  createChatConversationMy,
  deleteChatConversationMy,
  deleteChatConversationMyByUnpinned,
  getChatConversationMyList,
  updateChatConversationMy,
} from '#/api/ai/chat/conversation';

import RoleRepository from '../role/RoleRepository.vue';

// 定义组件 props
const props = defineProps({
  activeId: {
    type: [Number, null] as PropType<null | number>,
    default: null,
  },
});

// 定义钩子
const emits = defineEmits([
  'onConversationCreate',
  'onConversationClick',
  'onConversationClear',
  'onConversationDelete',
]);

const [Drawer, drawerApi] = useVbenDrawer({
  connectedComponent: RoleRepository,
});

// 定义属性
const searchName = ref<string>(''); // 对话搜索
const activeConversationId = ref<null | number>(null); // 选中的对话，默认为 null
const hoverConversationId = ref<null | number>(null); // 悬浮上去的对话
const conversationList = ref([] as AiChatConversationApi.ChatConversation[]); // 对话列表
const conversationMap = ref<any>({}); // 对话分组 (置顶、今天、三天前、一星期前、一个月前)
const loading = ref<boolean>(false); // 加载中
const loadingTime = ref<any>();

/** 搜索对话 */
async function searchConversation() {
  // 恢复数据
  if (searchName.value.trim().length === 0) {
    conversationMap.value = await getConversationGroupByCreateTime(
      conversationList.value,
    );
  } else {
    // 过滤
    const filterValues = conversationList.value.filter((item) => {
      return item.title.includes(searchName.value.trim());
    });
    conversationMap.value =
      await getConversationGroupByCreateTime(filterValues);
  }
}

/** 点击对话 */
async function handleConversationClick(id: number) {
  // 过滤出选中的对话
  const filterConversation = conversationList.value.find((item) => {
    return item.id === id;
  });
  // 回调 onConversationClick
  // noinspection JSVoidFunctionReturnValueUsed
  const success = emits('onConversationClick', filterConversation) as any;
  // 切换对话
  if (success) {
    activeConversationId.value = id;
  }
}

/** 获取对话列表 */
async function getChatConversationList() {
  try {
    // 加载中
    loadingTime.value = setTimeout(() => {
      loading.value = true;
    }, 50);

    // 1.1 获取 对话数据
    conversationList.value = await getChatConversationMyList();
    // 1.2 排序
    conversationList.value.sort((a, b) => {
      return Number(b.createTime) - Number(a.createTime);
    });
    // 1.3 没有任何对话情况
    if (conversationList.value.length === 0) {
      activeConversationId.value = null;
      conversationMap.value = {};
      return;
    }

    // 2. 对话根据时间分组(置顶、今天、一天前、三天前、七天前、30 天前)
    conversationMap.value = await getConversationGroupByCreateTime(
      conversationList.value,
    );
  } finally {
    // 清理定时器
    if (loadingTime.value) {
      clearTimeout(loadingTime.value);
    }
    // 加载完成
    loading.value = false;
  }
}

/** 按照 creteTime 创建时间，进行分组 */
async function getConversationGroupByCreateTime(
  list: AiChatConversationApi.ChatConversation[],
) {
  // 排序、指定、时间分组(今天、一天前、三天前、七天前、30天前)
  // noinspection NonAsciiCharacters
  const groupMap: any = {
    置顶: [],
    今天: [],
    一天前: [],
    三天前: [],
    七天前: [],
    三十天前: [],
  };
  // 当前时间的时间戳
  const now = Date.now();
  // 定义时间间隔常量（单位：毫秒）
  const oneDay = 24 * 60 * 60 * 1000;
  const threeDays = 3 * oneDay;
  const sevenDays = 7 * oneDay;
  const thirtyDays = 30 * oneDay;
  for (const conversation of list) {
    // 置顶
    if (conversation.pinned) {
      groupMap['置顶'].push(conversation);
      continue;
    }
    // 计算时间差（单位：毫秒）
    const diff = now - Number(conversation.createTime);
    // 根据时间间隔判断
    if (diff < oneDay) {
      groupMap['今天'].push(conversation);
    } else if (diff < threeDays) {
      groupMap['一天前'].push(conversation);
    } else if (diff < sevenDays) {
      groupMap['三天前'].push(conversation);
    } else if (diff < thirtyDays) {
      groupMap['七天前'].push(conversation);
    } else {
      groupMap['三十天前'].push(conversation);
    }
  }
  return groupMap;
}

async function createConversation() {
  // 1. 新建对话
  const conversationId = await createChatConversationMy(
    {} as unknown as AiChatConversationApi.ChatConversation,
  );
  // 2. 获取对话内容
  await getChatConversationList();
  // 3. 选中对话
  await handleConversationClick(conversationId);
  // 4. 回调
  emits('onConversationCreate');
}

/** 修改对话的标题 */
async function updateConversationTitle(
  conversation: AiChatConversationApi.ChatConversation,
) {
  // 1. 二次确认
  prompt({
    async beforeClose(scope) {
      if (scope.isConfirm) {
        if (scope.value) {
          try {
            // 2. 发起修改
            await updateChatConversationMy({
              id: conversation.id,
              title: scope.value,
            } as AiChatConversationApi.ChatConversation);
            message.success('重命名成功');
            // 3. 刷新列表
            await getChatConversationList();
            // 4. 过滤当前切换的
            const filterConversationList = conversationList.value.filter(
              (item) => {
                return item.id === conversation.id;
              },
            );
            if (
              filterConversationList.length > 0 &&
              filterConversationList[0] && // tip：避免切换对话
              activeConversationId.value === filterConversationList[0].id
            ) {
              emits('onConversationClick', filterConversationList[0]);
            }
          } catch {
            return false;
          }
        } else {
          message.error('请输入标题');
          return false;
        }
      }
    },
    component: () => {
      return h(Input, {
        placeholder: '请输入标题',
        allowClear: true,
        defaultValue: conversation.title,
        rules: [{ required: true, message: '请输入标题' }],
      });
    },
    content: '请输入标题',
    title: '修改标题',
    modelPropName: 'value',
  });
}

/** 删除聊天对话 */
async function deleteChatConversation(
  conversation: AiChatConversationApi.ChatConversation,
) {
  try {
    // 删除的二次确认
    await confirm(`是否确认删除对话 - ${conversation.title}?`);
    // 发起删除
    await deleteChatConversationMy(conversation.id);
    message.success('对话已删除');
    // 刷新列表
    await getChatConversationList();
    // 回调
    emits('onConversationDelete', conversation);
  } catch {}
}

async function handleClearConversation() {
  try {
    await confirm('确认后对话会全部清空，置顶的对话除外。');
    await deleteChatConversationMyByUnpinned();
    message.success('操作成功!');
    // 清空 对话 和 对话内容
    activeConversationId.value = null;
    // 获取 对话列表
    await getChatConversationList();
    // 回调 方法
    emits('onConversationClear');
  } catch {}
}

/** 对话置顶 */
async function handleTop(conversation: AiChatConversationApi.ChatConversation) {
  // 更新对话置顶
  conversation.pinned = !conversation.pinned;
  await updateChatConversationMy(conversation);
  // 刷新对话
  await getChatConversationList();
}

// ============ 角色仓库 ============

/** 角色仓库抽屉 */
const handleRoleRepository = async () => {
  drawerApi.open();
};

/** 监听选中的对话 */
const { activeId } = toRefs(props);
watch(activeId, async (newValue) => {
  activeConversationId.value = newValue;
});

// 定义 public 方法
defineExpose({ createConversation });

/** 初始化 */
onMounted(async () => {
  // 获取 对话列表
  await getChatConversationList();
  // 默认选中
  if (props.activeId) {
    activeConversationId.value = props.activeId;
  } else {
    // 首次默认选中第一个
    if (conversationList.value.length > 0 && conversationList.value[0]) {
      activeConversationId.value = conversationList.value[0].id;
      // 回调 onConversationClick
      await emits('onConversationClick', conversationList.value[0]);
    }
  }
});
</script>

<template>
  <Layout.Sider
    width="280px"
    class="conversation-container relative flex h-full flex-col justify-between overflow-hidden p-4"
  >
    <Drawer />
    <!-- 左顶部：对话 -->
    <div class="flex h-full flex-col">
      <Button class="h-9 w-full" type="primary" @click="createConversation">
        <IconifyIcon icon="lucide:plus" class="mr-1" />
        新建对话
      </Button>

      <Input
        v-model:value="searchName"
        size="large"
        class="search-input mt-4"
        placeholder="搜索历史记录"
        @keyup="searchConversation"
      >
        <template #prefix>
          <IconifyIcon icon="lucide:search" />
        </template>
      </Input>

      <!-- 左中间：对话列表 -->
      <div class="conversation-list mt-2 flex-1 overflow-auto">
        <!-- 情况一：加载中 -->
        <Empty v-if="loading" description="." v-loading="loading" />

        <!-- 情况二：按照 group 分组 -->
        <div
          v-for="conversationKey in Object.keys(conversationMap)"
          :key="conversationKey"
          class=""
        >
          <div
            v-if="conversationMap[conversationKey].length > 0"
            class="conversation-item classify-title pt-2"
          >
            <b class="mx-1">
              {{ conversationKey }}
            </b>
          </div>

          <div
            v-for="conversation in conversationMap[conversationKey]"
            :key="conversation.id"
            @click="handleConversationClick(conversation.id)"
            @mouseover="hoverConversationId = conversation.id"
            @mouseout="hoverConversationId = null"
            class="conversation-item mt-1"
          >
            <div
              class="conversation flex cursor-pointer flex-row items-center justify-between rounded-lg px-2 leading-10"
              :class="[
                conversation.id === activeConversationId
                  ? 'bg-primary-200'
                  : '',
              ]"
            >
              <div class="title-wrapper flex items-center">
                <Avatar
                  v-if="conversation.roleAvatar"
                  :src="conversation.roleAvatar"
                />
                <SvgGptIcon v-else class="size-8" />
                <span
                  class="max-w-36 overflow-hidden text-ellipsis whitespace-nowrap p-2 text-sm font-normal text-gray-600"
                >
                  {{ conversation.title }}
                </span>
              </div>

              <div
                v-show="hoverConversationId === conversation.id"
                class="button-wrapper relative right-0.5 flex items-center text-gray-400"
              >
                <Button
                  class="mr-0 px-1"
                  type="link"
                  @click.stop="handleTop(conversation)"
                >
                  <IconifyIcon
                    v-if="!conversation.pinned"
                    icon="lucide:arrow-up-to-line"
                  />
                  <IconifyIcon
                    v-if="conversation.pinned"
                    icon="lucide:arrow-down-from-line"
                  />
                </Button>
                <Button
                  class="mr-0 px-1"
                  type="link"
                  @click.stop="updateConversationTitle(conversation)"
                >
                  <IconifyIcon icon="lucide:edit" />
                </Button>
                <Button
                  class="mr-0 px-1"
                  type="link"
                  @click.stop="deleteChatConversation(conversation)"
                >
                  <IconifyIcon icon="lucide:trash-2" />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部占位 -->
      <div class="h-12 w-full"></div>
    </div>

    <!-- 左底部：工具栏 -->
    <div
      class="bg-card absolute bottom-1 left-0 right-0 mb-4 flex items-center justify-between px-5 leading-9 text-gray-400 shadow-sm"
    >
      <div
        class="flex cursor-pointer items-center text-gray-400"
        @click="handleRoleRepository"
      >
        <IconifyIcon icon="lucide:user" />
        <span class="ml-1">角色仓库</span>
      </div>
      <div
        class="flex cursor-pointer items-center text-gray-400"
        @click="handleClearConversation"
      >
        <IconifyIcon icon="lucide:trash" />
        <span class="ml-1">清空未置顶对话</span>
      </div>
    </div>
  </Layout.Sider>
</template>
