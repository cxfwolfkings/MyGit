# eclipse

**Lombok插件安装：**

1. 去[官网](https://projectlombok.org/download)下载最新版的Lombok插件

2. 在下载目录执行：`java -jar lommok.jar`

3. 等一会儿会弹出安装界面，选择IDE安装目录下的eclipse.exe，然后点击右下角的Install/Update 

4. 安装完毕到eclipse工具目录，查看eclipse.ini文件内容，会多出以下内容：

   ```bash
   -javaagent:xxx\...\lombok.jar
   ```

5. 然后重启Eclipse

6. 编码测试，新建一个项目，然后导入lomlock.jar

   如果是maven项目，配置插件坐标

   ```xml
   <dependencies>
   	<dependency>
   		<groupId>org.projectlombok</groupId>
   		<artifactId>lombok</artifactId>
   		<version>1.18.16</version>
   		<scope>provided</scope>
   	</dependency>
   </dependencies>
   ```

   创建一个java文件，名叫：DataObject.java

   ```java
   package code.model;
   
   import lombok.Builder;
   import lombok.Data;  
   import lombok.Getter;  
   import lombok.Setter;  
     
   @Data  
   @Builder
   public class DataObject {  
        private int id;     
        private String name;     
        private String password;    
   }  
   ```

   然后建测试代码：

   ```java
   package code.model;
   /**
    * @author dgm
    * @describe "测试Lombok build"
    */
   public class LombokTest {
   
   	//private final Log logger = LogFactory.getLog(getClass());
   
   	public static void main(String[] args) {
   		DataObject dataObject = DataObject.builder().id(1)
   		.name("dongguangming")
   		.password("123456").build();
   		System.out.println(dataObject);
   	}
   }
   ```

   输出，查看效果。