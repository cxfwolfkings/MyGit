### 插槽

#### 一、slot的作用

Q: 假如父组件需要在子组件内放一些DOM元素，那么这些DOM是显示呢还是不显示呢？

默认情况下是不会显示的，如下图所示，页面并没有显示父组件增加的`<span>我是魔鬼</span>`元素内容。那么我执意要加DOM元素到子组件上该怎么实现呢？这就用到了slot插槽，使用slot这个标签可以将父组件放在子组件的内容，放到它想显示的地方

```vue
<div id="app">  
  <children>  
    <span>我是魔鬼</span>  
    <!--上面这行不会显示-->  
  </children>  
</div>  
<script>  
  var vm = new Vue({  
      el: '#app',  
      components: {  
         children: {   
         template: "<h1>我是子组件</h1>"  
       }  
     }  
  });  
</script> 
```

#### 二、简单理解slot

通俗易懂的讲，slot具有“占坑”的作用，在子组件占好了位置，那父组件使用该子组件标签时，新添加的DOM元素就会自动填到这个坑里面

#### 三、单个slot

我们将上面的代码改一改 , 给child组件添加slot插槽，那么父组件的添加的DOM元素就填充到这个slot插槽里面了

注意：如果有多个DOM元素，会一起插入到`<slot></slot>`这个标签内

```vue
<div id="app">  
  <children>  
    <span>我是魔鬼</span>  
    <!--上面这行会显示在 “我是子组件” 数据后面-->  
  </children>  
</div>  
<script>  
  var vm = new Vue({  
      el: '#app',  
      components: {  
         children: {   
         template: "<h1>我是子组件</h1><slot></slot>"  
       }  
     }  
  });  
</script>
```

#### 四、具名插槽

现在我们需要将父组件添加的HTML标签放在子组件里的不同位置。具名插槽实现：先在子组件对应分发slot标签里，添加`name="name名"` 属性，其次父组件在要分发的标签里添加 `slot="name名"` 属性，然后就会将对应的标签放在对应的位置了。

简单理解就是：给子组件占的每一个坑取名，将父组件添加的HTML元素添加到指定名字的坑，就实现了分发内容在不同位置显示

【Child组件模板】

```vue
<template>
  <div>
    <slot name="header"></slot>
    <h1>我是子组件</h1>
    <slot name="footer"></slot>
  </div>
</template>
```

【父组件引用Child组件】

```vue
<Child>
  <span slot="header">我是header</span>
  <span slot="footer">我是footer</span>
</Child>
```

#### 五、编辑作用域

父组件模板的内容在父组件作用域内编译；子组件模板的内容在子组件作用域内编译

【Child组件模板】

```vue
<template>
  <div>
    <slot name="header"></slot>
    <h1>{{msg}}</h1>
    <slot></slot>
    <slot name="footer"></slot>
  </div>
</template>
<script>
export default {
  data() {
    return {
      msg: '我是子组件里面的内容'
    }
  }
}
</script>
```

【父组件引用Child组件】

```vue
<template>
  <Child>
    <span slot="header">我是header</span>
    <h1>{{msg}}</h1>
    <span slot="footer">我是footer</span>
  </Child>
</template>
<script>
export default {
  data() {
    return {
      msg: '我是父组件的内容'
    }
  }
}
</script>
```

#### 六、解构slot-scope

在子组件中插槽中通过：data绑定了数据，父组件可以通过`slot-scope="name"`来取得子组件作用域插槽：data绑定的数据，name的名称可以随便取，用来定义对象来代替取到的data数据。

【Child组件模板】

```vue
<template>
  <div>
    <!-- :data 也可以是 :row, :list 等任意属性名，只要父组件对应的属性名相同即可 -->
    <slot :data="data"></slot>
  </div>
</template>
<script>
export default {
  data() {
    return {
      data: ['Neinei','Laoba','Demi','Feiyan']
    }
  }
}
</script>
```

【父组件引用Child组件】

```vue
 <template>
    <!-- 循环数据列表 -->
    <Child>
      <div slot-scope="msg">
        <span v-for="item in msg.data">{{item}} </span>
      </div>
    </Child>
 
    <!-- 直接显示数据 -->
    <Child>
      <div slot-scope="msg">
        <span>{{msg.data}} </span>
      </div>
    </Child>
 
    <!-- 不使用其提供的数据, 作用域插槽退变成匿名插槽 -->
    <Child>
      <div>我是插槽</div>
    </Child>
  </template>
```





**参考**

- [https://blog.csdn.net/qq_38128179/article/details/85273522](https://blog.csdn.net/qq_38128179/article/details/85273522)