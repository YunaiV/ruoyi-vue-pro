<script setup lang="ts">
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { useUserStore } from '@vben/stores';
import { formatDate } from '@vben/utils';

// @ts-ignore - 安装 vue3-print-nb 局部指令 v-print
import vPrint from 'vue3-print-nb';

import { getProcessInstancePrintData } from '#/api/bpm/processInstance';
import { decodeFields } from '#/components/form-create';

const userStore = useUserStore();

const printData = ref<BpmProcessInstanceApi.ProcessPrintDataRespVO>();
const userName = computed(() => userStore.userInfo?.nickname ?? '');
const printTime = ref(formatDate(new Date(), 'YYYY-MM-DD HH:mm'));
const formFields = ref<any[]>([]);
const printDataMap = ref<Record<string, any>>({});

/** 打印配置 */
const printObj = ref({
  id: 'printDivTag',
  popTitle: '&nbsp;',
  extraCss: '/print.css',
  extraHead: '',
  zIndex: 20_003,
});

const [Modal, modalApi] = useVbenModal({
  closable: true,
  footer: false,
  title: '打印流程',
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }

    modalApi.lock();
    try {
      const { processInstanceId } = modalApi.getData<{
        processInstanceId: string;
      }>();
      if (processInstanceId) {
        await fetchPrintData(processInstanceId);
      }
    } finally {
      modalApi.unlock();
    }
  },
});

/** 获取打印数据 */
async function fetchPrintData(id: string) {
  printData.value = await getProcessInstancePrintData(id);
  initPrintDataMap();
  parseFormFields();
}

/** 解析表单字段 */
function parseFormFields() {
  if (!printData.value) return;

  const formFieldsObj = decodeFields(
    printData.value.processInstance.processDefinition?.formFields || [],
  );
  const processVariables = printData.value.processInstance.formVariables;
  const res: any = [];

  for (const item of formFieldsObj) {
    const id = item.field;
    const name = item.title;
    const fieldKey = item.field as string;
    const variable = processVariables[fieldKey];
    let html = variable;

    switch (item.type) {
      case 'checkbox':
      case 'radio':
      case 'select': {
        const options = item.options;
        const temp: any = [];
        if (Array.isArray(options)) {
          if (Array.isArray(variable)) {
            const labels = options
              .filter((o: any) => variable.includes(o.value))
              .map((o: any) => o.label);
            temp.push(...labels);
          } else {
            const opt = options.find((o: any) => o.value === variable);
            if (opt) {
              temp.push(opt.label);
            }
          }
        }
        html = temp.join(',');
        break;
      }
      case 'UploadImg': {
        const imgEl = document.createElement('img');
        imgEl.setAttribute('src', variable);
        imgEl.setAttribute('style', 'max-width: 600px;');
        html = imgEl.outerHTML;
        break;
      }
      // TODO 更多表单打印展示
    }

    printDataMap.value[fieldKey] = html;
    res.push({ id, name, html });
  }

  formFields.value = res;
}

/** 初始化打印数据映射 */
function initPrintDataMap() {
  if (!printData.value) return;

  printDataMap.value.startUser =
    printData.value.processInstance.startUser?.nickname || '';
  printDataMap.value.startUserDept =
    printData.value.processInstance.startUser?.deptName || '';
  printDataMap.value.processName = printData.value.processInstance.name;
  printDataMap.value.processNum = printData.value.processInstance.id;
  printDataMap.value.startTime = formatDate(
    printData.value.processInstance.startTime,
  );
  printDataMap.value.endTime = formatDate(
    printData.value.processInstance.endTime,
  );
  printDataMap.value.processStatus = getDictLabel(
    DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
    printData.value.processInstance.status,
  );
  printDataMap.value.printUser = userName.value;
  printDataMap.value.printTime = printTime.value;
}

/** 获取打印模板 HTML  */
function getPrintTemplateHTML() {
  if (!printData.value?.printTemplateHtml) return '';

  const parser = new DOMParser();
  const doc = parser.parseFromString(
    printData.value.printTemplateHtml,
    'text/html',
  );

  // 替换 mentions
  const mentions = doc.querySelectorAll('[data-w-e-type="mention"]');
  mentions.forEach((item) => {
    const htmlElement = item as HTMLElement;
    const mentionId = JSON.parse(
      decodeURIComponent(htmlElement.dataset.info ?? ''),
    ).id;
    item.innerHTML = printDataMap.value[mentionId] ?? '';
  });

  // 替换流程记录
  const processRecords = doc.querySelectorAll(
    '[data-w-e-type="process-record"]',
  );
  const processRecordTable: Element = document.createElement('table');

  if (processRecords.length > 0) {
    // 构建流程记录 html
    processRecordTable.setAttribute('class', 'w-full border-collapse');

    const headTr = document.createElement('tr');
    const headTd = document.createElement('td');
    headTd.setAttribute('colspan', '2');
    headTd.setAttribute('class', 'border border-black p-1.5 text-center');
    headTd.innerHTML = '流程记录';
    headTr.append(headTd);
    processRecordTable.append(headTr);

    printData.value?.tasks.forEach((item) => {
      const tr = document.createElement('tr');
      const td1 = document.createElement('td');
      td1.setAttribute('class', 'border border-black p-1.5');
      td1.innerHTML = item.name;
      const td2 = document.createElement('td');
      td2.setAttribute('class', 'border border-black p-1.5');
      td2.innerHTML = item.description;
      tr.append(td1);
      tr.append(td2);
      processRecordTable.append(tr);
    });
  }

  processRecords.forEach((item) => {
    item.innerHTML = processRecordTable.outerHTML;
  });

  // 返回 html
  return doc.body.innerHTML;
}
</script>

<template>
  <Modal class="w-2/3">
    <div id="printDivTag" class="break-all">
      <!-- eslint-disable vue/no-v-html  使用自定义打印模板 -->
      <div
        v-if="printData?.printTemplateEnable"
        v-html="getPrintTemplateHTML()"
      ></div>
      <div v-else-if="printData">
        <h2 class="mb-3 text-center text-xl font-bold">
          {{ printData.processInstance.name }}
        </h2>
        <div class="mb-2 flex justify-between text-sm">
          <div>
            {{ `流程编号: ${printData.processInstance.id}` }}
          </div>
          <div>
            {{ `打印人员: ${userName}` }}
          </div>
        </div>
        <table class="mt-3 w-full border-collapse">
          <tbody>
            <tr>
              <td class="w-1/4 border border-black p-1.5">发起人</td>
              <td class="w-1/4 border border-black p-1.5">
                {{ printData.processInstance.startUser?.nickname }}
              </td>
              <td class="w-1/4 border border-black p-1.5">发起时间</td>
              <td class="w-1/4 border border-black p-1.5">
                {{ formatDate(printData.processInstance.startTime) }}
              </td>
            </tr>
            <tr>
              <td class="w-1/4 border border-black p-1.5">所属部门</td>
              <td class="w-1/4 border border-black p-1.5">
                {{ printData.processInstance.startUser?.deptName }}
              </td>
              <td class="w-1/4 border border-black p-1.5">流程状态</td>
              <td class="w-1/4 border border-black p-1.5">
                {{
                  getDictLabel(
                    DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
                    printData.processInstance.status,
                  )
                }}
              </td>
            </tr>
            <tr>
              <td
                class="w-full border border-black p-1.5 text-center"
                colspan="4"
              >
                <h4>表单内容</h4>
              </td>
            </tr>
            <tr v-for="item in formFields" :key="item.id">
              <td class="w-1/5 border border-black p-1.5">
                {{ item.name }}
              </td>
              <td class="w-4/5 border border-black p-1.5" colspan="3">
                <div v-html="item.html"></div>
              </td>
            </tr>
            <tr>
              <td
                class="w-full border border-black p-1.5 text-center"
                colspan="4"
              >
                <h4>流程记录</h4>
              </td>
            </tr>
            <tr v-for="item in printData.tasks" :key="item.id">
              <td class="w-1/5 border border-black p-1.5">
                {{ item.name }}
              </td>
              <td class="w-4/5 border border-black p-1.5" colspan="3">
                {{ item.description }}
                <div v-if="item.signPicUrl && item.signPicUrl.length > 0">
                  <img class="h-10 w-[90px]" :src="item.signPicUrl" alt="" />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <template #footer>
      <div class="flex justify-end gap-2">
        <ElButton @click="modalApi.close()">取 消</ElButton>
        <ElButton v-print="printObj" type="primary">打 印</ElButton>
      </div>
    </template>
  </Modal>
</template>
