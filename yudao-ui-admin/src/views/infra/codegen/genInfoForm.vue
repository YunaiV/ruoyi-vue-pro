<template>
  <el-form ref="genInfoForm" :model="info" :rules="rules" label-width="150px">
    <el-row>
      <el-col :span="12">
        <el-form-item prop="templateType">
          <span slot="label">生成模板</span>
          <el-select v-model="info.templateType" @change="tplSelectChange">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INFRA_CODEGEN_TEMPLATE_TYPE)"
                :key="parseInt(dict.value)" :label="dict.label" :value="parseInt(dict.value)"/>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item prop="scene">
          <span slot="label">生成场景</span>
          <el-select v-model="info.scene">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INFRA_CODEGEN_SCENE)"
                       :key="parseInt(dict.value)" :label="dict.label" :value="parseInt(dict.value)"/>
          </el-select>
        </el-form-item>
      </el-col>

<!--      <el-col :span="12">-->
<!--        <el-form-item prop="packageName">-->
<!--          <span slot="label">-->
<!--            生成包路径-->
<!--            <el-tooltip content="生成在哪个java包下，例如 com.ruoyi.system" placement="top">-->
<!--              <i class="el-icon-question"></i>-->
<!--            </el-tooltip>-->
<!--          </span>-->
<!--          <el-input v-model="info.packageName" />-->
<!--        </el-form-item>-->
<!--      </el-col>-->

      <el-col :span="12">
        <el-form-item prop="moduleName">
          <span slot="label">
            模块名
            <el-tooltip content="模块名，即一级目录，例如 system、infra、tool 等等" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-input v-model="info.moduleName" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="businessName">
          <span slot="label">
            业务名
            <el-tooltip content="业务名，即二级目录，例如 user、permission、dict 等等" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-input v-model="info.businessName" />
        </el-form-item>
      </el-col>

<!--      <el-col :span="12">-->
<!--        <el-form-item prop="businessPackage">-->
<!--          <span slot="label">-->
<!--            业务包-->
<!--            <el-tooltip content="业务包，自定义二级目录。例如说，我们希望将 dictType 和 dictData 归类成 dict 业务" placement="top">-->
<!--              <i class="el-icon-question"></i>-->
<!--            </el-tooltip>-->
<!--          </span>-->
<!--          <el-input v-model="info.businessPackage" />-->
<!--        </el-form-item>-->
<!--      </el-col>-->

      <el-col :span="12">
        <el-form-item prop="className">
          <span slot="label">
            类名称
            <el-tooltip content="类名称（首字母大写），例如SysUser、SysMenu、SysDictData 等等" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-input v-model="info.className" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="classComment">
          <span slot="label">
            类描述
            <el-tooltip content="用作类描述，例如 用户" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-input v-model="info.classComment" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            上级菜单
            <el-tooltip content="分配到指定菜单下，例如 系统管理" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <treeselect :append-to-body="true" v-model="info.parentMenuId" :options="menus"
            :normalizer="normalizer" :show-count="true" placeholder="请选择系统菜单" />
        </el-form-item>
      </el-col>

      <el-col :span="24" v-if="info.genType == '1'">
        <el-form-item prop="genPath">
          <span slot="label">
            自定义路径
            <el-tooltip content="填写磁盘绝对路径，若不填写，则生成到当前Web项目下" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-input v-model="info.genPath">
            <el-dropdown slot="append">
              <el-button type="primary">
                最近路径快速选择
                <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="info.genPath = '/'">恢复默认的生成基础路径</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-input>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row v-show="info.tplCategory == 'tree'">
      <h4 class="form-header">其他信息</h4>
      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            树编码字段
            <el-tooltip content="树显示的编码字段名， 如：dept_id" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-select v-model="info.treeCode" placeholder="请选择">
            <el-option
              v-for="(column, index) in info.columns"
              :key="index"
              :label="column.columnName + '：' + column.columnComment"
              :value="column.columnName"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            树父编码字段
            <el-tooltip content="树显示的父编码字段名， 如：parent_Id" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-select v-model="info.treeParentCode" placeholder="请选择">
            <el-option
              v-for="(column, index) in info.columns"
              :key="index"
              :label="column.columnName + '：' + column.columnComment"
              :value="column.columnName"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            树名称字段
            <el-tooltip content="树节点的显示名称字段名， 如：dept_name" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-select v-model="info.treeName" placeholder="请选择">
            <el-option
              v-for="(column, index) in info.columns"
              :key="index"
              :label="column.columnName + '：' + column.columnComment"
              :value="column.columnName"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row v-show="info.tplCategory == 'sub'">
      <h4 class="form-header">关联信息</h4>
      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            关联子表的表名
            <el-tooltip content="关联子表的表名， 如：sys_user" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-select v-model="info.subTableName" placeholder="请选择" @change="subSelectChange">
            <el-option
              v-for="(table, index) in tables"
              :key="index"
              :label="table.tableName + '：' + table.tableComment"
              :value="table.tableName"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item>
          <span slot="label">
            子表关联的外键名
            <el-tooltip content="子表关联的外键名， 如：user_id" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </span>
          <el-select v-model="info.subTableFkName" placeholder="请选择">
            <el-option
              v-for="(column, index) in subColumns"
              :key="index"
              :label="column.columnName + '：' + column.columnComment"
              :value="column.columnName"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script>
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "BasicInfoForm",
  components: { Treeselect },
  props: {
    info: {
      type: Object,
      default: null
    },
    tables: {
      type: Array,
      default: null
    },
    menus: {
      type: Array,
      default: []
    },
  },
  data() {
    return {
      subColumns: [],
      rules: {
        templateType: [
          { required: true, message: "请选择生成模板", trigger: "blur" }
        ],
        scene: [
          { required: true, message: "请选择生成场景", trigger: "blur" }
        ],
        // packageName: [
        //   { required: true, message: "请输入生成包路径", trigger: "blur" }
        // ],
        moduleName: [
          { required: true, message: "请输入生成模块名", trigger: "blur" }
        ],
        businessName: [
          { required: true, message: "请输入生成业务名", trigger: "blur" }
        ],
        businessPackage: [
          { required: true, message: "请输入生成业务包", trigger: "blur" }
        ],
        className: [
          { required: true, message: "请输入生成类名称", trigger: "blur" }
        ],
        classComment: [
          { required: true, message: "请输入生成类描述", trigger: "blur" }
        ],
      }
    };
  },
  created() {},
  watch: {
    'info.subTableName': function(val) {
      this.setSubTableColumns(val);
    }
  },
  methods: {
    /** 转换菜单数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id,
        label: node.name,
        children: node.children
      };
    },
    /** 选择子表名触发 */
    subSelectChange(value) {
      this.info.subTableFkName = '';
    },
    /** 选择生成模板触发 */
    tplSelectChange(value) {
      if (value !== 1) {
        // TODO 芋艿：暂时不考虑支持树形结构
        this.$modal.msgError('暂时不考虑支持【树形】和【主子表】的代码生成。原因是：导致 vm 模板过于复杂，不利于胖友二次开发');
        return false;
      }
      if(value !== 'sub') {
        this.info.subTableName = '';
        this.info.subTableFkName = '';
      }
    },
    /** 设置关联外键 */
    setSubTableColumns(value) {
      for (var item in this.tables) {
        const name = this.tables[item].tableName;
        if (value === name) {
          this.subColumns = this.tables[item].columns;
          break;
        }
      }
    }
  }
};
</script>
