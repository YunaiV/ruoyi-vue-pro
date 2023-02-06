<template>
  <div class="panel-tab__content">
    <el-form
      size="mini"
      label-width="90px"
      :model="model"
      :rules="rules"
      @submit.native.prevent
    >
      <div v-if="elementBaseInfo.$type === 'bpmn:Process'">
        <!-- 如果是 Process 信息的时候，使用自定义表单 -->
        <el-link
          href="https://doc.iocoder.cn/bpm/#_3-%E6%B5%81%E7%A8%8B%E5%9B%BE%E7%A4%BA%E4%BE%8B"
          type="danger"
          target="_blank"
          >如何实现实现会签、或签？</el-link
        >
        <el-form-item label="流程标识" prop="key">
          <el-input
            v-model="model.key"
            placeholder="请输入流标标识"
            :disabled="model.id !== undefined && model.id.length > 0"
            @change="handleKeyUpdate"
          />
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input
            v-model="model.name"
            placeholder="请输入流程名称"
            clearable
            @change="handleNameUpdate"
          />
        </el-form-item>
      </div>
      <div v-else>
        <el-form-item label="ID">
          <el-input
            v-model="elementBaseInfo.id"
            clearable
            @change="updateBaseInfo('id')"
          />
        </el-form-item>
        <el-form-item label="名称">
          <el-input
            v-model="elementBaseInfo.name"
            clearable
            @change="updateBaseInfo('name')"
          />
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>
<script>

export default {
  name: "ElementBaseInfo",
  props: {
    businessObject: Object,
    model: Object, // 流程模型的数据
  },
  data () {
    return {
      elementBaseInfo: {},
      // 流程表单的下拉框的数据
      forms: [],
      // 流程模型的校验
      rules: {
        key: [{ required: true, message: "流程标识不能为空", trigger: "blur" }],
        name: [{ required: true, message: "流程名称不能为空", trigger: "blur" }],
      },
    }
  },
  watch: {
    businessObject: {
      immediate: false,
      handler: function (val) {
        if (val) {
          this.$nextTick(() => this.resetBaseInfo())
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
  created () {
    // 针对上传的 bpmn 流程图时，需要延迟 1 秒的时间，保证 key 和 name 的更新
    setTimeout(() => {
      this.handleKeyUpdate(this.model.key)
      this.handleNameUpdate(this.model.name)
    }, 1000)
  },
  methods: {
    resetBaseInfo () {
      this.bpmnElement = window?.bpmnInstances?.bpmnElement
      this.elementBaseInfo = JSON.parse(JSON.stringify(this.bpmnElement.businessObject))
    },
    handleKeyUpdate (value) {
      // 校验 value 的值，只有 XML NCName 通过的情况下，才进行赋值。否则，会导致流程图报错，无法绘制的问题
      if (!value) {
        return
      }
      if (!value.match(/[a-zA-Z_][\-_.0-9a-zA-Z$]*/)) {
        console.log('key 不满足 XML NCName 规则，所以不进行赋值')
        return
      }
      console.log('key 满足 XML NCName 规则，所以进行赋值')

      // 在 BPMN 的 XML 中，流程标识 key，其实对应的是 id 节点
      this.elementBaseInfo['id'] = value
      this.updateBaseInfo('id')
    },
    handleNameUpdate (value) {
      if (!value) {
        return
      }
      this.elementBaseInfo['name'] = value
      this.updateBaseInfo('name')
    },
    handleDescriptionUpdate (value) {
      // TODO 芋艿：documentation 暂时无法修改，后续在看看
      // this.elementBaseInfo['documentation'] = value;
      // this.updateBaseInfo('documentation');
    },
    updateBaseInfo (key) {
      // 触发 elementBaseInfo 对应的字段
      const attrObj = Object.create(null)
      attrObj[key] = this.elementBaseInfo[key]
      if (key === "id") {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
          id: this.elementBaseInfo[key],
          di: { id: `${this.elementBaseInfo[key]}_di` }
        })
      } else {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, attrObj)
      }
    }
  },
  beforeDestroy () {
    this.bpmnElement = null
  }
};
</script>
