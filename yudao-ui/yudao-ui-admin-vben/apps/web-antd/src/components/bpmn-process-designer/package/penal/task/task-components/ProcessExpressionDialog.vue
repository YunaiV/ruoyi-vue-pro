<!-- 表达式选择 -->
<script setup lang="ts">
import type { BpmProcessExpressionApi } from '#/api/bpm/processExpression';

import { reactive, ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { CommonStatusEnum } from '@vben/constants';

import { Button, Modal, Pagination, Table, TableColumn } from 'ant-design-vue';

import { getProcessExpressionPage } from '#/api/bpm/processExpression';

/** BPM 流程 表单 */
defineOptions({ name: 'ProcessExpressionDialog' });

/** 提交表单 */
const emit = defineEmits(['select']);
const dialogVisible = ref(false); // 弹窗的是否展示
const loading = ref(true); // 列表的加载中
const list = ref<BpmProcessExpressionApi.ProcessExpression[]>([]); // 列表的数据
const total = ref(0); // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  type: '',
  status: CommonStatusEnum.ENABLE,
});

/** 打开弹窗 */
const open = (type: string) => {
  queryParams.pageNo = 1;
  queryParams.type = type;
  getList();
  dialogVisible.value = true;
};
defineExpose({ open }); // 提供 open 方法，用于打开弹窗

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const data = await getProcessExpressionPage(queryParams);
    list.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

// 定义 select 事件，用于操作成功后的回调
const select = async (row: BpmProcessExpressionApi.ProcessExpression) => {
  dialogVisible.value = false;
  // 发送操作成功的事件
  emit('select', row);
};

// const handleCancel = () => {
//   dialogVisible.value = false;
// };
</script>
<template>
  <Modal
    title="请选择表达式"
    v-model:open="dialogVisible"
    width="1024px"
    :footer="null"
  >
    <ContentWrap>
      <Table
        :loading="loading"
        :data-source="list"
        :pagination="false"
        :scroll="{ x: 'max-content' }"
      >
        <TableColumn title="名字" align="center" data-index="name" />
        <TableColumn title="表达式" align="center" data-index="expression" />
        <TableColumn title="操作" align="center">
          <template #default="{ record }">
            <Button type="primary" @click="select(record)"> 选择 </Button>
          </template>
        </TableColumn>
      </Table>
      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <Pagination
          :total="total"
          v-model:current="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          show-size-changer
          @change="getList"
        />
      </div>
    </ContentWrap>
  </Modal>
</template>
