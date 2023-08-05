<template>
  <div>
    <el-dialog :visible.sync="dialogVisible" :title="title" @close="close" append-to-body width="800px">
      <el-form ref="form" :model="formData" :rules="rules" size="medium" label-width="120px"
               v-loading="formLoading">
        <el-form-item label-width="180px" label="渠道费率" prop="feeRate">
          <el-input v-model="formData.feeRate" placeholder="请输入渠道费率" clearable :style="{width: '100%'}">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="公众号 APPID" prop="config.appId">
          <el-input v-model="formData.config.appId" placeholder="请输入公众号 APPID" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="商户号" prop="config.mchId">
          <el-input v-model="formData.config.mchId" :style="{width: '100%'}"></el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="渠道状态" prop="status">
          <el-radio-group v-model="formData.status" size="medium">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)" :key="parseInt(dict.value)"
                      :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="API 版本" prop="config.apiVersion">
          <el-radio-group v-model="formData.config.apiVersion" size="medium">
            <el-radio label="v2">v2</el-radio>
            <el-radio label="v3">v3</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="formData.config.apiVersion === 'v2'">
          <el-form-item label-width="180px" label="商户密钥" prop="config.mchKey">
            <el-input v-model="formData.config.mchKey" placeholder="请输入商户密钥" clearable
                      :style="{width: '100%'}" type="textarea" :autosize="{minRows: 8, maxRows: 8}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="apiclient_cert.p12 证书" prop="config.keyContent">
            <el-input v-model="formData.config.keyContent" type="textarea"
                      placeholder="请上传 apiclient_cert.p12 证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload :limit="1" accept=".p12" action=""
                       :before-upload="p12FileBeforeUpload"
                       :http-request="keyContentUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
        </div>
        <div v-if="formData.config.apiVersion === 'v3'">
          <el-form-item label-width="180px" label="API V3 密钥" prop="config.apiV3Key">
            <el-input v-model="formData.config.apiV3Key" placeholder="请输入 API V3 密钥" clearable
                      :style="{width: '100%'}" type="textarea" :autosize="{minRows: 8, maxRows: 8}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="apiclient_key.pem 证书" prop="config.privateKeyContent">
            <el-input v-model="formData.config.privateKeyContent" type="textarea"
                      placeholder="请上传 apiclient_key.pem 证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="" prop="privateKeyContentFile">
            <el-upload ref="privateKeyContentFile"
                       :limit="1"
                       accept=".pem"
                       action=""
                       :before-upload="pemFileBeforeUpload"
                       :http-request="privateKeyContentUpload"
            >
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="apiclient_cert.perm证书" prop="config.privateCertContent">
            <el-input v-model="formData.config.privateCertContent" type="textarea"
                      placeholder="请上传apiclient_cert.perm证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="" prop="privateCertContentFile">
            <el-upload ref="privateCertContentFile"
                       :limit="1"
                       accept=".pem"
                       action=""
                       :before-upload="pemFileBeforeUpload"
                       :http-request="privateCertContentUpload"
            >
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item label-width="180px" label="备注" prop="remark">
          <el-input v-model="formData.remark" :style="{width: '100%'}" />
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
  name: "weixinChannelForm",
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
          mchId: '',
          apiVersion: '',
          mchKey: '',
          keyContent: '',
          privateKeyContent: '',
          privateCertContent: '',
          apiV3Key:'',
        }
      },
      rules: {
        feeRate: [{ required: true, message: '请输入渠道费率', trigger: 'blur' }],
        status: [{ required: true, message: '渠道状态不能为空', trigger: 'blur'}],
        'config.mchId': [{ required: true, message: '请传入商户号', trigger: 'blur'}],
        'config.appId': [{ required: true, message: '请输入公众号APPID', trigger: 'blur'}],
        'config.apiVersion': [{ required: true, message: 'API版本不能为空', trigger: 'blur'}],
        'config.mchKey': [{ required: true, message: '请输入商户密钥', trigger: 'blur' }],
        'config.keyContent': [{ required: true, message: '请上传 apiclient_cert.p12 证书', trigger: 'blur' }],
        'config.privateKeyContent': [{ required: true, message: '请上传 apiclient_key.pem 证书', trigger: 'blur' }],
        'config.privateCertContent': [{ required: true, message: '请上传 apiclient_cert.perm证 书', trigger: 'blur' }],
        'config.apiV3Key': [{ required: true, message: '请上传 api V3 密钥值', trigger: 'blur' }],
      },
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
        feeRate: undefined,
        remark: '',
        config: {
          appId: '',
          mchId: '',
          apiVersion: '',
          mchKey: '',
          keyContent: '',
          privateKeyContent: '',
          privateCertContent: '',
          apiV3Key:'',
        }
      }
      this.resetForm('form')
    },
    /**
     * apiclient_cert.p12、apiclient_cert.pem、apiclient_key.pem 上传前的校验
     */
    fileBeforeUpload(file, fileAccept) {
      let format = '.' + file.name.split(".")[1];
      if (format !== fileAccept) {
        debugger
        this.$message.error('请上传指定格式"' + fileAccept + '"文件');
        return false;
      }
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      return isRightSize
    },
    p12FileBeforeUpload(file) {
      this.fileBeforeUpload(file, '.p12')
    },
    pemFileBeforeUpload(file) {
      this.fileBeforeUpload(file, '.pem')
    },
    /**
     * 读取 apiclient_key.pem 到 privateKeyContent 字段
     */
    privateKeyContentUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.privateKeyContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    /**
     * 读取 apiclient_cert.pem 到 privateCertContent 字段
     */
    privateCertContentUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.privateCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    /**
     * 读取 apiclient_cert.p12 到 keyContent 字段
     */
    keyContentUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.formData.config.keyContent = e.target.result.split(',')[1]
      }
      readFile.readAsDataURL(event.file); // 读成 base64
    }
  }
}
</script>
