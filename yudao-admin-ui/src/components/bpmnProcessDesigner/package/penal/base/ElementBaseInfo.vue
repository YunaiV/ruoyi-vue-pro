<template>
  <div class="panel-tab__content">
    <el-form size="mini" label-width="90px" :model="model" :rules="rules" @submit.native.prevent>
      <div v-if="elementBaseInfo.$type === 'bpmn:Process'"> <!-- 如果是 Process 信息的时候，使用自定义表单 -->
        <el-form-item label="流程标识" prop="key">
          <el-input v-model="model.key" placeholder="请输入流标标识"
                    :disabled="model.id !== undefined && model.id.length > 0" @change="handleKeyUpdate" />
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="model.name" placeholder="请输入流程名称" clearable @change="handleNameUpdate" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="model.category" placeholder="请选择流程分类" clearable style="width: 100%">
            <el-option v-for="dict in categoryDictDatas" :key="dict.value" :label="dict.label" :value="dict.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="流程表单" prop="formId">
          <el-select v-model="model.formId" placeholder="请选择流程表单，非必选哟！" clearable style="width: 100%">
            <el-option v-for="form in forms" :key="form.id" :label="form.name" :value="form.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="流程描述" prop="description">
          <el-input type="textarea" v-model="model.description" clearable @change="handleDescriptionUpdate" />
        </el-form-item>
      </div>
      <div v-else>
        <el-form-item label="ID">
          <el-input v-model="elementBaseInfo.id" clearable @change="updateBaseInfo('id')"/>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="elementBaseInfo.name" clearable @change="updateBaseInfo('name')" />
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>
<script>
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {getSimpleForms} from "@/api/bpm/form";
import {getModel} from "@/api/bpm/model";

export default {
  name: "ElementBaseInfo",
  props: {
    businessObject: Object,
    model: Object, // 流程模型的数据
  },
  data() {
    return {
      elementBaseInfo: {},
      // 流程表单的下拉框的数据
      forms: [],
      // 流程模型的校验
      rules: {
        key: [{ required: true, message: "流程标识不能为空", trigger: "blur" }],
        name: [{ required: true, message: "流程名称不能为空", trigger: "blur" }],
        category: [{ required: true, message: "流程分类不能为空", trigger: "blur" }],
      },
      // 数据字典
      categoryDictDatas: getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY),
    };
  },
  watch: {
    businessObject: {
      immediate: false,
      handler: function(val) {
        if (val) {
          this.$nextTick(() => this.resetBaseInfo());
        }
      }
    },
    // 'model.key': {
    //   immediate: false,
    //   handler: function (val) {
    //     this.handleKeyUpdate(val)
    //   }
    // }
  },
  created() {
    // 获得流程表单的下拉框的数据
    getSimpleForms().then(response => {
      this.forms = response.data
    })
    // 针对上传的 bpmn 流程图时，需要延迟 1 秒的时间，保证 key 和 name 的更新
    setTimeout(() => {
      this.handleKeyUpdate(this.model.key)
      this.handleNameUpdate(this.model.name)
    }, 1000)
  },
  methods: {
    resetBaseInfo() {
      this.bpmnElement = window?.bpmnInstances?.bpmnElement;
      this.elementBaseInfo = JSON.parse(JSON.stringify(this.bpmnElement.businessObject));
    },
    handleKeyUpdate(value) {
      // 校验 value 的值，只有 XML NCName 通过的情况下，才进行赋值。否则，会导致流程图报错，无法绘制的问题
      if (!value) {
        return;
      }
      if (!value.match(/[a-zA-Z_][\-_.0-9_a-zA-Z$]*/)) {
        console.log('key 不满足 XML NCName 规则，所以不进行赋值');
        return;
      }
      console.log('key 满足 XML NCName 规则，所以进行赋值');

      // 在 BPMN 的 XML 中，流程标识 key，其实对应的是 id 节点
      this.elementBaseInfo['id'] = value;
      this.updateBaseInfo('id');
    },
    handleNameUpdate(value) {
      if (!value) {
        return
      }
      this.elementBaseInfo['name'] = value;
      this.updateBaseInfo('name');
    },
    handleDescriptionUpdate(value) {
      // TODO 芋艿：documentation 暂时无法修改，后续在看看
      // this.elementBaseInfo['documentation'] = value;
      // this.updateBaseInfo('documentation');
    },
    updateBaseInfo(key) {
      // 触发 elementBaseInfo 对应的字段
      const attrObj = Object.create(null);
      attrObj[key] = this.elementBaseInfo[key];
      if (key === "id") {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
          id: this.elementBaseInfo[key],
          di: { id: `${this.elementBaseInfo[key]}_di` }
        });
      } else {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, attrObj);
      }
    }
  },
  beforeDestroy() {
    this.bpmnElement = null;
  }
};
</script>
