<script lang="ts" setup>
import type { VxeTableInstance } from '#/adapter/vxe-table';
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Input } from 'ant-design-vue';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import { getDemo03CourseListByStudentId } from '#/api/infra/demo/demo03/normal';
import { $t } from '#/locales';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const list = ref<Demo03StudentApi.Demo03Course[]>([]); // 列表的数据
const tableRef = ref<VxeTableInstance>();
/** 添加学生课程 */
async function onAdd() {
  await tableRef.value?.insertAt({} as Demo03StudentApi.Demo03Course, -1);
}

/** 删除学生课程 */
async function handleDelete(row: Demo03StudentApi.Demo03Course) {
  await tableRef.value?.remove(row);
}

/** 提供获取表格数据的方法供父组件调用 */
defineExpose({
  getData: (): Demo03StudentApi.Demo03Course[] => {
    const data = list.value as Demo03StudentApi.Demo03Course[];
    const removeRecords =
      tableRef.value?.getRemoveRecords() as Demo03StudentApi.Demo03Course[];
    const insertRecords =
      tableRef.value?.getInsertRecords() as Demo03StudentApi.Demo03Course[];
    return data
      .filter((row) => !removeRecords.some((removed) => removed.id === row.id))
      ?.concat(insertRecords.map((row: any) => ({ ...row, id: undefined })));
  },
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    list.value = await getDemo03CourseListByStudentId(props.studentId!);
  },
  { immediate: true },
);
</script>

<template>
  <VxeTable ref="tableRef" :data="list" show-overflow class="mx-4">
    <VxeColumn field="name" title="名字" align="center">
      <template #default="{ row }">
        <Input v-model:value="row.name" />
      </template>
    </VxeColumn>
    <VxeColumn field="score" title="分数" align="center">
      <template #default="{ row }">
        <Input v-model:value="row.score" />
      </template>
    </VxeColumn>
    <VxeColumn field="operation" title="操作" align="center">
      <template #default="{ row }">
        <Button
          size="small"
          type="link"
          danger
          @click="handleDelete(row)"
          v-access:code="['infra:demo03-student:delete']"
        >
          {{ $t('ui.actionTitle.delete') }}
        </Button>
      </template>
    </VxeColumn>
  </VxeTable>
  <div class="mt-4 flex justify-center">
    <Button
      type="primary"
      ghost
      @click="onAdd"
      v-access:code="['infra:demo03-student:create']"
    >
      <IconifyIcon icon="lucide:plus" />
      {{ $t('ui.actionTitle.create', ['学生课程']) }}
    </Button>
  </div>
</template>
