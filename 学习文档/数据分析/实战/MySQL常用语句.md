# 常用语句



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

![x](D:/WorkingDir/GitLabRepo/Architect/学习文档/数据分析/Resources/db001.png)

load data语句是将文件的内容插入到表中，相当于是insert语句，而replace语句在一般的情况下和insert差不多，但是如果表中存在primary 或者unique索引的时候，如果插入的数据和原来的primary key或者unique相同的时候，会删除原来的数据，然后增加一条新的数据，所以有的时候执行一条replace语句相当于执行了一条delete和insert语句。

![x](D:/WorkingDir/GitLabRepo/Architect/学习文档/数据分析/Resources/db002.png)

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



## SQL&nbsp;Joins、统计、随机查询

7种join具体如下：

![x](D:\WorkingDir\GitLabRepo\Architect\学习文档\数据分析\Resources\db003.jpg)

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



### 筛选

WHERE、HAVING同时出现时，MySQL优先执行WHERE字句，HAVING对WHERE结果做进一步筛选。HAVING字句不容易优化，但是可以在GROUP BY查询中用作数学统计(SUM, MAX, MIN …)

MySQL不允许在WHERE后面使用假名

### 分页

`LAST_INSERT_ID()` 函数返回 MySql 为上一条 `INSERT` 命令生成的 `AUTO_INCREMENT` 值。

它只对本次连接有效，所以不存在并发问题，但是它与表无关，只要有 INSERT 操作，`AUTO_INCREMENT` 就有可能改变。

如果 INSERT 使用单条语句插入多条新纪录的语法，它获取的是第一条数据的id。



### 排序

排序规则：列->表->库->列字符集默认排序



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

