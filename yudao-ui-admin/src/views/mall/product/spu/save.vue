<template>
  <div class="container">
    <el-tabs v-model="activeName" class="tabs"  :before-leave="confirmLeave">
      <!-- 基础设置 -->
      <el-tab-pane label="基础设置" name="base">
        <el-form ref="base" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="baseForm.name" placeholder="请输入商品名称"/>
          </el-form-item>
          <el-form-item label="商品卖点">
            <el-input type="textarea" v-model="baseForm.sellPoint" placeholder="请输入商品卖点"/>
          </el-form-item>
          <!-- TODO @Luowenfeng：商品主图，80 x 80 即可 -->
          <el-form-item label="商品主图" prop="picUrls">
            <ImageUpload v-model="baseForm.picUrls" :value="baseForm.picUrls" :limit="10"/>
          </el-form-item>
          <!-- TODO @Luowenfeng：商品视频 -->
          <el-form-item label="商品品牌" prop="brandId">
            <el-select v-model="baseForm.brandId" placeholder="请选择商品品牌">
              <el-option v-for="item in brandList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品分类" prop="categoryIds">
            <el-cascader v-model="baseForm.categoryIds" placeholder="商品分类" style="width: 100%"
                         :options="categoryList" :props="propName" clearable />
          </el-form-item>
          <el-form-item label="是否上架" prop="status">
            <el-radio-group v-model="baseForm.status">
              <el-radio :label="0">立即上架</el-radio>
              <el-radio :label="1">放入仓库</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 价格库存 -->
      <el-tab-pane label="价格库存" name="rates" class="rates">
        <el-form ref="rates" :model="ratesForm" :rules="rules">
          <el-form-item label="启用多规格">
            <!-- TODO @Luowenfeng：改成开关的按钮；关闭，单规格；开启，多规格 -->
            <el-radio-group v-model="ratesForm.spec" @change="changeRadio">
              <el-radio :label="1">单规格</el-radio>
              <el-radio :label="2">多规格</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 动态添加规格属性 -->
          <div v-show="ratesForm.spec === 2">
            <div v-for="(specs, index) in dynamicSpec" :key="index" class="dynamic-spec">
              <!-- 删除按钮 -->
              <el-button type="danger" icon="el-icon-delete" circle class="spec-delete" @click="removeSpec(index)" />
              <div class="spec-header">
                规格项：
                <el-select v-model="specs.specId" filterable placeholder="请选择" @change="changeSpec">
                  <el-option v-for="item in propertyPageList" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </div>
              <div class="spec-values">
                <template v-for="(v, i) in specs.specValue">
                  <el-input v-model="v.name" class="spec-value" :key="i" disabled/>
                </template>
              </div>
            </div>
            <el-button type="primary" @click="dynamicSpec.push({specValue: []}); ratesForm.rates = []">添加规格项目</el-button>
          </div>

          <!-- 规格明细 -->
          <el-form-item label="规格明细">
            <el-table :data="ratesForm.rates" border style="width: 100%" ref="ratesTable">
              <template v-if="ratesForm.spec == 2">
                <el-table-column :key="index" v-for="(item, index) in dynamicSpec.filter(v => v.specName !== undefined)"
                                 :label="item.specName">
                  <template slot-scope="scope">
                    <el-input v-if="scope.row.spec" v-model="scope.row.spec[index]" disabled />
                  </template>
                </el-table-column>
              </template>
              <el-table-column label="规格图片" width="120px" :render-header="addRedStar" key="90">
                <template slot-scope="scope">
                    <ImageUpload v-model="scope.row.picUrl" :limit="1" :isShowTip="false"
                               style="width: 100px; height: 50px"/>
                </template>
              </el-table-column>
              <template v-if="ratesForm.spec === 2">
               <el-table-column label="sku名称" :render-header="addRedStar" key="91">
                  <template slot-scope="scope">
                    <el-form-item :prop="'rates.'+ scope.$index + '.name'" :rules="[{required: true, trigger: 'change'}]">
                      <el-input v-model="scope.row.name" />
                    </el-form-item>
                  </template>
                </el-table-column>
              </template>
              <el-table-column label="市场价(元)" :render-header="addRedStar" key="92">
                <template slot-scope="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.marketPrice'" :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.marketPrice"
                      oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''"/>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="销售价(元)" :render-header="addRedStar" key="93">
                <template slot-scope="scope">
                 <el-form-item :prop="'rates.'+ scope.$index + '.price'" :rules="[{required: true, trigger: 'change'}]">
                  <el-input v-model="scope.row.price" oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''"></el-input>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="成本价" :render-header="addRedStar" key="94">
                <template slot-scope="scope">
                <el-form-item :prop="'rates.'+ scope.$index + '.costPrice'" :rules="[{required: true, trigger: 'change'}]">
                  <el-input
                    v-model="scope.row.costPrice"
                    oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''"
                  ></el-input>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="库存" :render-header="addRedStar" key="95">
                <template slot-scope="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.stock'" :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.stock" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"></el-input>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="预警库存" key="96">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.warnStock" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="体积" key="97">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.volume" ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="重量" key="98">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.weight" ></el-input>
                </template>
              </el-table-column>
              <el-table-column label="条码" key="99">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.barCode"></el-input>
                </template>
              </el-table-column>
               <template v-if="ratesForm.spec === 2">
                <el-table-column fixed="right" label="操作" width="50" key="100">
                  <template slot-scope="scope">
                    <el-button @click="scope.row.status = 1" type="text" size="small" v-show="scope.row.status == undefined || scope.row.status == 0 ">禁用</el-button>
                    <el-button @click="scope.row.status = 0" type="text" size="small" v-show="scope.row.status == 1">启用</el-button>
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </el-form-item>
          <el-form-item label="虚拟销量" prop="virtualSalesCount">
            <!-- TODO @Luowenfeng：使用 input 类型即可 -->
            <el-input v-model="baseForm.virtualSalesCount" placeholder="请输入虚拟销量" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"/>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 商品详情 -->
      <el-tab-pane label="商品详情" name="third">
      <el-form ref="third" :model="baseForm" :rules="rules">
        <el-form-item prop="description">
          <editor v-model="baseForm.description" :min-height="380"/>
        </el-form-item>
      </el-form>
      </el-tab-pane>

      <!-- 销售设置 -->
      <el-tab-pane label="高级设置" name="fourth">
        <el-form ref="fourth" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="排序字段">
            <el-input v-model="baseForm.sort" placeholder="请输入排序字段" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"/>
          </el-form-item>
           <el-form-item label="是否展示库存" prop="showStock">
             <el-radio-group v-model="baseForm.showStock">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
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

import {getBrandList} from "@/api/mall/product/brand";
import {getProductCategoryList} from "@/api/mall/product/category";
import {createSpu, updateSpu, getSpu} from "@/api/mall/product/spu";
import {getPropertyPage,} from "@/api/mall/product/property";
import Editor from "@/components/Editor";
import ImageUpload from "@/components/ImageUpload";

export default {
  components: {
    Editor,
    ImageUpload
  },
  props:{//props列表
    type:{
      type:String,
      default:"add" //定义参数默认值
    },
    obj: Object
  },
  data() {
    return {
      activeName: "base", // TODO @Luowenfeng：切换时，不需要校验通过
      propName: {
        checkStrictly: true,
        label: "name",
        value: "id",
      },
      // 基础设置
      baseForm: {
        id: null,
        name: null,
        sellPoint: null,
        categoryIds: null,
        sort: null,
        description: null,
        picUrls: null,
        status: 0,
        virtualSalesCount: 0,
        showStock: true,
        brandId: null
      },
      categoryList: [],
      // 价格库存
      ratesForm: {
        spec: 1,
        // 规格明细
        rates: [{}]
      },
      dynamicSpec: [
        // {
        //   specId: 86,
        //   specName: "颜色",
        //   specValue:[{
        //      name: "红色",
        //      id: 225,
        //   }]
        // },
      ],
      propertyPageList: [],
      brandList: [],
      specValue: null,

      // 表单校验
      rules: {
        name:[{required: true, message: "商品名称不能为空", trigger: "blur"},],
        description: [{required: true, message: "描述不能为空", trigger: "blur"},],
        categoryIds: [{required: true, message: "分类id不能为空", trigger: "blur"},],
        status: [{required: true, message: "商品状态不能为空", trigger: "blur"}],
        picUrls: [{required: true, message: "商品轮播图地址不能为空", trigger: "blur"}],
        virtualSalesCount: [{required: true, message: "虚拟销量不能为空", trigger: "blur"}],
      },
    };
  },
  created() {
    this.getListBrand();
    this.getListCategory();
    this.getPropertyPageList();
    if(this.type == 'upd'){
      this.updateType(this.obj.id)
    }
  },
  methods: {
    removeSpec(index){
        this.dynamicSpec.splice(index, 1);
        this.changeRadio()
    },
    async confirmLeave(active, old){
      await this.$refs[old].validate((valid) => {
        console.log(valid)
        if (!valid) {
          return reject();
        }
      });
    },
    // 必选标识
    addRedStar(h, { column }) {
      return [
        h('span', { style: 'color: #F56C6C' }, '*'),
        h('span', ' ' + column.label)
      ];
    },
    changeRadio() {
      this.$refs.ratesTable.doLayout();
      if (this.ratesForm.spec == 1) {
        this.ratesForm.rates = [{}]
      } else {
        this.ratesForm.rates = []
        if (this.dynamicSpec.length > 0) {
          console.log( this.dynamicSpec)
          this.buildRatesFormRates()
        }
      }
    },
    // 构建规格明细笛卡尔积
    buildRatesFormRates() {
      let rates = [];
      this.dynamicSpec.map(v => v.specValue.map(m => m.name))
        .reduce((last, current) => {
          const array = [];
          last.forEach(par1 => {
            current.forEach(par2 => {
              let v
              if (par1 instanceof Array) {
                v = par1.concat(par2)
              } else {
                v = [par1, par2];
              }
              array.push(v)
            });
          });
          return array;
        })
        .forEach(v => {
          rates.push({spec: v, status: 0, name: Array.of(v).join()})
        });
      this.ratesForm.rates = rates
    },
    /** 查询分类 */
    getListCategory() {
      // 执行查询
      getProductCategoryList().then((response) => {
        this.categoryList = this.handleTree(response.data, "id", "parentId");
      });
    },
     /** 查询品牌列表 */
    getListBrand() {
      // 执行查询
      getBrandList().then((response) => {
        this.brandList = response.data;
      });
    },
    cancel() {
      this.$emit("closeDialog");
    },
    submit() {
       this.$refs[this.activeName].validate((valid) => {
        if (!valid) {
          return;
        }
      let rates = this.ratesForm.rates;

      // 价格元转分
      rates.forEach(r=>{
        r.marketPrice = r.marketPrice*100;
        r.price = r.price*100;
        r.costPrice = r.costPrice*100;
      })

      // 动态规格调整字段
      if (this.ratesForm.spec == 2) {
        rates.forEach(r => {
          let properties = []
            Array.of(r.spec).forEach(s => {
              let obj;
               if (s instanceof Array) {
                  obj = s;
               }else{
                  obj = Array.of(s);
               }
              obj.forEach((v, i) => {
                let specValue = this.dynamicSpec[i].specValue.find(o => o.name == v);
                let propertie = {};
                propertie.propertyId = this.dynamicSpec[i].specId;
                propertie.valueId = specValue.id;
                properties.push(propertie);
              })
            })
          r.properties = properties;
        })
      }else{
        rates[0].name = this.baseForm.name;
        rates[0].status = this.baseForm.status;
      }
      let form = this.baseForm

      if(form.picUrls instanceof Array){
        form.picUrls = form.picUrls.flatMap(m=>m.split(','))
      }else if(form.picUrls.split(',') instanceof Array){
        form.picUrls = form.picUrls.split(',').flatMap(m=>m.split(','))
      }else{
        form.picUrls = Array.of(form.picUrls)
      }
      console.log(rates)
      form.skus = rates;
      form.specType = this.ratesForm.spec;
      form.categoryId = form.categoryIds[this.baseForm.categoryIds.length - 1];


      if(form.id == null){
        createSpu(form).then((response) => {
          this.$modal.msgSuccess("新增成功");
        })
      }else{
        updateSpu(form).then((response) => {
          this.$modal.msgSuccess("修改成功");
        })
      }
      });
      this.$emit("closeDialog");
    },
    /** 查询规格 */
    getPropertyPageList() {
      // 执行查询
      getPropertyPage().then((response) => {
        this.propertyPageList = response.data.list;
      });
    },
    changeSpec(val) {
      let obj = this.propertyPageList.find(o => o.id == val);
      let dynamicSpec = this.dynamicSpec;
      let spec = dynamicSpec.find(o => o.specId == val)
      spec.specId = obj.id;
      spec.specName = obj.name;
      spec.specValue = obj.propertyValueList;
      this.dynamicSpec = dynamicSpec;
      this.buildRatesFormRates();
    },
    updateType(id){
        getSpu(id).then((response) =>{
            let data = response.data;
            this.baseForm.id=data.id;
            this.baseForm.name=data.name;
            this.baseForm.sellPoint=data.sellPoint;
            this.baseForm.categoryIds=data.categoryIds;
            this.baseForm.sort=data.sort;
            this.baseForm.description=data.description;
            this.baseForm.picUrls=data.picUrls;
            this.baseForm.status=data.status;
            this.baseForm.virtualSalesCount=data.virtualSalesCount;
            this.baseForm.showStock=data.showStock;
            this.baseForm.brandId=data.brandId;
            this.ratesForm.spec=data.specType;
            data.skus.forEach(r=>{
              r.marketPrice = this.divide(r.marketPrice, 100)
              r.price = this.divide(r.price, 100)
              r.costPrice = this.divide(r.costPrice, 100)
            })
            if(this.ratesForm.spec == 2){
              data.productPropertyViews.forEach(p=>{
                let obj = {};
                obj.specId = p.propertyId;
                obj.specName = p.name;
                obj.specValue = p.propertyValues;
                this.dynamicSpec.push(obj);
              })
              data.skus.forEach(s=>{
                s.spec = [];
                s.properties.forEach(sp=>{
                  let spec = data.productPropertyViews.find(o=>o.propertyId == sp.propertyId).propertyValues.find(v=>v.id == sp.valueId).name;
                   s.spec.push(spec)
                })
              })
            }
            this.ratesForm.rates=data.skus
        })
    }
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
    padding-bottom: 20px;

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
    padding-top: 5px;

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

.tabs {
  height: 500px;
  border-bottom: 2px solid #f2f2f2;

  .el-tab-pane {
    height: 445px;
    overflow-y: auto;
  }
}

// 库存价格图片样式修改
.rates {
  .component-upload-image {
    margin: auto;
  }

  .el-upload--picture-card {
    width: 100px;
    height: 50px;
    line-height: 60px;
    margin: auto;
  }

  .el-upload-list__item {
    width: 100px !important;
    height: 50px !important;
  }
}

.buttons {
  margin-top: 20px;
  height: 36px;

  button {
    float: right;
    margin-left: 15px;
  }
}
</style>
