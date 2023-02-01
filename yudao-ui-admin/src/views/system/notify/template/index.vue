<template>
  <div class="app-container">
    <doc-alert title="站内信配置" url="https://doc.iocoder.cn/notify/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入模板名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="模版编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入模版编码" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
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
                   v-hasPermi="['system:notify-template:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="模板编码" align="center" prop="code" />
      <el-table-column label="模板名称" align="center" prop="name" />
      <el-table-column label="类型" align="center" prop="type">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="发送人名称" align="center" prop="nickname" />
      <el-table-column label="模板内容" align="center" prop="content" width="300" />
      <el-table-column label="开启状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-share" @click="handleSendNotify(scope.row)"
                     v-hasPermi="['system:notify-template:send-notify']">测试</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:notify-template:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:notify-template:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="模版编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入模版编码" />
        </el-form-item>
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模版名称" />
        </el-form-item>
        <el-form-item label="发件人名称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入发件人名称" />
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input type="textarea" v-model="form.content" placeholder="请输入模板内容" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
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

    <!-- 对话框(发送站内信) -->
    <el-dialog title="发送站内信" :visible.sync="sendNotifyOpen" width="500px" append-to-body>
      <el-form ref="sendNotifyForm" :model="sendNotifyForm" :rules="sendNotifyRules" label-width="140px">
        <el-form-item label="模板内容" prop="content">
          <el-input v-model="sendNotifyForm.content" type="textarea" placeholder="请输入模板内容" readonly />
        </el-form-item>
        <el-form-item label="接收人" prop="userId">
          <el-select v-model="sendNotifyForm.userId" placeholder="请输入接收人" clearable style="width: 100%">
            <el-option v-for="item in users" :key="parseInt(item.id)" :label="item.nickname" :value="parseInt(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item v-for="param in sendNotifyForm.params" :key="param" :label="'参数 {' + param + '}'" :prop="'templateParams.' + param">
          <el-input v-model="sendNotifyForm.templateParams[param]" :placeholder="'请输入 ' + param + ' 参数'" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSendNotifyForm">确 定</el-button>
        <el-button @click="cancelSendNotify">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { createNotifyTemplate, updateNotifyTemplate, deleteNotifyTemplate, getNotifyTemplate, getNotifyTemplatePage,
  sendNotify } from "@/api/system/notify/template";
import {listSimpleUsers} from "@/api/system/user";
import {CommonStatusEnum} from "@/utils/constants";

export default {
  name: "NotifyTemplate",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 短信模板列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        status: null,
        code: null,
        title: null,
        createTime: []
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "模板名称不能为空", trigger: "blur" }],
        code: [{ required: true, message: "模版编码不能为空", trigger: "blur" }],
        nickname: [{ required: true, message: "发件人名称不能为空", trigger: "blur" }],
        content: [{ required: true, message: "模版内容不能为空", trigger: "blur" }],
        type: [{ required: true, message: "类型不能为空", trigger: "change" }],
        status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
      },
      // 用户列表
      users: [],
      // 发送短信
      sendNotifyOpen: false,
      sendNotifyForm: {
        params: [], // 模板的参数列表
      },
      sendNotifyRules: {
        userId: [{ required: true, message: "接收人不能为空", trigger: "blur" }],
        templateCode: [{ required: true, message: "模版编码不能为空", trigger: "blur" }],
        templateParams: { }
      }
    };
  },
  created() {
    this.getList();
    // 获得用户列表
    listSimpleUsers().then(response => {
      this.users = response.data;
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getNotifyTemplatePage(this.queryParams).then(response => {
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
        nickname: undefined,
        content: undefined,
        type: undefined,
        params: undefined,
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
      this.title = "添加站内信模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getNotifyTemplate(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改站内信模板";
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
          updateNotifyTemplate(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createNotifyTemplate(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除站内信模板编号为"' + id + '"的数据项?').then(function() {
        return deleteNotifyTemplate(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 发送站内信按钮 */
    handleSendNotify(row) {
      this.resetSendNotify(row);
      // 设置参数
      this.sendNotifyForm.content = row.content;
      this.sendNotifyForm.params = row.params;
      this.sendNotifyForm.templateCode = row.code;
      this.sendNotifyForm.templateParams = row.params.reduce(function(obj, item) {
        obj[item] = undefined;
        return obj;
      }, {});
      // 根据 row 重置 rules
      this.sendNotifyRules.templateParams = row.params.reduce(function(obj, item) {
        obj[item] = { required: true, message: '参数 ' + item + " 不能为空", trigger: "change" };
        return obj;
      }, {});
      // 设置打开
      this.sendNotifyOpen = true;
    },
    /** 重置发送站内信的表单 */
    resetSendNotify() {
      // 根据 row 重置表单
      this.sendNotifyForm = {
        content: undefined,
        params: undefined,
        userId: undefined,
        templateCode: undefined,
        templateParams: {}
      };
      this.resetForm("sendNotifyForm");
    },
    /** 取消发送站内信 */
    cancelSendNotify() {
      this.sendNotifyOpen = false;
      this.resetSendNotify();
    },
    /** 提交按钮 */
    submitSendNotifyForm() {
      this.$refs["sendNotifyForm"].validate(valid => {
        if (!valid) {
          return;
        }
        // 添加的提交
        sendNotify(this.sendNotifyForm).then(response => {
          this.$modal.msgSuccess("提交发送成功！发送结果，见发送日志编号：" + response.data);
          this.sendNotifyOpen = false;
        });
      });
    },
  }
};
</script>
