<script>
import { deepClone } from '@/utils/index'
import render from '@/components/render/render.js'
import {getAccessToken} from "@/utils/auth";

const ruleTrigger = {
  'el-input': 'blur',
  'el-input-number': 'blur',
  'el-select': 'change',
  'el-radio-group': 'change',
  'el-checkbox-group': 'change',
  'el-cascader': 'change',
  'el-time-picker': 'change',
  'el-date-picker': 'change',
  'el-rate': 'change'
}

const layouts = {
  colFormItem(h, scheme) {
    const config = scheme.__config__
    const listeners = buildListeners.call(this, scheme)

    let labelWidth = config.labelWidth ? `${config.labelWidth}px` : null
    if (config.showLabel === false) labelWidth = '0'
    return (
      <el-col span={config.span}>
        <el-form-item label-width={labelWidth} prop={scheme.__vModel__}
          label={config.showLabel ? config.label : ''}>
          <render conf={scheme} on={listeners} />
        </el-form-item>
      </el-col>
    )
  },
  rowFormItem(h, scheme) {
    let child = renderChildren.apply(this, arguments)
    if (scheme.type === 'flex') {
      child = <el-row type={scheme.type} justify={scheme.justify} align={scheme.align}>
              {child}
            </el-row>
    }
    return (
      <el-col span={scheme.span}>
        <el-row gutter={scheme.gutter}>
          {child}
        </el-row>
      </el-col>
    )
  }
}

function renderFrom(h) {
  const { formConfCopy } = this

  return (
    <el-row gutter={formConfCopy.gutter}>
      <el-form
        size={formConfCopy.size}
        label-position={formConfCopy.labelPosition}
        disabled={formConfCopy.disabled}
        label-width={`${formConfCopy.labelWidth}px`}
        ref={formConfCopy.formRef}
        // model不能直接赋值 https://github.com/vuejs/jsx/issues/49#issuecomment-472013664
        props={{ model: this[formConfCopy.formModel] }}
        rules={this[formConfCopy.formRules]}
      >
        {renderFormItem.call(this, h, formConfCopy.fields)}
        {formConfCopy.formBtns && formBtns.call(this, h)}
      </el-form>
    </el-row>
  )
}

function formBtns(h) {
  return <el-col>
    <el-form-item size="large">
      <el-button type="primary" onClick={this.submitForm}>提交</el-button>
      <el-button onClick={this.resetForm}>重置</el-button>
    </el-form-item>
  </el-col>
}

function renderFormItem(h, elementList) {
  const that = this
  const data = this[this.formConf.formModel]
  // const formRef = that.$refs[that.formConf.formRef] // 这里直接添加有问题，此时还找不到表单 $refs
  return elementList.map(scheme => {
    const config = scheme.__config__
    const layout = layouts[config.layout]

    // edit by 芋道源码，解决 el-upload 上传的问题
    // 参考 https://github.com/JakHuang/form-generator/blob/master/src/components/parser/example/Index.vue 实现
    const vModel = scheme.__vModel__
    const val = data[vModel]
    if (scheme.__config__.tag === 'el-upload') {
      // 回显图片
      scheme['file-list'] = (val || []).map(url => ({ name: url, url }))
      // 上传地址 + 请求头
      scheme.action = process.env.VUE_APP_BASE_API + "/admin-api/infra/file/upload"
      scheme.headers = { Authorization: "Bearer " + getAccessToken() }
      // 注意 on-success 不能绑定箭头函数！！！
      scheme['on-success'] = function (response, file, fileList) {
        if (response.code !== 0) {
          return;
        }
        // 添加到 data 中
        const prev = data[vModel] || []
        this.$set(data, vModel, [
          ...prev,
          response.data
        ])
        // 发起表单校验
        that.$refs[that.formConf.formRef].validateField(vModel)
      }
      // 注意 on-remove 不能绑定箭头函数！！！
      scheme['on-remove'] = function (file, fileList) {
        // 移除从 data 中
        const prev = data[vModel] || []
        const index = prev.indexOf(file.response.data)
        if (index === -1) {
          return
        }
        prev.splice(index, 1) // 直接移除即可，无需重复 set，因为 array 是引用
        // 发起表单校验
        that.$refs[that.formConf.formRef].validateField(vModel)
      }
    }

    if (layout) {
      return layout.call(this, h, scheme)
    }
    throw new Error(`没有与${config.layout}匹配的layout`)
  })
}

function renderChildren(h, scheme) {
  const config = scheme.__config__
  if (!Array.isArray(config.children)) return null
  return renderFormItem.call(this, h, config.children)
}

function setValue(event, config, scheme) {
  this.$set(config, 'defaultValue', event)
  this.$set(this[this.formConf.formModel], scheme.__vModel__, event)
}

function buildListeners(scheme) {
  const config = scheme.__config__
  const methods = this.formConf.__methods__ || {}
  const listeners = {}

  // 给__methods__中的方法绑定this和event
  Object.keys(methods).forEach(key => {
    listeners[key] = event => methods[key].call(this, event)
  })
  // 响应 render.js 中的 vModel $emit('input', val)
  listeners.input = event => setValue.call(this, event, config, scheme)

  return listeners
}

export default {
  components: {
    render
  },
  props: {
    formConf: {
      type: Object,
      required: true
    }
  },
  data() {
    const data = {
      formConfCopy: deepClone(this.formConf),
      [this.formConf.formModel]: {},
      [this.formConf.formRules]: {}
    }
    this.initFormData(data.formConfCopy.fields, data[this.formConf.formModel])
    this.buildRules(data.formConfCopy.fields, data[this.formConf.formRules])
    return data
  },
  methods: {
    initFormData(componentList, formData) {
      componentList.forEach(cur => {
        const config = cur.__config__
        if (cur.__vModel__) formData[cur.__vModel__] = config.defaultValue
        if (config.children) this.initFormData(config.children, formData)
      })
    },
    buildRules(componentList, rules) {
      componentList.forEach(cur => {
        const config = cur.__config__
        if (Array.isArray(config.regList)) {
          if (config.required) {
            const required = { required: config.required, message: cur.placeholder }
            if (Array.isArray(config.defaultValue)) {
              required.type = 'array'
              required.message = `请至少选择一个${config.label}`
            }
            required.message === undefined && (required.message = `${config.label}不能为空`)
            config.regList.push(required)
          }
          rules[cur.__vModel__] = config.regList.map(item => {
            item.pattern && (item.pattern = eval(item.pattern))
            item.trigger = ruleTrigger && ruleTrigger[config.tag]
            return item
          })
        }
        if (config.children) this.buildRules(config.children, rules)
      })
    },
    resetForm() {
      this.formConfCopy = deepClone(this.formConf)
      this.$refs[this.formConf.formRef].resetFields()
    },
    submitForm() {
      this.$refs[this.formConf.formRef].validate(valid => {
        if (!valid) return false
        // 触发 submit 事件
        // update by 芋道源码
        // this.$emit('submit', this[this.formConf.formModel])
        this.$emit('submit', {
          conf: this.formConfCopy,
          values: this[this.formConf.formModel]
        })
        return true
      })
    }
  },
  render(h) {
    return renderFrom.call(this, h)
  }
}
</script>
