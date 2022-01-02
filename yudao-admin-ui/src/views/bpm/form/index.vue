<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表单名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入表单名" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['bpm:form:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="表单名" align="center" prop="name" />
      <el-table-column label="开启状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.SYS_COMMON_STATUS, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleDetail(scope.row)"
                     v-hasPermi="['bpm:form:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['bpm:form:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['bpm:form:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!--表单配置详情-->
    <el-dialog title="表单详情" :visible.sync="detailOpen" width="50%" append-to-body>
      <div class="test-form">
        <parser :key="new Date().getTime()" :form-conf="detailForm" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {deleteForm, getForm, getFormPage} from "@/api/bpm/form";
import Parser from '@/utils/parser/Parser'

export default {
  name: "Form",
  components: {
    Parser
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 工作流的列表
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
      },
      // 表单详情
      detailOpen: false,
      detailForm: {}
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
      // 执行查询
      getFormPage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
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
    /** 详情按钮操作 */
    handleDetail(row) {
      this.detailOpen = true
      getForm(row.id).then(response => {
        const data = response.data
        // this.detailForm = {
        //   ...JSON.parse(data.conf),
        //   fields: this.decodeFields(data.fields)
        // }
        console.log({
          ...JSON.parse(data.conf),
          fields: this.decodeFields(data.fields)
        })
        this.detailForm = {
          fields: [
            {
              __config__: {
                label: '单行文本',
                labelWidth: null,
                showLabel: true,
                changeTag: true,
                tag: 'el-input',
                tagIcon: 'input',
                required: true,
                layout: 'colFormItem',
                span: 24,
                document: 'https://element.eleme.cn/#/zh-CN/component/input',
                regList: [
                  {
                    pattern: '/^1(3|4|5|7|8|9)\\d{9}$/',
                    message: '手机号格式错误'
                  }
                ]
              },
              __slot__: {
                prepend: '',
                append: ''
              },
              __vModel__: 'mobile',
              placeholder: '请输入手机号',
              style: {
                width: '100%'
              },
              clearable: true,
              'prefix-icon': 'el-icon-mobile',
              'suffix-icon': '',
              maxlength: 11,
              'show-word-limit': true,
              readonly: false,
              disabled: false
            },
            {
              __config__: {
                label: '日期范围',
                tag: 'el-date-picker',
                tagIcon: 'date-range',
                defaultValue: null,
                span: 24,
                showLabel: true,
                labelWidth: null,
                required: true,
                layout: 'colFormItem',
                regList: [],
                changeTag: true,
                document:
                  'https://element.eleme.cn/#/zh-CN/component/date-picker',
                formId: 101,
                renderKey: 1585980082729
              },
              style: {
                width: '100%'
              },
              type: 'daterange',
              'range-separator': '至',
              'start-placeholder': '开始日期',
              'end-placeholder': '结束日期',
              disabled: false,
              clearable: true,
              format: 'yyyy-MM-dd',
              'value-format': 'yyyy-MM-dd',
              readonly: false,
              __vModel__: 'field101'
            },
            {
              __config__: {
                layout: 'rowFormItem',
                tagIcon: 'row',
                label: '行容器',
                layoutTree: true,
                children: [
                  {
                    __config__: {
                      label: '评分',
                      tag: 'el-rate',
                      tagIcon: 'rate',
                      defaultValue: 0,
                      span: 24,
                      showLabel: true,
                      labelWidth: null,
                      layout: 'colFormItem',
                      required: true,
                      regList: [],
                      changeTag: true,
                      document: 'https://element.eleme.cn/#/zh-CN/component/rate',
                      formId: 102,
                      renderKey: 1586839671259
                    },
                    style: {},
                    max: 5,
                    'allow-half': false,
                    'show-text': false,
                    'show-score': false,
                    disabled: false,
                    __vModel__: 'field102'
                  }
                ],
                document: 'https://element.eleme.cn/#/zh-CN/component/layout',
                formId: 101,
                span: 24,
                renderKey: 1586839668999,
                componentName: 'row101',
                gutter: 15
              },
              type: 'default',
              justify: 'start',
              align: 'top'
            },
            {
              __config__: {
                label: '按钮',
                showLabel: true,
                changeTag: true,
                labelWidth: null,
                tag: 'el-button',
                tagIcon: 'button',
                span: 24,
                layout: 'colFormItem',
                document: 'https://element.eleme.cn/#/zh-CN/component/button',
                renderKey: 1594288459289
              },
              __slot__: {
                default: '测试按钮1'
              },
              type: 'primary',
              icon: 'el-icon-search',
              round: false,
              size: 'medium',
              plain: false,
              circle: false,
              disabled: false,
              on: {
                click: 'clickTestButton1'
              }
            }
          ],
          __methods__: {
            clickTestButton1() {
              console.log(
                `%c【测试按钮1】点击事件里可以访问当前表单：
                1) formModel='formData', 所以this.formData可以拿到当前表单的model
                2) formRef='elForm', 所以this.$refs.elForm可以拿到当前表单的ref(vue组件)
              `,
                'color:#409EFF;font-size: 15px'
              )
              console.log('表单的Model：', this.formData)
              console.log('表单的ref：', this.$refs.elForm)
            }
          },
          formRef: 'elForm',
          formModel: 'formData',
          size: 'small',
          labelPosition: 'right',
          labelWidth: 100,
          formRules: 'rules',
          gutter: 15,
          disabled: false,
          span: 24,
          formBtns: true,
          unFocusedComponentBorder: false
        }
      });
    },
    decodeFields(fields) {
      const drawingList = []
      fields.forEach(item => {
        drawingList.push(JSON.parse(item))
      })
      return drawingList
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({
        path:"/bpm/manager/form/edit"
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({
        path:"/bpm/manager/form/edit",
        query:{
          formId: row.id
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$confirm('是否确认删除工作流的编号为"' + id + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return deleteForm(id);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    }
  }
};
</script>
