<script lang="ts" setup>
import { onBeforeUnmount, reactive, ref, toRaw, watch } from 'vue';

import { Form, FormItem, Input } from 'ant-design-vue';

defineOptions({ name: 'ElementBaseInfo' });

const props = defineProps<{
  businessObject?: BusinessObject;
  model?: Model;
}>();

interface BusinessObject {
  id?: string;
  name?: string;
  $type: string;
  [key: string]: any;
}

interface Model {
  key?: string;
  name?: string;
  [key: string]: any;
}

const needProps = ref<Record<string, any>>({});
const bpmnElement = ref<any>();
const elementBaseInfo = ref<BusinessObject>({} as any);
// 流程表单的下拉框的数据
// const forms = ref([])
// 流程模型的校验
const rules = reactive<any>({
  id: [{ required: true, message: '流程标识不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }],
});

const bpmnInstances = () =>
  (window as any)?.bpmnInstances as {
    bpmnElement: any;
    modeling: {
      updateProperties: (element: any, properties: any) => void;
    };
  };
const resetBaseInfo = () => {
  // console.log(window, 'window');
  // console.log(bpmnElement.value, 'bpmnElement');

  bpmnElement.value = bpmnInstances()?.bpmnElement;
  // console.log(bpmnElement.value, 'resetBaseInfo11111111111')
  if (bpmnElement.value?.businessObject) {
    elementBaseInfo.value = bpmnElement.value.businessObject;
    needProps.value.type = bpmnElement.value.businessObject.$type;
  }
  // elementBaseInfo.value['typess'] = bpmnElement.value.businessObject.$type

  // elementBaseInfo.value = JSON.parse(JSON.stringify(bpmnElement.value.businessObject))
  // console.log(elementBaseInfo.value, 'elementBaseInfo22222222222')
};
const handleKeyUpdate = (value: any) => {
  // 校验 value 的值，只有 XML NCName 通过的情况下，才进行赋值。否则，会导致流程图报错，无法绘制的问题
  if (!value) {
    return;
  }
  if (!/[a-z_][-\w.$]*/i.test(value)) {
    // console.log('key 不满足 XML NCName 规则，所以不进行赋值');
    return;
  }
  // console.log('key 满足 XML NCName 规则，所以进行赋值');

  // 在 BPMN 的 XML 中，流程标识 key，其实对应的是 id 节点
  if (elementBaseInfo.value) {
    elementBaseInfo.value.id = value;
  }

  setTimeout(() => {
    updateBaseInfo('id');
  }, 100);
};

const handleNameUpdate = (value: any) => {
  // console.log(elementBaseInfo, 'elementBaseInfo');
  if (!value) {
    return;
  }
  if (elementBaseInfo.value) {
    elementBaseInfo.value.name = value;
  }

  setTimeout(() => {
    updateBaseInfo('name');
  }, 100);
};
// const handleDescriptionUpdate=(value)=> {
// TODO 芋艿：documentation 暂时无法修改，后续在看看
// this.elementBaseInfo['documentation'] = value;
// this.updateBaseInfo('documentation');
// }
const updateBaseInfo = (key: string) => {
  // console.log(key, 'key');
  // 触发 elementBaseInfo 对应的字段
  const attrObj: Record<string, any> = Object.create(null);

  // 安全检查
  if (!elementBaseInfo.value || !bpmnElement.value) {
    return;
  }

  // console.log(attrObj, 'attrObj')
  attrObj[key] = elementBaseInfo.value[key];
  // console.log(attrObj, 'attrObj111')
  // const attrObj = {
  //   id: elementBaseInfo.value[key]
  //   // di: { id: `${elementBaseInfo.value[key]}_di` }
  // }
  // console.log(elementBaseInfo, 'elementBaseInfo11111111111')
  needProps.value = { ...elementBaseInfo.value, ...needProps.value };

  if (key === 'id') {
    // console.log('jinru')
    // console.log(window, 'window');
    // console.log(bpmnElement.value, 'bpmnElement');
    // console.log(toRaw(bpmnElement.value), 'bpmnElement');
    bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
      id: elementBaseInfo.value[key],
      di: { id: `${elementBaseInfo.value[key]}_di` },
    });
  } else {
    // console.log(attrObj, 'attrObj');
    bpmnInstances().modeling.updateProperties(
      toRaw(bpmnElement.value),
      attrObj,
    );
  }
};

watch(
  () => props.businessObject,
  (val) => {
    // console.log(val, 'val11111111111111111111')
    if (val) {
      // nextTick(() => {
      resetBaseInfo();
      // })
    }
  },
);

watch(
  () => props.model?.key,
  (val) => {
    // 针对上传的 bpmn 流程图时，保证 key 和 name 的更新
    if (val) {
      handleKeyUpdate(props.model?.key as any);
      handleNameUpdate(props.model?.name as any);
    }
  },
  {
    immediate: true,
  },
);

// watch(
//   () => ({ ...props }),
//   (oldVal, newVal) => {
//     console.log(oldVal, 'oldVal')
//     console.log(newVal, 'newVal')
//     if (newVal) {
//       needProps.value = newVal
//     }
//   },
//   {
//     immediate: true
//   }
// )
// 'model.key': {
//   immediate: false,
//   handler: function (val) {
//     this.handleKeyUpdate(val)
//   }
// }
onBeforeUnmount(() => {
  bpmnElement.value = null;
});
</script>
<template>
  <div class="panel-tab__content">
    <Form :model="needProps" :rules="rules" layout="vertical">
      <div v-if="needProps.type === 'bpmn:Process'">
        <!-- 如果是 Process 信息的时候，使用自定义表单 -->
        <FormItem label="流程标识" name="id">
          <Input
            v-model:value="needProps.id"
            placeholder="请输入流标标识"
            :disabled="needProps.id !== undefined && needProps.id.length > 0"
            @change="handleKeyUpdate"
          />
        </FormItem>
        <FormItem label="流程名称" name="name">
          <Input
            v-model:value="needProps.name"
            placeholder="请输入流程名称"
            allow-clear
            @change="handleNameUpdate"
          />
        </FormItem>
      </div>
      <div v-else>
        <FormItem label="ID">
          <Input
            v-model:value="elementBaseInfo.id"
            allow-clear
            @change="updateBaseInfo('id')"
          />
        </FormItem>
        <FormItem label="名称">
          <Input
            v-model:value="elementBaseInfo.name"
            allow-clear
            @change="updateBaseInfo('name')"
          />
        </FormItem>
      </div>
    </Form>
  </div>
</template>
