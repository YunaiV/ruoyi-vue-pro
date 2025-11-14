<script setup lang="ts">
import type { PropType } from 'vue';

import type { AiModelChatRoleApi } from '#/api/ai/model/chatRole';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Avatar, Button, Card, Dropdown, Menu } from 'ant-design-vue';

// tabs ref

// 定义属性
const props = defineProps({
  loading: {
    type: Boolean,
    required: true,
  },
  roleList: {
    type: Array as PropType<AiModelChatRoleApi.ChatRole[]>,
    required: true,
  },
  showMore: {
    type: Boolean,
    required: false,
    default: false,
  },
});

// 定义钩子
const emits = defineEmits(['onDelete', 'onEdit', 'onUse', 'onPage']);
const tabsRef = ref<any>();

/** 操作：编辑、删除 */
async function handleMoreClick(data: any) {
  const type = data[0];
  const role = data[1];
  if (type === 'delete') {
    emits('onDelete', role);
  } else {
    emits('onEdit', role);
  }
}

/** 选中 */
function handleUseClick(role: any) {
  emits('onUse', role);
}

/** 滚动 */
async function handleTabsScroll() {
  if (tabsRef.value) {
    const { scrollTop, scrollHeight, clientHeight } = tabsRef.value;
    if (scrollTop + clientHeight >= scrollHeight - 20 && !props.loading) {
      await emits('onPage');
    }
  }
}
</script>

<template>
  <div
    class="relative flex h-full flex-wrap content-start items-start overflow-auto px-6 pb-36"
    ref="tabsRef"
    @scroll="handleTabsScroll"
  >
    <div class="mb-5 mr-5 inline-block" v-for="role in roleList" :key="role.id">
      <Card
        class="relative rounded-lg"
        :body-style="{
          position: 'relative',
          display: 'flex',
          flexDirection: 'row',
          justifyContent: 'flex-start',
          width: '240px',
          maxWidth: '240px',
          padding: '15px 15px 10px',
        }"
      >
        <!-- 更多操作 -->
        <div v-if="showMore" class="absolute right-2 top-0">
          <Dropdown>
            <Button type="link">
              <IconifyIcon icon="lucide:ellipsis-vertical" />
            </Button>
            <template #overlay>
              <Menu>
                <Menu.Item @click="handleMoreClick(['edit', role])">
                  <div class="flex items-center">
                    <IconifyIcon icon="lucide:edit" color="#787878" />
                    <span class="text-primary">编辑</span>
                  </div>
                </Menu.Item>
                <Menu.Item @click="handleMoreClick(['delete', role])">
                  <div class="flex items-center">
                    <IconifyIcon icon="lucide:trash" color="red" />
                    <span class="text-red-500">删除</span>
                  </div>
                </Menu.Item>
              </Menu>
            </template>
          </Dropdown>
        </div>

        <!-- 角色信息 -->
        <div>
          <Avatar :src="role.avatar" class="h-10 w-10 overflow-hidden" />
        </div>

        <div class="ml-2 w-4/5">
          <div class="h-20">
            <div class="max-w-32 text-lg font-bold">
              {{ role.name }}
            </div>
            <div class="mt-2 text-sm">
              {{ role.description }}
            </div>
          </div>
          <div class="mt-1 flex flex-row-reverse">
            <Button type="primary" size="small" @click="handleUseClick(role)">
              使用
            </Button>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>
