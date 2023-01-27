<template>
  <div class="app-container">
    <doc-alert title="邮件配置" url="https://doc.iocoder.cn/mail" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入模板名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="模板编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入模板编码" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="邮箱账号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请输入邮箱账号" clearable>
          <el-option v-for="account in accountOptions" :key="account.id" :value="account.id" :label="account.mail" />
        </el-select>
      </el-form-item>
      <el-form-item label="开启状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择开启状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:mail-template:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="模板编码" align="center" prop="code" />
      <el-table-column label="模板名称" align="center" prop="name" />
      <el-table-column label="模板标题" align="center" prop="title" />
      <el-table-column label="模板内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="邮箱账号" align="center" prop="accountId" width="200">
        <template v-slot="scope">
          {{ accountOptions.find(account => account.id === scope.row.accountId)?.mail }}
        </template>
      </el-table-column>
      <el-table-column label="发送人名称" align="center" prop="nickname" />
      <el-table-column label="开启状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-share" @click="handleSend(scope.row)"
                     v-hasPermi="['system:mail-template:send-mail']">测试</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:mail-template:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:mail-template:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="600px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="邮箱账号" prop="accountId">
          <el-select v-model="form.accountId" placeholder="请输入邮箱账号">
            <el-option v-for="account in accountOptions" :key="account.id" :value="account.id" :label="account.mail" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送人名称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入发送人名称" />
        </el-form-item>
        <el-form-item label="模板标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入模板标题" />
        </el-form-item>
        <el-form-item label="模板内容">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="开启状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 对话框(发送邮件) -->
    <el-dialog title="测试发送邮件" :visible.sync="sendOpen" width="500px" append-to-body>
      <el-form ref="sendForm" :model="sendForm" :rules="sendRules" label-width="140px">
        <el-form-item label="模板内容" prop="content">
          <editor v-model="sendForm.content" :min-height="192" readonly />
        </el-form-item>
        <el-form-item label="收件邮箱" prop="mail">
          <el-input v-model="sendForm.mail" placeholder="请输入收件邮箱" />
        </el-form-item>
        <el-form-item v-for="param in sendForm.params" :key="param" :label="'参数 {' + param + '}'" :prop="'templateParams.' + param">
          <el-input v-model="sendForm.templateParams[param]" :placeholder="'请输入 ' + param + ' 参数'" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSendForm">确 定</el-button>
        <el-button @click="cancelSend">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createMailTemplate, updateMailTemplate, deleteMailTemplate, getMailTemplate, getMailTemplatePage, sendMail } from "@/api/system/mail/template";
import Editor from '@/components/Editor';
import { CommonStatusEnum } from "@/utils/constants";
import { getSimpleMailAccountList } from "@/api/system/mail/account";

export default {
  name: "MailTemplate",
  components: {
    Editor,
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 邮件模版列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        code: null,
        accountId: null,
        status: null,
        createTime: [],
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "模板名称不能为空", trigger: "blur" }],
        code: [{ required: true, message: "模板编码不能为空", trigger: "blur" }],
        accountId: [{ required: true, message: "邮箱账号不能为空", trigger: "blur" }],
        title: [{ required: true, message: "模板标题不能为空", trigger: "blur" }],
        content: [{ required: true, message: "模板内容不能为空", trigger: "blur" }],
        status: [{ required: true, message: "开启状态不能为空", trigger: "blur" }],
      },
      // 邮箱账号
      accountOptions: [],

      // 发送邮箱
      sendOpen: false,
      sendForm: {
        params: [], // 模板的参数列表
      },
      sendRules: {
        mail: [{ required: true, message: "收件邮箱不能为空", trigger: "blur" }],
        templateCode: [{ required: true, message: "模版编码不能为空", trigger: "blur" }],
        templateParams: { }
      }
    };
  },
  created() {
    this.getList();
    // 获得邮箱账号列表
    getSimpleMailAccountList().then(response => {
      this.accountOptions = response.data
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getMailTemplatePage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        code: undefined,
        accountId: undefined,
        nickname: undefined,
        title: undefined,
        content: undefined,
        status: CommonStatusEnum.ENABLE,
        remark: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加邮件模版";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getMailTemplate(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改邮件模版";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateMailTemplate(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createMailTemplate(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除邮件模版编号为"' + id + '"的数据项?').then(function() {
          return deleteMailTemplate(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 发送短息按钮 */
    handleSend(row) {
      this.resetSend(row);
      // 设置参数
      this.sendForm.content = row.content;
      this.sendForm.params = row.params;
      this.sendForm.templateCode = row.code;
      this.sendForm.templateParams = row.params.reduce(function(obj, item) {
        obj[item] = undefined;
        return obj;
      }, {});
      // 根据 row 重置 rules
      this.sendRules.templateParams = row.params.reduce(function(obj, item) {
        obj[item] = { required: true, message: '参数 ' + item + " 不能为空", trigger: "change" };
        return obj;
      }, {});
      // 设置打开
      this.sendOpen = true;
    },
    /** 重置发送邮箱的表单 */
    resetSend() {
      // 根据 row 重置表单
      this.sendForm = {
        content: undefined,
        params: undefined,
        mail: undefined,
        templateCode: undefined,
        templateParams: {}
      };
      this.resetForm("sendForm");
    },
    /** 取消发送邮箱 */
    cancelSend() {
      this.sendOpen = false;
      this.resetSend();
    },
    /** 提交按钮 */
    submitSendForm() {
      this.$refs["sendForm"].validate(valid => {
        if (!valid) {
          return;
        }
        // 添加的提交
        sendMail(this.sendForm).then(response => {
          this.$modal.msgSuccess("提交发送成功！发送结果，见发送日志编号：" + response.data);
          this.sendOpen = false;
        });
      });
    },
  }
};
</script>
