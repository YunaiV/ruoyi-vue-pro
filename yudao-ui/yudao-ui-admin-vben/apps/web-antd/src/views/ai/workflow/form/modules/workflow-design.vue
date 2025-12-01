<script lang="ts" setup>
import type { Ref } from 'vue';

import { inject, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { Tinyflow } from '@vben/plugins/tinyflow';
import { isNumber } from '@vben/utils';

import { Button, Input, Select } from 'ant-design-vue';

import { testWorkflow } from '#/api/ai/workflow';

defineProps<{
  provider: any;
}>();

const tinyflowRef = ref<InstanceType<typeof Tinyflow> | null>(null);
const workflowData = inject('workflowData') as Ref;
const params4Test = ref<any[]>([]);
const paramsOfStartNode = ref<any>({});
const testResult = ref(null);
const loading = ref(false);
const error = ref(null);

const [Drawer, drawerApi] = useVbenDrawer({
  footer: false,
  closeOnClickModal: false,
  modal: false,
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    try {
      // 查找 start 节点
      const startNode = getStartNode();
      // 获取参数定义
      const parameters: any[] = (startNode.data?.parameters as any[]) || [];
      const paramDefinitions: Record<string, any> = {};
      // 加入参数选项方便用户添加非必须参数
      parameters.forEach((param: any) => {
        paramDefinitions[param.name] = param;
      });
      // 自动装载需必填的参数
      function mergeIfRequiredButNotSet(target: any[]) {
        const needPushList = [];
        for (const key in paramDefinitions) {
          const param = paramDefinitions[key];

          if (param.required) {
            const item = target.find((item: any) => item.key === key);

            if (!item) {
              needPushList.push({
                key: param.name,
                value: param.defaultValue || '',
              });
            }
          }
        }
        target.push(...needPushList);
      }
      mergeIfRequiredButNotSet(params4Test.value);

      // 设置参数
      paramsOfStartNode.value = paramDefinitions;
    } catch (error) {
      console.error('加载参数失败:', error);
    }
  },
});

/** 展示工作流测试抽屉 */
function testWorkflowModel() {
  drawerApi.open();
}

/** 运行流程 */
async function goRun() {
  try {
    const val = tinyflowRef.value?.getData();
    loading.value = true;
    error.value = null;
    testResult.value = null;

    // 查找start节点
    const startNode = getStartNode();
    // 获取参数定义
    const parameters: any[] = (startNode.data?.parameters as any[]) || [];
    const paramDefinitions: Record<string, any> = {};
    parameters.forEach((param: any) => {
      paramDefinitions[param.name] = param.dataType;
    });
    // 参数类型转换
    const convertedParams: Record<string, any> = {};
    for (const { key, value } of params4Test.value) {
      const paramKey = key.trim();
      if (!paramKey) {
        continue;
      }
      let dataType = paramDefinitions[paramKey];
      if (!dataType) {
        dataType = 'String';
      }
      try {
        convertedParams[paramKey] = convertParamValue(value, dataType);
      } catch (error: any) {
        throw new Error(`参数 ${paramKey} 转换失败: ${error.message}`);
      }
    }

    // 执行测试请求
    testResult.value = await testWorkflow({
      graph: JSON.stringify(val),
      params: convertedParams,
    });
  } catch (error: any) {
    error.value =
      error.response?.data?.message || '运行失败，请检查参数和网络连接';
  } finally {
    loading.value = false;
  }
}

/** 获取开始节点 */
function getStartNode() {
  if (tinyflowRef.value) {
    // TODO @xingyu：不确定是不是这里封装了 Tinyflow，现在 .getData() 会报错；
    const val = tinyflowRef.value.getData();
    const startNode = val!.nodes.find((node: any) => node.type === 'startNode');
    if (!startNode) {
      throw new Error('流程缺少开始节点');
    }
    return startNode;
  }
  throw new Error('请设计流程');
}

/** 添加参数项 */
function addParam() {
  params4Test.value.push({ key: '', value: '' });
}

/** 删除参数项 */
function removeParam(index: number) {
  params4Test.value.splice(index, 1);
}

/** 类型转换函数 */
function convertParamValue(value: string, dataType: string) {
  if (value === '') {
    return null;
  }
  switch (dataType) {
    case 'Number': {
      const num = Number(value);
      if (!isNumber(num)) throw new Error('非数字格式');
      return num;
    }
    case 'String': {
      return String(value);
    }
    case 'Boolean': {
      if (value.toLowerCase() === 'true') {
        return true;
      }
      if (value.toLowerCase() === 'false') {
        return false;
      }
      throw new Error('必须为 true/false');
    }
    case 'Array':
    case 'Object': {
      try {
        return JSON.parse(value);
      } catch (error: any) {
        throw new Error(`JSON格式错误: ${error.message}`);
      }
    }
    default: {
      throw new Error(`不支持的类型: ${dataType}`);
    }
  }
}

/** 表单校验 */
async function validate() {
  if (!workflowData.value || !tinyflowRef.value) {
    throw new Error('请设计流程');
  }
  workflowData.value = tinyflowRef.value.getData();
  return true;
}

defineExpose({ validate });
</script>

<template>
  <div class="relative h-[800px] w-full">
    <Tinyflow
      v-if="workflowData"
      ref="tinyflowRef"
      class-name="custom-class"
      class="h-full w-full"
      :data="workflowData"
      :provider="provider"
    />
    <div class="absolute right-8 top-8">
      <Button
        @click="testWorkflowModel"
        type="primary"
        v-access:code="['ai:workflow:test']"
      >
        测试
      </Button>
    </div>

    <Drawer title="工作流测试">
      <fieldset
        class="min-inline-size-auto m-0 rounded-lg border border-gray-200 px-3 py-4"
      >
        <legend class="ml-2 px-2.5 text-base font-semibold text-gray-600">
          <h3>运行参数配置</h3>
        </legend>
        <div class="p-2">
          <div
            class="mb-1 flex items-center justify-around"
            v-for="(param, index) in params4Test"
            :key="index"
          >
            <Select class="w-48" v-model="param.key" placeholder="参数名">
              <Select.Option
                v-for="(value, key) in paramsOfStartNode"
                :key="key"
                :value="key"
                :disabled="!!value?.disabled"
              >
                {{ value?.description || key }}
              </Select.Option>
            </Select>
            <Input
              class="mx-2 w-48"
              v-model:value="param.value"
              placeholder="参数值"
            />
            <Button danger plain circle @click="removeParam(index)">
              <template #icon>
                <IconifyIcon icon="lucide:trash" />
              </template>
            </Button>
          </div>
          <Button type="primary" plain class="mt-2" @click="addParam">
            添加参数
          </Button>
        </div>
      </fieldset>

      <fieldset
        class="m-0 mt-10 rounded-lg border border-gray-200 bg-card px-3 py-4"
      >
        <legend class="ml-2 px-2.5 text-base font-semibold text-gray-600">
          <h3>运行结果</h3>
        </legend>
        <div class="p-2">
          <div v-if="loading" class="text-primary">执行中...</div>
          <div v-else-if="error" class="text-danger">{{ error }}</div>
          <pre
            v-else-if="testResult"
            class="max-h-80 overflow-auto whitespace-pre-wrap rounded-lg bg-white p-3 font-mono text-sm leading-5"
          >
            {{ JSON.stringify(testResult, null, 2) }}
          </pre>
          <div v-else class="text-gray-400">点击运行查看结果</div>
        </div>
      </fieldset>

      <Button
        size="large"
        class="mt-2 w-full bg-green-500 text-white"
        @click="goRun"
      >
        运行流程
      </Button>
    </Drawer>
  </div>
</template>
