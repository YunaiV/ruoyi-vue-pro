<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmModelApi, ModelCategoryInfo } from '#/api/bpm/model';

import { computed, ref, watchEffect } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { confirm, EllipsisText, useVbenModal } from '@vben/common-ui';
import { BpmModelFormType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { useUserStore } from '@vben/stores';
import { cloneDeep, formatDateTime, isEqual } from '@vben/utils';

import { useDebounceFn } from '@vueuse/core';
import { useSortable } from '@vueuse/integrations/useSortable';
import {
  ElButton,
  ElCard,
  ElCollapse,
  ElCollapseItem,
  ElDropdown,
  ElLoading,
  ElMessage,
  ElTag,
  ElTooltip,
} from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteCategory } from '#/api/bpm/category';
import {
  cleanModel,
  deleteModel,
  deployModel,
  updateModelSortBatch,
  updateModelState,
} from '#/api/bpm/model';
import { $t } from '#/locales';

import CategoryRenameForm from '../../category/modules/rename-form.vue';
import FormCreateDetail from '../../form/modules/detail.vue';
import { useGridColumns } from '../data';

const props = defineProps<{
  categoryInfo: ModelCategoryInfo;
  isCategorySorting: boolean;
  isFirst?: boolean; // 是否为第一个分类
}>();

const emit = defineEmits(['success']);

const [CategoryRenameModal, categoryRenameModalApi] = useVbenModal({
  connectedComponent: CategoryRenameForm,
  destroyOnClose: true,
});

const [FormCreateDetailModal, formCreateDetailModalApi] = useVbenModal({
  connectedComponent: FormCreateDetail,
  destroyOnClose: true,
});

const router = useRouter();
const userId = useUserStore().userInfo?.id;

const isModelSorting = ref(false);
const originalData = ref<BpmModelApi.Model[]>([]);
const modelList = ref<BpmModelApi.Model[]>([]);
// TODO @jason：可以全部展开么？ @芋艿 上次讨论。好像是因为性能问题才只展开第一个分类
const isExpand = ref(props.isFirst); // 根据是否为第一个分类, 来设置初始展开状态

const sortableInstance = ref<any>(null); // 排序引用，以便后续启用或禁用排序
/** 解决 v-model 问题，使用计算属性 */
const expandKeys = computed(() => (isExpand.value ? ['1'] : []));

const { hasAccessByCodes } = useAccess();
/** 权限校验：通过 computed 解决列表的卡顿问题 */
const hasPermiUpdate = computed(() => {
  return hasAccessByCodes(['bpm:model:update']);
});
const hasPermiDelete = computed(() => {
  return hasAccessByCodes(['bpm:model:delete']);
});
const hasPermiDeploy = computed(() => {
  return hasAccessByCodes(['bpm:model:deploy']);
});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    data: modelList.value,
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions,
});

/** 处理模型的排序 */
function handleModelSort() {
  // 保存初始数据并确保数据完整
  if (props.categoryInfo.modelList && props.categoryInfo.modelList.length > 0) {
    originalData.value = cloneDeep(props.categoryInfo.modelList);
    modelList.value = cloneDeep(props.categoryInfo.modelList);
  }

  // 更新表格数据
  gridApi.setGridOptions({
    data: modelList.value,
  });

  // 展开数据
  isExpand.value = true;
  isModelSorting.value = true;
  // 如果排序实例不存在，则初始化
  if (sortableInstance.value) {
    // 已存在实例，则启用排序功能
    sortableInstance.value.option('disabled', false);
  } else {
    // 确保使用最新的数据
    modelList.value = cloneDeep(props.categoryInfo.modelList);
    // 更新表格数据
    gridApi.setGridOptions({
      data: modelList.value,
    });

    sortableInstance.value = useSortable(
      `.category-${props.categoryInfo.id} .vxe-table .vxe-table--body-wrapper:not(.fixed-right--wrapper) .vxe-table--body tbody`,
      modelList.value,
      {
        draggable: '.vxe-body--row',
        animation: 150,
        handle: '.drag-handle',
        disabled: false,
        onEnd: ({ newDraggableIndex, oldDraggableIndex }) => {
          if (oldDraggableIndex !== newDraggableIndex) {
            modelList.value.splice(
              newDraggableIndex ?? 0,
              0,
              modelList.value.splice(oldDraggableIndex ?? 0, 1)[0]!,
            );
          }
        },
      },
    );
  }
}

/** 处理模型的排序提交 */
async function handleModelSortSubmit() {
  // 确保数据已经正确同步
  if (!modelList.value || modelList.value.length === 0) {
    ElMessage.error('排序数据异常，请重试');
    return;
  }

  const loadingInstance = ElLoading.service({
    text: '正在保存排序...',
  });
  try {
    // 保存排序
    const ids = modelList.value.map((item) => item.id);
    await updateModelSortBatch(ids);
    // 刷新列表
    isModelSorting.value = false;
    ElMessage.success('排序模型成功');
    emit('success');
  } catch (error) {
    console.error('排序保存失败', error);
  } finally {
    loadingInstance.close();
  }
}

/** 处理模型的排序取消 */
function handleModelSortCancel() {
  // 恢复初始数据
  if (originalData.value && originalData.value.length > 0) {
    modelList.value = cloneDeep(originalData.value);
    // 更新表格数据
    gridApi.setGridOptions({
      data: modelList.value,
    });
  }
  // 禁用排序功能
  if (sortableInstance.value) {
    sortableInstance.value.option('disabled', true);
  }
  isModelSorting.value = false;
}

/** 处理下拉菜单命令 */
function handleCommand(command: string) {
  if (command === 'renameCategory') {
    // 打开重命名分类对话框
    categoryRenameModalApi.setData(props.categoryInfo).open();
  } else if (command === 'deleteCategory') {
    handleDeleteCategory();
  }
}

/** 删除流程分类 */
async function handleDeleteCategory() {
  if (props.categoryInfo.modelList.length > 0) {
    ElMessage.warning('该分类下仍有流程定义,不允许删除');
    return;
  }

  await confirm({
    beforeClose: async ({ isConfirm }) => {
      if (!isConfirm) return;
      // 发起删除
      const loadingInstance = ElLoading.service({
        text: `正在删除分类: "${props.categoryInfo.name}"...`,
      });
      try {
        await deleteCategory(props.categoryInfo.id);
      } finally {
        loadingInstance.close();
      }
      return true;
    },
    content: `确定要删除[${props.categoryInfo.name}]吗？`,
    icon: 'question',
  });
  ElMessage.success(
    $t('ui.actionMessage.deleteSuccess', [props.categoryInfo.name]),
  );
  // 刷新列表
  emit('success');
}

/** 处理表单详情点击 */
async function handleFormDetail(row: any) {
  if (row.formType === BpmModelFormType.NORMAL) {
    const data = {
      id: row.formId,
    };
    formCreateDetailModalApi.setData(data).open();
  } else {
    await router.push({
      path: row.formCustomCreatePath,
    });
  }
}

/** 判断是否是流程管理员 */
function isManagerUser(row: any) {
  return row.managerUserIds && row.managerUserIds.includes(userId);
}

/** 模型操作 */
async function modelOperation(type: string, id: number) {
  await router.push({
    name: 'BpmModelUpdate',
    params: { id, type },
  });
}

/** 发布流程 */
async function handleDeploy(row: any) {
  await confirm({
    beforeClose: async ({ isConfirm }) => {
      if (!isConfirm) return;
      // 发起部署
      const loadingInstance = ElLoading.service({
        text: `正在发布流程: "${row.name}"...`,
      });
      try {
        await deployModel(row.id);
      } finally {
        loadingInstance.close();
      }
      return true;
    },
    content: `确认要发布[${row.name}]流程吗？`,
    icon: 'question',
  });
  ElMessage.success(`发布[${row.name}]流程成功`);
  // 刷新列表
  emit('success');
}

/** '更多'操作按钮 */
function handleModelCommand(command: string, row: any) {
  switch (command) {
    case 'handleChangeState': {
      handleChangeState(row);
      break;
    }
    case 'handleClean': {
      handleClean(row);
      break;
    }
    case 'handleCopy': {
      modelOperation('copy', row.id);
      break;
    }
    case 'handleDefinitionList': {
      handleDefinitionList(row);
      break;
    }
    case 'handleDelete': {
      handleDelete(row);
      break;
    }
    case 'handleReport': {
      handleReport(row);
      break;
    }
    default: {
      break;
    }
  }
}

/** 更新状态操作 */
async function handleChangeState(row: any) {
  const state = row.processDefinition.suspensionState;
  const newState = state === 1 ? 2 : 1;
  const statusState = state === 1 ? '停用' : '启用';
  await confirm({
    beforeClose: async ({ isConfirm }) => {
      if (!isConfirm) return;
      // 发起更新状态
      const loadingInstance = ElLoading.service({
        text: `正在${statusState}流程: "${row.name}"...`,
      });
      try {
        await updateModelState(row.id, newState);
      } finally {
        loadingInstance.close();
      }
      return true;
    },
    content: `确认要${statusState}流程: "${row.name}" 吗？`,
    icon: 'question',
  });
  ElMessage.success(`${statusState} 流程: "${row.name}" 成功`);
  // 刷新列表
  emit('success');
}

/** 清理流程操作 */
async function handleClean(row: any) {
  await confirm({
    beforeClose: async ({ isConfirm }) => {
      if (!isConfirm) return;
      // 发起清理操作
      const loadingInstance = ElLoading.service({
        text: `正在清理流程: "${row.name}"...`,
      });
      try {
        await cleanModel(row.id);
      } finally {
        loadingInstance.close();
      }
      return true;
    },
    content: `确认要清理流程: "${row.name}" 吗？`,
    icon: 'question',
  });
  ElMessage.success(`清理流程: "${row.name}" 成功`);
  // 刷新列表
  emit('success');
}

/** 删除流程操作 */
async function handleDelete(row: any) {
  await confirm({
    beforeClose: async ({ isConfirm }) => {
      if (!isConfirm) return;
      // 发起删除操作
      const loadingInstance = ElLoading.service({
        text: $t('ui.actionMessage.deleting', [row.name]),
      });
      try {
        await deleteModel(row.id);
      } finally {
        loadingInstance.close();
      }
      return true;
    },
    content: `确认要删除流程: "${row.name}" 吗？`,
    icon: 'question',
  });

  ElMessage.success(`删除流程: "${row.name}" 成功`);
  // 刷新列表
  emit('success');
}

/** 跳转到指定流程定义列表 */
function handleDefinitionList(row: any) {
  router.push({
    name: 'BpmProcessDefinition',
    query: {
      key: row.key,
    },
  });
}

/** 跳转到流程报表页面 */
function handleReport(row: any) {
  router.push({
    name: 'BpmProcessInstanceReport',
    query: {
      processDefinitionId: row.processDefinition.id,
      processDefinitionKey: row.key,
    },
  });
}

/** 更新 modelList 模型列表 */
const updateModelList = useDebounceFn(() => {
  const newModelList = props.categoryInfo.modelList;
  if (!isEqual(modelList.value, newModelList)) {
    modelList.value = cloneDeep(newModelList);
    // 不再自动设置展开状态，除非是第一个分类
    // 关闭排序
    isModelSorting.value = false;
    // 重置排序实例
    sortableInstance.value = null;
    // 更新表格数据
    gridApi.setGridOptions({
      data: modelList.value,
    });
  }
}, 100);

/** 监听分类信息和排序状态变化 */
watchEffect(() => {
  if (props.categoryInfo?.modelList) {
    updateModelList();
  }

  if (props.isCategorySorting) {
    isExpand.value = false;
  }
});

/** 处理重命名成功 */
function handleRenameSuccess() {
  emit('success');
}
</script>

<template>
  <div>
    <ElCard
      body-style="padding: 0"
      shadow="hover"
      class="category-draggable-model mb-5 rounded-lg transition-all duration-300 ease-in-out"
    >
      <div class="flex h-12 items-center">
        <!-- 头部：分类名 -->
        <!-- TODO @jason：2）拖动后，直接请求排序，不用有个【保存】；排序模型分类，和排序分类里的模型，交互有点不同哈。@芋艿 好像 yudao-ui-admin-vue3 交互也是这样的，需要改吗? -->
        <div class="flex items-center">
          <ElTooltip v-if="isCategorySorting" content="拖动排序">
            <!-- drag-handle 标识可以拖动，不能删掉 -->
            <IconifyIcon
              icon="ic:round-drag-indicator"
              class="drag-handle ml-2.5 cursor-move text-2xl text-gray-500"
            />
          </ElTooltip>
          <div class="ml-4 mr-2 text-lg font-medium">
            {{ categoryInfo.name }}
          </div>
          <div class="text-gray-500">
            ({{ categoryInfo.modelList?.length || 0 }})
          </div>
        </div>

        <!-- 头部：操作 -->
        <div class="flex flex-1 items-center" v-show="!isCategorySorting">
          <div
            v-if="categoryInfo.modelList.length > 0"
            class="ml-3 flex cursor-pointer items-center transition-transform duration-300"
            :class="isExpand ? 'rotate-180' : 'rotate-0'"
            @click="isExpand = !isExpand"
          >
            <IconifyIcon
              icon="lucide:chevron-down"
              class="text-3xl text-gray-400"
            />
          </div>

          <div
            class="ml-auto flex items-center"
            :class="isModelSorting ? 'mr-4' : 'mr-8'"
          >
            <template v-if="!isModelSorting">
              <ElButton
                v-if="categoryInfo.modelList.length > 0"
                link
                size="small"
                class="flex items-center text-sm"
                @click.stop="handleModelSort"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:align-start-vertical" />
                </template>
                排序
              </ElButton>
              <ElDropdown placement="bottom" trigger="click">
                <ElButton link size="small" class="flex items-center text-sm">
                  <template #icon>
                    <IconifyIcon icon="lucide:settings" />
                  </template>
                  分类
                </ElButton>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleCommand('renameCategory')">
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleCommand('deleteCategory')">
                      删除分类
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </ElDropdown>
            </template>

            <template v-else>
              <ElButton @click.stop="handleModelSortCancel" class="mr-2">
                取 消
              </ElButton>
              <ElButton type="primary" @click.stop="handleModelSortSubmit">
                保存排序
              </ElButton>
            </template>
          </div>
        </div>
      </div>

      <!-- 模型列表 -->
      <ElCollapse
        v-model="expandKeys"
        class="collapse-no-padding bg-transparent"
      >
        <ElCollapseItem
          name="1"
          class="border-0 bg-transparent p-0"
          v-show="isExpand"
        >
          <template #title>
            <span></span>
          </template>
          <Grid
            v-if="modelList && modelList.length > 0"
            :class="`category-${categoryInfo.id}`"
          >
            <template #name="{ row }">
              <div class="flex items-center overflow-hidden">
                <ElTooltip
                  v-if="isModelSorting"
                  content="拖动排序"
                  placement="left"
                >
                  <!-- drag-handle 标识用于推动排序。 useSortable 用到 -->
                  <IconifyIcon
                    icon="ic:round-drag-indicator"
                    class="drag-handle mr-2.5 flex-shrink-0 cursor-move text-2xl text-gray-500"
                  />
                </ElTooltip>
                <div
                  v-if="!row.icon"
                  class="mr-2.5 flex h-9 w-9 flex-shrink-0 items-center justify-center rounded bg-blue-500 text-white"
                >
                  <span class="text-xs">
                    {{ row.name.substring(0, 2) }}
                  </span>
                </div>
                <img
                  v-else
                  :src="row.icon"
                  class="mr-2.5 h-9 w-9 flex-shrink-0 rounded"
                  alt="图标"
                />
                <div class="min-w-0 overflow-hidden">
                  <EllipsisText :tooltip-when-ellipsis="true">
                    {{ row.name }}
                  </EllipsisText>
                </div>
              </div>
            </template>
            <template #startUserIds="{ row }">
              <span v-if="!row.startUsers?.length && !row.startDepts?.length">
                全部可见
              </span>
              <span v-else-if="row.startUsers?.length === 1">
                {{ row.startUsers[0].nickname }}
              </span>
              <span v-else-if="row.startDepts?.length === 1">
                {{ row.startDepts[0].name }}
              </span>
              <span v-else-if="row.startDepts?.length > 1">
                <ElTooltip
                  placement="top"
                  :content="
                    row.startDepts.map((dept: any) => dept.name).join('、')
                  "
                >
                  {{ row.startDepts[0].name }}等
                  {{ row.startDepts.length }} 个部门可见
                </ElTooltip>
              </span>
              <span v-else-if="row.startUsers?.length > 1">
                <ElTooltip
                  placement="top"
                  :content="
                    row.startUsers.map((user: any) => user.nickname).join('、')
                  "
                >
                  {{ row.startUsers[0].nickname }}等
                  {{ row.startUsers.length }} 人可见
                </ElTooltip>
              </span>
            </template>
            <template #formInfo="{ row }">
              <ElButton
                v-if="row.formType === BpmModelFormType.NORMAL"
                link
                @click="handleFormDetail(row)"
              >
                {{ row.formName }}
              </ElButton>
              <ElButton
                v-else-if="row.formType === BpmModelFormType.CUSTOM"
                link
                @click="handleFormDetail(row)"
              >
                {{ row.formCustomCreatePath }}
              </ElButton>
              <span v-else>暂无表单</span>
            </template>
            <template #deploymentTime="{ row }">
              <div class="flex items-center justify-center">
                <span v-if="row.processDefinition" class="w-36">
                  {{ formatDateTime(row.processDefinition.deploymentTime) }}
                </span>
                <ElTag v-if="row.processDefinition">
                  v{{ row.processDefinition.version }}
                </ElTag>
                <ElTag v-else type="warning">未部署</ElTag>
                <ElTag
                  v-if="row.processDefinition?.suspensionState === 2"
                  type="warning"
                  class="ml-2.5"
                >
                  已停用
                </ElTag>
              </div>
            </template>
            <template #actions="{ row }">
              <div class="flex items-center justify-center space-x-2">
                <ElButton
                  link
                  size="small"
                  @click="modelOperation('update', row.id)"
                  :disabled="!isManagerUser(row) && !hasPermiUpdate"
                >
                  修改
                </ElButton>
                <ElButton
                  link
                  size="small"
                  @click="handleDeploy(row)"
                  :disabled="!isManagerUser(row) && !hasPermiDeploy"
                >
                  发布
                </ElButton>
                <ElDropdown placement="bottom-end" trigger="click">
                  <ElButton link size="small">更多</ElButton>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        @click="handleModelCommand('handleCopy', row)"
                      >
                        复制
                      </el-dropdown-item>
                      <el-dropdown-item
                        @click="handleModelCommand('handleDefinitionList', row)"
                      >
                        历史
                      </el-dropdown-item>

                      <el-dropdown-item
                        @click="handleModelCommand('handleReport', row)"
                        :disabled="!isManagerUser(row)"
                      >
                        报表
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="row.processDefinition"
                        @click="handleModelCommand('handleChangeState', row)"
                        :disabled="!isManagerUser(row)"
                      >
                        {{
                          row.processDefinition.suspensionState === 1
                            ? '停用'
                            : '启用'
                        }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        @click="handleModelCommand('handleClean', row)"
                        :disabled="!isManagerUser(row)"
                      >
                        清理
                      </el-dropdown-item>
                      <el-dropdown-item
                        @click="handleModelCommand('handleDelete', row)"
                        :disabled="!isManagerUser(row) && !hasPermiDelete"
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </ElDropdown>
              </div>
            </template>
          </Grid>
        </ElCollapseItem>
      </ElCollapse>
    </ElCard>

    <!-- 重命名分类弹窗 -->
    <CategoryRenameModal @success="handleRenameSuccess" />
    <!-- 流程表单详情对话框 -->
    <FormCreateDetailModal />
  </div>
</template>

<style scoped>
/* :deep() 实现样式穿透 */
.collapse-no-padding :deep(.el-collapse-item__header) {
  min-height: 0;
}

.collapse-no-padding :deep(.el-collapse-item__arrow) {
  display: none;
}
</style>
