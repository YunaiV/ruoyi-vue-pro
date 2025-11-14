<script lang="ts" setup>
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { formatDate, isEmpty } from '@vben/utils';

import {
  ElAvatar,
  ElDescriptions,
  ElDescriptionsItem,
  ElDivider,
  ElInput,
  ElMessage,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  getBrokerageUser,
  updateBindUser,
} from '#/api/mall/trade/brokerage/user';
import { DictTag } from '#/components/dict-tag';

import { useUpdateFormSchema } from '../data';

/** 修改分销用户 */
defineOptions({ name: 'BrokerageUserUpdateForm' });

const emit = defineEmits(['success']);

const formData = ref<any>({
  id: undefined,
  bindUserId: undefined,
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useUpdateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await formApi.setValues(formData.value);
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    try {
      await updateBindUser(formData.value);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  onOpenChange: async (isOpen: boolean) => {
    if (!isOpen) {
      return;
    }
    const data = modalApi.getData<MallBrokerageUserApi.BrokerageUser>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = {
        id: data.id,
        bindUserId: data.bindUserId,
      };
      await formApi.setValues(formData.value);
      if (data.bindUserId) {
        await handleSearchUser(data.bindUserId, '上级分销员');
      }
    } finally {
      modalApi.unlock();
    }
  },
});

/** 绑定用户信息 */
const bindUser = ref<MallBrokerageUserApi.BrokerageUser | undefined>();

/** 查询分销员 */
async function handleSearchUser(id: number, userType: string) {
  if (isEmpty(id)) {
    ElMessage.warning(`请先输入${userType}编号后重试！！！`);
    return;
  }
  if (formData.value?.bindUserId === formData.value?.id) {
    ElMessage.error('不能绑定自己为推广人');
    return;
  }

  const userData = await getBrokerageUser(id);
  if (!userData) {
    ElMessage.warning(`${userType}不存在`);
    return;
  }
  bindUser.value = userData as MallBrokerageUserApi.BrokerageUser;
}
</script>

<template>
  <Modal title="修改上级推广人" class="w-1/3">
    <Form>
      <template #bindUserId>
        <div class="flex items-center gap-2">
          <ElInput
            v-model="formData.bindUserId"
            placeholder="请输入上级推广员编号"
            class="flex-1"
          >
            <template #append>
              <ElButton
                type="primary"
                @click="handleSearchUser(formData.bindUserId, '上级推广员')"
              >
                <IconifyIcon icon="lucide:search" :size="15" />
              </ElButton>
            </template>
          </ElInput>
        </div>
      </template>
    </Form>

    <!-- 上级推广员信息展示 -->
    <ElDivider v-if="bindUser" />
    <ElDescriptions v-if="bindUser" title="上级推广员信息" :column="1" border>
      <ElDescriptionsItem label="头像">
        <ElAvatar :src="bindUser?.avatar" />
      </ElDescriptionsItem>
      <ElDescriptionsItem label="昵称">
        {{ bindUser?.nickname }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="分销资格">
        <DictTag
          :type="DICT_TYPE.INFRA_BOOLEAN_STRING"
          :value="bindUser?.brokerageEnabled"
        />
      </ElDescriptionsItem>
      <ElDescriptionsItem label="成为分销员的时间">
        {{ formatDate(bindUser?.brokerageTime) }}
      </ElDescriptionsItem>
    </ElDescriptions>
  </Modal>
</template>
