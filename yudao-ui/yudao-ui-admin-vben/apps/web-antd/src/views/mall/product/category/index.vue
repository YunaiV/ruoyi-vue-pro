<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCategoryApi } from '#/api/mall/product/category';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteCategory, getCategoryList } from '#/api/mall/product/category';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建分类 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 添加下级分类 */
function handleAppend(row: MallCategoryApi.Category) {
  formModalApi.setData({ parentId: row.id }).open();
}

/** 编辑分类 */
function handleEdit(row: MallCategoryApi.Category) {
  formModalApi.setData(row).open();
}

/** 删除分类 */
async function handleDelete(row: MallCategoryApi.Category) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteCategory(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 切换树形展开/收缩状态 */
const isExpanded = ref(false);
function handleExpand() {
  isExpanded.value = !isExpanded.value;
  gridApi.grid.setAllTreeExpand(isExpanded.value);
}

/** 查看商品操作 */
const router = useRouter();
function handleViewSpu(id: number) {
  router.push({
    path: '/mall/product/spu',
    query: { categoryId: id },
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_, formValues) => {
          return await getCategoryList(formValues);
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: true,
      reserve: true,
    },
  } as VxeTableGridOptions<MallCategoryApi.Category>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【产品】产品管理、产品分类"
        url="https://doc.iocoder.cn/crm/product/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="商品分类列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['分类']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['product:category:create'],
              onClick: handleCreate,
            },
            {
              label: isExpanded ? '收缩' : '展开',
              type: 'primary',
              onClick: handleExpand,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '新增下级',
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['product:category:create'],
              onClick: handleAppend.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['product:category:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '查看商品',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['product:category:update'],
              ifShow: row.parentId !== undefined && row.parentId > 0,
              onClick: handleViewSpu.bind(null, row.id!),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['product:category:delete'],
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
