// TODO @芋艿：是否有更好的组织形式？！
<script lang="ts" setup>
import type { SystemDeptApi } from '#/api/system/dept';

import { nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { handleTree } from '@vben/utils';

import { ElCard, ElCol, ElRow, ElTree } from 'element-plus';

import { getSimpleDeptList } from '#/api/system/dept';

defineOptions({ name: 'DeptSelectModal' });

const props = withDefaults(
  defineProps<{
    // 取消按钮文本
    cancelText?: string;
    // checkable 状态下节点选择完全受控
    checkStrictly?: boolean;
    // 确认按钮文本
    confirmText?: string;
    // 是否支持多选
    multiple?: boolean;
    // 标题
    title?: string;
  }>(),
  {
    cancelText: '取消',
    checkStrictly: false,
    confirmText: '确认',
    multiple: true,
    title: '部门选择',
  },
);

const emit = defineEmits<{
  confirm: [deptList: SystemDeptApi.Dept[]];
}>();

// 部门树形结构
const deptTree = ref<any[]>([]);
// 选中的部门 ID 列表
const selectedDeptIds = ref<number[]>([]);
// 部门数据
const deptData = ref<SystemDeptApi.Dept[]>([]);
// Tree 组件引用
const treeRef = ref();

// 对话框配置
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    // 获取选中的部门ID
    const selectedIds: number[] = props.checkStrictly
      ? treeRef.value?.getCheckedKeys() || []
      : selectedDeptIds.value;

    const deptArray = deptData.value.filter((dept) =>
      selectedIds.includes(dept.id!),
    );
    emit('confirm', deptArray);
    // 关闭并提示
    await modalApi.close();
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      deptTree.value = [];
      selectedDeptIds.value = [];
      return;
    }
    // 加载数据
    const data = modalApi.getData();
    if (!data) {
      return;
    }
    modalApi.lock();
    try {
      deptData.value = await getSimpleDeptList();
      deptTree.value = handleTree(deptData.value);
      // 等待 DOM 更新后再设置选中的节点
      await nextTick();
      // 设置已选择的部门
      if (data.selectedList?.length) {
        const selectedIds = data.selectedList
          .map((dept: SystemDeptApi.Dept) => dept.id)
          .filter((id: number) => id !== undefined);
        selectedDeptIds.value = selectedIds;
        treeRef.value.setCheckedKeys(selectedIds);
      }
    } finally {
      modalApi.unlock();
    }
  },
  destroyOnClose: true,
});

/** 处理选中状态变化 */
function handleCheck(
  _data: any,
  { checkedKeys }: { checkedKeys: (number | string)[] },
) {
  // 确保 checkedKeys 都是 number 类型
  const keys = checkedKeys.map((key) =>
    typeof key === 'string' ? Number(key) : key,
  );

  if (props.multiple) {
    selectedDeptIds.value = keys;
  } else {
    // 单选模式下，只保留最后选择的节点
    const lastSelectedId = keys[keys.length - 1];
    if (lastSelectedId) {
      selectedDeptIds.value = [lastSelectedId];
      treeRef.value?.setCheckedKeys([lastSelectedId]);
    }
  }
}
</script>
<template>
  <Modal :title="title" key="dept-select-modal" class="w-3/5">
    <ElRow class="h-full">
      <ElCol :span="24">
        <ElCard class="h-full">
          <ElTree
            v-if="deptTree.length > 0"
            ref="treeRef"
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            :check-strictly="checkStrictly"
            :default-expand-all="true"
            show-checkbox
            check-on-click-node
            node-key="id"
            @check="handleCheck"
          />
        </ElCard>
      </ElCol>
    </ElRow>
  </Modal>
</template>
