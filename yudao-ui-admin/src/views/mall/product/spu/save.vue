<template>
  <div class="container">
    <el-dialog
      title="请输入规格值，多个请换行"
      :visible.sync="dialogForSpec"
      append-to-body
      width="400px"
      @close="dialogForSpec = false; specValue = null"
    >
      <el-input
        v-model="specValue"
        type="textarea"
        :autosize="{ minRows: 6, maxRows: 6}"
        placeholder="请输入内容"
      />

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogForSpec = false; specValue = null">取 消</el-button>
        <el-button type="primary" @click="addSpecValue">确 定</el-button>
      </div>
    </el-dialog>

    <el-tabs v-model="activeName"  class="tabs">
      <!-- 基础设置 -->
      <el-tab-pane label="基础设置" name="base">
        <el-form ref="baseForm" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="baseForm.name" placeholder="请输入商品名称"/>
          </el-form-item>
          <el-form-item label="卖点" prop="sellPoint">
            <el-input v-model="baseForm.sellPoint" placeholder="请输入卖点"/>
          </el-form-item>
          <el-form-item label="分类id" prop="categoryIds">
            <el-cascader
              v-model="baseForm.categoryIds"
              placeholder="请输入分类id"
              style="width: 100%"
              :options="categoryList"
              :props="propName"
              clearable
            ></el-cascader>
          </el-form-item>
          <el-form-item label="商品主图" prop="picUrls">
            <ImageUpload v-model="baseForm.picUrl" :limit="1" />
          </el-form-item>
           <el-form-item label="商品轮播图" prop="picUrl">
            <ImageUpload v-model="baseForm.picUrls" :limit="10" />
          </el-form-item>
          <el-form-item label="排序字段" prop="sort">
            <el-input v-model="baseForm.sort" placeholder="请输入排序字段"/>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 价格库存 -->
      <el-tab-pane label="价格库存" name="rates" class="rates">
        <el-form :model="ratesForm" :rules="rules.ratesForm">
          <el-form-item label="商品规格">
            <el-radio-group v-model="ratesForm.spec" @change="changeRadio">
              <el-radio :label="0">单规格</el-radio>
              <el-radio :label="1">多规格</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 动态添加规格属性 -->
          <div v-show="ratesForm.spec === 1">
            <div
              v-for="(specs, index) in dynamicSpec"
              :key="index"
              class="dynamic-spec"
            >
              <!-- 删除按钮 -->
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                class="spec-delete"
                @click="dynamicSpec.splice(index, 1)"
              ></el-button>

              <div class="spec-header">
                规格项：
                <el-input
                  v-model="specs.spec_name"
                  placeholder="请填写规格项"
                  class="spec-name"
                ></el-input>
              </div>
              <div class="spec-values">
                <template v-for="(specsValue, i) in specs.spec_values">
                  <el-input
                    v-model="specs.spec_values[i]"
                    class="spec-value"
                    :key="specsValue"
                  ></el-input>
                </template>
                <el-button
                  type="primary"
                  icon="el-icon-plus"
                  circle
                  @click="dialogForSpec = true; currentSpec = index"
                ></el-button>
              </div>
            </div>
            <el-button type="primary" @click="dynamicSpec.push({spec_values: []}); ratesForm.rates = []"
            >添加规格项目
            </el-button
            >
          </div>

          <!-- 规格明细 -->
          <el-form-item label="规格明细">
            <el-table :data="ratesForm.rates" border style="width: 100%" ref="rates">
              
              <template v-if="ratesForm.spec == 1" > 
                <el-table-column :key="index" v-for="(item, index) in dynamicSpec.filter(v=>v.spec_name != undefined)" :label="item.spec_name" >
                <template slot-scope="scope">
                  <el-input
                    v-if="scope.row.spec"
                    v-model="scope.row.spec[index]"
                    disabled
                  ></el-input>
                </template>
              </el-table-column>
              </template>
             
              <el-table-column label="规格图片" width="120px">
                <template slot-scope="scope">
                  <ImageUpload v-model="scope.row.picUrl" :limit="1" :isShowTip="false" style="width: 100px; height: 50px" />
                </template>
              </el-table-column>
              <el-table-column label="市场价(元)">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.marketPrice"
                    type="number"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="销售价(元)" re>
                <template slot-scope="scope">
                  <el-input v-model="scope.row.price" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="成本价">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.costPrice"
                    type="number"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="库存">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.stock" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="体积">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.volume" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="重量">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.weight" type="number"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="条码">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.barCode"></el-input>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 商品详情 -->
      <el-tab-pane label="商品描述" name="third">
        <editor v-model="baseForm.description" :min-height="400" />
      </el-tab-pane>

      <!-- 销售设置 -->
      <el-tab-pane label="销售设置" name="fourth">
        <el-form ref="baseForm" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
        <el-form-item label="库存数量" prop="quantity">
          <el-input v-model="baseForm.quantity" placeholder="请输入库存数量" />
        </el-form-item>
        <el-form-item label="上下架状态" prop="status">
          <el-radio-group v-model="baseForm.status">
            <el-radio label="0">上架</el-radio>
            <el-radio label="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

  <div class="buttons">
  <el-button type="info" round @click="cancel">取消</el-button>
   <el-button type="success" round @click="submit">确认</el-button>
  </div>

  </div>
</template>

<script>
import {
  getProductCategoryList
} from "@/api/mall/product/category";

import Editor from "@/components/Editor";
import ImageUpload from "@/components/ImageUpload";
export default {
  components: {
    Editor,
    ImageUpload
  },
  data() {
    return {
      activeName: "base",
      propName: {
        checkStrictly: true,
        label: "name",
        value: "id",
      },
      // 基础设置
      baseForm: {
        name: null,
        sellPoint: null,
        categoryIds: null,
        sort: null,
        description: null,
        picUrl:null,
        picUrls: [],
        quantity: null
      },
      categoryList: [],

      // 价格库存
      ratesForm: {
        spec: 0,
        // 规格明细
        rates: [{}]
      },
      dynamicSpec: [
        // {
        //   spec_id: 86,
        //   spec_name: "颜色",
        //   spec_values: [],
        //   spec_value_ids: [225],
        // },
      ],
      dialogForSpec: false,
      specValue: null,
      currentSpec: null,

      // 表单校验
      rules: {
        description: [
          {required: true, message: "描述不能为空", trigger: "blur"},
        ],
        categoryIds: [
          {required: true, message: "分类id不能为空", trigger: "blur"},
        ],
        picUrls: [{required: true, message: "商品主图地址", trigger: "blur"}],
        sort: [
          {required: true, message: "排序字段不能为空", trigger: "blur"},
        ],
      },
    };
  },
  created() {
    this.getListCategory();
  },
  methods: {
    changeRadio() {

      this.$refs.rates.doLayout()
      if(this.ratesForm.spec == 0){
        this.ratesForm.rates = [{}]
      }else{
        this.ratesForm.rates = []
        if(this.dynamicSpec.length > 0){
          this.buildRatesFormRates()
        }
      }
    },
    // 构建规格明细笛卡尔积
  buildRatesFormRates(){
      let rates = [];
      this.dynamicSpec.map(v=>v.spec_values)
      .reduce((last, current) => {
        const array = [];
        last.forEach(par1 => {
            current.forEach(par2 => {
              let v 
              if(par1 instanceof Array){
                v = par1.concat(par2)
              }else{
                v = [par1, par2];
              }
              array.push(v)
            });
        });
        return array;
    })
    .forEach(v=>{
      rates.push({spec: v})
    });
    console.log(rates)
    this.ratesForm.rates = rates
  },
    addSpecValue() {
      this.dialogForSpec = false;
      let specValue = this.dynamicSpec[this.currentSpec].spec_values
        .concat(this.specValue.split(/[(\r\n)\r\n]+/))
        .filter(v => v != "");
      console.log(specValue)
      this.dynamicSpec[this.currentSpec].spec_values = [...new Set(specValue)];
      this.currentSpec = null;
      this.buildRatesFormRates()
    },
    /** 查询分类 */
    getListCategory() {
      // 执行查询
      getProductCategoryList().then((response) => {
        this.categoryList = this.handleTree(response.data, "id", "parentId");
      });
    },
    cancel(){
        this.$emit("closeDialog");
    },
  },
};
</script>

<style lang="scss">
.spec-dialog {
  width: 400px;
  height: 300px;
}

.dynamic-spec {
  background-color: #f2f2f2;
  width: 85%;
  margin: auto;
  margin-bottom: 10px;

  .spec-header {
    padding: 30px;
    padding-bottom: 0;

    .spec-name {
      display: inline;

      input {
        width: 30%;
      }
    }
  }

  .spec-values {
    width: 84%;
    padding: 25px;
    margin: auto;

    .spec-value {
      display: inline-block;
      margin-right: 10px;
      margin-bottom: 10px;
      width: 13%;

    }
  }

  .spec-delete {
    float: right;
    margin-top: 10px;
    margin-right: 10px;
  }
}

.tabs{
  height: 500px;
  border-bottom: 2px solid #f2f2f2;
  .el-tab-pane{
    height: 445px;
    overflow-y: auto;
  }
}

// 库存价格图片样式修改
.rates{
.component-upload-image{
  margin: auto;
}
.el-upload--picture-card{
  width: 100px;
  height: 50px;
  line-height: 60px;
  margin: auto;
}
.el-upload-list__item{
   width: 100px !important;
  height: 50px !important;
}
}
.buttons{
  margin-top: 20px;
  height: 36px;
  button{
    float: right;
    margin-left: 15px;
  }
}
</style>
