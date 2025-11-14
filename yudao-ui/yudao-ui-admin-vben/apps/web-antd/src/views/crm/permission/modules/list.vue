<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmPermissionApi } from '#/api/crm/permission';

import { ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deletePermissionBatch,
  deleteSelfPermission,
  getPermissionList,
  PermissionLevelEnum,
} from '#/api/crm/permission';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import Form from './form.vue';

const props = defineProps<{
  bizId: number; // 模块数据编号
  bizType: number; // 模块类型
  showAction: boolean; // 是否展示操作按钮
}>();

const emits = defineEmits<{
  (e: 'quitTeam'): void;
}>();

const gridData = ref<CrmPermissionApi.Permission[]>([]);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const userStore = useUserStore();

const validateOwnerUser = ref(false); // 负责人权限
const validateWrite = ref(false); // 编辑权限
const isPool = ref(false); // 是否是公海

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

const checkedRows = ref<CrmPermissionApi.Permission[]>([]);
function setCheckedRows({
  records,
}: {
  records: CrmPermissionApi.Permission[];
}) {
  if (records.some((item) => item.level === PermissionLevelEnum.OWNER)) {
    message.warning('不能选择负责人！');
    gridApi.grid.setAllCheckboxRow(false);
    return;
  }
  checkedRows.value = records;
}

/** 新建团队成员 */
function handleCreate() {
  formModalApi
    .setData({
      bizType: props.bizType,
      bizId: props.bizId,
    })
    .open();
}

/** 编辑团队成员 */
function handleEdit() {
  if (checkedRows.value.length === 0) {
    message.error('请先选择团队成员后操作！');
    return;
  }
  if (checkedRows.value.length > 1) {
    message.error('只能选择一个团队成员进行编辑！');
    return;
  }
  formModalApi
    .setData({
      bizType: props.bizType,
      bizId: props.bizId,
      id: checkedRows.value[0]?.id,
      level: checkedRows.value[0]?.level,
    })
    .open();
}

/** 删除团队成员 */
function handleDelete() {
  if (checkedRows.value.length === 0) {
    message.error('请先选择团队成员后操作！');
    return;
  }
  return new Promise((resolve, reject) => {
    confirm({
      content: `你要将${checkedRows.value.map((item) => item.nickname).join(',')}移出团队吗？`,
    })
      .then(async () => {
        const res = await deletePermissionBatch(
          checkedRows.value.map((item) => item.id!),
        );
        if (res) {
          // 提示并返回成功
          message.success($t('ui.actionMessage.operationSuccess'));
          handleRefresh();
          resolve(true);
        } else {
          reject(new Error('移出失败'));
        }
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 退出团队 */
async function handleQuit() {
  const permission = gridApi.grid
    .getData()
    .find(
      (item) =>
        item.id === userStore.userInfo?.id &&
        item.level === PermissionLevelEnum.OWNER,
    );
  if (permission) {
    message.warning('负责人不能退出团队！');
    return;
  }

  const userPermission = gridApi.grid
    .getData()
    .find((item) => item.id === userStore.userInfo?.id);
  if (!userPermission) {
    message.warning('你不是团队成员！');
    return;
  }
  await deleteSelfPermission(userPermission.id!);
  message.success('退出团队成员成功！');
  emits('quitTeam');
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params) => {
          const res = await getPermissionList({
            bizId: props.bizId,
            bizType: props.bizType,
          });
          gridData.value = res;
          return res;
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
  } as VxeTableGridOptions<CrmPermissionApi.Permission>,
  gridEvents: {
    checkboxAll: setCheckedRows,
    checkboxChange: setCheckedRows,
  },
});

defineExpose({
  openForm: handleCreate,
  validateOwnerUser,
  validateWrite,
  isPool,
});

watch(
  () => gridData.value,
  (data) => {
    isPool.value = false;
    if (data.length > 0) {
      isPool.value = data.some(
        (item) => item.level === PermissionLevelEnum.OWNER,
      );
      validateOwnerUser.value = false;
      validateWrite.value = false;
      const userId = userStore.userInfo?.id;
      gridData.value
        .filter((item) => item.userId === userId)
        .forEach((item) => {
          if (item.level === PermissionLevelEnum.OWNER) {
            validateOwnerUser.value = true;
            validateWrite.value = true;
          } else if (item.level === PermissionLevelEnum.WRITE) {
            validateWrite.value = true;
          }
        });
    } else {
      // 特殊：没有成员的情况下，说明没有负责人，是公海
      isPool.value = true;
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <div>
    <FormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('common.create'),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              ifShow: validateOwnerUser,
              onClick: handleCreate,
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              icon: ACTION_ICON.EDIT,
              ifShow: validateOwnerUser,
              onClick: handleEdit,
              disabled: checkedRows.length === 0,
            },
            {
              label: $t('common.delete'),
              type: 'primary',
              danger: true,
              icon: ACTION_ICON.DELETE,
              ifShow: validateOwnerUser,
              onClick: handleDelete,
              disabled: checkedRows.length === 0,
            },
            {
              label: '退出团队',
              type: 'primary',
              danger: true,
              ifShow: !validateOwnerUser,
              onClick: handleQuit,
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
