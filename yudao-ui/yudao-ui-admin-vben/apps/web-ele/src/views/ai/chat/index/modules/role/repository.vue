<script setup lang="ts">
import type { AiChatConversationApi } from '#/api/ai/chat/conversation';
import type { AiModelChatRoleApi } from '#/api/ai/model/chatRole';

import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElContainer,
  ElInput,
  ElMain,
  ElTabPane,
  ElTabs,
} from 'element-plus';

import { createChatConversationMy } from '#/api/ai/chat/conversation';
import { deleteMy, getCategoryList, getMyPage } from '#/api/ai/model/chatRole';

import Form from '../../../../model/chatRole/modules/form.vue';
import RoleCategoryList from './category-list.vue';
import RoleList from './list.vue';

const router = useRouter();

const [Drawer] = useVbenDrawer({
  title: '角色管理',
  footer: false,
  class: 'w-2/5',
});

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const loading = ref<boolean>(false); // 加载中
const activeTab = ref<string>('my-role'); // 选中的角色 Tab
const search = ref<string>(''); // 加载中
const myRoleParams = reactive({
  pageNo: 1,
  pageSize: 50,
});
const myRoleList = ref<AiModelChatRoleApi.ChatRole[]>([]); // my 分页大小
const publicRoleParams = reactive({
  pageNo: 1,
  pageSize: 50,
});
const publicRoleList = ref<AiModelChatRoleApi.ChatRole[]>([]); // public 分页大小
const activeCategory = ref<string>('全部'); // 选择中的分类
const categoryList = ref<string[]>([]); // 角色分类类别

/** tabs 点击 */
async function handleTabsClick(tab: any) {
  // 设置切换状态
  activeTab.value = tab;
  // 切换的时候重新加载数据
  await getActiveTabsRole();
}

/** 获取 my role 我的角色 */
async function getMyRole(append?: boolean) {
  const params: AiModelChatRoleApi.ChatRolePageReqVO = {
    ...myRoleParams,
    name: search.value,
    publicStatus: false,
  };
  const { list } = await getMyPage(params);
  if (append) {
    myRoleList.value.push(...list);
  } else {
    myRoleList.value = list;
  }
}

/** 获取 public role 公共角色 */
async function getPublicRole(append?: boolean) {
  const params: AiModelChatRoleApi.ChatRolePageReqVO = {
    ...publicRoleParams,
    category: activeCategory.value === '全部' ? '' : activeCategory.value,
    name: search.value,
    publicStatus: true,
  };
  const { list } = await getMyPage(params);
  if (append) {
    publicRoleList.value.push(...list);
  } else {
    publicRoleList.value = list;
  }
}

/** 获取选中的 tabs 角色 */
async function getActiveTabsRole() {
  if (activeTab.value === 'my-role') {
    myRoleParams.pageNo = 1;
    await getMyRole();
  } else {
    publicRoleParams.pageNo = 1;
    await getPublicRole();
  }
}

/** 获取角色分类列表 */
async function getRoleCategoryList() {
  categoryList.value = ['全部', ...(await getCategoryList())];
}

/** 处理分类点击 */
async function handlerCategoryClick(category: string) {
  // 切换选择的分类
  activeCategory.value = category;
  // 筛选
  await getActiveTabsRole();
}

async function handlerAddRole() {
  formModalApi.setData({ formType: 'my-create' }).open();
}

/** 编辑角色 */
async function handlerCardEdit(role: any) {
  formModalApi.setData({ formType: 'my-update', id: role.id }).open();
}

/** 添加角色成功 */
async function handlerAddRoleSuccess() {
  // 刷新数据
  await getActiveTabsRole();
}

/** 删除角色 */
async function handlerCardDelete(role: any) {
  await deleteMy(role.id);
  // 刷新数据
  await getActiveTabsRole();
}

/** 角色分页：获取下一页 */
async function handlerCardPage(type: string) {
  try {
    loading.value = true;
    if (type === 'public') {
      publicRoleParams.pageNo++;
      await getPublicRole(true);
    } else {
      myRoleParams.pageNo++;
      await getMyRole(true);
    }
  } finally {
    loading.value = false;
  }
}

/** 选择 card 角色：新建聊天对话 */
async function handlerCardUse(role: any) {
  // 1. 创建对话
  const data: AiChatConversationApi.ChatConversation = {
    roleId: role.id,
  } as unknown as AiChatConversationApi.ChatConversation;
  const conversationId = await createChatConversationMy(data);

  // 2. 跳转页面
  await router.push({
    path: '/ai/chat',
    query: {
      conversationId,
    },
  });
}

/** 初始化 */
onMounted(async () => {
  // 获取分类
  await getRoleCategoryList();
  // 获取 role 数据
  await getActiveTabsRole();
});
</script>

<template>
  <Drawer>
    <ElContainer
      class="absolute inset-0 flex h-full w-full flex-col overflow-hidden bg-card"
    >
      <FormModal @success="handlerAddRoleSuccess" />

      <ElMain class="relative m-0 flex-1 overflow-hidden p-0">
        <div class="absolute right-5 top-5 z-100 flex items-center">
          <!-- 搜索输入框 -->
          <ElInput
            v-model="search"
            class="w-60"
            placeholder="请输入搜索的内容"
            @keyup.enter="getActiveTabsRole"
          >
            <template #suffix>
              <IconifyIcon
                icon="lucide:search"
                class="cursor-pointer"
                @click="getActiveTabsRole"
              />
            </template>
          </ElInput>
          <ElButton
            v-if="activeTab === 'my-role'"
            type="primary"
            @click="handlerAddRole"
            class="ml-5"
          >
            <IconifyIcon icon="lucide:user" class="mr-1.5" />
            添加角色
          </ElButton>
        </div>
        <!-- 标签页内容 -->
        <ElTabs
          v-model="activeTab"
          class="relative h-full pb-4 pr-4"
          @tab-click="handleTabsClick"
        >
          <ElTabPane
            name="my-role"
            class="flex h-full flex-col overflow-y-auto"
            label="我的角色"
          >
            <RoleList
              :loading="loading"
              :role-list="myRoleList"
              :show-more="true"
              @on-delete="handlerCardDelete"
              @on-edit="handlerCardEdit"
              @on-use="handlerCardUse"
              @on-page="handlerCardPage('my')"
            />
          </ElTabPane>
          <ElTabPane
            name="public-role"
            class="flex h-full flex-col overflow-y-auto"
            label="公共角色"
          >
            <RoleCategoryList
              :category-list="categoryList"
              :active="activeCategory"
              @on-category-click="handlerCategoryClick"
              class="mx-6"
            />
            <RoleList
              :role-list="publicRoleList"
              @on-delete="handlerCardDelete"
              @on-edit="handlerCardEdit"
              @on-use="handlerCardUse"
              @on-page="handlerCardPage('public')"
              class="mt-5"
              loading
            />
          </ElTabPane>
        </ElTabs>
      </ElMain>
    </ElContainer>
  </Drawer>
</template>
