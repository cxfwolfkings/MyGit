# MySQL

1. 简介
   
   - [基础类型](#基础类型)
   - [索引](#索引)
   - [临时表](#临时表)
   - [缓存池](#缓存池)
   - [Explain详解](#Explain详解)
   - [筛选](#筛选)
   - [排序](#排序)
   - [分页](#分页)
   - [table瘦身](#table瘦身)
   - [SQL&nbsp;Joins、统计、随机查询](#SQL&nbsp;Joins、统计、随机查询)
   
2. 实战
   - [常用命令语句](#常用命令语句)
     - [控制外键约束](#控制外键约束)
     - [控制安全模式](#控制安全模式)
     
   - [常用SQL语句](#常用SQL语句)

     - [系统配置语句](#系统配置语句)
     - [对象查看语句](#对象查看语句)
     - [对象操作语句](#对象操作语句)

     - [字符串函数](#字符串函数)
     - [日期时间函数](#日期时间函数)
     - [JSON函数](#JSON函数)
     - [其它SQL](#其它SQL)

   - [Handler](#Handler)

   - [触发器](#触发器)

   - [事件调度器](#事件调度器)

   - [MySQL监控](#MySQL监控)

   - [死锁](#死锁)

3. 总结

   - 常见问题
  - [忘记密码](#忘记密码)
   
   - [常见错误](#常见错误)
   - [1. This function has none of DETERMINISTIC, NOSQL, ...](#1. This function has none of DETERMINISTIC, NOSQL, ...)
     - [2. Illegal mix of collations (utf8_unicode_ci,IMPLICIT) and ...](#2. Illegal mix of collations (utf8_unicode_ci,IMPLICIT) and ...)
     - [3. 非空字段插入空值](#3. 非空字段插入空值)
     - [4. MySQL Connector/NET Exception: Reading from the stream has failed](#4. MySQL Connector/NET Exception: Reading from the stream has failed)
     - [5. mysql 卡死 大部分线程长时间处于sending data的状态](#5. mysql 卡死 大部分线程长时间处于sending data的状态)
- [性能优化](#性能优化)
   - [引擎优化](#引擎优化)
   
   - [SQL优化](#SQL优化)
- [编码设置](#编码设置)
   - [压缩](#压缩)

- [参考](#参考)



## 简介

- 一款开源、免费的数据库软件。MySql官网: [https://www.mysql.com/
- MySQL不足（未验证）：
  1. MyISAM格式的数据表只有数据表锁定，没有数据行锁定，可以使用InnoDB格式数据表绕过这个问题
  2. MyISAM数据表不能热备份，即无需锁定数据表就可以在对数据表进行处理时同时对其进行备份。InnoDB可以，但是收费
  3. 不支持自定义类型
  4. 不能直接处理XML数据
  5. 没有OLAP功能。支持OLAP的数据库系统通常称为数据仓库(data warehouse)
  6. 从5.0版本开始支持的存储过程和触发器并不成熟
  7. 从4.1版本开始支持的GIS也不成熟[](https://www.mysql.com/)

- **MySQL版本：**
  1. Alpha：开发
  2. Beta：基本完成
  3. Gamma：更加稳定
  4. Production 或 Generally Available(GA)：足够成熟和稳定

- **MySQL许可证：**GPL(GNU Public License) 

- **SQL兼容性：**MySQL 支持 SQL，SQL 有许多种“方言”，可以通过调整 MySQL 服务器的配置开关 sql-mode 使它在行为上与 IBM DB2 和 Oracle 等多种数据库系统保持最大限度的兼容

- **MySQL数据文件：**Linux: `/var/lib/mysql`，Windows: `%MySQL%/data`
  - data/dbname/tablename.frm: 数据表结构定义
  - data/dbname/db.opt: 整个数据库的结构定义和设置
  - data/dbname/tablename.MYD: MyISAM数据表数据
  - data/dbname/tablename.MYI: MyISAM数据表索引
  - innodb_file_per_table: InnoDB存储方式（各自一个文件，统一的表空间）
  - data/dbname/tablename.idb: InnoDB数据表数据、索引默认存储
  - data/ibdata1,-2,-3: 表空间和撤销日志
  - data/ib_logfile0,-1,-2: InnoDB日志数据
  - data/dbname/tablename.TRG: 触发器



### 基础类型

1. **整数类型：**tinyint, smallint, mediumint, int, bigint

2. **浮点类型：**float, double

   > decimal 能够存储精确值的原因在于其内部按照字符串存储。

3. **日期类型：**date, time, datetime, timestamp, year

4. **字符串类型：**char, varchar

   > **char**
   >
   > **优点：**简单粗暴，不管你是多长的数据，我就按照规定的长度来存，用空格补全，取数据的时候整个整个的取，简单粗暴速度快
   >
   > **缺点：**貌似浪费空间，并且我们将来存储的数据的长度可能会参差不齐
   >
   > 
   >
   > **varchar：**不定长存储数据，更为精简和节省空间
   >
   > 在存数据的时候，会在每个数据前面加上一个头，这个头是1-2个bytes的数据，这个数据指的是后面跟着的这个数据的长度，1bytes能表示 2^8^=256，两个bytes表示 2^16^=65536，能表示0-65535的数字，所以varchar在存储的时候是这样的：1bytes+xxx+1bytes+xxx+1bytes+xxx，所以存的时候会比较麻烦，导致效率比char慢，取的时候也慢，先拿长度，再取数据。
   >
   > **优点：**节省了一些硬盘空间，一个acsii码的字符用一个bytes长度就能表示，但是也并不一定比char省，看一下官网给出的一个表格对比数据，当你存的数据正好是你规定的字段长度的时候，varchar反而占用的空间比char要多。
   >
   > **缺点：**存取速度都慢
   >
   > 
   >
   > 对于InnoDB数据表，内部的行存储格式没有区分固定长度和可变长度列（所有数据行都使用指向数据列值的头指针），因此在本质上，使用固定长度的CHAR列不一定比使用可变长度VARCHAR列性能要好。因而，主要的性能因素是数据行使用的存储总量。由于CHAR平均占用的空间多于VARCHAR，因此使用VARCHAR来最小化需要处理的数据行的存储总量和磁盘I/O是比较好的。
   >
   > 适合使用char：身份证号、手机号码、QQ号、username、password、银行卡号
   > 适合使用varchar：评论、朋友圈、微博
   
5. **枚举和集合类型**

   - **enum：**单选行为------枚举类型。只允许从值集合中选取单个值，而不能一次取多个值
   - **set：**多选行为。可以允许值集合中任意选择1或多个元素进行组合。对超出范围的内容将不允许注入，而对重复的值将进行自动去重。

   ```sql
   -- 1.创建表
   create table t8(id int, name char(18),gender enum('male','female'));
   -- 2.写入数据
   insert into t8 values(1,'alex','不详'); ---------不详无法写入
   insert into t8 values(1,'alex','male');-------------male可以写入
   insert into t8 values(1,'alex','female');------------female可以写入
   
   -- 1.创建表
   create table t9(id int,name char(18),hobby set('抽烟','喝酒','洗脚','按摩','烫头'));
   -- 2.写入数据
   insert into t9 values(1,'太白','烫头,抽烟,喝酒,按摩');
   insert into t9 values(1,'大壮','洗脚,洗脚,洗脚,按摩,按摩,打游戏');
   ```



### 索引

MySQL索引中可以分为聚集索引与非聚集索引两类，在网络上也见过聚簇的说法

**聚集索引**

>索引的键值逻辑顺序决定了表数据行的物理存储顺序

也就是在数据库上连接的记录在磁盘上的物理存储地址也是相邻的，注意这一点特性，我们可以分析出它的适用情况。由于聚集索引规定了数据项，也可以说是记录在表中的物理存储顺序，物理顺序唯一，自然每张表中的聚集索引也是唯一的，但是它可以包含多个列，多个字段。

>聚集索引类似于新华字典中用拼音去查找汉字

拼音检索表于书记顺序都是按照a~z排列的，就像相同的逻辑顺序于物理顺序一样，当你需要查找a,ai两个读音的字，或是想一次寻找多个傻(sha)的同音字时，也许向后翻几页，或紧接着下一行就得到结果了。

进一步来说，当你需要查询的数据经常被分组看待（分类），或是经常查询范围性的数据（本月，本周总结），不同值的小数目等情况时，可以使用聚集索引。

**非聚集索引**

自然，非聚集索引也就是存储的键值逻辑连续，但是在表数据行物理存储顺序上不一定连续的索引

>也就是索引的逻辑顺序与磁盘上的物理存储顺序不同。  
>非聚集索引类似在新华字典上通过偏旁部首来查询汉字

检索表也许是按照横、竖、撇来排列的，但是由于正文中是a~z的拼音顺序，所以就类似于逻辑地址于物理地址的不对应。同时适用的情况就在于分组，大数目的不同值，频繁更新的列中，这些情况即不适合聚集索引。

**索引扩展**

>某些情况下索引与物理存储逻辑有关：

其中存在一种情况，MySQL 的 MyISAM 引擎 B+ 树式的存储结构，把叶子结点上存放的并不是数据本身，而是存放数据的地址，所以在使用索引时，例如主索引、辅助索引有时达不到想要的效果，而且都是非聚集索引。

>对于主键

主键不一定适合加上聚集索引，有时甚至是一种对这个唯一的聚集索引的浪费（虽然在 SQLServer 中主键默认为聚集索引），并非在任何字段上加上聚集/非聚集索引都能提高查询效率。下面我们结合实际情况分析。

>创建“索引”的利与弊

优势：

- 能够保证数据每一行的唯一性
- 合理运用时加快数据的查询速度
- 增强表与表之间的链接，参考完整性
- 减少分组、排序等操作的查询时间
- 优化查询过程，提高系统性能

弊端：

- 创建、维护索引的时间会随着数据量的增加而增加
- 自然，索引也是需要占据物理空间的
- 增删改查数据的时候，也会由于索引的存在而增加时间，类似于多了一个属性，也会降低表更新的速度

总而言之，这只是 MySQL 查询时优化速度等方面的冰山一角，还是需要多分析，多考虑，根据实际情况去选择各种辅助功能的使用，才能得到相对最高的效率。

参考：[https://www.cnblogs.com/zlcxbb/p/5757245.html](https://www.cnblogs.com/zlcxbb/p/5757245.html)

在 MySQL 中，主要有四种类型的索引，分别为：**B-Tree 索引**，**Hash 索引**，**Fulltext 索引** 和 **R-Tree 索引**。我们主要分析 B-Tree 索引。

B-Tree 索引是 MySQL 数据库中使用最为频繁的索引类型，除了 Archive 存储引擎之外的其他所有的存储引擎都支持 B-Tree 索引。Archive 引擎直到 MySQL 5.1 才支持索引，而且只支持索引单个 AUTO_INCREMENT 列。

不仅仅在 MySQL 中是如此，实际上在其他的很多数据库管理系统中 B-Tree 索引也同样是作为最主要的索引类型，这主要是因为 B-Tree 索引的存储结构在数据库的数据检索中有非常优异的表现。

一般来说， MySQL 中的 B-Tree 索引的物理文件大多都是以 Balance Tree 的结构来存储的，也就是所有实际需要的数据都存放于 Tree 的 Leaf Node（叶子节点），而且`到任何一个 Leaf Node 的最短路径的长度都是完全相同的`，所以我们大家都称之为 B-Tree 索引。

当然，可能各种数据库（或 MySQL 的各种存储引擎）在存放自己的 B-Tree 索引的时候会对存储结构稍作改造。如 `Innodb 存储引擎的 B-Tree 索引实际使用的存储结构实际上是 B+Tree`，也就是在 B-Tree 数据结构的基础上做了很小的改造，在每一个 Leaf Node 上面出了存放索引键的相关信息之外，还`存储了指向与该 Leaf Node 相邻的后一个 LeafNode 的指针信息（增加了顺序访问指针）`，这主要是为了加快检索多个相邻 Leaf Node 的效率考虑。

下面主要讨论 MyISAM 和 InnoDB 两个存储引擎的索引实现方式：

>1、MyISAM 索引实现：MyISAM 索引文件和数据文件是分离的，索引文件仅保存数据记录的地址。**

在 MyISAM 中，主索引和辅助索引(Secondary key)在结构上没有任何区别，只是主索引要求 key 是唯一的，而辅助索引的 key 可以重复。

MyISAM 中索引检索的算法为首先按照 B+Tree 搜索算法搜索索引，如果指定的 Key 存在，则取出其 data 域的值，然后以 data 域的值为地址，读取相应数据记录。

MyISAM 的索引方式也叫做“非聚集”的，之所以这么称呼是为了与 InnoDB 的聚集索引区分。

>2、InnoDB索引实现：也使用 B+Tree 作为索引结构，但具体实现方式却与 MyISAM 截然不同。

在 InnoDB 中，表数据文件本身就是按 B+Tree 组织的一个索引结构，这棵树的叶节点 data 域保存了完整的数据记录。这个索引的 key 是数据表的主键，因此 InnoDB 表数据文件本身就是主索引。这种索引叫做 **聚集索引**。

因为 InnoDB 的数据文件本身要按主键聚集，所以 InnoDB 要求表必须有主键（MyISAM可以没有），如果没有显式指定，则 MySQL 系统会自动选择一个可以唯一标识数据记录的列作为主键，如果不存在这种列，则 MySQL 自动为 InnoDB 表生成一个隐含字段作为主键，这个字段长度为6个字节，类型为长整形。

InnoDB 的所有辅助索引都引用主键作为 data 域。InnoDB 表是基于聚簇索引建立的。因此InnoDB 的索引能提供一种非常快速的主键查找性能。不过，它的辅助索引（Secondary Index，也就是非主键索引）也会包含主键列，所以，如果主键定义的比较大，其他索引也将很大。如果想在表上定义很多索引，则争取尽量把主键定义得小一些。InnoDB 不会压缩索引。

>聚集索引这种实现方式使得按主键的搜索十分高效，但是辅助索引搜索需要检索两遍索引：首先检索辅助索引获得主键，然后用主键到主索引中检索获得记录。

不同存储引擎的索引实现方式对于正确使用和优化索引都非常有帮助，例如知道了 InnoDB 的索引实现后，就很容易明白：

1. 为什么不建议使用过长的字段作为主键，因为所有辅助索引都引用主索引，过长的主索引会令辅助索引变得过大。
2. 用非单调的字段作为主键在 InnoDB 中不是个好主意，因为 InnoDB 数据文件本身是一颗 B+Tree，非单调的主键会造成在插入新记录时数据文件为了维持 B+Tree 的特性而频繁的分裂调整，十分低效，而使用自增字段作为主键则是一个很好的选择。

>InnoDB 索引和 MyISAM 索引的区别：

- 一是主索引的区别，InnoDB 的数据文件本身就是索引文件。而 MyISAM 的索引和数据是分开的。
- 二是辅助索引的区别：InnoDB 的辅助索引 data 域存储相应记录主键的值而不是地址。而 MyISAM 的辅助索引和主索引没有多大区别。



### 临时表

一般来说，分为两类：

**1. MySQL 临时表引擎，名字叫做 Memory**。比如

```
create table tmp1(id int, str1 varchar(100) ) engine = memory;
```

由参数 max_heap_table_size 来控制，超过报错。

**2. 非临时表的引擎**，这里又分为两类：

1）用户自定义的临时表，比如:

```
create temporary table (id int, str1 varchar(100) );
```

2）SQL执行过程中产生的内部临时表，比如：UNION , 聚合类ORDER BY，派生表，大对象字段的查询，子查询或者半连接的固化等等场景。

那么这两种临时表的计数器通常用 `show global status like '%tmp_%tables%'` 来查看。以上结果分别代表，只创建磁盘上的临时表计数以及临时表的总计数。这两个计数器由参数 tmp_table_size 和 max_heap_table_size 两个取最小值来控制。

那在 MySQL 5.7 之前，这个 SQL 运行中产生的临时表是 MYISAM，而且只能是 MYISAM。那 MySQL 从 5.7 开始提供了参数 Internal_tmp_mem_storage_engine 来定义内部的临时表引擎，可选值为 MYISAM 和 INNODB 。当然这里我们选择 INNODB 。并且把内部的临时表默认保存在临时表空间 ibtmp1（可以用参数 innodb_temp_data_file_path 设置大小以及步长等）下。当然这里我们得控制下 ibtmp1 的大小，要不然一个烂SQL就把磁盘整爆了。

但是MySQL 5.7 之前都没有解决如下问题:

- VARCHAR的变长存储。那就是如果临时表的字段定义是 VARCHAR(200)，那么映射到内存里处理的字段变为CHAR(200)。假设 VARCHAR(200) 就存里一个字符 "Y", 那岂不是很大的浪费。
- 大对象的默认磁盘存储，比如 TEXT，BLOB， JSON等，不管里面存放了啥，直接转化为磁盘存储。

MySQL 8.0 开始，专门实现了一个临时表的引擎 TempTable , 解决了 VARCHAR 字段的变长存储以及大对象的内存存储。由变量 interal_tmp_mem_storage_engine 来控制，可选值为 TempTable（默认）和 Memory；新引擎的大小由参数 temp_table_max_ram 来控制，默认为1G。超过了则存储在磁盘上（ibtmp1）。并且计数器由性能字典的表 memory_summary_global_by_event_name 来存储。

```sql
SELECT * FROM performance_schema. memory_summary_global_by_event_name WHERE event_name like '%temptable%';

*************************** 1. row ***************************
EVENT_NAME: **memory/temptable/physical_disk**
COUNT_ALLOC: 0
COUNT_FREE: 0
SUM_NUMBER_OF_BYTES_ALLOC: 0
SUM_NUMBER_OF_BYTES_FREE: 0
LOW_COUNT_USED: 0
CURRENT_COUNT_USED: 0
HIGH_COUNT_USED: 0
LOW_NUMBER_OF_BYTES_USED: 0
CURRENT_NUMBER_OF_BYTES_USED: 0
HIGH_NUMBER_OF_BYTES_USED: 0
*************************** 2. row ***************************
EVENT_NAME: **memory/temptable/physical_ram**
COUNT_ALLOC: 1
COUNT_FREE: 0
SUM_NUMBER_OF_BYTES_ALLOC: 1048576
SUM_NUMBER_OF_BYTES_FREE: 0
LOW_COUNT_USED: 0
CURRENT_COUNT_USED: 1
HIGH_COUNT_USED: 1
LOW_NUMBER_OF_BYTES_USED: 0
CURRENT_NUMBER_OF_BYTES_USED: 1048576
HIGH_NUMBER_OF_BYTES_USED: 1048576

2 rows in set (0.03 sec)
```

以上 memory/temptable/physical_disk 代表放入磁盘上的临时表计数情况。memory/temptable/physical_ram 代表放入内存的临时表计数情况。

**那总结下MySQL 8.0 引入的 TempTable 引擎：**

- 默认内部临时表引擎。
- 支持变长字符类型的实际存储。
- 设置变量 temp_table_max_ram 来控制实际存储内存区域大小。

**[tmp_table_size参数](https://www.cnblogs.com/uphold/p/11378109.html)**

1、参数查看

方法一：mysql> show variables like 'tmp_table_size';
方法二：直接查看my.cnf文件tmp_table_size参数值

2、参数配置

方法一：mysql> set global tmp_table_size=16*1024*1024; 重启后会丢失使用my.cnf参数
方法二：直接修改my.cnf文件tmp_table_size参数值，但需要重启实例生效

3、参数值意义

tmp_table_size参数配置内部内存临时表的大小。 此参数不适用用户创建的MEMORY表，用户创建的MEMORY表用max_heap_table_size参数配置。

实际限制由tmp_table_size和max_heap_table_size的值中较小的一个确定，如果内存中的临时表超出限制，MySQL自动将其转换为磁盘上的MyISAM表。如果要执行许多 GROUP BY查询，可以增加tmp_table_size的值（或如有必要，也可以使用max_heap_table_size）。

执行计划中Extra字段包含有“Using temporary” 时会产生临时表。

4、外料

MySQL中临时表主要有两类，包括外部临时表和内部临时表。外部临时表是通过语句create temporary table...创建的临时表，临时表只在本会话有效，会话断开后，临时表数据会自动清理。内部临时表主要有两类，一类是information_schema中临时表，另一类是会话执行查询时，如果执行计划中包含有“Using temporary”时，会产生临时表。内部临时表与外部临时表的一个区别在于，我们看不到内部临时表的表结构定义文件frm。而外部临时表的表定义文件frm，一般是以 `#sql{进程id}{线程id}` 序列号组成，因此不同会话可以创建同名的临时表。

临时表与普通表的主要区别在于是否在实例，会话，或语句结束后，自动清理数据。比如，内部临时表，我们在一个查询中，如果要存储中间结果集，而查询结束后，临时表就会自动回收，不会影响用户表结构和数据。另外就是，不同会话的临时表可以重名，所有多个会话执行查询时，如果要使用临时表，不会有重名的担忧。5.7引入了临时表空间后，所有临时表都存储在临时表空间（非压缩）中，临时表空间的数据可以复用。临时表并非只支持Innodb引擎，还支持myisam引擎，memory引擎等。因此，临时表我们看不到实体（idb文件），但其实不一定是内存表，也可能存储在临时表空间中。

临时表既可以是innodb引擎表，也可以是memory引擎表。这里所谓的内存表，是说memory引擎表，通过建表语句create table ...engine=memory，数据全部在内存，表结构通过frm管理，同样的内部的memory引擎表，也是看不到frm文件中，甚至看不到information_schema在磁盘上的目录。在MySQL内部，information_schema里面的临时表就包含两类：innodb引擎的临时表和memory引擎的临时表。比如 TABLES 表属于 memory 临时表，而 columns, processlist 属于 innodb 引擎临时表。内存表所有数据都在内存中，在内存中数据结构是一个数组（堆表），所有数据操作都在内存中完成，对于小数据量场景，速度比较快（不涉及物理IO操作）。但内存毕竟是有限的资源，因此，如果数据量比较大，则不适合用内存表，而是选择用磁盘临时表（innodb引擎），这种临时表采用B+树存储结构（innodb引擎），innodb的bufferpool资源是共享的，临时表的数据可能会对bufferpool的热数据有一定的影响，另外，操作可能涉及到物理IO。memory引擎表实际上也是可以创建索引的，包括Btree索引和Hash索引，所以查询速度很快，主要缺陷是内存资源有限。

5、官网信息

| Property            | Value                |
| ------------------- | -------------------- |
| Command-Line Format | --tmp-table-size=#   |
| System Variable     | tmp_table_size       |
| Scope               | Global, Session      |
| Dynamic             | Yes                  |
| Type                | integer              |
| Default Value       | 16777216             |
| Minimum Value       | 1024                 |
| Maximum Value       | 18446744073709551615 |

The maximum size of internal in-memory temporary tables. This variable does not apply to user-created MEMORY tables.

The actual limit is determined from whichever of the values of tmp_table_size and max_heap_table_size is smaller. If an in-memory temporary table exceeds the limit, MySQL automatically converts it to an on-disk MyISAM table. Increase the value of tmp_table_size (and max_heap_table_size if necessary) if you do many advanced GROUP BY queries and you have lots of memory.

You can compare the number of internal on-disk temporary tables created to the total number of internal temporary tables created by comparing the values of the Created_tmp_disk_tables and Created_tmp_tables variables.

 6、针对报错信息：Table '/mysql/data3001/tmp/#sql_ca3c_0' is marked as crashed and should be repaired



## 缓存池

参考：

1. https://www.cnblogs.com/FengGeBlog/p/10144768.html
2. https://www.cnblogs.com/chengyunblogs/p/11929040.html
3. https://blog.csdn.net/n88lpo/article/details/86486263
4. https://blog.csdn.net/weixin_33882452/article/details/94318658

![x](E:/WorkingDir/Office/Resources/tbms0029.png)

应用系统分层架构，为了加速数据访问，会把最常访问的数据，放在缓存(cache)里，避免每次都去访问数据库。操作系统，会有缓冲池(buffer pool)机制，避免每次访问磁盘，以加速数据的访问。MySQL作为一个存储系统，同样具有缓冲池(buffer pool)机制，以避免每次查询数据都进行磁盘IO，主要作用：

> 1、存在的意义是加速查询 
>
> 2、缓冲池(buffer pool) 是一种常见的**降低磁盘访问** 的机制；
>
> 3、缓冲池通常以页(page **16K**)为单位缓存数据；
>
> 4、缓冲池的常见管理算法是**LRU**，memcache，OS，InnoDB都使用了这种算法；
>
> 5、InnoDB对普通LRU进行了优化：将缓冲池分为`老生代`和`新生代`，入缓冲池的页，优先进入老生代，该页被访问，才进入新生代，以解决预读失效的问题页被访问。且在老生代**停留时间超过配置阈值**的，才进入新生代，以解决批量数据访问，大量热数据淘汰的问题

**预读失效**：

> 由于预读(Read-Ahead)，提前把页放入了缓冲池，但最终MySQL并没有从页中读取数据，称为预读失效

![x](E:/WorkingDir/Office/Resources/tbms0030.png)

**缓冲池污染**：

> 当某一个SQL语句，要批量扫描大量数据时，可能导致把缓冲池的所有页都替换出去，导致大量热数据被换出，MySQL性能急剧下降，这种情况叫缓冲池污染。解决办法：加入`老生代停留时间窗口`策略后，短时间内被大量加载的页，并不会立刻插入新生代头部，而是优先淘汰那些，短期内仅仅访问了一次的页。





### 筛选

WHERE、HAVING同时出现时，MySQL优先执行WHERE字句，HAVING对WHERE结果做进一步筛选。HAVING字句不容易优化，但是可以在GROUP BY查询中用作数学统计(SUM, MAX, MIN …)

MySQL不允许在WHERE后面使用假名

### 分页

`LAST_INSERT_ID()` 函数返回 MySql 为上一条 `INSERT` 命令生成的 `AUTO_INCREMENT` 值。

它只对本次连接有效，所以不存在并发问题，但是它与表无关，只要有 INSERT 操作，`AUTO_INCREMENT` 就有可能改变。

如果 INSERT 使用单条语句插入多条新纪录的语法，它获取的是第一条数据的id。



### 排序

排序规则：列->表->库->列字符集默认排序



## table瘦身

**空洞**：

> MySQL执行`delete`命令其实只是把记录的位置，或者数据页标记为了`可复用`，但磁盘文件的大小是不会变的。通过delete命令是不能回收表空间的。这些可以复用，而没有被使用的空间，看起来就像是`空洞`。插入时候引发分裂同样会产生空洞。

**重建表思路**：

> 1、新建一个跟A表结构相同的表B 
>
> 2、按照主键ID将A数据一行行读取同步到表B 
>
> 3、用表B替换表A实现效果上的瘦身。

**重建表指令**：

> 1、alter table A engine=InnoDB，慎重用，牛逼的DBA都用下面的开源工具。
>
> 2、推荐Github：gh-ost



## SQL&nbsp;Joins、统计、随机查询

7种join具体如下：

![x](./Resources/db003.jpg)

**统计**：

> 1、MyISAM模式下把一个表的总行数存在了磁盘上，直接拿来用即可 
>
> 2、InnoDB引擎由于 MVCC 的原因，需要把数据读出来然后累计求和 
>
> 3、性能来说 由好到坏：count(字段) < count(主键id) < count(1) ≈ count(*)，`尽量用count(*)。`

**随机查询**：

```
mysql> select word from words order by rand() limit 3;
```

直接使用`order by rand()`，[explain](https://mp.weixin.qq.com/s?__biz=MzI4NjI1OTI4Nw==&mid=2247488546&idx=1&sn=732ca84abf572196ddf76597fe096969&scene=21#wechat_redirect) 这个语句发现需要 `Using temporary`和 `Using filesort`，查询的执行代价往往是比较大的。所以在设计的时要避开这种写法。

```
mysql> select count(*) into @C from t;
set @Y1 = floor(@C * rand());
set @Y2 = floor(@C * rand());
set @Y3 = floor(@C * rand());
select * from t limit @Y1,1; 
select * from t limit @Y2,1;
select * from t limit @Y3,1;
```

这样可以避免临时表跟排序的产生，最终查询行数 = C + (Y1+1) + (Y2+1) + (Y3+1)

**exist 和 in 对比**：

> 1、in查询时首先查询子查询的表，然后将内表和外表做一个`笛卡尔积`，然后按照条件进行筛选。
>
> 2、子查询使用 exists，会先进行主查询，将查询到的每行数据`循环带入`子查询校验是否存在，过滤出整体的返回数据。
>
> 3、两表大小相当，in 和 exists 差别不大。`内表大，用 exists 效率较高；内表小，用 in 效率较高`。
>
> 4、查询用not in 那么内外表都进行全表扫描，没有用到索引；而not extsts 的子查询依然能用到表上的索引。`not exists都比not in要快`。



## 常用命令语句



```sh
# 导出数据库
mysqldump -uroot -pleadchina30045016747 --databases dbName --no-tablespaces --no-create-db --no-create-info --skip-triggers --skip-add-locks --flush-privileges > src.sql
# 导入数据库
mysql -uroot -p123 < master.sql
```





#### 控制外键约束

```sql
-- 禁用
SET FOREIGN_KEY_CHECKS = 0;
-- 启用
SET FOREIGN_KEY_CHECKS = 1;
-- 查看当前值
SELECT @@FOREIGN_KEY_CHECKS;
```

#### 控制安全模式

```sql
show variables like 'sql_safe_updates';
set sql_safe_updates=1; --安全模式打开状态
set sql_safe_updates=0; --安全模式关闭状态
```



### 常用SQL语句



#### 系统配置语句

```sql
-- 安全模式
show variables like 'sql_safe_updates';
set sql_safe_updates=1; --安全模式打开状态
set sql_safe_updates=0; --安全模式关闭状态

-- 改变数据表的类型（MyISAM、InnoDB）：
ALTER TABLE tbName ENGINE = tpName;
show engines;
show table status from db_name where name='table_name';
show create table table_name;
-- 如果 `MyISAM` 数据表包含全文索引或地理数据，转换不能成功（`InnoDB` 不支持这些功能）。
-- 如果对大量数据表进行转换，unix/Linux下的 `mysql_convert_table_format` 脚本很值得选用：
-- 如果tbname没有指定，会转换所有数据表
-- mysql数据库中的表类型都是`MyISAM`，保存着内部管理信息，千万不能转换！
mysql_convert_table_format [opt] –type=InnoDB dbname [tbname]

SHOW CHARACTER SET -- 查看一个给定字符集的默认排序方式
SHOW COLLATION -- 查看所有字符集
-- 临时改变排序方式
SELECT LoginName FROM t_user ORDER BY LoginName COLLATE utf8_unicode_ci
-- 永久改变排序方式
ALTER TABLE t_user MODIFY LoginName VARCHAR(20)
  CHARACTER SET utf8 COLLATE utf8_unicode_ci
-- 临时改变字符集及排序方式（无法用索引，转换慢，查询慢）
SELECT LoginName FROM t_user
  ORDER BY CONVERT(LoginName USING latin1) COLLATE latin1_swedish_ci
```



#### 对象查看语句

```sql
show databases;
desc tableName; -- 查看表结构
show tables from dbName;
show columns from tableName; -- 查看表中的列
show index from tableName; -- 查询索引
show create proc[edure] procName; -- 查看创建存储过程信息
show procedure status;
show function status;
show profiles
```

https://dev.mysql.com/doc/relnotes/connector-j/8.0/en/news-8-0-19.html

MySQL Server 8.0.17 deprecated the display width for the TINYINT, SMALLINT, MEDIUMINT, INT, and BIGINT data types when the ZEROFILL modifier is not used, and MySQL Server 8.0.19 has removed the display width for those data types from results of SHOW CREATE TABLE, SHOW CREATE FUNCTION, and queries on INFORMATION_SCHEMA.COLUMNS, INFORMATION_SCHEMA.ROUTINES, and INFORMATION_SCHEMA.PARAMETERS (except for the display width for signed TINYINT(1)). This patch adjusts Connector/J to those recent changes of MySQL Server and, as a result, DatabaseMetaData, ParameterMetaData, and ResultSetMetaData now report identical results for all the above-mentioned integer types and also for the FLOAT and DOUBLE data types. (Bug #30477722)

***从8.0.17版本开始，TINYINT, SMALLINT, MEDIUMINT, INT, and BIGINT类型的显示宽度将失效。***



#### 对象操作语句

```sql
-- 查看现有索引：
-- 命令行（报错？）
SHOW INDEX FROM tablename
-- 查看数据库所有索引
SELECT * FROM mysql.`innodb_index_stats` a WHERE a.`database_name` = '数据库名';
-- 查看某一表索引
SELECT * FROM mysql.`innodb_index_stats` a WHERE a.`database_name` = '数据库名' and a.table_name like '%表名%';
-- 删除索引：
DROP INDEX indexname ON tablename
-- 增加一个索引：
-- ALTER TABLE可用于创建普通索引、UNIQUE索引和PRIMARY KEY等
-- 索引名index_name可选，缺省时，MySQL将根据第一个索引列赋一个名称。
-- 另外，ALTER TABLE允许在单个语句中更改多个表，因此可以同时创建多个索引。
ALTER TABLE tblname ADD PRIMARY KEY (indexcols …)
ALTER TABLE tblname ADD INDEX [indexname] (indexcols …)
ALTER TABLE tblname ADD UNIQUE [indexname] (indexcols …)
ALTER TABLE tblname ADD FULLTEXT [indexname] (indexcols …)
-- 只对被索引字段的前16个字符进行索引：
ALTER TABLE titles ADD INDEX idxtitle (title(16))
-- CREATE INDEX可用于对表增加普通索引或UNIQUE索引，可用于建表时创建索引。
CREATE INDEX index_name ON table_name (column_list)
CREATE UNIQUE INDEX index_name ON table_name (column_list)
-- table_name、index_name和column_list具有与ALTER TABLE语句中相同的含义，索引名不可选。
-- 另外，不能用CREATE INDEX语句创建PRIMARY KEY索引。
-- 删除一个索引：
ALTER TABLE tblname  DROP PRIMARY KEY
ALTER TABLE tblname  DROP INDEX indexname
ALTER TABLE tblname  DROP FOREIGN KEY indexname

-- 增加一个数据列：
ALTER TABLE tblname ADD newcolumn coltype coloptions [FIRST|AFTER existingcolumn]
-- 修改一个数据列：
ALTER TABLE tblname CHANGE oldcolumn newcolumn coltype coloptions
-- 删除一个数据列：
ALTER TABLE tblname DROP column

-- 1. 复制表结构及其数据：
create table table_name_new as select * from table_name_old
-- 2. 只复制表结构：
create table table_name_new as select * from table_name_old where 1=2;
-- 或者：
create table table_name_new like table_name_old
-- 3. 只复制表数据：
--   3.1 如果两个表结构一样：
insert into table_name_new select * from table_name_old
--   3.2 如果两个表结构不一样：
insert into table_name_new(column1,column2...) select column1,column2... from table_name_old

-- 改变全体文本数据列上的字符集：
ALTER TABLE tblname CONVERT TO CHARACTER SET charsetname

/*
改变数据表的类型（MyISAM、InnoDB）：
  1. 如果 `MyISAM` 数据表包含全文索引或地理数据，转换不能成功（`InnoDB` 不支持这些功能）。
  2. 如果对大量数据表进行转换，unix/Linux下的 `mysql_convert_table_format` 脚本很值得选用（如果tbname没有指定，会转换所有数据表）
  3. mysql数据库中的表类型都是`MyISAM`，保存着内部管理信息，千万不能转换！
*/
ALTER TABLE tblname ENGINE typename
mysql_convert_table_format [opt] –type=InnoDB dbname [tbname]
```



#### 字符串函数

```sql
-- ","分割
SELECT SUBSTRING_INDEX(SUBSTRING_INDEX('10321,30001',',',help_topic_id+1),',',-1) AS num 
FROM mysql.help_topic 
WHERE help_topic_id < LENGTH('10321,30001')-LENGTH(REPLACE('10321,30001',',',''))+1;

-- "|"分割
SELECT SUBSTRING_INDEX(SUBSTRING_INDEX('10321|30001','|',help_topic_id+1),'|',-1) AS num 
FROM mysql.help_topic 
WHERE help_topic_id < LENGTH('10321|30001')-LENGTH(REPLACE('10321|30001','|',''))+1;
```



#### 日期时间函数

```sql
-- 1. 获得当前日期时间
now(), sysdate()
-- now() 在执行开始时值就得到了， sysdate() 在函数执行时动态得到值
select now(), sleep(3), now();
current_timestamp, current_timestamp()

-- 2. 日期转换函数、时间转换函数
select date_format('2008-08-08 22:23:01', '%Y%m%d%H%i%s');
select time_format('22:23:01', '%H%i%s');
select str_to_date('08/09/2008', '%m/%d/%Y'); -- 2008-08-09
select str_to_date('08/09/08' , '%m/%d/%y'); -- 2008-08-09
select str_to_date('08.09.2008', '%m.%d.%Y'); -- 2008-08-09
select str_to_date('08:09:30', '%h:%i:%s'); -- 08:09:30
select str_to_date('08.09.2008 08:09:30', '%m.%d.%Y %h:%i:%s'); -- 2008-08-09 08:09:30
select to_days('0000-00-00'); -- 0
select to_days('2008-08-08'); -- 733627
SELECT FROM_DAYS(733627); -- 2008-08-08
select time_to_sec('01:00:05'); -- 3605
select sec_to_time(3605); -- '01:00:05'
-- makdedate(year,dayofyear)
select makedate(2001,31); -- '2001-01-31'
select makedate(2001,32); -- '2001-02-01'
-- maketime(hour,minute,second)
select maketime(12,15,30); -- '12:15:30'
select unix_timestamp(); -- 1218290027
select unix_timestamp('2008-08-08'); -- 1218124800
select unix_timestamp('2008-08-08 12:30:00'); -- 1218169800
select from_unixtime(1218290027); -- '2008-08-09 21:53:47'
select from_unixtime(1218124800); -- '2008-08-08 00:00:00'
select from_unixtime(1218169800); -- '2008-08-08 12:30:00'
select from_unixtime(1218169800, '%Y %D %M %h:%i:%s %x'); -- '2008 8th August 12:30:00 2008'

-- 3. 日期时间计算函数
select date_add(now(), interval 1 day); -- add 1 day
select date_add(now(), interval 1 hour); -- add 1 hour
select date_add(now(), interval 1 minute); -- ...
select date_add(now(), interval 1 second);
select date_add(now(), interval 1 microsecond);
select date_add(now(), interval 1 week);
select date_add(now(), interval 1 month);
select date_add(now(), interval 1 quarter);
select date_add(now(), interval 1 year);
select date_add(now(), interval -1 day); -- sub 1 day
select date_sub('1998-01-01 00:00:00', interval '1 1:1:1' day_second); -- 1997-12-30 22:58:59
select datediff('2008-08-08', '2008-08-01'); -- 7
select datediff('2008-08-01', '2008-08-08'); -- -7
select timediff('2008-08-08 08:08:08', '2008-08-08 00:00:00'); -- 08:08:08
select timediff('08:08:08', '00:00:00'); -- 08:08:08
select timestamp('2008-08-08'); -- 2008-08-08 00:00:00
select timestamp('2008-08-08 08:00:00', '01:01:01'); -- 2008-08-08 09:01:01
select timestamp('2008-08-08 08:00:00', '10 01:01:01'); -- 2008-08-18 09:01:01
select timestampadd(day, 1, '2008-08-08 08:00:00'); -- 2008-08-09 08:00:00
select timestampdiff(year,'2002-05-01','2001-01-01'); -- -1
select timestampdiff(day ,'2002-05-01','2001-01-01'); -- -485
select timestampdiff(hour,'2008-08-08 12:00:00','2008-08-08 00:00:00'); -- -12
-- 时区转换：convert_tz(dt,from_tz,to_tz)
select convert_tz('2008-08-08 12:00:00', '+08:00', '+00:00'); -- 2008-08-08 04:00:00
```



#### 统计函数

**concat()**

- 功能：将多个字符串连接成一个字符串。

- 语法：`concat(str1, str2,...)`

返回结果为连接参数产生的字符串，如果有任何一个参数为null，则返回值为null。

**concat_ws()**

- 功能：和concat() 一样，将多个字符串连接成一个字符串，但是可以一次性指定分隔符（concat_ws就是concat with separator）

- 语法：`concat_ws(separator, str1, str2, ...)`

  第一个参数指定分隔符。需要注意的是分隔符不能为null，如果为null，则返回结果为null

 **group_concat()**

- 功能：将group by产生的同一个分组中的值连接起来，返回一个字符串结果

- 语法：`group_concat([distinct] 要连接的字段 [order by 排序字段 asc/desc] [separator '分隔符'])`

  通过使用distinct可以排除重复值；如果希望对结果中的值进行排序，可以使用order by子句；separator是一个字符串值，缺省为一个逗号。

 **WITH ROLLUP**

- 功能：加在 GROUP BY … 语句之后，增加统计记录



#### JSON函数

```sql
-- 创建json
json_array([val[, val] ...])  -- 创建json数组 
json_object([key, val[, key, val] ...])  -- 创建json对象
json_quote  -- 将json转成json字符串类型

-- 查询json 
json_contains(json_doc, val[, path])  -- 判断是否包含某个json值
json_contains_path(json_doc, 'one|all', path[, path] ...)  -- 判断某个路径下是否包json值

-- 提取json值
json_extract(json_doc, path[, path] ...)
column->path    -- json_extract的简洁写法，MySQL 5.7.9开始支持
column->>path   -- json_unquote(column -> path)的简洁写法
json_keys(json_doc[, path])  -- 提取json中的键值为json数组
json_search(json_doc, 'one|all', search_str[, escape_char[, path] ...])  -- 按给定字符串关键字搜索json，返回匹配的路径

-- 修改json 
json_append -- 废弃，MySQL 5.7.9开始改名为json_array_append
json_array_append(json_doc, path, val[, path, val] ...) -- 末尾添加数组元素，如果原有值是数值或json对象，则转成数组后，再添加元素
json_array_insert(json_doc, path, val[, path, val] ...) -- 插入数组元素
json_insert(json_doc, path, val[, path, val] ...) -- 插入值（插入新值，但不替换已经存在的旧值）
json_merge(json_doc, json_doc[, json_doc] ...) -- 合并json数组或对象
json_remove(json_doc, path[, path] ...) -- 删除json数据
json_replace(json_doc, path, val[, path, val] ...) -- 替换值（只替换已经存在的旧值）
json_set(json_doc, path, val[, path, val] ...) -- 设置值（替换旧值，并插入不存在的新值）
json_unquote -- 去除json字符串的引号，将值转成string类型

-- 返回json属性 
json_depth(json_doc) -- 返回json文档的最大深度
json_length(json_doc[, path]) -- 返回json文档的长度
json_type -- 返回json值得类型
json_valid -- 判断是否为合法json文档

-- 示例
insert into t values(5,JSON_Object('key1',v1,'key2',v2));
insert into t values(4,JSON_Array(v1,v2,v3));
update t set js = json_set('{"a":1,"s":"abc"}','$.a',456,'$.b','bbb') where id = 1;
-- 结果js={"a":456,"s":"abc","b":"bbb"}
```



#### 其它SQL

```sql
-- 编辑排序清单里的数据记录
-- 语法：`UPDATE…ORDER BY…LIMIT`
-- 示例：
UPDATE tablename SET mydata = 0 ORDER BY name LIMIT 10;

-- 更新关联数据表里的数据记录
-- 示例：
UPDATE table1,table2 SET table1.columnA = table2.columnB
WHERE table1.keyID = table2.keyID;

-- 删除排序清单里的数据记录
-- 语法：`DELTE…ORDER BY…LIMIT`
-- 示例：
DELETE FROM table ORDER BY column LIMIT 1;

-- 删除关联数据表里的数据记录
DELETE t1,t2 FROM t1,t2,t3 WHERE condition1 AND condition2 …;
-- DELETE命令只从FROM关键字前的table中删除数据。
-- 数据表之间的关联关系也可以用JOIN操作符来建立。
-- 如果要删除的数据列上有外键约束，可以有如下解决方法：
-- 1.暂时关闭外键约束检查机制
SET foreign_key_check = 0 -- 关闭
SET foreign_key_check = 1 -- 开启
-- 2. 定义外键约束时加上ON DELETE CASCADE选项，级联删除。但是有可能删除掉其它数据表里仍需使用的数据。
-- 有时候，彻底抛弃外键或使用MyISAM数据表（不支持数据一致性规则）


-- 分页
/**
 * 获取门店列表
 */
CREATE PROCEDURE sp_get_shops_by_project(
    searchType INT,
    projId INT,
    userId INT,
    userType INT,
    pageIndex INT,
    pageSize INT,
    searchCondition VARCHAR(60)
)
BEGIN
DECLARE periodId INT;
DECLARE mbdName VARCHAR(60);
-- 获取总期数
-- SELECT COUNT(id) INTO totalRounds FROM t_period_master WHERE ProjectId = projId AND Preview = 0 AND has_data = 1 AND has_users = 1;
IF userType = 0 THEN -- 普通外部用户，需要根据mbd权限查看门店
    SELECT IFNULL(u.period_id, 0) into periodId
    FROM t_user u
    WHERE u.id = userId;

    -- 下面的句式不能同时给多个参数赋值
    SELECT IFNULL(um.mbd_name, '') into mbdName
    FROM t_user u
    INNER JOIN t_user_mbd um ON u.id = um.user_id
    WHERE u.id = userId;

    IF searchCondition != '' THEN
        SET @searchTreeNodeAttrs = '';

        CALL sp_query_tree_nodes(
            searchCondition, 't_mbd_master', 'mbd_name', 'parent_name', 'id',
            CONCAT(' and project_id = ', projId, ' and period_id = ', periodId),
            1, 0, @searchTreeNodeAttrs
        );

        IF @searchTreeNodeAttrs IS NOT NULL AND @searchTreeNodeAttrs != '' THEN
            SET @dynamicWhere = CONCAT(' AND (m.mbd_name LIKE ''%',
            searchCondition, '%'' OR m.city LIKE ''%', searchCondition,
            '%'' OR m.mbd_code LIKE ''%', searchCondition,
            '%'' OR m.id IN (', @searchTreeNodeAttrs, '))');
        ELSE
            SET @dynamicWhere = CONCAT(' AND (m.mbd_name LIKE ''%',
            searchCondition, '%'' OR m.city LIKE ''%', searchCondition,
            '%'' OR m.mbd_code LIKE ''%', searchCondition,
            '%'')');
        END IF;
    ELSE
        SET @dynamicWhere = '';
    END IF;

    SET @treeNodeAttrs = '';

    CALL sp_query_tree_nodes(mbdName, 't_mbd_master', 'mbd_name',
    'parent_name', 'id',
    CONCAT(' and project_id = ', projId, ' and period_id = ', periodId),
    0, 0, @treeNodeAttrs);

    IF @treeNodeAttrs IS NOT NULL AND @treeNodeAttrs != '' THEN
        SET @mbdQuery = CONCAT(' AND m.id IN (', @treeNodeAttrs, ')');
    ELSE
        SET @mbdQuery = '';
    END IF;

    IF searchType = 1 THEN
        SET @sql = CONCAT(
        'SELECT COUNT(m.mbd_code)',
        -- INTO @totalShops',
        ' FROM t_mbd_master m',
        ' LEFT JOIN t_cubedata_01 c ON m.mbd_name = c.mbd_name AND m.project_id = c.project_id AND m.period_id = c.period_id',
        ' WHERE m.level = 2 AND m.project_id = ', projId,
        ' AND c.fact_name = ''平均分''',
        ' AND m.period_id = ', periodId,
        @mbdQuery, @dynamicWhere
    );
    PREPARE tempQuery FROM @sql;
    EXECUTE tempQuery;
    DEALLOCATE PREPARE tempQuery;
    ELSE -- SET totalShops = @totalShops;
        SET @sql = CONCAT(
        'SELECT m.mbd_code mbdCode, m.mbd_name mbdName, m.mbd_title mbdTitle, c.fact_value factValue',
        ' FROM t_mbd_master m',
        ' LEFT JOIN t_cubedata_01 c ON m.mbd_name = c.mbd_name AND m.project_id = c.project_id AND m.period_id = c.period_id',
        ' WHERE m.level = 2 AND m.project_id = ', projId,
        ' AND c.fact_name = ''平均分''',
        ' AND m.period_id = ', periodId,
        @mbdQuery, @dynamicWhere,
        ' ORDER BY m.period_id, m.id LIMIT ', pageSize, ' OFFSET ', pageIndex
        );
        PREPARE tempQuery FROM @sql;
        EXECUTE tempQuery;
        DEALLOCATE PREPARE tempQuery;
    END IF;
ELSE -- 超级用户
    SELECT IFNULL(id, 0) INTO periodId
    FROM t_period_master
    WHERE ProjectId = projId
    AND Preview <> 1
    AND has_users = 1
    ORDER BY update_time DESC
    LIMIT 1;

    IF searchCondition != '' THEN
        SET @searchTreeNodeAttrs = '';

CALL sp_query_tree_nodes(
    searchCondition,
    't_mbd_master',
    'mbd_name',
    'parent_name',
    'id',
    CONCAT(
        ' and project_id = ',
        projId,
        ' and period_id = ',
        periodId
    ),
    1,
    0,
    @searchTreeNodeAttrs
);

IF @searchTreeNodeAttrs IS NOT NULL
AND @searchTreeNodeAttrs != '' THEN
SET
    @dynamicWhere = CONCAT(
        ' AND (m.mbd_name LIKE ''%',
        searchCondition,
        '%'' OR m.city LIKE ''%',
        searchCondition,
        '%'' OR m.mbd_code LIKE ''%',
        searchCondition,
        '%'' OR m.id IN (',
        @searchTreeNodeAttrs,
        '))'
    );

ELSE
SET
    @dynamicWhere = CONCAT(
        ' AND (m.mbd_name LIKE ''%',
        searchCondition,
        '%'' OR m.city LIKE ''%',
        searchCondition,
        '%'' OR m.mbd_code LIKE ''%',
        searchCondition,
        '%'')'
    );

END IF;

ELSE
SET
    @dynamicWhere = '';

END IF;

IF searchType = 1 THEN
SET
    @sql = CONCAT(
        'SELECT COUNT(m.mbd_code)',
        -- INTO @totalShops',
        ' FROM t_mbd_master m',
        ' LEFT JOIN t_cubedata_01 c ON m.mbd_name = c.mbd_name AND m.project_id = c.project_id AND m.period_id = c.period_id',
        ' WHERE m.level = 2 AND m.project_id = ',
        projId,
        ' AND c.fact_name = ''平均分''',
        ' AND m.period_id = ',
        periodId,
        @dynamicWhere
    );

PREPARE tempQuery
FROM
    @sql;

EXECUTE tempQuery;

DEALLOCATE PREPARE tempQuery;

ELSE -- SET totalShops = @totalShops;
SET
    @sql = CONCAT(
        'SELECT m.mbd_code mbdCode, m.mbd_name mbdName, m.mbd_title mbdTitle, c.fact_value factValue',
        ' FROM t_mbd_master m',
        ' LEFT JOIN t_cubedata_01 c ON m.mbd_name = c.mbd_name AND m.project_id = c.project_id AND m.period_id = c.period_id',
        ' WHERE m.level = 2 AND m.project_id = ',
        projId,
        ' AND c.fact_name = ''平均分''',
        ' AND m.period_id = ',
        periodId,
        @dynamicWhere,
        ' ORDER BY m.period_id, m.id LIMIT ',
        pageSize,
        ' OFFSET ',
        pageIndex
    );

PREPARE tempQuery
FROM
    @sql;

    EXECUTE tempQuery;
    DEALLOCATE PREPARE tempQuery;
    END IF;
END IF;
END
-- 调用
CALL sp_get_shops_by_project(1, 1, 1, 0, 0, 900, '');

-- 临时表
/**
 * 查询单店报表数据，使用中
 */
CREATE PROCEDURE sp_get_stores_list(
  columnConfig VARCHAR(2000), -- 查询字段
  whereCondition VARCHAR(2000), -- 查询条件
  orderCondition VARCHAR(50), projectCode VARCHAR(50),
  pageSize INT, -- pageSize为0时，不分页，供导出使用
  startIndex INT
)
BEGIN
DECLARE confirmFields VARCHAR(200);
DECLARE pageQuery VARCHAR(200);

SET confirmFields = '';
SET pageQuery = '';
SET @sql = CONCAT(
  'CREATE TEMPORARY TABLE tmp_CanShowComplainDays',
  ' SELECT MIN(sd.Date_Code) DateCode, sd.DataRound DataRoundCode FROM ',
  '(SELECT Date_Code, DataRound FROM ', projectCode,
  '_t_storedata GROUP BY DataRound, Date_Code) sd',
  ' LEFT JOIN t_disputeconfig dc ON sd.DataRound = dc.DataRoundCode ',
  'AND dc.ProjectCode = ''', projectCode,
  ''' WHERE TIMESTAMPDIFF(DAY, sd.Date_Code, CURDATE()) <= ',
  'dc.CanShowComplainDays - 1 + (SELECT COUNT(*) FROM t_holidays',
  'WHERE sd.Date_Code <= holidays AND CURDATE() >= holidays ',
  'AND years = YEAR(CURDATE())) GROUP BY sd.DataRound'
);
PREPARE tmpData FROM @sql;
DROP TABLE IF EXISTS tmp_CanShowComplainDays;
EXECUTE tmpData;
DEALLOCATE PREPARE tmpData;
SET @sql = CONCAT(
  'CREATE TEMPORARY TABLE tmp_ComplainDays',
  ' SELECT MIN(sd.Date_Code) DateCode, sd.DataRound DataRoundCode FROM (',
  'SELECT Date_Code, DataRound FROM ', projectCode,
  '_t_storedata GROUP BY DataRound, Date_Code) sd',
  ' LEFT JOIN t_disputeconfig dc ON sd.DataRound = dc.DataRoundCode ',
  'AND dc.ProjectCode = ''', projectCode,
  ''' WHERE TIMESTAMPDIFF(DAY, sd.Date_Code, CURDATE()) <= ',
  'dc.ComplainDays - 1 + (SELECT COUNT(*) FROM t_holidays ',
  'WHERE sd.Date_Code <= holidays AND CURDATE() >= holidays ',
  'AND years = YEAR(CURDATE())) GROUP BY sd.DataRound'
);
PREPARE tmpData FROM @sql;
DROP TABLE IF EXISTS tmp_ComplainDays;
EXECUTE tmpData;
DEALLOCATE PREPARE tmpData;

IF pageSize != 0 THEN
  SET confirmFields = ' DATE_FORMAT(sd.Date_Code, ''%Y-%m-%d'') 上传时间, sm.客户号 客户编号, sd.DataRound 轮次, sm.客户标准名称 客户名称, (CASE WHEN sd.Date_Code >= tc.DateCode THEN 0 ELSE 1 END) 能否申诉';
  SET pageQuery = CONCAT(' LIMIT ', pageSize, ' OFFSET ', startIndex);
  IF columnConfig != '' AND columnConfig IS NOT NULL THEN
    SET columnConfig = CONCAT(columnConfig, ',');
  END IF;
END IF;

SET @sql = CONCAT(
  'SELECT ', columnConfig, confirmFields, ' FROM ', ProjectCode,
  '_t_storedata sd', ' INNER JOIN ', ProjectCode,
  '_t_storemaster sm ON sm.客户号 = sd.Store_Code',
  ' LEFT JOIN tmp_CanShowComplainDays ts ON sd.DataRound = ts.DataRoundCode',
  ' LEFT JOIN tmp_ComplainDays tc ON sd.DataRound = tc.DataRoundCode',
  whereCondition, ' AND sd.Date_Code >= ts.DateCode',
  ' AND NOT EXISTS(SELECT Store_Code FROM t_storecomplain WHERE Project_Code = ''', ProjectCode,
  ''' AND DateRound = sd.DataRound AND Store_Code = sd.Store_Code)',
  orderCondition, pageQuery);
PREPARE tmpData FROM @sql;
EXECUTE tmpData;
DEALLOCATE PREPARE tmpData;

END;
--
CALL sp_get_stores_list(
    '客户号,客户标准名称,客户简称,客户总部名称,地址,周围标志性建筑物,联络人,电话,全国,渠道类型,DSR_PSR_DWR,客户性质,客户级别,直辖市,城市代码,地级市,县级市,办事处,OTC总部,OTC_CODE,大区总监,大区总监编号,本级岗位_大区总监,大区总监负责人,MUDID_2,大区总监负责人MUDID,大区,大区编号,本级岗位_大区,大区负责人,MUDID_3,大区MUDID,所属团队,所属团队编号,本级岗位_团队代表,所属团队代表,MUDID_4,所属团队代表MUDID,销售代表,销售代表编号,本级岗位_销售代表,MUDID_5,销售代表MUDID,地区,工作地,报备,OTC_001,OTC_002,OTC_003,OTC_004,OTC_005,OTC_006,OTC_007,OTC_008,OTC_009,OTC_010,OTC_011,OTC_012',
    ' where 1 = 1 and ((全国 = ''全国''))',
    ' order by 客户号 ASC',
    'p01',
    10,
    0
)

-- 字符串转成行
/**
 * 字符串转换成数组行
 */
CREATE PROCEDURE sp_str_transform_rows(
    toSplitString Text,
    splitChar VARCHAR(2)
) BEGIN -- DROP TABLE IF EXISTS tmp_filter;  
CREATE TEMPORARY TABLE tmp_filter(splitString VARCHAR(200));

SET
    @splitValue = toSplitString;

SET
    @counts = LENGTH(toSplitString) - LENGTH(REPLACE(toSplitString, splitChar, ''));

-- SELECT @counts;
SET
    @i = 1;

WHILE @i <= @counts DO
INSERT INTO
    tmp_filter
VALUES
    (SUBSTRING_INDEX(@splitValue, splitChar, 1));

SET
    @splitValue = SUBSTRING_INDEX(@splitValue, splitChar, @i - @counts -1);

SET
    @i = @i + 1;

END WHILE;

INSERT INTO
    tmp_filter
values
    (@splitValue);

SELECT
    *
FROM
    tmp_filter;

DROP TABLE tmp_filter;

END CALL sp_str_transform_rows('1,2,3', ',') -- 分支
/**
 * App用户登录，变量和参数同名有影响
 */
CREATE PROCEDURE sp_login_by_app_user(
    userName VARCHAR(255),
    `passwords` VARCHAR(255),
    `language` VARCHAR(2)
) BEGIN DECLARE user_id INT DEFAULT 0;

-- 默认用户id为0
DECLARE newUserId INT DEFAULT 0;

-- 获取最新轮次的用户
SELECT
    IFNULL(id, 0) into newUserId
FROM
    t_user
WHERE
    `NAME` = userName
ORDER BY
    period_id DESC
LIMIT
    1;

SELECT
    IFNULL(id, 0) into user_id
FROM
    t_user
WHERE
    id = newUserId
    AND `PASSWORD` = `passwords`;

IF user_id = 0 THEN
SELECT
    IFNULL(id, 0) INTO user_id
FROM
    t_manager
WHERE
    `NAME` = userName
    AND `PASSWORD` = `passwords`
    AND `status` = 1
    AND role = 2
LIMIT
    1;

IF user_id > 0 THEN
SELECT
    id,
    `name`,
    '' AS roundId,
    '' AS mbdName,
    '' AS customName,
    'SuperUser' AS role,
    token
FROM
    t_manager
WHERE
    id = user_id;

END IF;

ELSE
SELECT
    u.id,
    u.`name`,
    u.period_id AS roundId,
    um.mbd_name mbdName,
    (
        CASE
            `language`
            WHEN 'en' THEN c.e_name
            ELSE c.c_name
        END
    ) AS customName,
    'ClientUser' AS role,
    u.token
FROM
    t_user u
    LEFT JOIN t_user_mbd um ON u.id = um.user_id
    LEFT JOIN t_project p ON u.project_id = p.id
    LEFT JOIN t_customer c ON p.customer_id = c.id
WHERE
    u.id = user_id;

END IF;

END

-- 循环

/**
 * 获取时间段内的假期天数
 */
CREATE FUNCTION GetHolidaysCount(
  holidays VARCHAR(2000),
  beginDate datetime,
  endDate datetime
) RETURNS int BEGIN DECLARE beginDateValue DOUBLE;

DECLARE endDateValue DOUBLE;
DECLARE holiday DOUBLE;
DECLARE counts INT;
DECLARE itemIndex int;

SET counts = 0;
SET beginDateValue = DATE_FORMAT(beginDate, '%m.%d') - 0.00;
SET endDateValue = DATE_FORMAT(endDate, '%m.%d') - 0.00;
SET itemIndex = INSTR(holidays, ',');

WHILE itemIndex > 0 DO
  SET holiday = LEFT(holidays, itemIndex - 1) - 0.00;
  SET holidays = SUBSTRING(holidays FROM itemIndex + 1);
  SET itemIndex = INSTR(holidays, ',');
  IF holiday >= beginDateValue AND holiday <= endDateValue THEN
    SET counts = counts + 1;
  END IF;
END WHILE;

IF holidays >= beginDateValue AND holidays <= endDateValue THEN
  SET counts = counts + 1;
END IF;

RETURN counts;
END

-- 游标

/**
 * 查询单店报表数据
 */
CREATE PROCEDURE GetStoreTable(
    masterColumnQuery VARCHAR(2000),
    dataColumnQuery VARCHAR(2000),
    masterWhereCondition VARCHAR(2000),
    dataWhereCondition VARCHAR(2000),
    orderCondition VARCHAR(50),
    ProjectCode VARCHAR(50),
    PageSize INT,
    StartIndex INT,
    OUT totalCount INT
) BEGIN DECLARE t_beginDate DATE;

DECLARE t_dateround VARCHAR(50);

DECLARE t_storecode VARCHAR(50) DEFAULT '';

DECLARE maxCnt INT DEFAULT 0;
DECLARE i INT DEFAULT 0;

DECLARE cursorDone INT DEFAULT 0;

DECLARE cur CURSOR FOR
SELECT
    MIN(ts.Date_Code),
    ts.DataRound
FROM
    tmp_DataRound ts
    LEFT JOIN t_disputeconfig dc ON ts.DataRound = dc.DataRoundCode
    AND dc.ProjectCode = ProjectCode
WHERE
    TIMESTAMPDIFF(DAY, ts.Date_Code, curdate()) <= dc.CanShowComplainDays - 1 + (
        SELECT
            count(*)
        FROM
            t_holidays
        WHERE
            ts.Date_Code <= holidays
            AND CURDATE() >= holidays
            AND years = YEAR(CURDATE())
    )
GROUP BY
    ts.DataRound;

DECLARE curRound CURSOR FOR
SELECT
    DISTINCT DateRound
FROM
    t_storecomplain
WHERE
    Project_Code = ProjectCode;

DECLARE CONTINUE HANDLER FOR SQLSTATE '02000'
SET
    cursorDone = 1;

-- 单店Master表处理
SET
    @sql = CONCAT(
        'CREATE TEMPORARY TABLE tmp_MasterTable SELECT ',
        masterColumnQuery,
        ' 客户号 客户编号 FROM ',
        ProjectCode,
        '_t_storemaster ',
        masterWhereCondition
    );

PREPARE storeMaster
FROM
    @sql;

DROP TABLE IF EXISTS tmp_MasterTable;

EXECUTE storeMaster;

-- 单店Data表处理
SET @sql = CONCAT(
  'CREATE TEMPORARY TABLE tmp_DataRound ',
  'SELECT Date_Code,DataRound FROM ', ProjectCode, '_t_storedata ', 'GROUP BY DataRound, Date_Code ORDER BY DataRound, Date_Code'
);
PREPARE tmpData FROM @sql;
DROP TABLE IF EXISTS tmp_DataRound;
EXECUTE tmpData;

SET @sql = CONCAT(
  'CREATE TEMPORARY TABLE tmp_StoreTable ',
  'SELECT ', dataColumnQuery, ' Store_Code StoreCode,DATE_FORMAT(Date_Code, ''%Y-%m-%d'') 上传时间,', 'DataRound 轮次 ',
  'FROM ', ProjectCode, '_t_storedata WHERE 1 = 1 AND');

OPEN cur;
cursorLoop:
LOOP
  FETCH cur INTO t_beginDate, t_dateround;
  IF cursorDone = 1 THEN
    LEAVE cursorLoop;
  END IF;
  SET @sql = CONCAT(@sql, ' (DataRound = ''', t_dateround,
    ''' AND Date_Code > ''', t_beginDate, ''') OR');
END LOOP;
CLOSE cur;

IF RIGHT(@sql, 2) = 'OR' THEN
  SET @sql = MID(@sql, 1, CHAR_LENGTH(@sql) -3);
ELSEIF RIGHT(@sql, 3) = 'AND' THEN
  SET @sql = MID(@sql, 1, CHAR_LENGTH(@sql) -4);
END IF;

DROP TABLE IF EXISTS Gather_Data_Tmp;
CREATE TEMPORARY TABLE Gather_Data_Tmp(
  Tmp_Id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  Store_Code VARCHAR(50) NOT NULL,
  DateRound VARCHAR(8192) NOT NULL,
  PRIMARY KEY (Tmp_Id)
) ENGINE = MyISAM DEFAULT CHARSET = utf8;

SET @cond = ' AND (';
SET @cond1 = '';

OPEN curRound;
cursorLoop:
LOOP
  FETCH curRound INTO t_dateround;
  SET @cond = CONCAT(@cond, '(DataRound=''', t_dateround, ''' AND Store_Code NOT IN (');
  SET @cond1 = CONCAT(@cond1, 'DataRound <> ''', t_dateround, ''' AND ');
  
  TRUNCATE TABLE Gather_Data_Tmp;
  
  INSERT INTO Gather_Data_Tmp (Store_Code, DateRound)
  SELECT DISTINCT Store_Code, DateRound
  FROM t_storecomplain
  WHERE Project_Code = ProjectCode
  AND DateRound = t_dateround
  GROUP BY DateRound, Store_Code;

  SELECT MIN(Tmp_Id) INTO i FROM Gather_Data_Tmp;
  SELECT MAX(Tmp_Id) INTO maxCnt FROM Gather_Data_Tmp;

  WHILE i <= maxCnt DO
    SELECT Store_Code INTO t_storecode
    FROM Gather_Data_Tmp
    WHERE Tmp_Id = i;

    SET @cond = CONCAT(@cond, '''', t_storecode, ''',');

    SET i = i + 1;
  END WHILE;

  IF RIGHT(@cond, 1) = ',' THEN
    SET @cond = MID(@cond, 1, CHAR_LENGTH(@cond) -1);
  END IF;

  SET @cond = CONCAT(@cond, ')) OR ');

  IF cursorDone = 1 THEN
    LEAVE cursorLoop;
  END IF;
END LOOP;
CLOSE curRound;

IF RIGHT(@cond1, 4) = 'AND ' THEN
  SET @cond1 = MID(@cond1, 1, CHAR_LENGTH(@cond1) -5);
END IF;

IF RIGHT(@cond, 1) = '(' THEN
  SET @cond = '';
ELSEIF RIGHT(@cond, 3) = 'OR ' THEN
  SET @cond = CONCAT(@cond, '(', @cond1, '))');
END IF;

SET @sql = CONCAT(@sql, @cond, dataWhereCondition);

-- SELECT @sql;
PREPARE storeData FROM @sql;
DROP TABLE IF EXISTS tmp_StoreTable;
EXECUTE storeData;

-- 两个临时表关联
SET @sql = 'ALTER TABLE tmp_MasterTable ADD INDEX tmp_MasterTable_客户编号 (客户编号);';
PREPARE addIndex1 FROM @sql;
EXECUTE addIndex1;

SET @sql = 'ALTER TABLE tmp_StoreTable ADD INDEX tmp_StoreTable_StoreCode (StoreCode);';
PREPARE addIndex2 FROM @sql;
EXECUTE addIndex2;

SELECT COUNT(tm.客户编号)
FROM tmp_MasterTable tm
INNER JOIN tmp_StoreTable ts ON tm.客户编号 = ts.StoreCode INTO totalCount;

SET
    @sql = CONCAT(
        'SELECT tm.*,ts.* FROM tmp_MasterTable tm ',
        'INNER JOIN tmp_StoreTable ts ON tm.客户编号 = ts.StoreCode ',
        orderCondition,
        ' LIMIT ',
        PageSize,
        ' OFFSET ',
        StartIndex
    );

PREPARE selectStore
FROM
    @sql;

EXECUTE selectStore;

END;

-- 调用
CALL GetStoreTable(
    '客户号,客户标准名称,客户简称,客户总部名称,地址,周围标志性建筑物,联络人,电话,全国,渠道类型,DSR_PSR_DWR,客户性质,客户级别,直辖市,城市代码,地级市,县级市,办事处,OTC总部,OTC_CODE,大区总监,大区总监编号,本级岗位_大区总监,大区总监负责人,MUDID_2,大区总监负责人MUDID,大区,大区编号,本级岗位_大区,大区负责人,MUDID_3,大区MUDID,所属团队,所属团队编号,本级岗位_团队代表,所属团队代表,MUDID_4,所属团队代表MUDID,销售代表,销售代表编号,本级岗位_销售代表,MUDID_5,销售代表MUDID,地区,工作地,报备,',
    'OTC_001,OTC_002,OTC_003,OTC_004,OTC_005,OTC_006,OTC_007,OTC_008,OTC_009,OTC_010,OTC_011,OTC_012,',
    ' where 1 = 1 and ((全国 = ''全国''))',
    ' and DataRound = ''2016Q3''',
    ' order by 客户号 ASC',
    'p01',
    10,
    1,
    @totalCount
)
SELECT
    @totalCount;

-- 遍历父节点
CREATE PROCEDURE sp_query_tree_nodes_up(
  node VARCHAR(10), -- 查询的某个节点值
  tableName VARCHAR(20), -- 查询表名
  childAttr VARCHAR(20), -- 子字段
  parentAttr VARCHAR(20), -- 父子段
  searchAttr VARCHAR(20), -- 查询字段
  `condition` VARCHAR(200), -- 查询条件
  searchType INT, -- 0:精确查找  1:模糊匹配
  deepLevel INT, -- 遍历层数，用于实现只取某一层级的节点
  OUT treeNodes Text -- 返回查询字段
)
BEGIN
DECLARE sTemp Text;
DECLARE sTempChd Text;
DECLARE sTempChdOfCondition Text;
DECLARE beginTag INT;
DECLARE deeps INT;

SET sTemp = '';
SET sTempChd = node;
SET beginTag = 1;
SET deeps = 1;
IF searchType = 0 THEN
  SET @whereQuery = CONCAT(' WHERE ',childAttr,' = ''',sTempChd,'''');
ELSE
  SET @whereQuery = CONCAT(' WHERE ',childAttr,' LIKE ''%',sTempChd,'%''');
END IF;

-- 包含当前节点的值
SET @sql = CONCAT('SELECT ',searchAttr,' INTO @s1 FROM ',tableName,
  @whereQuery,`condition`);
PREPARE tempQuery FROM @sql;
EXECUTE tempQuery;
DEALLOCATE PREPARE tempQuery;

SET sTempChdOfCondition = @s1;
SET sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');

out_label:
BEGIN
WHILE sTempChd IS NOT NULL AND sTempChd <> '' AND sTempChd <> '0' DO
  IF beginTag = 1 THEN
    SET @sql = CONCAT('SELECT ',parentAttr,' INTO @s1 FROM ',tableName,
      @whereQuery,`condition`);
  ELSE
    SET @sql = CONCAT('SELECT ',parentAttr,' INTO @s1 FROM ',tableName,
      ' WHERE ',childAttr,' = ''',sTempChd,'''',`condition`);
  END IF;
  -- SELECT @sql;
  PREPARE tempQuery FROM @sql;
  -- SET @s = sTempChd;
  EXECUTE tempQuery /*USING @s*/;
  DEALLOCATE PREPARE tempQuery;
  SET sTempChd = @s1;
  IF sTempChd IS NOT NULL AND sTempChd <> '' AND sTempChd <> '0' THEN
    SET @sql = CONCAT('SELECT ',searchAttr,' INTO @s1 FROM ',tableName,
      ' WHERE ',childAttr,' = ''',sTempChd,'''',`condition`);
    PREPARE tempQuery FROM @sql;
    EXECUTE tempQuery;
    DEALLOCATE PREPARE tempQuery;
    SET sTempChdOfCondition = @s1;
    IF sTempChdOfCondition IS NOT NULL AND sTempChdOfCondition <> '' THEN
      IF deepLevel = 0 THEN
        SET sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');
      ELSE IF deepLevel = deeps THEN
        SET sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');
        LEAVE out_label;
      END IF;
    END IF;
  END IF;
  SET beginTag = beginTag + 1;
  SET deeps = deeps + 1;
END WHILE;
END out_label;

IF RIGHT(sTemp, 1) = ',' THEN
  SET sTemp = MID(sTemp, 1, CHAR_LENGTH(sTemp) -1);
END IF;
SET treeNodes = sTemp;
END

CALL sp_query_tree_nodes_up('福州路店','t_mbd_master','mbd_name','parent_name','id',' and project_id = 1 and period_id = 2',0,0,@treeNodes);

SELECT @treeNodes

-- 遍历子节点
CREATE PROCEDURE sp_query_tree_nodes(
        node VARCHAR(100),
        -- 查询的某个节点值
        tableName VARCHAR(20),
        -- 查询表名
        childAttr VARCHAR(20),
        -- 子字段
        parentAttr VARCHAR(20),
        -- 父子段
        searchAttr VARCHAR(20),
        -- 查询字段
        `condition` VARCHAR(200),
        -- 查询条件
        searchType INT,
        -- 0:精确查找  1:模糊匹配
        deepLevel INT,
        -- 遍历层数，用于实现只取某一层级的节点，为0时，获取整个结构
        OUT treeNodes Text -- 返回查询字段
    ) BEGIN DECLARE sTemp Text;

DECLARE sTempChd Text;

DECLARE sTempChdOfCondition Text;

DECLARE beginTag INT;

DECLARE deeps INT;

SET
    sTemp = '';

SET
    sTempChd = node;

-- 初始为当前节点值
SET
    beginTag = 1;

-- 查询第一层的标识，精确匹配和模糊匹配都是对应于第一层
SET
    deeps = 1;

IF searchType = 0 THEN -- 精确查找
SET
    @whereQuery = CONCAT(
        ' WHERE FIND_IN_SET(',
        parentAttr,
        ',''',
        sTempChd,
        ''') > 0'
    );

ELSE -- 模糊查找
SET
    @whereQuery = CONCAT(
        ' WHERE ',
        parentAttr,
        ' LIKE ''%',
        sTempChd,
        '%'''
    );

END IF;

SET
    GLOBAL group_concat_max_len = 600000;

-- 返回的数结构中加上当前节点
SET
    @sql = CONCAT(
        'SELECT ',
        searchAttr,
        ' INTO @s1 FROM ',
        tableName,
        ' WHERE ',
        childAttr,
        ' = ''',
        node,
        ''' ',
        `condition`
    );

PREPARE tempQuery
FROM
    @sql;

EXECUTE tempQuery;

DEALLOCATE PREPARE tempQuery;

SET
    sTempChdOfCondition = @s1;

SET
    sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');

out_label :BEGIN WHILE sTempChd IS NOT NULL
AND sTempChd <> '' DO IF beginTag = 1 THEN -- 查询子一层
SET
    @sql = CONCAT(
        'SELECT GROUP_CONCAT(',
        childAttr,
        ') INTO @s1 FROM ',
        tableName,
        @whereQuery,
        `condition`
    );

ELSE -- 查询其它层
SET
    @sql = CONCAT(
        'SELECT GROUP_CONCAT(',
        childAttr,
        ') INTO @s1 FROM ',
        tableName,
        ' WHERE FIND_IN_SET(',
        parentAttr,
        ',''',
        sTempChd,
        ''') > 0',
        `condition`
    );

END IF;

-- SELECT @sql;
PREPARE tempQuery
FROM
    @sql;

-- SET @s = sTempChd;
EXECUTE tempQuery
/*USING @s*/
;

DEALLOCATE PREPARE tempQuery;

SET
    sTempChd = @s1;

IF sTempChd IS NOT NULL
AND sTempChd <> '' THEN -- 如果存在子节点，就获取子节点的值
SET
    @sql = CONCAT(
        'SELECT GROUP_CONCAT(',
        searchAttr,
        ') INTO @s1 FROM ',
        tableName,
        ' WHERE FIND_IN_SET(',
        childAttr,
        ',''',
        sTempChd,
        ''') > 0',
        `condition`
    );

PREPARE tempQuery
FROM
    @sql;

EXECUTE tempQuery;

DEALLOCATE PREPARE tempQuery;

SET
    sTempChdOfCondition = @s1;

IF sTempChdOfCondition IS NOT NULL
AND sTempChdOfCondition <> '' THEN IF deepLevel = 0 THEN -- 获取整个结构
SET
    sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');

ELSE -- 获取特定一层的节点，主观感受使用
IF deepLevel = deeps THEN
SET
    sTemp = CONCAT(sTemp, sTempChdOfCondition, ',');

LEAVE out_label;

END IF;

END IF;

END IF;

END IF;

SET
    beginTag = beginTag + 1;

SET
    deeps = deeps + 1;

-- 执行一次，层数+1
END WHILE;

END out_label;

IF RIGHT(sTemp, 1) = ',' THEN -- 删除最后一个逗号
SET
    sTemp = MID(sTemp, 1, CHAR_LENGTH(sTemp) -1);

END IF;

SET
    treeNodes = sTemp;

END CALL sp_query_tree_nodes(
    '',
    't_mbd_master',
    'mbd_name',
    'parent_name',
    'id',
    ' and project_id = 1 and period_id = 2',
    1,
    0,
    @treeNodes
);

SELECT
    @treeNodes SHOW VARIABLES LIKE "group_concat_max_len";

SET
    GLOBAL group_concat_max_len = 60000;

-- 4、动态拼接
/**
 * App用户获取项目列表
 */
CREATE PROCEDURE sp_get_project_by_app_user(
    userId INT,
    userType INT,
    pageIndex INT,
    pageSize INT,
    projectName VARCHAR(60),
    `language` VARCHAR(2)
) BEGIN DECLARE customerQuery VARCHAR(60);

DECLARE projectQuery VARCHAR(60);

DECLARE roundQuery VARCHAR(200);

SET
    @sql = 'SELECT p.id projectId,';

-- 项目号
SET
    @searchCondition = '';

IF `language` = 'en' THEN
SET
    customerQuery = 'ifnull(c.e_name, c.c_name) customerName,';

SET
    projectQuery = 'ifnull(p.e_name, p.c_name) projectName,';

SET
    roundQuery = '(SELECT ifnull(pm.e_name, pm.c_name) FROM t_period_master pm WHERE pm.ProjectId = p.id AND pm.Preview <> 1 AND pm.has_data = 1 AND pm.has_users = 1 ORDER BY pm.update_time DESC LIMIT 1) roundName';

IF projectName != '' THEN
SET
    @searchCondition = CONCAT(
        ' AND ifnull(p.e_name, p.c_name) like ''%',
        projectName,
        '%'''
    );

END IF;

ELSE
SET
    customerQuery = 'ifnull(c.c_name, c.e_name) customerName,';

SET
    projectQuery = 'ifnull(p.c_name, p.e_name) projectName,';

SET
    roundQuery = '(SELECT ifnull(pm.c_name, pm.e_name) FROM t_period_master pm WHERE pm.ProjectId = p.id AND pm.Preview <> 1 AND pm.has_data = 1 AND pm.has_users = 1 ORDER BY pm.update_time DESC LIMIT 1) roundName';

IF projectName != '' THEN
SET
    @searchCondition = CONCAT(
        ' AND ifnull(p.c_name, p.e_name) like ''%',
        projectName,
        '%'''
    );

END IF;

END IF;

SET
    @sql = CONCAT(
        @sql,
        customerQuery,
        -- 客户名
        projectQuery,
        -- 项目名
        ' DATE_FORMAT(p.update_time, ''%Y-%m-%d'') updateTime,',
        -- 更新时间
        roundQuery,
        -- 最新轮次
        ' FROM t_project p',
        ' LEFT JOIN t_customer c ON p.customer_id = c.id'
    );

IF userType = 1 THEN -- 超级用户获取项目列表
SET
    @sql = CONCAT(@sql, ' WHERE 1 = 1');

ELSE -- 普通用户获取项目列表
SET
    @sql = CONCAT(
        @sql,
        ' INNER JOIN t_user u ON u.project_id = p.id ',
        ' WHERE u.id = ',
        userId
    );

END IF;

SET
    @sql = CONCAT(
        @sql,
        @searchCondition,
        ' AND p.deleted = 0',
        -- 项目未删除
        ' AND ((p.c_des IS NOT NULL AND p.c_des != '''') OR (p.e_des IS NOT NULL AND p.e_des != '''')) ',
        -- 项目介绍已提交
        ' AND ((p.c_method IS NOT NULL AND p.c_method != '''') OR (p.e_method IS NOT NULL AND p.e_method != '''')) ',
        -- 测评方法已提交
        ' AND (SELECT COUNT(r.id) FROM t_report r WHERE r.project_id = p.id AND r.status = 1) > 0 ',
        -- 趋势分析已提交
        ' AND (SELECT COUNT(pe.id) FROM t_period_master pe WHERE pe.ProjectId = p.id AND pe.Preview <> 1 AND pe.has_data = 1 AND pe.has_users = 1) > 0'
    );

SET
    @sql = CONCAT(
        @sql,
        ' ORDER BY p.update_time DESC LIMIT ',
        pageSize,
        ' OFFSET ',
        pageIndex
    );

PREPARE projectList
FROM
    @sql;

EXECUTE projectList;

END

-- 临时表和游标性能对比：

CREATE DEFINER=`root`@`%` PROCEDURE `debug`(
    IN `beginTime` int,
    IN `checkTime` int
)
BEGIN  
DECLARE t_id VARCHAR(64) DEFAULT '';  
DECLARE t_item TINYINT DEFAULT 0;  
DECLARE t_result VARCHAR(8192) DEFAULT '';  

DECLARE cursorDone INT DEFAULT 0;  
DECLARE cur CURSOR FOR
  SELECT Asset_Id, Check_Item, Check_Result
  from IDC_Gather_Info
  WHERE Check_Time > beginTime
  AND Check_Time <= checkTime;  

DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET cursorDone = 1;

OPEN cur;  
cursorLoop:LOOP  
  FETCH cur INTO t_id, t_item, t_result;  
  IF cursorDone = 1 THEN  
    LEAVE cursorLoop;  
  END IF;  
END LOOP;  
CLOSE cur;  
END

测试结果：

1. 数据量15万，存储过程执行失败，提示错误：Incorrect key file for table '/tmp/#sql_3044_0.MYI';try to repair it
2. 数据量5万，执行成功，耗时31.051s
3. 数据量1万，执行成功，耗时1.371s

使用临时表替换游标：

CREATE DEFINER=`root`@`%` PROCEDURE `debug`(
    IN `beginTime` int, 
    IN `checkTime` int
)
BEGIN
DECLARE t_id VARCHAR(64) DEFAULT '';  
DECLARE t_item TINYINT DEFAULT 0;  
DECLARE t_result VARCHAR(8192) DEFAULT '';  

DECLARE maxCnt INT DEFAULT 0;  
DECLARE i INT DEFAULT 0;  

DROP TABLE IF EXISTS Gather_Data_Tmp;  
CREATE TEMPORARY TABLE Gather_Data_Tmp(  
    `Tmp_Id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  
    `Asset_Id` VARCHAR(16) NOT NULL,  
    `Check_Item` TINYINT(1) NOT NULL,  
    `Check_Result` VARCHAR(8192) NOT NULL,  
    PRIMARY KEY (`Tmp_Id`)  
)ENGINE=MyISAM DEFAULT CHARSET=utf8;  

SET @tSql = CONCAT('INSERT INTO Gather_Data_Tmp (`Asset_Id`, `Check_Item`, `Check_Result`)
SELECT Asset_Id, Check_Item, Check_Result
FROM IDC_Gather_Info
WHERE Check_Time > ', beginTime,
' AND Check_Time <= ', checkTime);  

PREPARE gatherData FROM @tSql;  
EXECUTE gatherData;  

SELECT MIN(`Tmp_Id`) INTO i FROM Gather_Data_Tmp;  
SELECT MAX(`Tmp_Id`) INTO maxCnt FROM Gather_Data_Tmp;  

WHILE i <= maxCnt DO
    -- 变量赋值
    SELECT Asset_Id, Check_Item, Check_Result
    INTO t_id, t_item, t_result
    FROM Gather_Data_Tmp
    WHERE Tmp_Id = i;  

    SET i = i + 1;  
END WHILE;  
END

1. 数据量15万，执行成功，耗时8.928s
2. 数据量5万，执行成功，耗时2.994s
3. 数据量1万，执行成功，耗时0.634s

可以看到Mysql的游标在处理大一点的数据量时还是比较乏力的，仅适合用于操作几百上千的小数据量。

```



### 常见问题

#### 忘记密码

1、忘记root密码

```sh
# 停止服务
net stop mysql
# 跳过认证登录（8.0以上）
mysqld --shared-memory --skip-grant-tables
# 上面的窗口卡住，另起一个窗口
mysql
# 重置root密码为空
update mysql.user set authentication_string='' where User = 'root';
# 退出所有命令行，重新登陆数据库，（若显示服务未启动，需先启动MySQL服务，输入net start mysql）
# root登录
mysql -uroot
# 修改root密码
alter user 'root'@'localhost' identified by  '123';
# 刷新权限
flush privileges;
```

2、误删root用户

```sh
# 往user表中插入root用户:
insert into user set user='root',ssl_cipher=''x509_issuer='',x509_subject='';
# 给新建的root用户授权:
update user set Host='localhost',select_priv='y',insert_priv='y',update_priv='y',
Alter_priv='y',delete_priv='y',create_priv='y',drop_priv='y',reload_priv='y',shutdown_priv='y',Process_priv='y',file_priv='y',grant_priv='y',References_priv='y',index_priv='y',create_user_priv='y',show_db_priv='y',super_priv='y',create_tmp_table_priv='y',Lock_tables_priv='y',execute_priv='y',repl_slave_priv='y',repl_client_priv='y',create_view_priv='y',show_view_priv='y',create_routine_priv='y',alter_routine_priv='y',create_user_priv='y' where user='root';
```

3、修改其它用户密码

1） 用`SET PASSWORD`命令

```mysql
set password for 用户名@localhost = password('新密码');
set password for root@localhost = password('123');
```

2）用mysqladmin

```sh
mysqladmin -u用户名 -p旧密码 password 新密码
mysqladmin -uroot -p123456 password 123
```

3）更新user表

```sql
use mysql;
update user set password=password('123') where user='root' and host='localhost';
flush privileges;
```







### 常见错误



#### 1. This function has none of DETERMINISTIC, NOSQL, ...

```sql
set global log_bin_trust_function_creators = TRUE;
```

这是我们开启了bin-log, 我们就必须指定我们的函数是否是：

1. DETERMINISTIC 不确定的
2. NO SQL 没有SQl语句，当然也不会修改数据
3. READS SQL DATA 只是读取数据，当然也不会修改数据
4. MODIFIES SQL DATA 要修改数据
5. CONTAINS SQL 包含了SQL语句

其中在 function 里面，只有 DETERMINISTIC, NO SQL 和 READS SQL DATA 被支持。如果我们开启了 bin-log, 我们就必须为我们的 function 指定一个参数。



#### 2. Illegal mix of collations (utf8_unicode_ci,IMPLICIT) and ...

```sql
CONVERT('xxx' USING utf8) COLLATE utf8_unicode_ci
```

存储过程中给字符串变量设置了超出长度的值，也有可能报此异常



#### 3. 非空字段插入空值

问题：Incorrect integer value: '' for column 'id' at row 1

解决：my.ini中查找sql-mode，默认为

sql-mode="STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"

删除`STRICT_TRANS_TABLES`, `NO_AUTO_CREATE_USER`

重启mysql后即可 



#### 4. MySQL Connector/NET Exception: Reading from the stream has failed

参考：https://blog.csdn.net/fancyf/article/details/78295964

连接字符串：**SslMode=None**



#### 5. mysql 卡死 大部分线程长时间处于sending data的状态

sending data状态表示两种情况，一种是mysql已经查询了数据，正在发给客户端；另一种情况是，mysql已经知道某些数据需要去什么地方读取，正在从数据文件中读取。

临时表、存储库引擎、缓存池设置的容量是否足够？

默认情况下，临时表空间文件是自动扩展的，在正常关闭或初始化中止时，将删除临时表空间，并在每次启动服务器时重新创建。

对临时表空间的大小进行限制，允许自动增长，但最大容量有上限：

```ini
[mysqld]
innodb_temp_data_file_path=ibtmp1:12M:autoextend:max:500M
```

设置了上限的大小，当数据文件达到最大大小时，查询将失败，并显示一条错误消息，表明表已满，查询不能往下执行，避免 ibtmp1 过大。

8.0 的临时表空间分为会话临时表空间和全局临时表空间，会话临时表空间存储用户创建的临时表和当 InnoDB 配置为磁盘内部临时表的存储引擎时由优化器创建的内部临时表，当会话断开连接时，其临时表空间将被截断并释放回池中；也就是说，在 8.0 中有一个专门的会话临时表空间，当会话被杀掉后，可以回收磁盘空间；而原来的 ibtmp1 是现在的全局临时表空间，存放的是对用户创建的临时表进行更改的回滚段，在 5.7 中 ibtmp1 存放的是用户创建的临时表和磁盘内部临时表；
也就是在 8.0 和 5.7 中 ibtmp1 的用途发生了变化，5.7 版本临时表的数据存放在 ibtmp1 中，在 8.0 版本中临时表的数据存放在会话临时表空间，如果临时表发生更改，更改的 undo 数据存放在 ibtmp1 中；

总结：在 mysql5.7 时，杀掉会话，临时表会释放，但是仅仅是在 ibtmp 文件里标记一下，空间是不会释放回操作系统的。如果要释放空间，需要重启数据库；在 mysql8.0 中可以通过杀掉会话来释放临时表空间。



### Handler

```sql
DECLARE {EXIT | CONTINUE}
HANDLER FOR
{error-number | SQLSTATE error-string | condition}
SQL statement
```

上述定义包括：

- Handler Type (CONTINUE,EXIT) 处理类型 继续或退出
- Handler condition (SQLSTATE,MYSQL ERROR,CONDITION) 触发条件
- Handler actions（错误触发的操作）

>注意：
>
>1、exit只退出当前的block。exit 意思是当动作成功提交后，退出所在的复合语句。即declare exit handler for... 所在的复合语句。  
>2、如果定义了handler action，会在 continue 或 exit 之前执行
>
>发生错误的条件有：
>
>1、MYSQL错误代码  
>2、ANSI-standard SQLSTATE code  
>3、命名条件。可使用系统内置的SQLEXCEPTION,SQLWARNING和NOT FOUND

例1：

当错误代码为1062时将duplicate_key的值设为1，并继续执行当前任务

declare continue handler for 1062 set duplicate_key=1;

下面的跟上面一样，只是使用的条件为ANSI标准错误代码

declare continue handler for sqlstate '23000' set duplicate_key=1;

当发生SQLEXCEPTION时，将L_error设为1，并继续

declare continue handler for SQLEXCEPTION set L_error=1;

小提示：

当你在MYSQL客户端执行命令并产生错误时，会得到MYSQL和ANSI的SQLSTATE code，

附常见错误号对照表

| MySQL error code | SQLSTATE code | Error message                                                |
| ---------------- | ------------- | ------------------------------------------------------------ |
| 1011             | HY000         | Error on delete of '%s' (errno: %d)                          |
| 1021             | HY000         | Disk full (%s); waiting for someone to free some space . . . |
| 1022             | 23000         | Can't write; duplicate key in table '%s'                     |
|                  |               | 1027 HY000 '%s' is locked against change
1036 HY000 Table '%s' is read only
1048 23000 Column '%s' cannot be null
1062 23000 Duplicate entry '%s' for key %d
1099 HY000 Table '%s' was locked with a READ lock and can't be updated
1100 HY000 Table '%s' was not locked with LOCK TABLES
1104 42000 The SELECT would examine more than MAX_JOIN_SIZE rows; check your WHERE and use SET SQL_BIG_SELECTS=1 or SET SQL_MAX_JOIN_SIZE=# if the SELECT is okay
1106 42000 Incorrect parameters to procedure '%s'
1114 HY000 The table '%s' is full
1150 HY000 Delayed insert thread couldn't get requested lock for table %s
1165 HY000 INSERT DELAYED can't be used with table '%s' because it is locked with LOCK TABLES
1242 21000 Subquery returns more than 1 row
1263 22004 Column set to default value; NULL supplied to NOT NULL column '%s' at row %ld
1264 22003 Out of range value adjusted for column '%s' at row %ld
1265 1000 Data truncated for column '%s' at row %ld
1312 0A000 SELECT in a stored program must have INTO
1317 70100 Query execution was interrupted
1319 42000 Undefined CONDITION: %s
1325 24000 Cursor is already open
1326 24000 Cursor is not open
1328 HY000 Incorrect number of FETCH variables
1329 2000 No data to FETCH
1336 42000 USE is not allowed in a stored program
1337 42000 Variable or condition declaration after cursor or handler declaration
1338 42000 Cursor declaration after handler declaration
1339 20000 Case not found for CASE statement
1348 HY000 Column '%s' is not updatable
1357 HY000 Can't drop a %s from within another stored routine
1358 HY000 GOTO is not allowed in a stored program handler
1362 HY000 Updating of %s row is not allowed in %s trigger
1363 HY000 There is no %s row in %s trigger |

命名条件：

declare conditon_name condition for {SQLSTATE sqlstate_code | MYSQL_ERROR_CODE};

例如：

declare foreign_key_error condition for 1216;

declare continue handler for foreign_key_error mysql_statements;

优先级：当同时使用MYSQL错误码，标准SQLSTATE错误码，命名条件（SQLEXCEPTION）来定义错误处理时，其捕获顺序是（只捕获一条错误）：MYSQL码->SQLSTATE->命名条件

作用域：

1、包括begin...end内的语句

declare continue handler for 1048 select 'attempt to insert a null value';
begin
  insert into a values(6,null);
end;


若a表第二字段定义为非空，则会触发1048错误

2、若错误处理在begin...end内定义，则在之外的语句不会触发错误发生

BEGIN
  BEGIN
    DECLARE CONTINUE HANDLER FOR 1216 select 'Foreign key constraint violated';
  END;
  INSERT INTO departments (department_name,manager_id,location) VALUES ('Elbonian HR','Catbert','Catbertia');
END;
3、能够捕获其它存储过程抛出的错误

下面再通过几个例子来掌握MySQL存储过程中异常处理的使用。

例一：error-number

准备工作

CREATE TABLE `t1` (
`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
复制代码
CREATE TABLE `t2` (
  `cid` INT(10) UNSIGNED NULL DEFAULT NULL,
  INDEX `FK__t1` (`cid`),
  CONSTRAINT `FK__t1` FOREIGN KEY (`cid`) REFERENCES `t1` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;
复制代码
创建存储过程 

复制代码
delimiter //
create procedure a(var1 int)
begin
  declare exit handler for 1452 insert into error_log values(
    concat('time:',current_date,'.Foreign Key Reference Failure For Value=',var1)
  );
  insert into t2 values(var1);
end;//
复制代码
如果有1452错误，则当插入到表error_log这个语句完成后，退出（exit），这里申明异常处理的语句在上面begin...end的复合语句中，所以这里退出，其实就表示退出了该存储过程。

例二：sqlstate error-string

准备工作

CREATE TABLE `t4` (
  `id` INT(10) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


复制代码
create procedure p23()
begin
  begin
  declare exit handler for sqlstate '23000' set @x2=1;
    set @x=1;
    insert into t4 values(1);
    set @x=2;
  end;
  begin
    declare exit handler for sqlstate '23000' set @x2=9;
    insert into t4 values(1);
  end;
  set @x=3;
end

例三：

begin
  declare exit handler for sqlstate '23000' set @x2=1;
  set @x=1;
  insert into t4 values(1);
  set @x=2;
  begin
    declare exit handler for sqlstate '23000' set @x2=9;
    insert into t4 values(1);
  end;
  set @x=3;
end

error-number的例子
create procedure p22(var1 int)
begin
  declare exit handler for 1216 insert into error_log values(
    concat('time:' , current_date , '.Foreign Key Reference Failure For Value='
    ,var1)
  );
  insert into t3 values(var1);
end;//

sqlstate error-string的例子

create procedure p23()
begin
  declare continue handler for sqlstate '23000' set @x2=1;
  set @x=1;
  insert into t4 values(1);
  set @x=2;
  insert into t4 values(1);
  set @x=3;
end;//

condition的例子

declare 'name' condition for sqlstate '23000';
declare exit handler for 'name' rollback;



### 触发器

```sql
-- 创建触发器
CREATE TRIGGER trigger_name trigger_time trigger_event ON tb_name FOR EACH ROW trigger_stmt
-- trigger_name：触发器的名称
-- tirgger_time：触发时机，为BEFORE或者AFTER
-- trigger_event：触发事件，为INSERT、DELETE或者UPDATE
-- tb_name：表示建立触发器的表名，就是在哪张表上建立触发器
-- trigger_stmt：触发器的程序体，可以是一条SQL语句或者是用BEGIN和END包含的多条语句
-- 所以可以说MySQL创建以下六种触发器：
BEFORE INSERT,BEFORE DELETE,BEFORE UPDATE
AFTER INSERT,AFTER DELETE,AFTER UPDATE

-- 查看触发器
SHOW TRIGGERS [FROM schema_name];

-- 删除触发器
DROP TRIGGER [IF EXISTS] [schema_name.]trigger_name
```

![x](./Resources/db001.png)

load data语句是将文件的内容插入到表中，相当于是insert语句，而replace语句在一般的情况下和insert差不多，但是如果表中存在primary 或者unique索引的时候，如果插入的数据和原来的primary key或者unique相同的时候，会删除原来的数据，然后增加一条新的数据，所以有的时候执行一条replace语句相当于执行了一条delete和insert语句。

![x](./Resources/db002.png)

**限制和注意事项：**

1. 触发程序不能调用将数据返回客户端的存储程序，也不能使用采用CALL语句的动态SQL语句，但是允许存储程序通过参数将数据返回触发程序，也就是存储过程或者函数通过OUT或者INOUT类型的参数将数据返回触发器是可以的，但是不能调用直接返回数据的过程。
2. 不能在触发器中使用以显示或隐式方式开始或结束事务的语句，如START TRANSACTION,COMMIT或ROLLBACK。
3. OLD 是只读的，而 NEW 则可以在触发器中使用 SET 赋值，这样不会再次触发触发器，造成循环调用

> 注意事项：MySQL的触发器是按照BEFORE触发器、行操作、AFTER触发器的顺序执行的，其中任何一步发生错误都不会继续执行剩下的操作，如果对事务表进行的操作，如果出现错误，那么将会被回滚，如果是对非事务表进行操作，那么就无法回滚了，数据可能会出错。

**总结：**

触发器是基于行触发的，所以删除、新增或者修改操作可能都会激活触发器，所以不要编写过于复杂的触发器，也不要增加过多的触发器，这样会对数据的插入、修改或者删除带来比较严重的影响，同时也会带来可移植性差的后果，所以在设计触发器的时候一定要有所考虑。

触发器是一种特殊的存储过程，它在插入，删除或修改特定表中的数据时触发执行，它比数据库本身标准的功能有更精细和更复杂的数据控制能力。

数据库触发器有以下的作用：

1. 安全性。可以基于数据库的值使用户具有操作数据库的某种权利。
   - 可以基于时间限制用户的操作，例如不允许下班后和节假日修改数据库数据。
   - 可以基于数据库中的数据限制用户的操作，例如不允许股票的价格的升幅一次超过10%。

2. 审计。可以跟踪用户对数据库的操作。  
   - 审计用户操作数据库的语句。
   - 把用户对数据库的更新写入审计表。

3. 实现复杂的数据完整性规则
   - 实现非标准的数据完整性检查和约束。触发器可产生比规则更为复杂的限制。与规则不同，触发器可以引用列或数据库对象。例如，触发器可回退任何企图吃进超过自己保证金的期货。
   - 提供可变的缺省值。

4. 实现复杂的非标准的数据库相关完整性规则。触发器可以对数据库中相关的表进行连环更新。例如，在auths表author_code列上的删除触发器可导致相应删除在其它表中的与之匹配的行。
   - 在修改或删除时级联修改或删除其它表中的与之匹配的行。
   - 在修改或删除时把其它表中的与之匹配的行设成NULL值。
   - 在修改或删除时把其它表中的与之匹配的行级联设成缺省值。
   - 触发器能够拒绝或回退那些破坏相关完整性的变化，取消试图进行数据更新的事务。当插入一个与其主健不匹配的外部键时，这种触发器会起作用。例如，可以在books.author_code 列上生成一个插入触发器，如果新值与auths.author_code列中的某值不匹配时，插入被回退。

5. 同步实时地复制表中的数据。

6. 自动计算数据值，如果数据的值达到了一定的要求，则进行特定的处理。例如，如果公司的帐号上的资金低于5万元则立即给财务人员发送警告数据。

补充：在MySQL中，BEGIN … END 语句的语法为：

```sql
BEGIN
  [statement_list]
END
```


其中，statement_list 代表一个或多个语句的列表，列表内的每条语句都必须用分号（;）来结尾。而在MySQL中，分号是语句结束的标识符，遇到分号表示该段语句已经结束，MySQL可以开始执行了。因此，解释器遇到statement_list 中的分号后就开始执行，然后会报出错误，因为没有找到和 BEGIN 匹配的 END。

这时就会用到 DELIMITER 命令（DELIMITER 是定界符，分隔符的意思），它是一条命令，不需要语句结束标识，语法为：`DELIMITER new_delemiter`，new_delemiter 可以设为1个或多个长度的符号，默认的是分号（;），我们可以把它修改为其他符号，如`DELIMITER $`，在这之后的语句，以分号结束，解释器不会有什么反应，只有遇到了$，才认为是语句结束。注意，使用完之后，应该把它给修改回来。

示例：

```sql
DROP TRIGGER IF EXISTS T_BEFORE_ADD_ON_PROJ;
DELIMITER $
CREATE TRIGGER T_BEFORE_ADD_ON_PROJ BEFORE INSERT
ON biz_project FOR EACH ROW
BEGIN
  IF IFNULL(NEW.ORG_STR, '') = '' THEN
	  SET NEW.ORG_STR = lead_basic.FUNC_GET_ORG_BY_USER(NEW.PROJECT_RESPONSIBLE_USER);
  END IF;
	IF IFNULL(NEW.MACHINE_NO, '') = '' AND IFNULL(NEW.MACHINE_ID, 0) > 0 THEN
	  SET NEW.MACHINE_NO = (SELECT MACHINE_NAME FROM lead_basic.bas_machine WHERE ID = NEW.MACHINE_ID LIMIT 1);
  END IF;
	IF IFNULL(NEW.CUSTOMER_NAME, '') = '' AND IFNULL(NEW.CUSTOMER_ID, 0) > 0 THEN
	  SET NEW.CUSTOMER_NAME = (SELECT CONCAT(r.REGION_NAME, '-', c.CUSTOMER_NAME) FROM lead_basic.bas_customer c LEFT JOIN lead_basic.bas_region r ON r.ID = c.CUSTOMER_REGION WHERE c.ID = NEW.CUSTOMER_ID LIMIT 1);
  END IF;
END$
```



### 事件调度器

https://www.cnblogs.com/ctaixw/p/5660531.html



### MySQL监控

一、监控采集依据：主要基于show global status对数据进行采集

二、对用户进行授权，然后使用show global status进行采集分析

mysql -uroot -p”xxxx” -e "show global status" ###查看所有的值

监控项注释：

Aborted_clients ##客户端不能正常连接，失败的连接数量。

Aborted_connects ##客户端中断数量，可能有恶意连接。

\###吞吐量

Bytes_received ##从所有客户端接收到的字节数。

Bytes_sent ##发送给所有客户端的字节数。

\###com admin 语句执行数量

Com_commit ##统计提交语句次数

com_delete ##统计删除语句

com_delete_multi ##最小

com_insert ##统计插入语句

com_rollback ##事务回滚

Connections ##不管是否成功连接到mysql的个数

\###临时表数量

Created_tmp_disk_tables ##服务器创建的临时表数量

Created_tmp_files ##已经创建的临时文件数量

Created_tmp_tables ##服务器执行语句时自动创建的内存中的临时表的数量。如果Created_tmp_disk_tables较大，你可能要增加tmp_table_size值使临时表基于内存而不基于硬盘。

\##后台预读线程读取到Innodb缓冲池的页的数量

Innodb_buffer_pool_reads ##不能满足InnoDB必须单页读取的缓冲池中的逻辑读数量。

Innodb_buffer_pool_read_ahead ##预读的次数

Innodb_buffer_pool_read_requests ##从缓冲池中读取的页的次数

*缓冲池的命中率=

innodb_buffer_pool_read_requests/(innodb_buffer_pool_read_requests+innodb_buffer_pool_read_ahead+innodb_buffer_pool_reads)

innodb_data_read 总共读入的字节数；

innodb_data_reads 发起读取请求的次数，每次读取可能需要读取多个页。

*平均每次读取的字节数=innodb_data_read/innodb_data_reads

Innodb_rows_deleted ##执行deleted操作的次数

Innodb_rows_inserted ##执行insert操作的次数

Innodb_rows_read ##执行select操作的次数

Innodb_rows_updated ##执行update操作的次数

\###针对MyISAM引擎：

key_buffer_size ##缓冲池大小

Key_blocks_unused ##未使用的缓存簇(blocks)数

Key_blocks_used ##表示曾经用到的最大的blocks数

\* 这台服务器，所有的缓存都用到了，要么增加key_buffer_size，要么就是过渡索引了，把缓存占满了，理想设置：

Key_blocks_used / (Key_blocks_unused + Key_blocks_used) * 100% ≈ 80%

Key_reads ##在内存中没有找到直接从硬盘读取索引

Key_read_requests ##一共索引请求

\* 计算索引未名字概率：

key_cache_miss_rate = Key_reads / Key_read_requests * 100%

\###Qcache查询缓冲区：

Qcache_free_blocks ##Query Cache 中目前还有多少剩余的blocks

Qcache_free_memory ##Query Cache 剩余的内存大小

Qcache_hits ##多少次命中

Qcache_inserts ##多少次未命中的插入： Qcache_hits / ( Qcache_hits + Qcache_inserts )

Qcache_lowmem_prunes ##多少条Query 因为内存不足而被清除出Query Cache

Qcache_not_cached ##因为query_cache_type 的设置或者不能被cache 的Query 的数量；

Qcache_queries_in_cache ##当前Query Cache中的cache 的Query数量

Slow_queries ##慢查询

Sort_range ##通过range scan 完成的排序总次数

Sort_rows ##排序总行数

Sort_scan ##通过扫描完成的排序总次数

Table_locks_immediate ##可以立即获取锁的查询次数。

Table_locks_waited ##不能立即获取锁的查询次数。

Uptime ##mysql 运行时长

**三、****zabbix agnet****自定义key：**

UserParameter=mysql[*],mysql -uroot -pxxx -e "show global status"|grep "$1" | cut -f2

Mysql[Uptime]

Grep uptime | cut -f2

Myslq[Table_locks_waited]

注释：通过key传回的值，$1筛选出我们要的值。



## 死锁

死锁是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，可以认为如果一个资源被锁定，它总会在以后某个时间被释放。而死锁发生在当多个进程访问同一数据库时，其中每个进程拥有的锁都是其他进程所需的，由此造成每个进程都无法继续下去。

InnoDB的并发写操作会触发死锁，InnoDB也提供了死锁检测机制，可以通过设置innodb_deadlock_detect参数打开或关闭死锁检测：

```sql
-- 打开死锁检测，数据库发生死锁时自动回滚（默认选项）
innodb_deadlock_detect = on
-- 关闭死锁检测，发生死锁的时候，用锁超时来处理，
-- 通过设置锁超时参数innodb_lock_wait_timeout可以在超时发生时回滚被阻塞的事务
innodb_deadlock_detect = off
-- 保存全部死锁日志（该选项默认关闭，打开时死锁日志存放到error_log配置的文件里面）
set global innodb_print_all_deadlocks = on
```

还可以通过设置InnDB Monitors来进一步观察锁冲突详细信息。设置InnoDB Monitors方法：

```sql
create database test;
use test
create table innodb_monitor(a INT) engine=innodb;
create table innodb_tablespace_monitor(a INT) engine=innodb;
create table innodb_lock_monitor(a INT) engine=innodb;
create table innodb_table_monitor(a INT) engine=innodb;
```

常用SQL语句：

```sql
-- 查看死锁
show engine innodb status

-- 查询是否锁表
show OPEN TABLES where In_use > 0;

-- 数据库版本查询
select version();

-- 引擎查询
show create table {tableName};
SHOW TABLE STATUS FROM dbName WHERE name = 'tbName'

-- 事务隔离级别查询方法
select @@tx_isolation;

-- 事务隔离级别设置方法（只对当前Session生效）：
set session transaction isolation level read committed;
/**
 * 注意：
 *   1. 如果数据库是分库的，以上SQL语句需要在单库上执行，不能在逻辑库执行。
 *   2. 全局生效，需要修改my.ini
 */

-- 方法1：利用 metadata_locks 视图
-- 此方法仅适用于 MySQL 5.7 以上版本，该版本 performance_schema 新增了 metadata_locks，
-- 如果上bai锁前启用了元数据锁的探针（默认是未启用的），可以比较容易的定位全局锁会话。
-- 1：查看当前的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_TRX;
-- 2：查看当前锁定的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS;
-- 3：查看当前等锁的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;

-- 方法2：利用 events_statements_history 视图
-- 此方法适用于 MySQL 5.6 以上版本，启用 performance_schema.eventsstatements_history（5.6 默认未启用，5.7 默认启用），
-- 该表会 SQL 历史记录执行，如果请求太多，会自动清理早期的信息，有可能将上锁会话的信息清理掉。

-- 方法3：利用 gdb 工具
-- 如果上述两种都用不了或者没来得及启用，可以尝试第三种方法。
-- 利用 gdb 找到所有线程信息，查看每个线程中持有全局锁对象，输出对应的会话 ID。
-- 也可以使用 gdb 交互模式，但 attach mysql 进程后 mysql 会完全 hang 住，读请求也会受到影响，不建议使用交互模式。

-- 方法4：show processlist
-- 如果备份程序使用的特定用户执行备份，如果是 root 用户备份，那 time 值越大的是持锁会话的概率越大，
-- 如果业务也用 root 访问，重点是 state 和 info 为空的，这里有个小技巧可以快速筛选，筛选后尝试 kill 对应 ID，
-- 再观察是否还有 wait global read lock 状态的会话。

-- 方法5：重启！
```

**解决思路：**

1. 使用临时表保存全部待操作记录（增删改）
2. 给业务表加表级锁
3. 将临时表数据同步到业务表
4. 释放表级锁

**问题：**业务表在大量并发操作下，会发生什么？



## 总结

1. 在1个SQL语句中临时表只能查询一次！连接断开后，自动删除
2. 存储过程（函数）的迁移不要使用 Navicat，会引起 **编码** 异常！！！用自己的脚本创建。



### 编码设置

> 前言：在创建数据库的时候，会有这样一个选项->排序规则，平时在创建数据库的时候并没有注意，只是选择了默认，也没感觉有什么问题，今天看到这个突然好奇起来，所以看了一些资料做了以下的一些总结，若有错误之处请斧正。

这个排序规则的作用是什么？可以避免一些在数据库导入时出现的一些错误。很多时候在导入数据库的时候会出现字符乱码的问题，但是如果定制编码的话，就会更容易的发现问题。在mysql中我们经常使用的是utf8_unicode_ci和utf8_general_ci,两者还是有一些区别的，当前，utf8_unicode_ci校对规则仅部分支持Unicode校对规则算法。一些字符还是不能支持。并且，不能完全支持组合的记号。这主要影响越南和俄罗斯的一些少数民族语言，如：Udmurt 、Tatar、Bashkir和Mari。

utf8_general_ci是一个遗留的 校对规则，不支持扩展。它仅能够在字符之间进行逐个比较。这意味着utf8_general_ci校对规则进行的比较速度很快，但是与使用utf8_unicode_ci的 校对规则相比，比较正确性较差）。

例如，使用utf8_general_ci和utf8_unicode_ci两种 校对规则下面的比较相等： Ä = A Ö = O Ü = U 两种校对规则之间的区别是，对于utf8_general_ci下面的等式成立： ß = s 但是，对于utf8_unicode_ci下面等式成立： ß = ss 对于一种语言仅当使用utf8_unicode_ci排序做的不好时，才执行与具体语言相关的utf8字符集 校对规则。例如，对于德　　语和法语，utf8_unicode_ci工作的很好，因此不再需要为这两种语言创建特殊的utf8校对规则。 utf8_general_ci也适用与德语和法语，除了‘ß'等于‘s'，而不是‘ss'之外。　　如果你的应用能够接受这些，那么应该使用utf8_general_ci，因为它速度快。否则，使用utf8_unicode_ci，因为它比较准确。

上面我们讲到utf8_xxxx_ci，但是对于上面的编码格式后面的ci还是有些不解，当然不只是ci，还有ki，wi什么的，他们代表的是什么尼？

排序规则名称由两部份构成，前半部份是指本排序规则所支持的字符集。如：Chinese_PRC_CS_AI_WS，前半部份：指UNICODE字符集，Chinese_PRC指针对大陆简体字UNICODE的排序规则。排序规则的后半部份即后缀含义：BIN 二进制排序、CI(CS) 是否区分大小写（CI不区分，CS区分）、AI(AS) 是否区分重音（AI不区分，AS区分）、KI(KS) 是否区分假名类型（KI不区分，KS区分）、WI(WS) 是否区分宽度（WI不区分，WS）。

现在对排序规则有一定的了解之后就明白自己需要那种编码格式了，平时我都是使用utf8_general_ci，最好是做到编码统一，就会减少数据库乱码这种情况的发生。

```sql
-- gbk: create database `test2` default character set gbk collate gbk_chinese_ci;
-- utf8: create database `test2` default character set utf8 collate utf8_general_ci;

show variables like '%character%';
set character_set_client = utf8;
set character_set_connection = utf8;
set character_set_database = utf8;
set character_set_results = utf8;/*这里要注意很有用*/
set character_set_server = utf8;

show variables like '%collation%';
set collation_connection = utf8_unicode_ci;
set collation_database = utf8_unicode_ci;
set collation_server = utf8_unicode_ci;

-- 查看数据表的编码格式
show create table <表名>;
-- 修改数据库的编码格式
alter database <数据库名> character set utf8;
-- 修改数据表格编码格式
alter table <表名> character set utf8;
-- 修改字段编码格式
alter table <表名> change <字段名> <字段名> <类型> character set utf8;

-- my.ini中配置默认编码
default-character-set=utf8

-- 数据库连接串中指定字符集：
url=jdbc:mysql://yourip/college?user=root&password=yourpassword&useunicode=true&characterencoding=gbk
```





## 性能优化



### 引擎优化

**1. 内存利用方面**

**innodb_buffer_pool_size**

这个是 Innodb 最重要的参数，和 MyISAM 的 key_buffer_size 有相似之处，但也是有差别的。这个参数主要缓存 innodb 表的索引，数据，插入数据时的缓冲。

该参数分配内存的原则：这个参数默认分配只有8M，可以说是非常小的一个值。如果是一个专用DB服务器，那么他可以占到内存的70%-80%。这个参数不能动态更改，所以分配需多考虑。分配过大，会使 Swap 占用过多，致使 Mysql 的查询特慢。如果你的数据比较小，那么可分配是你的 数据大小+10% 左右做为这个参数的值。

例如：数据大小为50M，那么给这个值分配 innodb_buffer_pool_size＝64M

设置方法，在my.cnf文件里：innodb_buffer_pool_size=4G

> 注意：
>
> 在 Mysql5.7 版本之前，调整 innodb_buffer_pool_size 大小必须在 my.cnf 配置里修改，然后重启 mysql 进程才可以生效。
> 如今到了 Mysql5.7 版本，就可以直接动态调整这个参数，方便了很多。
>
> 尤其是在服务器内存增加之后，运维人员不能粗心大意，要记得调大 Innodb_Buffer_Pool_size 这个参数。数据库配置后，要注意检查 Innodb_Buffer_Pool_size 这个参数的设置是否合理
>
> 需要注意的地方：
>
> 在调整 innodb_buffer_pool_size 期间，用户的请求将会阻塞，直到调整完毕，所以请勿在白天调整，在凌晨3-4点低峰期调整。
> 调整时，内部把数据页移动到一个新的位置，单位是块。如果想增加移动的速度，需要调整 innodb_buffer_pool_chunk_size 参数的大小，默认是128M。

**innodb_additional_mem_pool_size**

用来存放 Innodb 的内部目录，这个值不用分配太大，系统可以自动调。通常设置 16Ｍ 够用了，如果表比较多，可以适当的增大。

设置方法，在 my.cnf 文件里：`innodb_additional_mem_pool_size = 16M`

**2. 关于日志方面**

**innodb_log_file_size**

作用：指定在一个日志组中，每个log的大小。结合 innodb_buffer_pool_size 设置其大小，25%-100%。避免不需要的刷新。

> 注意：这个值分配的大小和数据库的写入速度，事务大小，异常重启后的恢复有很大的关系。一般取256M可以兼顾性能和recovery的速度。

分配原则：几个日值成员大小加起来差不多和你的 innodb_buffer_pool_size 相等。上限为每个日值上限大小为4G。一般控制在几个Log文件相加大小在2G以内为佳。具体情况还需要看你的事务大小，数据大小为依据。

说明：这个值分配的大小和数据库的写入速度，事务大小，异常重启后的恢复有很大的关系。

设置方法：在my.cnf文件里：`innodb_log_file_size = 256M`

**innodb_log_files_in_group**

作用：指定你有几个日值组。

分配原则：一般我们可以用2-3个日值组。默认为两个。

设置方法：在my.cnf文件里：
innodb_log_files_in_group=3

innodb_log_buffer_size：
作用：事务在内存中的缓冲，也就是日志缓冲区的大小， 默认设置即可，具有大量事务的可以考虑设置为16M。
如果这个值增长过快，可以适当的增加innodb_log_buffer_size
另外如果你需要处理大理的TEXT，或是BLOB字段，可以考虑增加这个参数的值。
设置方法：在my.cnf文件里：
innodb_log_buffer_size=3M

innodb_flush_logs_at_trx_commit
作用：控制事务的提交方式,也就是控制log的刷新到磁盘的方式。
分配原则：这个参数只有3个值（0，1，2）.默认为1，性能更高的可以设置为0或是2，这样可以适当的减少磁盘IO（但会丢失一秒钟的事务。），游戏库的MySQL建议设置为0。主库请不要更改了。
其中：
0：log buffer中的数据将以每秒一次的频率写入到log file中，且同时会进行文件系统到磁盘的同步操作，但是每个事务的commit并不会触发任何log buffer 到log file的刷新或者文件系统到磁盘的刷新操作；
1：（默认为1）在每次事务提交的时候将logbuffer 中的数据都会写入到log file，同时也会触发文件系统到磁盘的同步；
2：事务提交会触发log buffer 到log file的刷新，但并不会触发磁盘文件系统到磁盘的同步。此外，每秒会有一次文件系统到磁盘同步操作。
说明：
这个参数的设置对Ｉｎｎｏｄｂ的性能有很大的影响，所以在这里给多说明一下。
当这个值为1时：innodb 的事务LOG在每次提交后写入日值文件，并对日值做刷新到磁盘。这个可以做到不丢任何一个事务。
当这个值为2时：在每个提交，日志缓冲被写到文件，但不对日志文件做到磁盘操作的刷新,在对日志文件的刷新在值为2的情况也每秒发生一次。但需要注意的是，由于进程调用方面的问题，并不能保证每秒１００％的发生。从而在性能上是最快的。但操作系统崩溃或掉电才会删除最后一秒的事务。
当这个值为0时：日志缓冲每秒一次地被写到日志文件，并且对日志文件做到磁盘操作的刷新，但是在一个事务提交不做任何操作。mysqld进程的崩溃会删除崩溃前最后一秒的事务。
从以上分析，当这个值不为１时，可以取得较好的性能，但遇到异常会有损失，所以需要根据自已的情况去衡量。
设置方法：在my.cnf文件里：
innodb_flush_logs_at_trx_commit=1

3）文件IO分配，空间占用方面
innodb_file_per_table
作用：使每个Innodb的表，有自已独立的表空间。如删除文件后可以回收那部分空间。默认是关闭的，建议打开（innodb_file_per_table=1）
分配原则：只有使用不使用。但DB还需要有一个公共的表空间。
设置方法：在my.cnf文件里：
innodb_file_per_table=1

innodb_file_io_threads
作用：文件读写IO数，这个参数只在Windows上起作用。在Linux上只会等于4，默认即可！
设置方法：在my.cnf文件里：
innodb_file_io_threads=4

innodb_open_files
作用：限制Innodb能打开的表的数据。
分配原则：这个值默认是300。如果库里的表特别多的情况，可以适当增大为1000。innodb_open_files的大小对InnoDB效率的影响比较小。但是在InnoDBcrash的情况下，innodb_open_files设置过小会影响recovery的效率。所以用InnoDB的时候还是把innodb_open_files放大一些比较合适。
设置方法：在my.cnf文件里：
innodb_open_files=800

innodb_data_file_path
指定表数据和索引存储的空间，可以是一个或者多个文件。最后一个数据文件必须是自动扩充的，也只有最后一个文件允许自动扩充。这样，当空间用完后，自动扩充数据文件就会自动增长（以8MB为单位）以容纳额外的数据。
例如： innodb_data_file_path=/disk1/ibdata1:900M;/disk2/ibdata2:50M:autoextend 两个数据文件放在不同的磁盘上。数据首先放在ibdata1 中，当达到900M以后，数据就放在ibdata2中。
设置方法，在my.cnf文件里：
innodb_data_file_path =ibdata1:1G;ibdata2:1G;ibdata3:1G;ibdata4:1G;ibdata5:1G;ibdata6:1G:autoextend

innodb_data_home_dir
放置表空间数据的目录，默认在mysql的数据目录，设置到和MySQL安装文件不同的分区可以提高性能。
设置方法，在my.cnf文件里：（比如mysql的数据目录是/data/mysql/data，这里可以设置到不通的分区/home/mysql下）
innodb_data_home_dir = /home/mysql

4）其它相关参数（适当的增加table_cache）
这里说明一个比较重要的参数：
innodb_flush_method
作用：Innodb和系统打交道的一个IO模型
分配原则：
Windows不用设置。
linux可以选择：O_DIRECT
直接写入磁盘，禁止系统Cache了
设置方法：在my.cnf文件里：
innodb_flush_method=O_DIRECT

innodb_max_dirty_pages_pct
作用：在buffer pool缓冲中，允许Innodb的脏页的百分比，值在范围1-100,默认为90，建议保持默认。
这个参数的另一个用处：当Innodb的内存分配过大，致使Swap占用严重时，可以适当的减小调整这个值，使达到Swap空间释放出来。建义：这个值最大在90%，最小在15%。太大，缓存中每次更新需要致换数据页太多，太小，放的数据页太小，更新操作太慢。
设置方法：在my.cnf文件里：
innodb_max_dirty_pages_pct＝90
动态更改需要有管理员权限：
set global innodb_max_dirty_pages_pct=50;

innodb_thread_concurrency
同时在Innodb内核中处理的线程数量。建议默认值。
设置方法，在my.cnf文件里：
innodb_thread_concurrency = 16

5）公共参数调优
skip-external-locking
MyISAM存储引擎也同样会使用这个参数，MySQL4.0之后，这个值默认是开启的。
作用是避免MySQL的外部锁定(老版本的MySQL此参数叫做skip-locking)，减少出错几率增强稳定性。建议默认值。
设置方法，在my.cnf文件里：
skip-external-locking

skip-name-resolve
禁止MySQL对外部连接进行DNS解析（默认是关闭此项设置的，即默认解析DNS），使用这一选项可以消除MySQL进行DNS解析的时间。
但需要注意，如果开启该选项，则所有远程主机连接授权都要使用IP地址方式，否则MySQL将无法正常处理连接请求！如果需要，可以设置此项。
设置方法，在my.cnf文件里：（我这线上mysql数据库中打开了这一设置）
skip-name-resolve

max_connections
设置最大连接（用户）数，每个连接MySQL的用户均算作一个连接，max_connections的默认值为100。此值需要根据具体的连接数峰值设定。
设置方法，在my.cnf文件里：
max_connections = 3000

query_cache_size
查询缓存大小，如果表的改动非常频繁，或者每次查询都不同，查询缓存的结果会减慢系统性能。可以设置为0。
设置方法，在my.cnf文件里：
query_cache_size = 512M

sort_buffer_size
connection级的参数，排序缓存大小。一般设置为2-4MB即可。
设置方法，在my.cnf文件里：
sort_buffer_size = 1024M

read_buffer_size
connection级的参数。一般设置为2-4MB即可。
设置方法，在my.cnf文件里：
read_buffer_size = 1024M

max_allowed_packet
网络包的大小，为避免出现较大的网络包错误，建议设置为16M
设置方法，在my.cnf文件里：
max_allowed_packet = 16M

table_open_cache
当某一连接访问一个表时，MySQL会检查当前已缓存表的数量。如果该表已经在缓存中打开，则会直接访问缓存中的表，以加快查询速度；如果该表未被缓存，则会将当前的表添加进缓存并进行查询。
通过检查峰值时间的状态值Open_tables和Opened_tables，可以决定是否需要增加table_open_cache的值。
如果发现open_tables等于table_open_cache，并且opened_tables在不断增长，那么就需要增加table_open_cache的值;设置为512即可满足需求。
设置方法，在my.cnf文件里：
table_open_cache = 512

myisam_sort_buffer_size
实际上这个myisam_sort_buffer_size参数意义不大，这是个字面上蒙人的参数，它用于ALTER TABLE, OPTIMIZE TABLE, REPAIR TABLE 等命令时需要的内存。默认值即可。
设置方法，在my.cnf文件里：
myisam_sort_buffer_size = 8M

thread_cache_size
线程缓存，如果一个客户端断开连接，这个线程就会被放到thread_cache_size中（缓冲池未满），SHOW STATUS LIKE 'threads%';如果 Threads_created 不断增大，那么当前值设置要改大，改到 Threads_connected 值左右。（通常情况下，这个值改善性能不大），默认8即可
设置方法，在my.cnf文件里：
thread_cache_size = 8

innodb_thread_concurrency
线程并发数，建议设置为CPU内核数*2
设置方法，在my.cnf文件里：
innodb_thread_concurrency = 8

key_buffer_size
仅作用于 MyISAM存储引擎，用来设置用于缓存 MyISAM存储引擎中索引文件的内存区域大小。如果我们有足够的内存，这个缓存区域最好是能够存放下我们所有的 MyISAM 引擎表的所有索引，以尽可能提高性能。不要设置超过可用内存的30%。即使不用MyISAM表，也要设置该值8-64M，用于临时表。
设置方法，在my.cnf文件里：
key_buffer_size = 8M



### SQL优化

SQL优化主要分4个方向：`SQL语句跟索引`、`表结构`、`系统配置`、`硬件`。

总优化思路就是**最大化利用索引**、**尽可能避免全表扫描**、**减少无效数据的查询**：

> 1、减少数据访问：设置`合理的字段类型`，启用压缩，通过索引访问等减少磁盘 IO。
>
> 2、返回更少的数据：只`返回需要`的字段和数据分页处理，减少磁盘 IO 及网络 IO。
>
> 3、减少交互次数：`批量` DML 操作，函数存储等减少数据连接次数。
>
> 4、减少服务器 CPU 开销：**尽量减少数据库排序操作以及全表查询**，减少 CPU 内存占用 。
>
> 5、分表分区：使用`表分区`，可以增加并行操作，更大限度利用 CPU 资源。

**SQL语句优化大致举例**：

> 1、合理建立覆盖索引：可以有效减少回表。
>
> 2、union，or，in都能命中索引，建议使用in 
>
> 3、负向条件(!=、<>、not in、not exists、not like 等) 索引不会使用索引，建议用in。
>
> 4、在列上进行运算或使用函数会使索引失效，从而进行全表扫描 
>
> 5、小心隐式类型转换，原字符串用整型会触发`CAST`函数导致索引失效。原int用字符串则会走索引。
>
> 6、不建议使用%前缀模糊查询。
>
> 7、多表关联查询时，小表在前，大表在后。在 MySQL 中，执行 from 后的表关联查询是从左往右执行的（Oracle 相反），第一张表会涉及到全表扫描。
>
> 8、调整 Where 字句中的连接顺序，MySQL 采用从左往右，自上而下的顺序解析 where 子句。根据这个原理，应将过滤数据多的条件往前放，最快速度缩小结果集。

**SQL调优大致思路**：

1、先用慢查询日志定位具体需要优化的sql 

2、使用 [explain](https://mp.weixin.qq.com/s?__biz=MzI4NjI1OTI4Nw==&mid=2247488546&idx=1&sn=732ca84abf572196ddf76597fe096969&scene=21#wechat_redirect) 执行计划查看索引使用情况 

3、重点关注（一般情况下根据这4列就能找到索引问题）：

> 1、key（查看有没有使用索引） 
>
> 2、key_len（查看索引使用是否充分）
>
> 3、type（查看索引类型） 
>
> 4、Extra（查看附加信息：排序、临时表、where条件为false等）

4、根据上1步找出的索引问题优化sql 5、再回到第2步

![x](./Resources/db004.PNG)

**表结构优化**：

> 1、尽量使用TINYINT、SMALLINT、MEDIUM_INT作为整数类型而非INT，如果非负则加上UNSIGNED 。
>
> 2、VARCHAR的长度只分配真正需要的空间 。
>
> 3、尽量使用TIMESTAMP而非DATETIME 。
>
> 4、单表不要有太多字段，建议在20以内。
>
> 5、避免使用NULL字段，很难查询优化且占用额外索引空间。字符串默认为''。

**读写分离**：

> 只在主服务器上写，只在从服务器上读。对应到数据库集群一般都是一主一从、一主多从。业务服务器把需要写的操作都写到主数据库中，读的操作都去从库查询。主库会同步数据到从库保证数据的一致性。一般 [读写分离](https://mp.weixin.qq.com/s?__biz=MzA5NDIzNzY1OQ==&mid=2735617707&idx=2&sn=6fd038b3385c1175a6efd4ef00543e35&scene=21#wechat_redirect) 的实现方式有两种：`代码封装`跟`数据库中间件`。

**分库分表**：[分库分表](https://mp.weixin.qq.com/s?__biz=MzkzNTEwOTAxMA==&mid=2247484479&idx=1&sn=97358231f0f7086f0056fc5bb4e8afff&scene=21#wechat_redirect)分为垂直和水平两个方式，一般是`先垂直后水平`。

> 1、`垂直分库`：将应用分为若干模块，比如订单模块、用户模块、商品模块、支付模块等等。其实就是微服务的理念。
>
> 2、`垂直分表`：一般将不常用字段跟数据较大的字段做拆分。
>
> 3、`水平分表`：根据场景选择什么字段作分表字段，比如淘宝日订单1000万，用userId作分表字段，数据查询支持到最近6个月的订单，超过6个月的做归档处理，那么6个月的数据量就是18亿，分1024张表，每个表存200W数据，hash(userId)%100找到对应表格。
>
> 4、`ID生成器`：[分布式ID](https://mp.weixin.qq.com/s?__biz=MzI4NjI1OTI4Nw==&mid=2247485459&idx=1&sn=9baf434bdeebe98be60bcde7df702f22&scene=21#wechat_redirect) 需要跨库全局唯一方便查询存储-检索数据，确保唯一性跟数字递增性。

目前主要流行的分库分表工具 就是`Mycat`和`sharding-sphere`。

**TiDB**：开源`分布式`数据库，结合了传统的 RDBMS 和NoSQL 的最佳特性。TiDB 兼容 MySQL，`支持无限的水平扩展`，具备强一致性和高可用性。TiDB 的目标是为 OLTP(Online Transactional Processing) 和 OLAP (Online Analytical Processing) 场景提供一站式的解决方案。TiDB 具备如下核心特点

> 1、支持 MySQL 协议（开发接入成本低）。
>
> 2、100% 支持事务（数据一致性实现简单、可靠）。
>
> 3、无限水平拓展（不必考虑分库分表），不停服务。
>
> 4、TiDB 支持和 MySQL 的互备。
>
> 5、遵循jdbc原则，学习成本低，强关系型，强一致性，不用担心主从配置，不用考虑分库分表，还可以无缝动态扩展。

适合：

> 1、原业务的 MySQL 的业务遇到单机容量或者性能瓶颈时，可以考虑使用 TiDB 无缝替换 MySQL。
>
> 2、大数据量下，MySQL 复杂查询很慢。
>
> 3、大数据量下，数据增长很快，接近单机处理的极限，不想分库分表或者使用数据库中间件等对业务侵入性较大、对业务有约束的 Sharding 方案。
>
> 4、大数据量下，有高并发实时写入、实时查询、实时统计分析的需求。
>
> 5、有分布式事务、多数据中心的数据 100% 强一致性、auto-failover 的高可用的需求。

不适合：

> 1、单机 MySQL 能满足的场景也用不到 TiDB。
>
> 2、数据条数少于 5000w 的场景下通常用不到 TiDB，TiDB 是为大规模的数据场景设计的。
>
> 3、如果你的应用数据量小（所有数据千万级别行以下），且没有高可用、强一致性或者多数据中心复制等要求，那么就不适合使用 TiDB。





**1、为查询缓存优化你的查询**

大多数的 MySQL 服务器都开启了查询缓存。这是提高性最有效的方法之一，而且这是被 MySQL 的数据库引擎处理的。当有很多相同的查询被执行了多次的时候，这些查询结果会被放到一个缓存中，这样，后续的相同的查询就不用操作表而直接访问缓存结果了。

这里最主要的问题是，对于程序员来说，这个事情是很容易被忽略的。因为，我们某些查询语句会让 MySQL 不使用缓存。请看下面的示例：

```php
// 查询缓存不开启
$r = mysql_query("SELECT username FROM user WHERE signup_date >= CURDATE()");

// 开启查询缓存
$today = date("Y-m-d");
$r = mysql_query("SELECT username FROM user WHERE signup_date >= '$today'");
```

上面两条 SQL 语句的差别就是 CURDATE() ，MySQL 的查询缓存对这个函数不起作用。所以，像 NOW() 和 RAND() 或是其它的诸如此类的 SQL 函数都不会开启查询缓存，因为这些函数的返回是不定的。所以，你所需要的就是用一个变量来代替 MySQL 的函数，从而开启缓存。

**2、SQL语句中IN包含的值不应过多**

MySQL对于IN做了相应的优化，即将IN中的常量全部存储在一个数组里面，而且这个数组是排好序的。但是如果数值较多，产生的消耗也是比较大的。再例如：select id from t where num in(1,2,3) 对于连续的数值，能用between就不要用in了；再或者使用连接来替换。

**3、当只要一行数据时使用 LIMIT 1**

当你查询表的有些时候，你已经知道结果只会有一条结果，但因为你可能需要去 `fetch` 游标，或是你也许会去检查返回的记录数。

在这种情况下，加上 `LIMIT 1` 可以增加性能。这样一样，MySQL 数据库引擎会在找到一条数据后停止搜索，而不是继续往后查少下一条符合记录的数据。

**4、为搜索字段建索引**

如果在你的表中，有某个字段你总要会经常用来做搜索，那么，请为其建立索引。

另外，你应该也需要知道什么样的搜索是不能使用正常的索引的。例如，当你需要在一篇大的文章中搜索一个词时，如：`WHERE post_content LIKE '%apple%'`，索引可能是没有意义的。你可能需要使用 MySQL全文索引 或是自己做一个索引（比如说：搜索关键词或是 Tag 什么的）。

**5、在Join表的时候使用相同类型的列，并将其索引**

不同类型字段Join，无法使用索引！对于那些 STRING 类型，还需要有相同的字符集才行。

**6、千万不要 ORDER BY RAND()**

```sql
select id from `dynamic` order by rand() limit 1000;
-- 上面的SQL语句，可优化为：
select id from `dynamic` t1 join (select rand() * (select max(id) from `dynamic`) as nid) t2 on t1.id > t2.nid limit 1000;
```

**7、避免 SELECT *，如果排序字段没有用到索引，就尽量少排序**

**8、永远为每张表设置一个ID**

最好是一个 INT 型的（推荐使用UNSIGNED），并设置上自动增加的 `AUTO_INCREMENT` 标志，使用 VARCHAR 类型来当主键会使用得性能下降。

只有一个情况是例外，那就是“关联表”的“外键”，也就是说，这个表的主键，通过若干个别的表的主键构成，我们把这个情况叫做“外键”。比如：有一个“学生表”有学生的ID，有一个“课程表”有课程ID，那么，“成绩表”就是“关联表”了，其关联了学生表和课程表，在成绩表中，学生ID和课程ID叫“外键”，其共同组成主键。

**9、使用 ENUM 而不是 VARCHAR**

ENUM 类型是非常快和紧凑的。在实际上，其保存的是 TINYINT，但其外表上显示为字符串。这样一来，用这个字段来做一些选项列表变得相当的完美。

**10、从 PROCEDURE ANALYSE() 取得建议**

只有表中有实际的数据，这些建议才会变得有用；数据不够多，决策可能就做得不够准；数据越来越多，建议才会变得准确。一定要记住，你才是最终做决定的人。

**11、尽可能的使用 NOT NULL**

"Empty" 和 "NULL" 有多大的区别（如果是INT，那就是0和NULL）？如果你觉得它们之间没有什么区别，那么你就不要使用NULL。（在 Oracle 里，NULL 和 Empty 的字符串是一样的！)

NULL 也需要额外的空间，并且，在进行比较的时候，程序会更复杂。

当然，这里并不是说不能使用 NULL，现实情况很复杂，依然会有一些情况，需要使用 NULL 值。

**12、Prepared Statements**

Prepared Statements 很像存储过程，是一种运行在后台的 SQL 语句集合，我们可以从使用 prepared statements 获得很多好处，无论是性能问题还是安全问题。

Prepared Statements 可以检查一些你绑定好的变量，这样可以保护你的程序不会受到“SQL注入式”攻击。当然，你也可以手动地检查你的这些变量，然而，手动的检查容易出问题，而且经常会被程序员忘了。当我们使用一些 framework 或是 ORM 的时候，这样的问题会好一些。

在性能方面，当一个相同的查询被使用多次的时候，这会为你带来可观的性能优势。你可以给这些 Prepared Statements 定义一些参数，而 MySQL 只会解析一次。

最新版本的 MySQL 在传输 Prepared Statements 是使用二进制形式，所以这会使得网络传输非常有效率。

当然，也有一些情况下，我们需要避免使用Prepared Statements，因为其不支持查询缓存，但据说版本5.1后支持了。

**13、无缓冲的查询**

正常的情况下，当你在你的脚本中执行一个SQL语句的时候，你的程序会停在那里直到这个SQL语句返回，然后你的程序再往下继续执行。你可以使用无缓冲查询来改变这个行为。

>思考：使用场景？

**14、把 IP 地址存成 UNSIGNED INT**

可以使用 `INET_ATON()` 来把一个字符串 IP 转成一个整形，并使用 `INET_NTOA()` 把一个整形转成一个字符串 IP。

**15、固定长度的表会更快**

如果表中的所有字段都是“固定长度”的，整个表会被认为是 [static 或 fixed-length](http://dev.mysql.com/doc/refman/5.1/en/static-format.html)。例如，表中没有如下类型的字段：VARCHAR，TEXT，BLOB。只要你包括了其中一个这些字段，那么这个表就不是“固定长度静态表”了，这样，MySQL 引擎会用另一种方法来处理。

固定长度的表会提高性能，因为MySQL搜寻得会更快一些，因为这些固定的长度是很容易计算下一个数据的偏移量的，所以读取的自然也会很快。而如果字段不是定长的，那么，每一次要找下一条的话，需要程序找到主键。

并且，固定长度的表也更容易被缓存和重建。不过，唯一的副作用是，固定长度的字段会浪费一些空间，因为定长的字段无论你用不用，他都是要分配那么多的空间。

使用“垂直分割”技术，你可以分割你的表成为两个一个是定长的，一个则是不定长的。

**16、垂直分割**

“垂直分割”是一种把数据库中的表按列变成几张表的方法，这样可以降低表的复杂度和字段的数目，从而达到优化的目的。

示例一：在 "Users" 表中有一个字段是家庭地址，这个字段是可选字段，而且你在数据库操作的时候除了个人信息外，并不需要经常读取或是改写这个字段。那么，为什么不把他放到另外一张表中呢？ 这样会让你的表有更好的性能。大多数时候，对于用户表来说，只有用户ID，用户名，口令，用户角色等会被经常使用，小一点的表总是会有好的性能。

示例二：你有一个叫 "last_login" 的字段，它会在每次用户登录时被更新。但是，***每次更新时会导致该表的查询缓存被清空***。所以，你可以把这个字段放到另一个表中，这样就不会影响你对用户ID，用户名，用户角色的不停地读取了，因为查询缓存会帮你增加很多性能。

另外，需要注意的是，这些被分出去的字段所形成的表，不需要经常Join，否则，性能会比不分割时还要差，而且，会是指数级的下降。

**17、拆分大的 DELETE 或 INSERT 语句**

如果需要在一个在线的网站上去执行一个大的 DELETE 或 INSERT 查询，你需要非常小心，要避免你的操作让你的整个网站停止响应。因为这两个操作是会锁表的，表一锁住，别的操作都进不来了。

Apache 会有很多的子进程或线程。所以，其工作起来相当有效率，而我们的服务器却不希望有太多的子进程，线程和数据库链接，这是极大的占服务器资源的事情，尤其是内存。

如果你把表锁上一段时间，比如30秒钟，那么对于一个有很高访问量的站点来说，这30秒所积累的访问进程/线程，数据库链接，打开的文件数，可能不仅仅会让你的WEB服务Crash，还可能会让你的整台服务器挂掉。

所以，如果有这种大的处理，一定要拆分，使用 LIMIT 条件是一个好方法！

**18、越小的列会越快**

对于大多数数据库引擎来说，硬盘操作可能是最大的瓶颈。所以，把数据变得紧凑会非常有帮助，因为这减少了对硬盘的访问。

参看 MySQL 的文档 [Storage Requirements](http://dev.mysql.com/doc/refman/5.0/en/storage-requirements.html) 查看所有的数据类型。

如果一个表只会有几行（比如说字典表，配置表），那么，我们就没有理由使用 INT 来做主键，使用 MEDIUMINT, SMALLINT 或是更小的 TINYINT 会更经济一些。如果你不需要记录时间，使用 DATE 要比 DATETIME 好得多。

当然，你也需要留足够的扩展空间，不然，日后来干这个事，你会死的很难看，参看[Slashdot的例子（2009年11月06日）](http://news.slashdot.org/article.pl?sid=06/11/09/1534204)，一个简单的 ALTER TABLE 语句花了3个多小时，因为里面有一千六百万条数据。

**19、选择正确的存储引擎**

在 MySQL 中常用两个存储引擎 MyISAM 和 InnoDB，每个引擎都有利有弊。酷壳以前文章[《MySQL: InnoDB 还是 MyISAM?》](https://coolshell.cn/articles/652.html)讨论过这个事情。

MyISAM 适合于一些需要大量查询的应用，但其对于大量写操作并不是很友好。甚至你只是需要 update 一个字段，整个表都会被锁起来，而别的进程，就算是读进程都无法操作直到表被释放。不过，MyISAM 对于 `SELECT COUNT(*)` 这类的计算是超快无比的。

InnoDB 是一个非常复杂的存储引擎，它比 MyISAM 还慢，但是它支持“行锁” ，于是在写操作比较多的时候，会更优秀。并且，他还支持更多的高级应用，比如：事务。

下面是MySQL的手册：

- [The MyISAM Storage Engine](https://dev.mysql.com/doc/refman/8.0/en/myisam-storage-engine.html)

**20、使用一个对象关系映射器（Object Relational Mapper）**

使用 ORM (Object Relational Mapper)，你能够获得可靠的性能增长。一个 ORM 可以做的所有事情，也能被手动的编写出来。但是，这需要一个高级专家。

ORM 的最重要的是 "Lazy Loading"，也就是说，只有在需要去取值的时候才会真正的去做。但你也要小心这种机制的副作用，因为这很可能会去创建很多很多小的查询降低性能。

ORM 还可以把你的 SQL 语句打包成一个事务，这会比单独执行他们快得多得多。

PHP 的 ORM：[Doctrine](http://www.doctrine-project.org/)。

**21、小心“永久链接”**

“永久链接”的目的是用来减少重新创建 MySQL 链接的次数。当一个链接被创建了，它会永远处在连接的状态，就算是数据库操作已经结束了。而且，自从我们的 Apache 开始重用它的子进程后——也就是说，下一次的 HTTP 请求会重用 Apache 的子进程，并重用相同的 MySQL 链接。

PHP手册：[mysql_pconnect()](http://php.net/manual/en/function.mysql-pconnect.php)

在理论上来说，这听起来非常的不错。但是从个人经验上来说，这个功能制造出来的麻烦事更多。因为，你只有有限的链接数，内存问题，文件句柄数，等等。

而且，Apache 运行在极端并行的环境中，会创建很多很多的子进程。这就是为什么这种“永久链接”的机制工作不好的原因。在你决定要使用“永久链接”之前，你需要好好地考虑一下你的整个系统的架构。

**22、如果限制条件中其他字段没有索引，尽量少用or**

or两边的字段中，如果有一个不是索引字段，而其他条件也不是索引字段，会造成该查询不走索引的情况。很多时候使用union all或者是union（必要的时候）的方式来代替“or”会得到更好的效果。

**23、尽量用union all代替union**

union和union all的差异主要是前者需要将结果集合并后再进行唯一性过滤操作，这就会涉及到排序，增加大量的CPU运算，加大资源消耗及延迟。当然，union all的前提条件是两个结果集没有重复数据。

**24、巧用STRAIGHT_JOIN**

inner join是由MySQL选择驱动表，但是有些特殊情况需要选择另个表作为驱动表，比如有group by、order by等「Using filesort」、「Using temporary」时。STRAIGHT_JOIN来强制连接顺序，在STRAIGHT_JOIN左边的表名就是驱动表，右边则是被驱动表。在使用STRAIGHT_JOIN有个前提条件是该查询是内连接，也就是inner join。其他链接不推荐使用STRAIGHT_JOIN，否则可能造成查询结果不准确。

**25、关于JOIN优化**

LEFT JOIN A表为驱动表，INNER JOIN MySQL会自动找出那个数据少的表作用驱动表，RIGHT JOIN B表为驱动表。

**注意：**

**1）MySQL中没有full join，可以用以下方式来解决：**

```sql
select * from A left join B on B.name = A.name where B.name is null union allselect * from B;
```

**2）尽量使用inner join，避免left join：**

参与联合查询的表至少为2张表，一般都存在大小之分。如果连接方式是inner join，在没有其他过滤条件的情况下MySQL会自动选择小表作为驱动表，但是left join在驱动表的选择上遵循的是左边驱动右边的原则，即left join左边的表名为驱动表。

**3）合理利用索引：**

被驱动表的索引字段作为on的限制字段。

**4）利用小表去驱动大表**



### 压缩

参考：[https://www.jb51.net/article/116140.htm](#https://www.jb51.net/article/116140.htm)



### 备份

备份：

```bash
:: 设置MySql数据库的IP
set ipaddress=10.30.100.106
set port=3306

:: 要备份MySql数据库名
set db_name1=lead_pm
set db_name2=lead_basic
set db_name3=lead_pm_1.6
set db_name4=lead_basic_1.6
set db_name5=lead_perf

set db_name6=lead_attence
set db_name7=lead_general

:: 获取当前月份 yyyymm 202006
:: 获取备份时的时间戳 yyyymmddHHmiss 20200622164535
set backup_month=%date:~0,4%%date:~5,2%
set backup_date=%date:~0,4%%date:~5,2%%date:~8,2%
:: set backup_time=%date:~0,4%%date:~5,2%%date:~8,2%%time:~0,2%%time:~3,2%%time:~6,2%

:: 备份目录(如果没有则创建)
SET floder=C:\Database\backup\%backup_month%
if not exist %floder% md "%floder%" 

:: 设置mysqldump 备份的参数
set pm_db=-uroot -p123 -h %ipaddress% -P %port% %db_name1%
set basic_db=-uroot -p123 -h %ipaddress% -P %port% %db_name2%
set pm_db_1_6=-uroot -p123 -h %ipaddress% -P %port% %db_name3%
set basic_db_1_6=-uroot -p123 -h %ipaddress% -P %port% %db_name4%
set perf_db=-uroot -p123 -h %ipaddress% -P %port% %db_name5%

set attence_db=-uroot -p123 -h %ipaddress% -P %port% %db_name6%
set general_db=-uroot -p123 -h %ipaddress% -P %port% %db_name7%

:: 使用mysqldump对指定的MySql进行备份
C:\mysql_test\bin\mysqldump --single-transaction %pm_db% --opt -R -E >Product_Full_%db_name1%_%backup_date%.sql
C:\mysql_test\bin\mysqldump --single-transaction %basic_db% --opt -R -E >Product_Full_%db_name2%_%backup_date%.sql
C:\mysql_test\bin\mysqldump --single-transaction %pm_db_1_6% --opt -R -E >Product_Full_%db_name3%_%backup_date%.sql
C:\mysql_test\bin\mysqldump --single-transaction %basic_db_1_6% --opt -R -E >Product_Full_%db_name4%_%backup_date%.sql
C:\mysql_test\bin\mysqldump --single-transaction %perf_db% --opt -R -E >Product_Full_%db_name5%_%backup_date%.sql

C:\mysql_test\bin\mysqldump --single-transaction %attence_db% --opt -R -E >Product_Full_%db_name6%_%backup_date%.sql
C:\mysql_test\bin\mysqldump --single-transaction %general_db% --opt -R -E >Product_Full_%db_name7%_%backup_date%.sql

:: 用7-zip对备份出的sql文件进行压缩，此处使用7zip进行压缩，也可以换成其它的压缩命令
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name1%_%backup_date%.zip"  Product_Full_%db_name1%_%backup_date%.sql
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name2%_%backup_date%.zip"  Product_Full_%db_name2%_%backup_date%.sql
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name3%_%backup_date%.zip"  Product_Full_%db_name3%_%backup_date%.sql
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name4%_%backup_date%.zip"  Product_Full_%db_name4%_%backup_date%.sql
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name5%_%backup_date%.zip"  Product_Full_%db_name5%_%backup_date%.sql

"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name6%_%backup_date%.zip"  Product_Full_%db_name6%_%backup_date%.sql
"C:\Program Files\7-Zip\7z.exe" a "%floder%\Product_Full_%db_name7%_%backup_date%.zip"  Product_Full_%db_name7%_%backup_date%.sql

::  删除已压缩的备份文件
del Product_Full_*.sql

:: 生成ftp的参数文件，把压缩后的zip文件上传到FTP服务器
echo open 10.30.100.105 8080>>temp.txt    
echo user administrator>>temp.txt
echo Qwerty654>>temp.txt
echo cd BackUp\Test>>temp.txt
echo put "%floder%\Product_Full_%db_name1%_%backup_date%.zip">>temp.txt
echo put "%floder%\Product_Full_%db_name2%_%backup_date%.zip">>temp.txt
echo put "%floder%\Product_Full_%db_name3%_%backup_date%.zip">>temp.txt
echo put "%floder%\Product_Full_%db_name4%_%backup_date%.zip">>temp.txt
echo put "%floder%\Product_Full_%db_name5%_%backup_date%.zip">>temp.txt

echo put "%floder%\Product_Full_%db_name6%_%backup_date%.zip">>temp.txt
echo put "%floder%\Product_Full_%db_name7%_%backup_date%.zip">>temp.txt
echo bye>>temp.txt

:: 执行上传命令
ftp -i -n -s:temp.txt  

:: 删除ftp的临时参数文件
del temp.txt
:: 删除两天前备份的压缩文件
:: del MYSQL_100_%del_zip_file%.zip 
```

恢复：

```bash

```



## 升华



**Mysql** **性能优化教程**

# 目录

目录................................................................................................................................. 1

背景及目标...................................................................................................................... 2

Mysql 执行优化............................................................................................................... 2

认识数据索引............................................................................................................ 2

为什么使用数据索引能提高效率......................................................................... 2

如何理解数据索引的结构................................................................................... 2

优化实战范例..................................................................................................... 3

认识影响结果集........................................................................................................ 4

影响结果集的获取.............................................................................................. 4

影响结果集的解读.............................................................................................. 4

常见案例及优化思路.......................................................................................... 5

理解执行状态............................................................................................................ 7

常见关注重点..................................................................................................... 7

执行状态分析..................................................................................................... 8

分析流程............................................................................................................ 9

常见案例解析................................................................................................... 11

总结................................................................................................................. 12

Mysql 运维优化............................................................................................................. 14

存储引擎类型.......................................................................................................... 14

内存使用考量.......................................................................................................... 14

性能与安全性考量................................................................................................... 14

存储/写入压力优化.................................................................................................. 15

运维监控体系.......................................................................................................... 15

Mysql 架构优化............................................................................................................. 17

架构优化目标.......................................................................................................... 17

防止单点隐患................................................................................................... 17

方便系统扩容................................................................................................... 17

安全可控，成本可控........................................................................................ 17

分布式方案............................................................................................................. 18

分库&拆表方案................................................................................................ 18

反范式设计（冗余结构设计）.......................................................................... 20

主从架构.......................................................................................................... 21

故障转移处理................................................................................................... 22

缓存方案................................................................................................................. 22

缓存结合数据库的读取..................................................................................... 22

缓存结合数据库的写入..................................................................................... 23

总结............................................................................................................................... 24

 

 

# 背景及目标

l 厦门游家公司（4399.com）用于员工培训和分享。

l 针对用户群为已经使用过mysql环境，并有一定开发经验的工程师

l 针对高并发，海量数据的互联网环境。

l 本文语言为口语，非学术标准用语。

l 以实战和解决具体问题为主要目标，非应试，非常规教育。友情提醒，在校生学习本教程可能对成绩提高有害无益。

l 非技术挑战，非高端架构师培训，请高手自动忽略。

l 本文档在2011年7月-12月持续更新，加强了影响结果集分析的内容并增补优化实战案例若干。

# Mysql 执行优化

## 认识数据索引

### 为什么使用数据索引能提高效率

n 关系型数据库的数据索引（Btree及常见索引结构）的存储是有序的。

n 在有序的情况下，通过索引查询一个数据是无需遍历索引记录的

n 关系型数据库数据索引的查询效率趋近于二分法查询效率，趋近于 log2(N)。

n 极端情况下（更新请求少，更新实时要求低，查询请求频繁），建立单向有序序列可替代数据索引。

n HASH索引的查询效率是寻址操作，趋近于1次查询，比有序索引查询效率更高，但是不支持比对查询，区间查询，排序等操作，仅支持key-value类型查询。不是本文重点。

### 如何理解数据索引的结构

n 数据索引通常默认采用btree索引，（内存表也使用了hash索引）。

n 仅就有序前提而言，单向有序排序序列是查找效率最高的（二分查找，或者说折半查找），使用树形索引的目的是为了达到快速的更新和增删操作。

n 在极端情况下（比如数据查询需求量非常大，而数据更新需求极少，实时性要求不高，数据规模有限），直接使用单一排序序列，折半查找速度最快。

n 在进行索引分析和SQL优化时，可以将数据索引字段想象为单一有序序列，并以此作为分析的基础。涉及到复合索引情况，复合索引按照索引顺序拼凑成一个字段，想象为单一有序序列，并以此作为分析的基础。

n 一条数据查询只能使用一个索引，索引可以是多个字段合并的复合索引。但是一条数据查询不能使用多个索引。

### 优化实战范例

l 实战范例1： ip地址反查

n 资源： Ip地址对应表，源数据格式为 startip, endip, area 

源数据条数为 10万条左右，呈很大的分散性

n 目标：  需要通过任意ip查询该ip所属地区

性能要求达到每秒1000次以上的查询效率

n 挑战：  如使用 between startip and endip 这样的条件数据库操作，因为涉及两个字段的between and, 无法有效使用索引。

如果每次查询请求需要遍历10万条记录，根本不行。

n 方法：  一次性排序（只在数据准备中进行，数据可存储在内存序列）

​       折半查找（每次请求以折半查找方式进行）

l 实战范例2：目标：查找与访问者同一地区的异性，按照最后登录时间逆序

n 挑战：高访问量社区的高频查询，如何优化。

​       查询SQL: select * from user where area=’$area’ and sex=’$sex’ order by lastlogin desc limit 0,30;

​        建立复合索引并不难， area+sex+lastlogin 三个字段的复合索引,如何理解？

n 解读：首先，忘掉btree，将索引字段理解为一个排序序列。

另外，牢记数据查询只能使用一个索引，每个字段建立独立索引的情况下，也只能有一条索引被使用！

​    如果只使用area会怎样？搜索会把符合area的结果全部找出来，然后在这里面遍历，选择命中sex的并排序。 遍历所有 area=’$area’数据！

如果使用了area+sex，略好，仍然要遍历所有area=’$area’ and sex=’$sex’数据，然后在这个基础上排序！！

​    Area+sex+lastlogin复合索引时（切记lastlogin在最后），该索引基于area+sex+lastlogin 三个字段合并的结果排序，该列表可以想象如下。

​    广州女$时间1

​    广州女$时间2

​    广州女$时间3

​       …

​    广州男

….

​    深圳女

….

数据库很容易命中到 area+sex的边界，并且基于下边界向上追溯30条记录，搞定！在索引中迅速命中所有结果，无需二次遍历！

## 认识影响结果集

### 影响结果集的获取

n 通过Explain 分析SQL，查看 rows 列内容

n 通过慢查询日志的Rows_examined: 后面的数字

n 影响结果集数字是查询优化的重要中间数字，工程师在开发和调试过程中，应随时关注这一数字。

### 影响结果集的解读

n 查询条件与索引的关系决定影响结果集。

u 影响结果集不是输出结果数，不是查询返回的记录数，而是索引所扫描的结果数。

u 范例 select * from user where area=’厦门’ and sex=’女’ 

l 假设 索引为 area

l 假设User表中 area=’厦门’的有 125000条，而搜索返回结果为60233条。

l 影响结果集是125000条，索引先命中125000条厦门用户，再遍历以sex=’女’进行筛选操作，得到60233条结果。

l 如果该SQL 增加 limit 0,30的后缀。查询时，先命中 area=’厦门’，然后依顺序执行 sex=’女’ 筛选操作，直到满足可以返回30条为止，所涉及记录数未知。除非满足条件的结果不足30条，否则不会遍历125000条记录。

l 但是如果SQL中涉及了排序操作，比如 order by lastlogin desc 再有limit 0,30时，排序需要遍历所有area=’厦门’ 的记录，而不是满足即止。

n 影响结果集越趋近于实际输出或操作的目标结果集，索引效率越高。

n 影响结果集与查询开销的关系可以理解为线性相关。减少一半影响结果集，即可提升一倍查询效率！当一条搜索query可以符合多个索引时，选择影响结果集最少的索引。

n SQL的优化，核心就是对结果集的优化，认识索引是增强对结果集的判断，基于索引的认识，可以在编写SQL的时候，对该SQL可能的影响结果集有预判，并做出适当的优化和调整。

n Limit 的影响，需要斟酌对待

u 如果索引与查询条件和排序条件完全命中，影响结果集就是limit后面的数字（$start + $end），比如 limit 200,30 影响结果集是230. 而不是30.

u 如果索引只命中部分查询条件，甚至无命中条件，在无排序条件情况下，会在索引命中的结果集 中遍历到满足所有其他条件为止。比如 select * from user limit 10; 虽然没用到索引，但是因为不涉及二次筛选和排序，系统直接返回前10条结果，影响结果集依然只有10条，就不存在效率影响。

u 如果搜索所包含的排序条件没有被索引命中，则系统会遍历是所有索引所命中的结果，并且排序。例如 Select * from user order by timeline desc limit 10; 如果timeline不是索引，影响结果集是全表，就存在需要全表数据排序，这个效率影响就巨大。再比如 Select * from user where area=’厦门’ order by timeline desc limit 10; 如果area是索引，而area+timeline未建立索引，则影响结果集是所有命中 area=’厦门’的用户，然后在影响结果集内排序。

 

### 常见案例及优化思路

n 毫秒级优化案例

u 某游戏用户进入后显示最新动态，SQL为 select * from userfeed where uid=$uid order by timeline desc limit 20; 主键为$uid 。 该SQL每天执行数百万次之多，高峰时数据库负载较高。 通过 show processlist 显示大量进程处于Sending data状态。没有慢查询记录。 仔细分析发现，因存在较多高频用户访问，命中 uid=$uid的影响结果集通常在几百到几千，在上千条影响结果集情况下，该SQL查询开销通常在0.01秒左右。 建立uid+timeline 复合索引，将排序引入到索引结构中，影响结果集就只有limit 后面的数字，该SQL查询开销锐减至0.001秒，数据库负载骤降。

n Innodb锁表案例

u 某游戏数据库使用了innodb，innodb是行级锁，理论上很少存在锁表情况。出现了一个SQL语句(delete from tabname where xid=…)，这个SQL非常用SQL，仅在特定情况下出现，每天出现频繁度不高（一天仅10次左右），数据表容量百万级，但是这个xid未建立索引，于是悲惨的事情发生了，当执行这条delete 的时候，真正删除的记录非常少，也许一到两条，也许一条都没有；但是！由于这个xid未建立索引，delete操作时遍历全表记录，全表被delete操作锁定，select操作全部被locked，由于百万条记录遍历时间较长，期间大量select被阻塞，数据库连接过多崩溃。

这种非高发请求，操作目标很少的SQL，因未使用索引，连带导致整个数据库的查询阻塞，需要极大提高警觉。

n 实时排名策略优化

u 背景： 用户提交游戏积分，显示实时排名。

u 原方案： 

l 提交积分是插入记录，略， 

l select count(*) from jifen where gameid=$gameid and fenshu>$fenshu

u 问题与挑战

l 即便索引是 gameid+fenshu 复合索引，涉及count操作，当分数较低时，影响结果集巨大，查询效率缓慢，高峰期会导致连接过多。

u 优化思路

l 减少影响结果集，又要取得实时数据，单纯从SQL上考虑，不太有方法。

l 将游戏积分预定义分成数个积分断点，然后分成积分区间，原始状态，每个区间设置一个统计数字项，初始为0。

l 每次积分提交时，先确定该分数属于哪两个区间之间，这个操作非常简单，因为区间是预定义的，而且数量很少，只需遍历即可，找到最该分数符合的区间， 该区间的统计数字项（独立字段，可用内存处理，异步回写数据库或文件）+1。 记录该区间上边界数字为$duandian。

l SQL: select count(*) from jifen where gameid=$gameid and fenshu>$fenshu and fenshu<$duandian，如果处于第一区间，则无需$duandian，这样因为第一区间本身也是最好的成绩，影响结果集不会很多。 通过该SQL获得其在该区间的名次。

l 获取前面区间的总数总和。（该数字是直接从上述提到的区间统计数字获取，不需要进行count操作）将区间内名次+前区间的统计数字和，获得总名次。

l 该方法关键在于，积分区间需要合理定义，保证积分提交成绩能平均散落在不同区间。

l 如涉及较多其他条件，如日排行，总排行，以及其他独立用户去重等，请按照影响结果集思路自行发挥。

u Redis方案

l Redis数据结构包括String,list,dict和Zset四种，在本案例中是非常好的替代数据库的方案，本文档只做简介，不做额外扩展。

l String 哈希索引，key-value结构，主键查询效率极高，不支持排序，比较查询。

l List 队列结构，在数据异步写入处理中可以替代memcache。

l Dict 数组结构，存储结构化，序列化内容，可以针对数组中的特定列进行操作。

l Zset 有序数组结构，分两个子结构，第一是多层树形的存储结构，第二是每个树形节点的计数器，这样类似于前面的分段方式，可以理解为多层分段方式，所以查询效率更高，缺点是更新效率有所增加。

n 论坛翻页优化

u 背景，常见论坛帖子页 SQL: select * from post where tagid=$tagid order by lastpost limit $start, $end 翻页 。索引为 tagid+lastpost 复合索引

u 挑战， 超级热帖，几万回帖，用户频频翻到末页，limit 25770,30 一个操作下来，影响结果集巨大(25770+30)，查询缓慢。

u 解决方法：

l 只涉及上下翻页情况

n 每次查询的时候将该页查询结果中最大的 $lastpost和最小的分别记录为 $minlastpost 和 $maxlastpost ，上翻页查询为 select * from post where tagid=$tagid and lastpost<$minlastpost order by lastpost desc limit 30; 下翻页为 select * from post where tagid=$tagid and lastpost>$maxlastpost order by lastpost limit 30; 使用这种方式，影响结果集只有30条，效率极大提升。

l 涉及跳转到任意页

n 互联网上常见的一个优化方案可以这样表述，select * from post where tagid=$tagid and lastpost>=(select lastpost from post where tagid=$tagid order by lastpost limit $start,1) order by lastpost limit 30; 或者 select * from post where pid in (select pid from post where tagid=$tagid order by lastpost limit $start,30); (第2条S语法在新的mysql版本已经不支持，新版本mysql in的子语句不再支持limit条件，但可以分解为两条SQL实现，原理不变，不做赘述)

n 以上思路在于，子查询的影响结果集仍然是$start +30，但是数据获取的过程（Sending data状态）发生在索引文件中，而不是数据表文件，这样所需要的系统开销就比前一种普通的查询低一个数量级，而主查询的影响结果集只有30条，几乎无开销。但是切记，这里仍然涉及了太多的影响结果集操作。

u 延伸问题：

l 来自于uchome典型查询 SELECT * FROM uchome_thread WHERE tagid='73820' ORDER BY displayorder DESC, lastpost DESC LIMIT $start,30; 

l 如果换用 如上方法，上翻页代码 SELECT * FROM uchome_thread WHERE tagid='73820' and lastpost<$minlastpost ORDER BY displayorder DESC,lastpost DESC LIMIT 0,30; 下翻页代码SELECT * FROM uchome_thread WHERE tagid='73820' and lastpost>$maxlastpost ORDER BY displayorder DESC, lastpost ASC LIMIT 0,30;

l 这里涉及一个order by 索引可用性问题，当order by中 复合索引的字段，一个是ASC，一个是DESC 时，其排序无法在索引中完成。 所以只有上翻页可以正确使用索引，影响结果集为30。下翻页无法在排序中正确使用索引，会命中所有索引内容然后排序，效率低下。

l 总结：

n 基于影响结果集的理解去优化，不论从数据结构，代码，还是涉及产品策略上，都需要贯彻下去。

n 涉及 limit $start,$num的搜索，如果$start巨大，则影响结果集巨大，搜索效率会非常难过低，尽量用其他方式改写为 limit 0,$num； 确系无法改写的情况下，先从索引结构中获得 limit $start,$num 或limit $start,1 ；再用in操作或基于索引序的 limit 0,$num 二次搜索。

n 请注意，我这里永远不会讲关于外键和join的优化，因为在我们的体系里，这是根本不允许的！ 架构优化部分会解释为什么。

## 理解执行状态

### 常见关注重点

l 慢查询日志，关注重点如下

n 是否锁定，及锁定时间

u 如存在锁定，则该慢查询通常是因锁定因素导致，本身无需优化，需解决锁定问题。

n 影响结果集

u 如影响结果集较大，显然是索引项命中存在问题，需要认真对待。

l Explain 操作

n 索引项使用

u 不建议用using index做强制索引，如未如预期使用索引，建议重新斟酌表结构和索引设置。

n 影响结果集

u 这里显示的数字不一定准确，结合之前提到对数据索引的理解来看，还记得嘛？就把索引当作有序序列来理解，反思SQL。

l Set profiling , show profiles for query操作

n 执行开销

u 注意，有问题的SQL如果重复执行，可能在缓存里，这时要注意避免缓存影响。通过这里可以看到。

u 执行时间超过0.005秒的频繁操作SQL建议都分析一下。

u 深入理解数据库执行的过程和开销的分布

l Show processlist 执行状态监控

n 这是在数据库负载波动时经常进行的一项操作

n 具体参见如下

### 执行状态分析

l Sleep 状态 

n 通常代表资源未释放，如果是通过连接池，sleep状态应该恒定在一定数量范围内

n 实战范例： 因前端数据输出时（特别是输出到用户终端）未及时关闭数据库连接，导致因网络连接速度产生大量sleep连接，在网速出现异常时，数据库 too many connections 挂死。

n 简单解读，数据查询和执行通常只需要不到0.01秒，而网络输出通常需要1秒左右甚至更长，原本数据连接在0.01秒即可释放，但是因为前端程序未执行close操作，直接输出结果，那么在结果未展现在用户桌面前，该数据库连接一直维持在sleep状态！

l Waiting for net, reading from net, writing to net

n 偶尔出现无妨

n 如大量出现，迅速检查数据库到前端的网络连接状态和流量

n 案例: 因外挂程序，内网数据库大量读取，内网使用的百兆交换迅速爆满，导致大量连接阻塞在waiting for net，数据库连接过多崩溃

l Locked状态

n 有更新操作锁定

n 通常使用innodb可以很好的减少locked状态的产生，但是切记，更新操作要正确使用索引，即便是低频次更新操作也不能疏忽。如上影响结果集范例所示。

n 在myisam的时代，locked是很多高并发应用的噩梦。所以mysql官方也开始倾向于推荐innodb。

l Copy to tmp table

n 索引及现有结构无法涵盖查询条件，才会建立一个临时表来满足查询要求，产生巨大的恐怖的i/o压力。

n 很可怕的搜索语句会导致这样的情况，如果是数据分析，或者半夜的周期数据清理任务，偶尔出现，可以允许。频繁出现务必优化之。

n Copy to tmp table 通常与连表查询有关，建议逐渐习惯不使用连表查询。

n 实战范例：

u 某社区数据库阻塞，求救，经查，其服务器存在多个数据库应用和网站，其中一个不常用的小网站数据库产生了一个恐怖的copy to tmp table 操作，导致整个硬盘i/o和cpu压力超载。Kill掉该操作一切恢复。

l Sending data

n Sending data 并不是发送数据，别被这个名字所欺骗，这是从物理磁盘获取数据的进程，如果你的影响结果集较多，那么就需要从不同的磁盘碎片去抽取数据，

n 偶尔出现该状态连接无碍。

n 回到上面影响结果集的问题，一般而言，如果sending data连接过多，通常是某查询的影响结果集过大，也就是查询的索引项不够优化。

n 前文提到影响结果集对SQL查询效率线性相关，主要就是针对这个状态的系统开销。

n 如果出现大量相似的SQL语句出现在show proesslist列表中，并且都处于sending data状态，优化查询索引，记住用影响结果集的思路去思考。

l Storing result to query cache

n 出现这种状态，如果频繁出现，使用set profiling分析，如果存在资源开销在SQL整体开销的比例过大（即便是非常小的开销，看比例），则说明query cache碎片较多

n 使用flush query cache 可即时清理，也可以做成定时任务

n Query cache参数可适当酌情设置。

l Freeing items

n 理论上这玩意不会出现很多。偶尔出现无碍

n 如果大量出现，内存，硬盘可能已经出现问题。比如硬盘满或损坏。

n i/o压力过大时，也可能出现Free items执行时间较长的情况。

l Sorting for …

n 和Sending data类似，结果集过大，排序条件没有索引化，需要在内存里排序，甚至需要创建临时结构排序。

l 其他

n 还有很多状态，遇到了，去查查资料。基本上我们遇到其他状态的阻塞较少，所以不关心。

### 分析流程

l 基本流程

n 详细了解问题状况

u Too many connections 是常见表象，有很多种原因。

u 索引损坏的情况在innodb情况下很少出现。

u 如出现其他情况应追溯日志和错误信息。

n 了解基本负载状况和运营状况

u 基本运营状况

l 当前每秒读请求

l 当前每秒写请求

l 当前在线用户

l 当前数据容量

u 基本负载情况

l 学会使用这些指令

n Top 

n Vmstat

n uptime 

n iostat 

n df 

l Cpu负载构成

n 特别关注i/o压力( wa%)

n 多核负载分配

l 内存占用

n Swap分区是否被侵占

n 如Swap分区被侵占，物理内存是否较多空闲

l 磁盘状态

n 硬盘满和inode节点满的情况要迅速定位和迅速处理

n 了解具体连接状况

u 当前连接数 

l Netstat –an|grep 3306|wc –l

l Show processlist

u 当前连接分布 show processlist

l 前端应用请求数据库不要使用root帐号！

n Root帐号比其他普通帐号多一个连接数许可。

n 前端使用普通帐号，在too many connections的时候root帐号仍可以登录数据库查询 show processlist!

n 记住，前端应用程序不要设置一个不叫root的root帐号来糊弄！非root账户是骨子里的，而不是名义上的。

l 状态分布

n 不同状态代表不同的问题，有不同的优化目标。

n 参见如上范例。

l 雷同SQL的分布

n 是否较多雷同SQL出现在同一状态

u 当前是否有较多慢查询日志

l 是否锁定

l 影响结果集

n 频繁度分析

u 写频繁度

l 如果i/o压力高，优先分析写入频繁度

l Mysqlbinlog 输出最新binlog文件，编写脚本拆分

l 最多写入的数据表是哪个

l 最多写入的数据SQL是什么

l 是否存在基于同一主键的数据内容高频重复写入？

n 涉及架构优化部分，参见架构优化-缓存异步更新

u 读取频繁度

l 如果cpu资源较高，而i/o压力不高，优先分析读取频繁度

l 程序中在封装的db类增加抽样日志即可，抽样比例酌情考虑，以不显著影响系统负载压力为底线。

l 最多读取的数据表是哪个

l 最多读取的数据SQL是什么

n 该SQL进行explain 和set profiling判定

n 注意判定时需要避免query cache影响

u 比如，在这个SQL末尾增加一个条件子句 and 1=1 就可以避免从query cache中获取数据，而得到真实的执行状态分析。 

l 是否存在同一个查询短期内频繁出现的情况

n 涉及前端缓存优化

n 抓大放小，解决显著问题

u 不苛求解决所有优化问题，但是应以保证线上服务稳定可靠为目标。

u 解决与评估要同时进行，新的策略或解决方案务必经过评估后上线。

### 常见案例解析

l 现象：服务器出现too many connections 阻塞

n 入手点：

u 查看服务器状态，cpu占用，内存占用，硬盘占用，硬盘i/o压力

u 查看网络流量状态，mysql与应用服务器的输入输出状况

u 通过Show processlist查看当前运行清单

l 注意事项，日常应用程序连接数据库不要使用root账户，保证故障时可以通过root 进入数据库查看 show processlist。

n 状态分析：

u 参见如上执行状态清单，根据连接状态的分布去确定原因。

n 紧急恢复

u 在确定故障原因后，应通过kill掉阻塞进程的方式 立即恢复数据库。

n 善后处理

u 以下针对常见问题简单解读

u Sleep 连接过多导致，应用端及时释放连接，排查关联因素。

u Locked连接过多，如源于myisam表级锁，更innodb引擎;如源于更新操作使用了不恰当的索引或未使用索引，改写更新操作SQL或建立恰当索引。

u Sending data连接过多，用影响结果集的思路优化SQL查询，优化表索引结构。

u Free items连接过多，i/o压力过大 或硬盘故障

u Waiting for net , writing to net 连接过多， mysql与应用服务器连接阻塞。

u 其他仍参见如上执行状态清单所示分析。

u 如涉及不十分严格安全要求的数据内容，可用定期脚本跟踪请求进程，并kill掉僵死进程。如数据安全要求较严格，则不能如此进行。

l 现象：数据库负载过高，响应缓慢。

n 入手点：

u 查看cpu状态，服务器负载构成

n 分支1：i/o占用过高。

u 步骤1： 检查内存是否占用swap分区，排除因内存不足导致的i/o开销。

u 步骤2：通过iostat 指令分析i/o是否集中于数据库硬盘，是否是写入度较高。

u 步骤3：如果压力来自于写，使用mysqlbinlog 解开最新的binlog文件。

u 步骤4：编写日志分析脚本或grep指令，分析每秒写入频度和写入内容。

l 写入频度不高，则说明i/o压力另有原因或数据库配置不合理。

u 步骤5：编写日志分析脚本或grep 指令，分析写入的数据表构成，和写入的目标构成。

u 步骤6：编写日志分析脚本，分析是否存在同一主键的重复写入。 比如出现大量 update post set views=views+1 where tagid=****的操作，假设在一段时间内出现了2万次，而其中不同的tagid有1万次，那么就是有50%的请求是重复update请求，有可以通过异步更新合并的空间。

u 提示一下，以上所提及的日志分析脚本编写，正常情况下不应超过1个小时，而对系统负载分析所提供的数据支持价值是巨大的，对性能优化方案的选择是非常有意义的，如果您认为这项工作是繁冗而且复杂的工作，那么一定是在分析思路和目标把握上出现了偏差。

n 分支2：i/o占用不高，CPU 占用过高

u 步骤1：查看慢查询日志

u 步骤2：不断刷新查看Show processlist清单，并把握可能频繁出现的处于Sending data状态的SQL。

u 步骤3：记录前端执行SQL

l 于前端应用程序执行查询的封装对象内，设置随机采样，记录前端执行的SQL，保证有一定的样本规模，并且不会带来前端i/o负载的激增。

l 基于采样率和记录频率，获得每秒读请求次数数据指标。

l 编写日志分析脚本，分析采样的SQL构成，所操作的数据表，所操作的主键。

l 对频繁重复读取的SQL(完全一致的SQL)进行判定，是否数据存在频繁变动，是否需要实时展现最新数据，如有可能，缓存化，并预估缓存命中率。

l 对频繁读取但不重复的(SQL结构一致，但条件中的数据不一致)SQL进行判定，是否索引足够优化，影响结果集与输出结果是否足够接近。

u 步骤4：将导致慢查询的SQL或频繁出现于show processlist状态的SQL，或采样记录的频繁度SQL进行分析，按照影响结果集的思路和索引理解来优化。

u 步骤5：对如上难以界定问题的SQL进行 set profiling 分析。

u 步骤6：优化后分析继续采样跟踪分析。并跟踪比对结果。

n 善后处理

u 日常跟踪脚本，不断记录一些状态信息。保证每个时间节点都能回溯。

u 确保随时能了解服务器的请求频次，读写请求的分布。

u 记录一些未造成致命影响的隐患点，可暂不解决，但需要记录。

u 如确系服务器请求频次过高，可基于负载分布决定硬件扩容方案，比如i/o压力过高可考虑固态硬盘；内存占用swap可考虑增加内容容量等。用尽可能少的投入实现最好的负载支撑能力，而不是简单的买更多服务器。

### 总结

l 要学会怎样分析问题，而不是单纯拍脑袋优化

l 慢查询只是最基础的东西，要学会优化0.01秒的查询请求。

l 当发生连接阻塞时，不同状态的阻塞有不同的原因，要找到原因，如果不对症下药，就会南辕北辙

n 范例：如果本身系统内存已经超载，已经使用到了swap，而还在考虑加大缓存来优化查询，那就是自寻死路了。

l 影响结果集是非常重要的中间数据和优化指标，学会理解这一概念，理论上影响结果集与查询效率呈现非常紧密的线性相关。

l 监测与跟踪要经常做，而不是出问题才做

n 读取频繁度抽样监测

u 全监测不要搞，i/o吓死人。

u 按照一个抽样比例抽样即可。

u 针对抽样中发现的问题，可以按照特定SQL在特定时间内监测一段全查询记录，但仍要考虑i/o影响。

n 写入频繁度监测

u 基于binlog解开即可，可定时或不定时分析。

n 微慢查询抽样监测

u 高并发情况下，查询请求时间超过0.01秒甚至0.005秒的，建议酌情抽样记录。

n 连接数预警监测

u 连接数超过特定阈值的情况下，虽然数据库没有崩溃，建议记录相关连接状态。

l 学会通过数据和监控发现问题，分析问题，而后解决问题顺理成章。特别是要学会在日常监控中发现隐患，而不是问题爆发了才去处理和解决。

**
**

# Mysql 运维优化

## 存储引擎类型

l Myisam 速度快，响应快。表级锁是致命问题。

l Innodb 目前主流存储引擎

n 行级锁 

u 务必注意影响结果集的定义是什么

u 行级锁会带来更新的额外开销，但是通常情况下是值得的。

n 事务提交 

u 对i/o效率提升的考虑

u 对安全性的考虑

l HEAP 内存引擎

n 频繁更新和海量读取情况下仍会存在锁定状况

## 内存使用考量

l 理论上，内存越大，越多数据读取发生在内存，效率越高

l Query cache的使用

n 如果前端请求重复度不高，或者应用层已经充分缓存重复请求，query cache不必设置很大，甚至可以不设置。

n 如果前端请求重复度较高，无应用层缓存，query cache是一个很好的偷懒选择

u 对于中等以下规模数据库应用，偷懒不是一个坏选择。

u 如果确认使用query cache，记得定时清理碎片，flush query cache.

l 要考虑到现实的硬件资源和瓶颈分布

l 学会理解热点数据，并将热点数据尽可能内存化

n 所谓热点数据，就是最多被访问的数据。

n 通常数据库访问是不平均的，少数数据被频繁读写，而更多数据鲜有读写。

n 学会制定不同的热点数据规则，并测算指标。

u 热点数据规模，理论上，热点数据越少越好，这样可以更好的满足业务的增长趋势。

u 响应满足度，对响应的满足率越高越好。

u 比如依据最后更新时间，总访问量，回访次数等指标定义热点数据，并测算不同定义模式下的热点数据规模

## 性能与安全性考量

l 数据提交方式

n innodb_flush_log_at_trx_commit = 1 每次自动提交，安全性高，i/o压力大

n innodb_flush_log_at_trx_commit = 2 每秒自动提交，安全性略有影响，i/o承载强。

l 日志同步

n Sync-binlog  =1 每条自动更新，安全性高，i/o压力大

n Sync-binlog = 0 根据缓存设置情况自动更新，存在丢失数据和同步延迟风险，i/o承载力强。

n 个人建议保存binlog日志文件，便于追溯 更新操作和系统恢复。

n 如对日志文件的i/o压力有担心，在内存宽裕的情况下，可考虑将binlog 写入到诸如 /dev/shm 这样的内存映射分区，并定时将旧有的binlog转移到物理硬盘。

l 性能与安全本身存在相悖的情况，需要在业务诉求层面决定取舍

n 学会区分什么场合侧重性能，什么场合侧重安全

n 学会将不同安全等级的数据库用不同策略管理

## 存储/写入压力优化

l 顺序读写性能远高于随机读写

l 将顺序写数据和随机读写数据分成不同的物理磁盘进行，有助于i/o压力的疏解

·     数据库文件涉及索引等内容，写入是随即写

·     binlog文件是顺序写

·     淘宝数据库存储优化是这样处理的

l 部分安全要求不高的写入操作可以用 /dev/shm 分区存储，简单变成内存写。

l 多块物理硬盘做raid10，可以提升写入能力

l 关键存储设备优化，善于比对不同存储介质的压力测试数据。

·     例如fusion-io在新浪和淘宝都有较多使用。

l 涉及必须存储较为庞大的数据量时

·     压缩存储，可以通过增加cpu开销（压缩算法）减少i/o压力。前提是你确认cpu相对空闲而i/o压力很大。 新浪微博就是压缩存储的典范。

·     通过md5去重存储，案例是QQ的文件共享，以及dropbox这样的共享服务，如果你上传的是一个别人已有的文件，计算md5后，直接通过md5定位到原有文件，这样可以极大减少存储量。涉及文件共享，头像共享，相册等应用，通过这种方法可以减少超过70%的存储规模，对硬件资源的节省是相当巨大的。缺点是，删除文件需要甄别该md5是否有其他人使用。 去重存储，用户量越多，上传文件越多，效率越高！

·     文件尽量不要存储到数据库内。尽量使用独立的文件系统存储，该话题不展开。

## 运维监控体系

l 系统监控

n 服务器资源监控

u Cpu, 内存，硬盘空间，i/o压力

u 设置阈值报警

n 服务器流量监控

u 外网流量，内网流量

u 设置阈值报警

n 连接状态监控

u Show processlist 设置阈值，每分钟监测，超过阈值记录

l 应用监控

n 慢查询监控

u 慢查询日志

u 如果存在多台数据库服务器，应有汇总查阅机制。

n 请求错误监控

u 高频繁应用中，会出现偶发性数据库连接错误或执行错误，将错误信息记录到日志，查看每日的比例变化。

u 偶发性错误，如果数量极少，可以不用处理，但是需时常监控其趋势。

u 会存在恶意输入内容，输入边界限定缺乏导致执行出错，需基于此防止恶意入侵探测行为。

n 微慢查询监控

u 高并发环境里，超过0.01秒的查询请求都应该关注一下。

n 频繁度监控

u 写操作，基于binlog，定期分析。

u 读操作，在前端db封装代码中增加抽样日志，并输出执行时间。

u 分析请求频繁度是开发架构 进一步优化的基础

u 最好的优化就是减少请求次数！

l 总结：

n 监控与数据分析是一切优化的基础。

n 没有运营数据监测就不要妄谈优化！

n 监控要注意不要产生太多额外的负载，不要因监控带来太多额外系统开销

**
**

# Mysql 架构优化

## 架构优化目标

### 防止单点隐患

l 所谓单点隐患，就是某台设备出现故障，会导致整体系统的不可用，这个设备就是单点隐患。

l 理解连带效应，所谓连带效应，就是一种问题会引发另一种故障，举例而言，memcache+mysql是一种常见缓存组合，在前端压力很大时，如果memcache崩溃，理论上数据会通过mysql读取，不存在系统不可用情况，但是mysql无法对抗如此大的压力冲击，会因此连带崩溃。因A系统问题导致B系统崩溃的连带问题，在运维过程中会频繁出现。

n 实战范例： 在mysql连接不及时释放的应用环境里，当网络环境异常（同机房友邻服务器遭受拒绝服务攻击，出口阻塞），网络延迟加剧，空连接数急剧增加，导致数据库连接过多崩溃。

n 实战范例2：前端代码 通常我们封装 mysql_connect和memcache_connect，二者的顺序不同，会产生不同的连带效应。如果mysql_connect在前，那么一旦memcache连接阻塞，会连带mysql空连接过多崩溃。

n 连带效应是常见的系统崩溃，日常分析崩溃原因的时候需要认真考虑连带效应的影响，头疼医头，脚疼医脚是不行的。

### 方便系统扩容

l 数据容量增加后，要考虑能够将数据分布到不同的服务器上。

l 请求压力增加时，要考虑将请求压力分布到不同服务器上。

l 扩容设计时需要考虑防止单点隐患。

### 安全可控，成本可控

l 数据安全，业务安全

l 人力资源成本>带宽流量成本>硬件成本

n 成本与流量的关系曲线应低于线性增长（流量为横轴，成本为纵轴）。

n 规模优势

l 本教程仅就与数据库有关部分讨论，与数据库无关部门请自行参阅其他学习资料。

​    

## 分布式方案

### 分库&拆表方案

l 基本认识

n 用分库&拆表是解决数据库容量问题的唯一途径。

n 分库&拆表也是解决性能压力的最优选择。

n 分库 – 不同的数据表放到不同的数据库服务器中（也可能是虚拟服务器）

n 拆表 – 一张数据表拆成多张数据表，可能位于同一台服务器，也可能位于多台服务器（含虚拟服务器）。

l 去关联化原则

n 摘除数据表之间的关联，是分库的基础工作。

n 摘除关联的目的是，当数据表分布到不同服务器时，查询请求容易分发和处理。

n 学会理解反范式数据结构设计，所谓反范式，第一要点是不用外键，不允许Join操作，不允许任何需要跨越两个表的查询请求。第二要点是适度冗余减少查询请求，比如说，信息表，fromuid, touid, message字段外，还需要一个fromuname字段记录用户名，这样查询者通过touid查询后，能够立即得到发信人的用户名，而无需进行另一个数据表的查询。

n 去关联化处理会带来额外的考虑，比如说，某一个数据表内容的修改，对另一个数据表的影响。这一点需要在程序或其他途径去考虑。

l 分库方案

n 安全性拆分

u 将高安全性数据与低安全性数据分库，这样的好处第一是便于维护，第二是高安全性数据的数据库参数配置可以以安全优先，而低安全性数据的参数配置以性能优先。参见运维优化相关部分。

n 基于业务逻辑拆分

u 根据数据表的内容构成，业务逻辑拆分，便于日常维护和前端调用。

u 基于业务逻辑拆分，可以减少前端应用请求发送到不同数据库服务器的频次，从而减少链接开销。

u 基于业务逻辑拆分，可保留部分数据关联，前端web工程师可在限度范围内执行关联查询。

n 基于负载压力拆分

u 基于负载压力对数据结构拆分，便于直接将负载分担给不同的服务器。

u 基于负载压力拆分，可能拆分后的数据库包含不同业务类型的数据表，日常维护会有一定的烦恼。

n 混合拆分组合

u 基于安全与业务拆分为数据库实例，但是可以使用不同端口放在同一个服务器上。

u 基于负载可以拆分为更多数据库实例分布在不同数据库上

u 例如，

l 基于安全拆分出A数据库实例，

l 基于业务拆分出B,C数据库实例，

l C数据库存在较高负载，基于负载拆分为C1,C2,C3,C4等 实例。

l 数据库服务器完全可以做到 A+B+C1 为一台，C2,C3,C4各单独一台。

 

l 分表方案

n 数据量过大或者访问压力过大的数据表需要切分

n 纵向分表（常见为忙闲分表）

u 单数据表字段过多，可将频繁更新的整数数据与非频繁更新的字符串数据切分

u 范例 user表 ，个人简介，地址，QQ号，联系方式，头像 这些字段为字符串类型，更新请求少； 最后登录时间，在线时常，访问次数，信件数这些字段为整数型字段，更新频繁，可以将后面这些更新频繁的字段独立拆出一张数据表，表内容变少，索引结构变少，读写请求变快。

n 横向切表

u 等分切表，如哈希切表或其他基于对某数字取余的切表。等分切表的优点是负载很方便的分布到不同服务器；缺点是当容量继续增加时无法方便的扩容，需要重新进行数据的切分或转表。而且一些关键主键不易处理。

u 递增切表，比如每1kw用户开一个新表，优点是可以适应数据的自增趋势；缺点是往往新数据负载高，压力分配不平均。

u 日期切表，适用于日志记录式数据，优缺点等同于递增切表。

u 个人倾向于递增切表，具体根据应用场景决定。

n 热点数据分表

u 将数据量较大的数据表中将读写频繁的数据抽取出来，形成热点数据表。通常一个庞大数据表经常被读写的内容往往具有一定的集中性，如果这些集中数据单独处理，就会极大减少整体系统的负载。

u 热点数据表与旧有数据关系

l 可以是一张冗余表，即该表数据丢失不会妨碍使用，因源数据仍存在于旧有结构中。优点是安全性高，维护方便，缺点是写压力不能分担，仍需要同步写回原系统。

l 可以是非冗余表，即热点数据的内容原有结构不再保存，优点是读写效率全部优化；缺点是当热点数据发生变化时，维护量较大。

l 具体方案选择需要根据读写比例决定，在读频率远高于写频率情况下，优先考虑冗余表方案。

u 热点数据表可以用单独的优化的硬件存储，比如昂贵的闪存卡或大内存系统。

u 热点数据表的重要指标

l 热点数据的定义需要根据业务模式自行制定策略，常见策略为，按照最新的操作时间；按照内容丰富度等等。

l 数据规模，比如从1000万条数据，抽取出100万条热点数据。

l 热点命中率，比如查询10次，多少次命中在热点数据内。

l 理论上，数据规模越小，热点命中率越高，说明效果越好。需要根据业务自行评估。

u 热点数据表的动态维护

l 加载热点数据方案选择

n 定时从旧有数据结构中按照新的策略获取

n 在从旧有数据结构读取时动态加载到热点数据

l 剔除热点数据方案选择

n 基于特定策略，定时将热点数据中访问频次较少的数据剔除

n 如热点数据是冗余表，则直接删除即可，如不是冗余表，需要回写给旧有数据结构。

u 通常，热点数据往往是基于缓存或者key-value 方案冗余存储，所以这里提到的热点数据表，其实更多是理解思路，用到的场合可能并不多….

### 反范式设计（冗余结构设计）

l 反范式设计的概念

n 无外键，无连表查询。

n 便于分布式设计，允许适度冗余，为了容量扩展允许适度开销。

n 基于业务自由优化，基于i/o 或查询设计，无须遵循范式结构设计。

l 冗余结构设计所面临的典型场景

n 原有展现程序涉及多个表的查询，希望精简查询程序

n 数据表拆分往往基于主键，而原有数据表往往存在非基于主键的关键查询，无法在分表结构中完成。

n 存在较多数据统计需求（count, sum等），效率低下。

l 冗余设计方案

n 基于展现的冗余设计

u 为了简化展现程序，在一些数据表中往往存在冗余字段

u 举例，信息表  message，存在字段 fromuid,touid,msg,sendtime  四个字段，其中 touid+sendtime是复合索引。存在查询为 select * from message where touid=$uid order by sendtime desc limit 0,30;

u 展示程序需要显示发送者姓名，此时通常会在message表中增加字段fromusername，甚至有的会增加fromusersex，从而无需连表查询直接输出信息的发送者姓名和性别。这就是一种简单的，为了避免连表查询而使用的冗余字段设计。

n 基于查询的冗余设计

u 涉及分表操作后，一些常见的索引查询可能需要跨表，带来不必要的麻烦。确认查询请求远大于写入请求时，应设置便于查询项的冗余表。

u 冗余表要点

l 数据一致性，简单说，同增，同删，同更新。

l 可以做全冗余，或者只做主键关联的冗余，比如通过用户名查询uid，再基于uid查询源表。

u 实战范例1

l 用户分表，将用户库分成若干数据表

l 基于用户名的查询和基于uid的查询都是高并发请求。

l 用户分表基于uid分成数据表，同时基于用户名做对应冗余表。

l 如果允许多方式登陆，可以有如下设计方法

n uid,passwd,用户信息等等，主数据表，基于uid 分表

n ukey,ukeytype,uid 基于ukey分表，便于用户登陆的查询。分解成如下两个SQL。

u select uid from ulist_key_13 where ukey=’$username’ and ukeytype=‘login’;

u select * from ulist_uid_23 where uid=$uid and passwd=’$passwd’;

n ukeytype定义用户的登陆依据，比如用户名，手机号，邮件地址，网站昵称等。 Ukey+ukeytype 必须唯一。

n 此种方式需要登陆密码统一，对于第三方connect接入模式，可以通过引申额外字段完成。

u 实战范例2：用户游戏积分排名

l 表结构 uid,gameid,score 参见前文实时积分排行。表内容巨大，需要拆表。

l 需求1：基于游戏id查询积分排行

l 需求2：基于用户id查询游戏积分记录

l 解决方案：建立完全相同的两套表结构，其一以uid为拆表主键，其二以gameid为拆表主键，用户提交积分时，向两个数据结构同时提交。

u 实战范例3：全冗余查询结构

l 主信息表仅包括 主键及备注memo 字段（text类型），只支持主键查询，可以基于主键拆表。所以需要展现和存储的内容均在memo字段重体现。

l 对每一个查询条件，建立查询冗余表，以查询条件字段为主键，以主信息表主键id 为内容。

l 日常查询只基于查询冗余表，然后通过in的方式从主信息表获得内容。

l 优点是结构扩展非常方便，只需要扩展新的查询信息表即可，核心思路是，只有查询才需要独立的索引结构，展现无需独立字段。

l 缺点是只适合于相对固定的查询架构，对于更加灵活的组合查询束手无策。

n 基于统计的冗余结构

u 为了减少会涉及大规模影响结果集的表数据操作，比如count，sum操作。应将一些统计类数据通过冗余数据结构保存。

u 冗余数据结构可能以字段方式存在，也可能以独立数据表结构存在，但是都应能通过源数据表恢复。

u 实战范例：

l 论坛板块的发帖量，回帖量，每日新增数据等。

l 网站每日新增用户数等。

l 参见Discuz论坛系统数据结构，有较多相关结构。

l 参见前文分段积分结构，是典型用于统计的冗余结构。

l 后台可以通过源数据表更新该数字。

l Redis的Zset类型可以理解为存在一种冗余统计结构。

n 历史数据表

u 历史数据表对应于热点数据表，将需求较少又不能丢弃的数据存入，仅在少数情况下被访问。

### 主从架构

l 基本认识

n 读写分离对负载的减轻远远不如分库分表来的直接。

n 写压力会传递给从表，只读从库一样有写压力，一样会产生读写锁！

n 一主多从结构下，主库是单点隐患，很难解决（如主库当机，从库可以响应读写，但是无法自动担当主库的分发功能）

n 主从延迟也是重大问题。一旦有较大写入问题，如表结构更新，主从会产生巨大延迟。

l 应用场景

n 在线热备

n 异地分布

u 写分布，读统一。

u 仍然困难重重，受限于网络环境问题巨多！

n 自动障碍转移

u 主崩溃，从自动接管

n 个人建议，负载均衡主要使用分库方案，主从主要用于热备和障碍转移。

l 潜在优化点

n 为了减少写压力，有些人建议主不建索引提升i/o性能，从建立索引满足查询要求。个人认为这样维护较为麻烦。而且从本身会继承主的i/o压力，因此优化价值有限。该思路特此分享，不做推荐。

### 故障转移处理

l 要点

n 程序与数据库的连接，基于虚地址而非真实ip，由负载均衡系统监控。

n 保持主从结构的简单化，否则很难做到故障点摘除。

l 思考方式

n 遍历对服务器集群的任何一台服务器，前端web，中间件，监控，缓存，db等等，假设该服务器出现故障，系统是否会出现异常？用户访问是否会出现异常。

n 目标：任意一台服务器崩溃，负载和数据操作均会很短时间内自动转移到其他服务器，不会影响业务的正常进行。不会造成恶性的数据丢失。（哪些是可以丢失的，哪些是不能丢失的）

## 缓存方案

### 缓存结合数据库的读取

l Memcached是最常用的缓存系统

l Mysql 最新版本已经开始支持memcache插件，但据牛人分析，尚不成熟，暂不推荐。

l 数据读取

n 并不是所有数据都适合被缓存，也并不是进入了缓存就意味着效率提升。

n 命中率是第一要评估的数据。

n 如何评估进入缓存的数据规模，以及命中率优化，是非常需要细心分析的。

l 实景分析： 前端请求先连接缓存，缓存未命中连接数据库，进行查询，未命中状态比单纯连接数据库查询多了一次连接和查询的操作；如果缓存命中率很低，则这个额外的操作非但不能提高查询效率，反而为系统带来了额外的负载和复杂性，得不偿失。

n 相关评估类似于热点数据表的介绍。

n 善于利用内存，请注意数据存储的格式及压缩算法。

l Key-value 方案繁多，本培训文档暂不展开。

### 缓存结合数据库的写入

l 利用缓存不但可以减少数据读取请求，还可以减少数据库写入i/o压力

l 缓存实时更新，数据库异步更新

n 缓存实时更新数据，并将更新记录写入队列

n 可以使用类似mq的队列产品，自行建立队列请注意使用increment来维持队列序号。

n 不建议使用 get 后处理数据再set的方式维护队列

l 测试范例：

l 范例1 

$var=Memcache_get($memcon,”var”);

 $var++;

memcache_set($memcon,”var”,$var);

这样一个脚本，使用apache ab去跑，100个并发，跑10000次，然后输出缓存存取的数据，很遗憾，并不是1000，而是5000多，6000多这样的数字，中间的数字全在 get & set的过程中丢掉了。

原因，读写间隔中其他并发写入，导致数据丢失。

l 范例2

用memcache_increment来做这个操作，同样跑测试

会得到完整的10000，一条数据不会丢。

l 结论： 用increment存储队列编号，用标记+编号作为key存储队列内容。

n 后台基于缓存队列读取更新数据并更新数据库

l 基于队列读取后可以合并更新

l 更新合并率是重要指标

l 实战范例：

某论坛热门贴，前端不断有views=views+1数据更新请求。

缓存实时更新该状态

后台任务对数据库做异步更新时，假设执行周期是5分钟，那么五分钟可能会接收到这样的请求多达数十次乃至数百次，合并更新后只执行一次update即可。

类似操作还包括游戏打怪，生命和经验的变化；个人主页访问次数的变化等。

n 异步更新风险

l 前后端同时写，可能导致覆盖风险。

l 使用后端异步更新，则前端应用程序就不要写数据库，否则可能造成写入冲突。一种兼容的解决方案是，前端和后端不要写相同的字段。

l 实战范例：

用户在线上时，后台异步更新用户状态。

管理员后台屏蔽用户是直接更新数据库。

结果管理员屏蔽某用户操作完成后，因该用户在线有操作，后台异步更新程序再次基于缓存更新用户状态，用户状态被复活，屏蔽失效。

l 缓存数据丢失或服务崩溃可能导致数据丢失风险。

l 如缓存中间出现故障，则缓存队列数据不会回写到数据库，而用户会认为已经完成，此时会带来比较明显的用户体验问题。

l 一个不彻底的解决方案是，确保高安全性，高重要性数据实时数据更新，而低安全性数据通过缓存异步回写方式完成。此外，使用相对数值操作而不是绝对数值操作更安全。

n 范例：支付信息，道具的购买与获得，一旦丢失会对用户造成极大的伤害。而经验值，访问数字，如果只丢失了很少时间的内容，用户还是可以容忍的。

n 范例：如果使用 Views=Views+…的操作，一旦出现数据格式错误，从binlog中反推是可以进行数据还原，但是如果使用Views=特定值的操作，一旦缓存中数据有错误，则直接被赋予了一个错误数据，无法回溯！

l 异步更新如出现队列阻塞可能导致数据丢失风险。

l 异步更新通常是使用缓存队列后，在后台由cron或其他守护进程写入数据库。

l 如果队列生成的速度>后台更新写入数据库的速度，就会产生阻塞，导致数据越累计越多，数据库响应迟缓，而缓存队列无法迅速执行，导致溢出或者过期失效。

n 建议使用内存队列产品而不使用memcache 来进行缓存异步更新。

# 总结

- 第一步，完成数据库查询的优化，需要理解索引结构，才能学会判断影响结果集。而影响结果集对查询效率线性相关，掌握这一点，编写数据查询语句就很容易判断系统开销，了解业务压力趋势。
- 第二步，在SQL语句已经足够优化的基础上，学会对数据库整体状况的分析，能够对异常和负载的波动有正确的认识和解读；能够对系统资源的分配和瓶颈有正确的认识。
- 学会通过监控和数据来进行系统的评估和优化方案设计，杜绝拍脑袋，学会抓大放小，把握要点的处理方法。
- 第三步，在彻底掌握数据库语句优化和运维优化的基础上，学会分布式架构设计，掌握复杂，大容量数据库系统的搭建方法。
- 最后，分享一句话，学会把问题简单化，正如Caoz 常说的，你如果认为这个问题很复杂，你一定想错了。
- 感谢您的阅读，如对您有帮助，请在百度文库给本文五分好评，并推荐给您的朋友，多谢。



### 参考

1. SQL基础：https://juejin.im/post/6844903790571700231
2. SQL面试：https://sowhat.blog.csdn.net/article/details/71158104
3. MySQL拷问：https://www.jianshu.com/nb/22933318
4. [顺丰面试：MySQL十连击](https://mp.weixin.qq.com/s/ZoCZLG3o3AZBDSO1y3nbmw)
5. [Innodb重要参数优化](https://www.cnblogs.com/chengyunblogs/p/11929040.html)
6. https://www.cnblogs.com/assistants/p/11958998.html
7. https://www.cnblogs.com/wy123/p/12724252.html
8. https://www.cnblogs.com/hanwuxing/p/10367147.html
9. https://www.cnblogs.com/zejin2008/p/5262751.html
10. https://www.cnblogs.com/out8/p/4222166.html
11. https://www.cnblogs.com/zhuyeshen/p/12084845.html
12. https://www.cnblogs.com/mintsd/p/13062308.html
13. https://blog.csdn.net/zdhsoft/article/details/89373364
14. https://www.ddpool.cn/article/56666.html
15. https://www.cnblogs.com/csj2018/p/9955405.html
16. https://www.jb51.net/article/159737.htm
17. https://www.cnblogs.com/wintersoft/p/10787474.html
18. https://blog.csdn.net/qq_27607965/article/details/79925288
19. https://www.cnblogs.com/cyun/p/4308960.html
20. https://www.cnblogs.com/liaojie970/p/6824773.html
21. [MySQL 8.0能彻底解决困扰运维的复制延迟问题！](http://blog.itpub.net/31547898/viewspace-2200045/)
22. https://www.cnblogs.com/python-daxiong/p/12310564.html

