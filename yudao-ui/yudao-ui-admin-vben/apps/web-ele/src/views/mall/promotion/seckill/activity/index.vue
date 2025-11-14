<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSeckillActivityApi } from '#/api/mall/promotion/seckill/seckillActivity';

import { onMounted } from 'vue';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { ElLoading, ElMessage, ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  closeSeckillActivity,
  deleteSeckillActivity,
  getSeckillActivityPage,
} from '#/api/mall/promotion/seckill/seckillActivity';
import { getSimpleSeckillConfigList } from '#/api/mall/promotion/seckill/seckillConfig';

import { useGridColumns, useGridFormSchema } from './data';
import { formatConfigNames, formatTimeRange, setConfigList } from './formatter';
import Form from './modules/form.vue';

defineOptions({ name: 'SeckillActivity' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 编辑活动 */
function handleEdit(row: MallSeckillActivityApi.SeckillActivity) {
  formModalApi.setData(row).open();
}

/** 创建活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 关闭活动 */
async function handleClose(row: MallSeckillActivityApi.SeckillActivity) {
  await confirm('确认关闭该秒杀活动吗？');
  await closeSeckillActivity(row.id as number);
  ElMessage.success('关闭成功');
  handleRefresh();
}

/** 删除活动 */
async function handleDelete(row: MallSeckillActivityApi.SeckillActivity) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteSeckillActivity(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSeckillActivityPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    cellConfig: {
      height: 250,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallSeckillActivityApi.SeckillActivity>,
});

/** 初始化 */
onMounted(async () => {
  // 获得秒杀时间段配置
  const configList = await getSimpleSeckillConfigList();
  setConfigList(configList);
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】秒杀活动"
        url="https://doc.iocoder.cn/mall/promotion-seckill/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="秒杀活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['秒杀活动']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:seckill-activity:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>

      <template #configIds="{ row }">
        <div class="flex flex-wrap gap-1">
          <ElTag
            v-for="(configId, index) in row.configIds"
            :key="index"
            class="mr-1"
          >
            {{ formatConfigNames(configId) }}
          </ElTag>
        </div>
      </template>

      <template #timeRange="{ row }">
        {{ formatTimeRange(row.startTime, row.endTime) }}
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:seckill-activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '关闭',
              type: 'danger',
              link: true,
              auth: ['promotion:seckill-activity:close'],
              ifShow: row.status === 0,
              onClick: handleClose.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              auth: ['promotion:seckill-activity:delete'],
              ifShow: row.status !== 0,
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
