<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import { reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { DICT_TYPE, MpMsgType } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  DatePicker,
  Form,
  FormItem,
  Input,
  Modal,
  Pagination,
  Select,
} from 'ant-design-vue';

import { getMessagePage } from '#/api/mp/message';
import { WxAccountSelect, WxMsg } from '#/views/mp/components';

import MessageTable from './message-table.vue';

defineOptions({ name: 'MpMessage' });

const loading = ref(false);
const total = ref(0); // 数据的总页数
const list = ref<any[]>([]); // 当前页的列表数据

const queryParams = reactive<{
  accountId: number;
  createTime: [Dayjs, Dayjs] | undefined;
  openid: string;
  pageNo: number;
  pageSize: number;
  type: string;
}>({
  accountId: -1,
  createTime: undefined,
  openid: '',
  pageNo: 1,
  pageSize: 10,
  type: MpMsgType.Text,
}); // 搜索参数

const queryFormRef = ref(); // 搜索的表单

// 消息对话框
const messageBoxVisible = ref(false);
const messageBoxUserId = ref(0);

/** 侦听 accountId */
function onAccountChanged(id: number) {
  queryParams.accountId = id;
  queryParams.pageNo = 1;
  handleQuery();
}

/** 查询列表 */
function handleQuery() {
  queryParams.pageNo = 1;
  getList();
}

async function getList() {
  try {
    loading.value = true;
    const data = await getMessagePage(queryParams);
    list.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

/** 重置按钮操作 */
async function resetQuery() {
  // 暂存 accountId，并在 reset 后恢复
  const accountId = queryParams.accountId;
  queryFormRef.value?.resetFields();
  queryParams.accountId = accountId;
  handleQuery();
}

/** 打开消息发送窗口 */
async function handleSend(userId: number) {
  messageBoxUserId.value = userId;
  messageBoxVisible.value = true;
}

/** 分页改变事件 */
function handlePageChange(page: number, pageSize: number) {
  queryParams.pageNo = page;
  queryParams.pageSize = pageSize;
  getList();
}

/** 显示总条数 */
function showTotal(total: number) {
  return `共 ${total} 条`;
}
</script>

<template>
  <Page auto-content-height class="flex flex-col">
    <!-- 搜索工作栏 -->
    <div class="mb-4 rounded-lg bg-background p-4">
      <Form
        ref="queryFormRef"
        :model="queryParams"
        layout="inline"
        class="search-form"
      >
        <FormItem label="公众号" name="accountId">
          <WxAccountSelect @change="onAccountChanged" />
        </FormItem>
        <FormItem label="消息类型" name="type">
          <Select
            v-model:value="queryParams.type"
            placeholder="请选择消息类型"
            class="!w-[240px]"
          >
            <Select.Option
              v-for="dict in getDictOptions(DICT_TYPE.MP_MESSAGE_TYPE)"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </Select.Option>
          </Select>
        </FormItem>
        <FormItem label="用户标识" name="openid">
          <Input
            v-model:value="queryParams.openid"
            placeholder="请输入用户标识"
            allow-clear
            class="!w-[240px]"
          />
        </FormItem>
        <FormItem label="创建时间" name="createTime">
          <DatePicker.RangePicker
            v-model:value="queryParams.createTime"
            :show-time="true"
            class="!w-[240px]"
          />
        </FormItem>
        <FormItem>
          <Button type="primary" @click="handleQuery">
            <template #icon>
              <IconifyIcon icon="mdi:magnify" />
            </template>
            搜索
          </Button>
          <Button class="ml-2" @click="resetQuery">
            <template #icon>
              <IconifyIcon icon="mdi:refresh" />
            </template>
            重置
          </Button>
        </FormItem>
      </Form>
    </div>

    <!-- 列表 -->
    <div class="flex-1 rounded-lg bg-background p-4">
      <MessageTable :list="list" :loading="loading" @send="handleSend" />
      <div v-show="total > 0" class="mt-4 flex justify-end">
        <Pagination
          v-model:current="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          show-size-changer
          show-quick-jumper
          :show-total="showTotal"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- 发送消息的弹窗 -->
    <Modal
      v-model:open="messageBoxVisible"
      title="粉丝消息列表"
      :width="800"
      :footer="null"
      destroy-on-close
    >
      <WxMsg :user-id="messageBoxUserId" />
    </Modal>
  </Page>
</template>

<style scoped>
.search-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style>
