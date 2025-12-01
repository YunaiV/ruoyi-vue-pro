<script lang="ts" setup>
import type { MpAccountApi } from '#/api/mp/account';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { ElMessage, ElOption, ElSelect } from 'element-plus';

import { getSimpleAccountList } from '#/api/mp/account';

defineOptions({ name: 'WxAccountSelect' });

const emit = defineEmits<{
  (e: 'change', id: number, name: string): void;
}>();

const { push } = useRouter();

const account = ref<MpAccountApi.Account>({
  id: -1,
  name: '',
}); // 当前选中的公众号
const accountList = ref<MpAccountApi.Account[]>([]); // 公众号列表

/** 查询公众号列表 */
async function handleQuery() {
  accountList.value = await getSimpleAccountList();
  if (accountList.value.length === 0) {
    ElMessage.error(
      '未配置公众号，请在【公众号管理 -> 账号管理】菜单，进行配置',
    );
    await push({ name: 'MpAccount' });
    return;
  }

  // 默认选中第一个，如无数据则不执行
  const first = accountList.value[0];
  if (first) {
    account.value.id = first.id;
    account.value.name = first.name;
    emit('change', account.value.id, account.value.name);
  }
}

/** 切换选中公众号 */
function onChanged(id: number) {
  const found = accountList.value.find((v) => v.id === id);
  if (found) {
    account.value.name = found.name;
    emit('change', account.value.id, account.value.name);
  }
}

/** 初始化 */
onMounted(handleQuery);
</script>

<template>
  <ElSelect
    v-model="account.id"
    placeholder="请选择公众号"
    class="!w-full"
    @change="onChanged"
  >
    <ElOption
      v-for="item in accountList"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </ElSelect>
</template>
