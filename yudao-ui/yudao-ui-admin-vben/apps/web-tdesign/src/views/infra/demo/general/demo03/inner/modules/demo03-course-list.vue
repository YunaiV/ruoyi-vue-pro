<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { nextTick, ref, watch } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import { getDemo03CourseListByStudentId } from '#/api/infra/demo/demo03/normal';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const loading = ref(true); // 列表的加载中
const list = ref<Demo03StudentApi.Demo03Course[]>([]); // 列表的数据
/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    if (!props.studentId) {
      return [];
    }
    list.value = await getDemo03CourseListByStudentId(props.studentId!);
  } finally {
    loading.value = false;
  }
}

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await getList();
  },
  { immediate: true },
);
</script>

<template>
  <ContentWrap title="学生课程列表">
    <VxeTable :data="list" show-overflow :loading="loading">
      <VxeColumn field="id" title="编号" align="center" />
      <VxeColumn field="studentId" title="学生编号" align="center" />
      <VxeColumn field="name" title="名字" align="center" />
      <VxeColumn field="score" title="分数" align="center" />
      <VxeColumn field="createTime" title="创建时间" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </VxeColumn>
    </VxeTable>
  </ContentWrap>
</template>
