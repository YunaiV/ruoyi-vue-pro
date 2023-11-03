<template>
  <div>
    <el-dialog :visible.sync="dialogVisible" :title="title" @closed="close" append-to-body width="800px">
      <el-form ref="form" :model="formData" :rules="rules" size="medium" label-width="100px" v-loading="formLoading">
        <el-form-item label-width="180px" label="渠道费率" prop="feeRate">
          <el-input v-model="formData.feeRate" placeholder="请输入渠道费率" clearable :style="{width: '100%'}">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="开放平台 APPID" prop="config.appId">
          <el-input v-model="formData.config.appId" placeholder="请输入开放平台 APPID" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="渠道状态" prop="status">
          <el-radio-group v-model="formData.status" size="medium">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)" :key="parseInt(dict.value)"
                      :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="网关地址" prop="config.serverUrl">
          <el-radio-group v-model="formData.config.serverUrl" size="medium">
            <el-radio label="https://openapi.alipay.com/gateway.do">线上环境</el-radio>
            <el-radio label="https://openapi-sandbox.dl.alipaydev.com/gateway.do">沙箱环境</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="算法类型" prop="config.signType">
          <el-radio-group v-model="formData.config.signType" size="medium">
            <el-radio key="RSA2" label="RSA2">RSA2</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="公钥类型" prop="config.mode">
          <el-radio-group v-model="formData.config.mode" size="medium">
            <el-radio  key="公钥模式" :label="1">公钥模式</el-radio>
            <el-radio  key="证书模式" :label="2">证书模式</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="formData.config.mode === 1">
          <el-form-item label-width="180px" label="应用私钥" prop="config.privateKey">
            <el-input type="textarea" :autosize="{minRows: 8, maxRows: 8}" v-model="formData.config.privateKey"
                      placeholder="请输入应用私钥" clearable :style="{width: '100%'}">
            </el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="支付宝公钥" prop="config.alipayPublicKey">
            <el-input
              type="textarea"
              :autosize="{minRows: 8, maxRows: 8}"
              v-model="formData.config.alipayPublicKey"
              placeholder="请输入支付宝公钥" clearable
              :style="{width: '100%'}">
            </el-input>
          </el-form-item>
        </div>
        <div v-if="formData.config.mode === 2">
          <el-form-item label-width="180px" label="商户公钥应用证书" prop="config.appCertContent">
            <el-input v-model="formData.config.appCertContent" type="textarea"
                      placeholder="请上传商户公钥应用证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              action=""
              ref="privateKeyContentFile"
              :limit="1"
              :accept="fileAccept"
              :http-request="appCertUpload"
              :before-upload="fileBeforeUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="支付宝公钥证书" prop="config.alipayPublicCertContent">
            <el-input v-model="formData.config.alipayPublicCertContent" type="textarea"
                      placeholder="请上传支付宝公钥证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              ref="privateCertContentFile"
              action=""
              :limit="1"
              :accept="fileAccept"
              :before-upload="fileBeforeUpload"
              :http-request="alipayPublicCertUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="根证书" prop="config.rootCertContent">
            <el-input
              v-model="formData.config.rootCertContent"
              type="textarea"
              placeholder="请上传根证书"
              readonly :autosize="{minRows: 8, maxRows: 8}"
              :style="{width: '100%'}">
            </el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              ref="privateCertContentFile"
              :limit="1"
              :accept="fileAccept"
              action=""
              :before-upload="fileBeforeUpload"
              :http-request="rootCertUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item label-width="180px" label="备注" prop="remark">
          <el-input v-model="formData.remark" :style="{width: '100%'}"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { createChannel, getChannel, updateChannel } from "@/api/pay/channel";
import { CommonStatusEnum } from "@/utils/constants";

export default {
  name: "alipayChannelForm",
  data() {
    return {
      dialogVisible: false,
      formLoading: false,
      title:'',
      formData: {
        appId: '',
        code: '',
        status: undefined,
        feeRate: undefined,
        remark: '',
        config: {
          appId: '',
          serverUrl: null,
          signType: '',
          mode: null,
          privateKey: '',
          alipayPublicKey: '',
          appCertContent: '',
          alipayPublicCertContent: '',
          rootCertContent: ''
        }
      },
      rules: {
        feeRate: [{ required: true,  message: '请输入渠道费率', trigger: 'blur' }],
        status: [{ required: true,  message: '渠道状态不能为空',  trigger: 'blur' }],
        'config.appId': [{ required: true, message: '请输入开放平台上创建的应用的 ID', trigger: 'blur' }],
        'config.serverUrl': [{ required: true, message: '请传入网关地址', trigger: 'blur' }],
        'config.signType': [{ required: true, message: '请传入签名算法类型', trigger: 'blur' }],
        'config.mode': [{ required: true, message: '公钥类型不能为空', trigger: 'blur'}],
        'config.privateKey': [{ required: true, message: '请输入商户私钥', trigger: 'blur' }],
        'config.alipayPublicKey': [{ required: true, message: '请输入支付宝公钥字符串', trigger: 'blur' }],
        'config.appCertContent': [{ required: true,  message: '请上传商户公钥应用证书', trigger: 'blur' }],
        'config.alipayPublicCertContent': [{ required: true, message: '请上传支付宝公钥证书', trigger: 'blur'}],
        'config.rootCertContent': [{ required: true, message: '请上传指定根证书', trigger: 'blur' }],
      },
      fileAccept: ".crt",
    }
  },
  methods: {
    open(appId, code) {
      this.dialogVisible = true;
      this.formLoading = true;
      this.reset(appId, code);
      getChannel(appId, code).then(response => {
        if (response.data && response.data.id) {
          this.formData = response.data;
          this.formData.config = JSON.parse(response.data.config);
        }
        this.title = !this.formData.id ? '创建支付渠道' : '编辑支付渠道'
      }).finally(() => {
        this.formLoading = false;
      });
    },
    close() {
      this.dialogVisible = false;
      this.reset(undefined, undefined);
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        const data = { ...this.formData };
        data.config = JSON.stringify(this.formData.config);
        if (!data.id) {
          createChannel(data).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.$emit('success')
            this.close();
          });
        } else {
          updateChannel(data).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.$emit('success')
            this.close();
          })
        }
      });
    },
    /** 重置表单 */
    reset(appId, code) {
      this.formData = {
        appId: appId,
        code: code,
        status: CommonStatusEnum.ENABLE,
        remark: '',
        feeRate: null,
        config: {
          appId: '',
          serverUrl: null,
          signType: 'RSA2',
          mode: null,
          privateKey: '',
          alipayPublicKey: '',
          appCertContent: '',
          alipayPublicCertContent: '',
          rootCertContent: ''
        }
      }
      this.resetForm('form')
    },
    fileBeforeUpload(file) {
      let format = '.' + file.name.split(".")[1];
      if (format !== this.fileAccept) {
        this.$message.error('请上传指定格式"' + this.fileAccept + '"文件');
        return false;
      }
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      return isRightSize
    },
    appCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.appCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    alipayPublicCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.alipayPublicCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    rootCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.rootCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    }
  }
}
</script>
