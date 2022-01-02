<template>
  <div class="panel-tab__content">
    <el-form size="mini" label-width="90px" :model="model" :rules="rules" @submit.native.prevent>
      <el-form-item label="流程标识" prop="key">
        <el-input v-model="model.key" placeholder="请输入流标标识"
                  :disabled="model.id !== undefined && model.id.length > 0"/>
      </el-form-item>
      <el-form-item label="流程名称" prop="name">
        <el-input v-model="model.name" placeholder="请输入流程名称" clearable />
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
        <el-input type="textarea" v-model="model.description" clearable />
      </el-form-item>
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
    }
  },
  created() {
    // 获得流程表单的下拉框的数据
    getSimpleForms().then(response => {
      this.forms = response.data
    })
  },
  methods: {
    resetBaseInfo() {
      this.bpmnElement = window?.bpmnInstances?.bpmnElement;
      this.elementBaseInfo = JSON.parse(JSON.stringify(this.bpmnElement.businessObject));
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
