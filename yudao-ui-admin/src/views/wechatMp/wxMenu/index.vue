<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="父ID" prop="parentId">
        <el-input v-model="queryParams.parentId" placeholder="请输入父ID" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="菜单类型 1文本消息；2图文消息；3网址链接；4小程序" prop="menuType">
        <el-select v-model="queryParams.menuType" placeholder="请选择菜单类型 1文本消息；2图文消息；3网址链接；4小程序" clearable size="small">
          <el-option label="请选择字典生成" value=""/>
        </el-select>
      </el-form-item>
      <el-form-item label="菜单等级" prop="menuLevel">
        <el-input v-model="queryParams.menuLevel" placeholder="请输入菜单等级" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="模板ID" prop="tplId">
        <el-input v-model="queryParams.tplId" placeholder="请输入模板ID" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="菜单URL" prop="menuUrl">
        <el-input v-model="queryParams.menuUrl" placeholder="请输入菜单URL" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="排序" prop="menuSort">
        <el-input v-model="queryParams.menuSort" placeholder="请输入排序" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="微信账号ID" prop="wxAccountId">
        <el-input v-model="queryParams.wxAccountId" placeholder="请输入微信账号ID" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="小程序appid" prop="miniprogramAppid">
        <el-input v-model="queryParams.miniprogramAppid" placeholder="请输入小程序appid" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="小程序页面路径" prop="miniprogramPagepath">
        <el-input v-model="queryParams.miniprogramPagepath" placeholder="请输入小程序页面路径" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
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
                   v-hasPermi="['wechatMp:wx-menu:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['wechatMp:wx-menu:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="主键" align="center" prop="id"/>
      <el-table-column label="父ID" align="center" prop="parentId"/>
      <el-table-column label="菜单名称" align="center" prop="menuName"/>
      <el-table-column label="菜单类型 1文本消息；2图文消息；3网址链接；4小程序" align="center" prop="menuType"/>
      <el-table-column label="菜单等级" align="center" prop="menuLevel"/>
      <el-table-column label="模板ID" align="center" prop="tplId"/>
      <el-table-column label="菜单URL" align="center" prop="menuUrl"/>
      <el-table-column label="排序" align="center" prop="menuSort"/>
      <el-table-column label="微信账号ID" align="center" prop="wxAccountId"/>
      <el-table-column label="小程序appid" align="center" prop="miniprogramAppid"/>
      <el-table-column label="小程序页面路径" align="center" prop="miniprogramPagepath"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['wechatMp:wx-menu:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['wechatMp:wx-menu:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="父ID" prop="parentId">
          <el-input v-model="form.parentId" placeholder="请输入父ID"/>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称"/>
        </el-form-item>
        <el-form-item label="菜单类型 1文本消息；2图文消息；3网址链接；4小程序" prop="menuType">
          <el-select v-model="form.menuType" placeholder="请选择菜单类型 1文本消息；2图文消息；3网址链接；4小程序">
            <el-option label="请选择字典生成" value=""/>
          </el-select>
        </el-form-item>
        <el-form-item label="菜单等级" prop="menuLevel">
          <el-input v-model="form.menuLevel" placeholder="请输入菜单等级"/>
        </el-form-item>
        <el-form-item label="模板ID" prop="tplId">
          <el-input v-model="form.tplId" placeholder="请输入模板ID"/>
        </el-form-item>
        <el-form-item label="菜单URL" prop="menuUrl">
          <el-input v-model="form.menuUrl" placeholder="请输入菜单URL"/>
        </el-form-item>
        <el-form-item label="排序" prop="menuSort">
          <el-input v-model="form.menuSort" placeholder="请输入排序"/>
        </el-form-item>
        <el-form-item label="微信账号ID" prop="wxAccountId">
          <el-input v-model="form.wxAccountId" placeholder="请输入微信账号ID"/>
        </el-form-item>
        <el-form-item label="小程序appid" prop="miniprogramAppid">
          <el-input v-model="form.miniprogramAppid" placeholder="请输入小程序appid"/>
        </el-form-item>
        <el-form-item label="小程序页面路径" prop="miniprogramPagepath">
          <el-input v-model="form.miniprogramPagepath" placeholder="请输入小程序页面路径"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {
    createWxMenu,
    updateWxMenu,
    deleteWxMenu,
    getWxMenu,
    getWxMenuPage,
    exportWxMenuExcel
  } from "@/api/wechatMp/wxMenu";

  export default {
    name: "WxMenu",
    components: {},
    data() {
      return {
        // 遮罩层
        loading: true,
        // 导出遮罩层
        exportLoading: false,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 微信菜单列表
        list: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        dateRangeCreateTime: [],
        // 查询参数
        queryParams: {
          pageNo: 1,
          pageSize: 10,
          parentId: null,
          menuName: null,
          menuType: null,
          menuLevel: null,
          tplId: null,
          menuUrl: null,
          menuSort: null,
          wxAccountId: null,
          miniprogramAppid: null,
          miniprogramPagepath: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {}
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询列表 */
      getList() {
        this.loading = true;
        // 处理查询参数
        let params = {...this.queryParams};
        this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
        // 执行查询
        getWxMenuPage(params).then(response => {
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
          parentId: undefined,
          menuName: undefined,
          menuType: undefined,
          menuLevel: undefined,
          tplId: undefined,
          menuUrl: undefined,
          menuSort: undefined,
          wxAccountId: undefined,
          miniprogramAppid: undefined,
          miniprogramPagepath: undefined,
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
        this.dateRangeCreateTime = [];
        this.resetForm("queryForm");
        this.handleQuery();
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加微信菜单";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id;
        getWxMenu(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改微信菜单";
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
            updateWxMenu(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
            return;
          }
          // 添加的提交
          createWxMenu(this.form).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const id = row.id;
        this.$modal.confirm('是否确认删除微信菜单编号为"' + id + '"的数据项?').then(function () {
          return deleteWxMenu(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
      },
      /** 导出按钮操作 */
      handleExport() {
        // 处理查询参数
        let params = {...this.queryParams};
        params.pageNo = undefined;
        params.pageSize = undefined;
        this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
        // 执行导出
        this.$modal.confirm('是否确认导出所有微信菜单数据项?').then(() => {
          this.exportLoading = true;
          return exportWxMenuExcel(params);
        }).then(response => {
          this.$download.excel(response, '微信菜单.xls');
          this.exportLoading = false;
        }).catch(() => {
        });
      }
    }
  };
</script>
