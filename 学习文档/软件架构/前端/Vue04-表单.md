### 表单

你可以用 `v-model` 指令在表单控件元素上创建双向数据绑定。

![x](http://viyitech.cn/public/images/52.png)

`v-model` 会根据控件类型自动选取正确的方法来更新元素。

**修饰符**

***.lazy***

在默认情况下，`v-model` 在 input 事件中同步输入框的值与数据，但你可以添加一个修饰符`lazy`，从而转变为在 change 事件中同步：

```html
<!-- 在 "change" 而不是 "input" 事件中更新 -->
<input v-model.lazy="msg" >
```

***.number***

如果想自动将用户的输入值转为 Number 类型（如果原值的转换结果为NaN则返回原值），可以添加一个修饰符number给v-model来处理输入值：

```html
<input v-model.number="age" type="number">
```

这通常很有用，因为在 `type="number"` 时HTML中输入的值也总是会返回字符串类型。

***.trim***

如果要自动过滤用户输入的首尾空格，可以添加 `trim` 修饰符到 `v-model` 上过滤输入：

```html
<input v-model.trim="msg">
```

**表单验证**

通过 form rule 实现。示例如下：

该示例实现的是密码输入框的输入字符验证

html：

```html
<el-form ref="myModel" :model="form" :rules="rules" size="small" label-width="100px">
  <el-form-item label="密码" prop="Password">
    <el-input v-model="form.Password" autocomplete="off" :maxlength="12" :minlength="6" style="width:300px" show-password>
    </el-input>
  </el-form-item>
</el-form>
```

js:

```js
export default {
  name: "demo",
  data: function() {
    return {
      form: {
        Password: ""
      },
      rules: {
        Password: [
          { required: true, message: "请输入密码", trigger: ["blur", "change"] },
          { min: 6, message: "最少输入6位密码", trigger: ["blur", "change"] },
          { validator: this.validatePwdRule, trigger: ["blur", "change"] }
        ]
      }
    }
  },
  methods: {
    validatePwdRule(rule, value, callback) {
      // const pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？%]");
      const pattern = new RegExp(/[^\a-\z\A-\Z0-9_]/);
      const result = value.match(pattern);
      if (result) {
        callback(new Error("密码只能输入字母、数字和下划线"));
      } else {
        callback();
      }
    }
  }
}
```

在这个示例中，我们监听了"blur", "change"事件，如果输入字符不满足要求，会给出提示！