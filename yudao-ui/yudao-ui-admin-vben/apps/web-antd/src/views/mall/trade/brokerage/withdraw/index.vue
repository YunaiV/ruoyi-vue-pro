<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageWithdrawApi } from '#/api/mall/trade/brokerage/withdraw';

import { h } from 'vue';

import { confirm, Page, prompt } from '@vben/common-ui';
import {
  BrokerageWithdrawStatusEnum,
  BrokerageWithdrawTypeEnum,
  DICT_TYPE,
} from '@vben/constants';
import { formatDateTime } from '@vben/utils';

import { Input, message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  approveBrokerageWithdraw,
  getBrokerageWithdrawPage,
  rejectBrokerageWithdraw,
} from '#/api/mall/trade/brokerage/withdraw';
import { DictTag } from '#/components/dict-tag';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 分销佣金提现 */
defineOptions({ name: 'BrokerageWithdraw' });

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 审核通过 */
async function handleApprove(row: MallBrokerageWithdrawApi.BrokerageWithdraw) {
  await confirm('确定要审核通过吗？');
  const hideLoading = message.loading({
    content: '审核通过中 ...',
    duration: 0,
  });
  try {
    await approveBrokerageWithdraw(row.id);
    message.success($t('ui.actionMessage.operationSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 审核驳回 */
function handleReject(row: MallBrokerageWithdrawApi.BrokerageWithdraw) {
  prompt({
    component: () => {
      return h(Input, {
        placeholder: '请输入驳回原因',
        allowClear: true,
        rules: [{ required: true, message: '请输入驳回原因' }],
      });
    },
    content: '请输入驳回原因',
    title: '驳回',
    modelPropName: 'value',
  }).then(async (val) => {
    if (val) {
      await rejectBrokerageWithdraw({
        id: row.id!,
        auditReason: val,
      });
      handleRefresh();
    }
  });
}

/** 重新转账 */
async function handleRetryTransfer(
  row: MallBrokerageWithdrawApi.BrokerageWithdraw,
) {
  await confirm('确定要重新转账吗？');
  const hideLoading = message.loading({
    content: '审核通过中 ...',
    duration: 0,
  });
  try {
    await approveBrokerageWithdraw(row.id);
    message.success($t('ui.actionMessage.operationSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
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
    cellConfig: {
      height: 90,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getBrokerageWithdrawPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
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
  } as VxeTableGridOptions<MallBrokerageWithdrawApi.BrokerageWithdraw>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="佣金提现列表">
      <template #withdraw-info="{ row }">
        <div
          v-if="row.type === BrokerageWithdrawTypeEnum.WALLET.type"
          class="text-left"
        >
          -
        </div>
        <div v-else class="text-left">
          <div v-if="row.userAccount" class="text-left">
            账号：{{ row.userAccount }}
          </div>
          <div v-if="row.userName" class="text-left">
            真实姓名：{{ row.userName }}
          </div>
          <template v-if="row.type === BrokerageWithdrawTypeEnum.BANK.type">
            <div v-if="row.bankName" class="text-left">
              银行名称：{{ row.bankName }}
            </div>
            <div v-if="row.bankAddress" class="text-left">
              开户地址：{{ row.bankAddress }}
            </div>
          </template>
          <div v-if="row.qrCodeUrl" class="mt-2 text-left">
            <div class="flex items-start gap-2">
              <span class="flex-shrink-0">收款码：</span>
              <img :src="row.qrCodeUrl" class="h-10 w-10 flex-shrink-0" />
            </div>
          </div>
        </div>
      </template>
      <template #status-info="{ row }">
        <div class="text-left">
          <DictTag
            :value="row.status"
            :type="DICT_TYPE.BROKERAGE_WITHDRAW_STATUS"
          />
          <div
            v-if="row.auditTime"
            class="mt-1 text-left text-xs text-gray-500"
          >
            时间：{{ formatDateTime(row.auditTime) }}
          </div>
          <div
            v-if="row.auditReason"
            class="mt-1 text-left text-xs text-gray-500"
          >
            审核原因：{{ row.auditReason }}
          </div>
          <div
            v-if="row.transferErrorMsg"
            class="mt-1 text-left text-xs text-red-500"
          >
            转账失败原因：{{ row.transferErrorMsg }}
          </div>
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '通过',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['trade:brokerage-withdraw:audit'],
              ifShow:
                row.status === BrokerageWithdrawStatusEnum.AUDITING.status &&
                !row.payTransferId,
              onClick: () => handleApprove(row),
            },
            {
              label: '驳回',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['trade:brokerage-withdraw:audit'],
              ifShow:
                row.status === BrokerageWithdrawStatusEnum.AUDITING.status &&
                !row.payTransferId,
              onClick: () => handleReject(row),
            },
            {
              label: '重新转账',
              type: 'link',
              icon: ACTION_ICON.REFRESH,
              auth: ['trade:brokerage-withdraw:audit'],
              ifShow:
                row.status === BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.status,
              onClick: () => handleRetryTransfer(row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
