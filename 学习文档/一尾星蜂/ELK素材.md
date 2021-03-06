#### 基础简介

**为什么要用ES？**

世界已被数据淹没。但是，大部分数据库在从大量数据中提取可用知识时较为低效，不能满足需求，比如：在一般数据库中，我们只能通过时间戳或精确值进行过滤，但是高效地进行全文检索、同义词处理、通过相关性给查询结果评分，从原始数据中生成分析与聚合数据都比较困难，这就是ES脱颖而出的地方：不经过大型批处理任务就近实时（Near Realtime：简写NRT）的做到这些操作！

所以 ES 鼓励用户去探索与利用数据，不因为查询数据太困难，就让它们烂在数据仓库里。

**ES使用场景**

1. 存储

   ES 天然支持分布式，具备存储海量数据的能力，其搜索和数据分析的功能都建立在 ES 存储的海量的数据之上；ES 很方便的作为海量数据的存储工具，特别是在数据量急剧增长的当下，ES结合爬虫等数据收集工具可以发挥很大用处

2. 搜索

   ES 使用倒排索引，每个字段都被索引且可用于搜索，更是提供了丰富的搜索api，在海量数据下近实时实现近秒级的响应,基于Lucene的开源搜索引擎，为搜索引擎（全文检索，高亮，搜索推荐等）提供了检索的能力。 具体场景:

   - Stack Overflow（国外的程序异常讨论论坛），IT问题，程序的报错，提交上去，有人会跟你讨论和回答，全文检索，搜索相关问题和答案，程序报错了，就会将报错信息粘贴到里面去，搜索有没有对应的答案；
   - GitHub（开源代码管理），搜索上千亿行代码；
   - 日志数据分析，logstash采集日志，ElasticSearch进行复杂的数据分析（ELK技术，elasticsearch+logstash+kibana）；

3. 数据分析
   ES也提供了大量数据分析的api和丰富的聚合能力，支持在海量数据的基础上进行数据的分析和处理。比如说：使用爬虫爬取不同电商平台的某个商品的数据，通过 ES 进行各个平台的历史价格、购买力等等的数据分析。

**起源**

回忆时光，这是一个关于ES起源的传奇故事，描述了ES的诞生过程。

**流行度**

在开源搜索引擎中，ES的流行程度是傲视群雄的，这是一张各个搜索引擎流行度评分的列表。

**定义**

ES是一个开源的搜索引擎，建立在一个全文搜索引擎库 [Apache Lucene™](https://lucene.apache.org/core/) 基础之上。 Lucene 可以说是当下最先进、高性能、全功能的搜索引擎库—无论是开源还是私有。

ES 使用 Java 编写，内部使用 Lucene 做索引与搜索，它的目的是使全文检索变得简单，所以通过封装隐藏了 Lucene 的复杂性，取而代之的是一套简单一致的 RESTful API。

当然ES不仅仅只是一个全文搜索引擎。 它可以被下面这样准确的形容：

- 一个分布式的实时文档存储，每个字段可以被索引与搜索
- 一个分布式实时分析搜索引擎
- 能胜任上百个服务节点的扩展，并支持 PB 级别的结构化或者非结构化数据

ES 将所有的功能打包成单独的服务，这样我们可以通过程序与它提供的简单的 RESTful API 进行通信，可以使用自己喜欢的编程语言充当 web 客户端。

**ES核心概念**

1）Cluster：集群

ES可以作为一个独立的单个搜索服务器。不过，为了处理大型数据集，实现容错和高可用性，ES可以运行在许多互相合作的服务器上。这些服务器的集合称为集群。

2）Node：节点

形成集群的每个服务器称为节点。

集群中有多个节点，其中有一个为主节点，这个主节点是通过选举产生的，主从节点是对于集群内部来说的。

es 的一个概念就是去中心化，字面上理解就是无中心节点，这是对于集群外部来说的，因为从外部来看es 集群，在逻辑上是个整体，与任何一个节点的通信就等价于和整个es 集群通信。

主节点的职责是负责管理集群状态，包括管理分片的状态和副本的状态，以及节点的发现和删除。

只需要在同一个网段之内启动多个es 节点，就可以自动组成一个集群。默认情况下es 会自动发现同一网段内的节点，自动组成集群。

3）Shard：分片

当有大量的文档时，由于内存的限制、磁盘处理能力不足、无法足够快的响应客户端的请求等，一个节点可能不够。这种情况下，数据可以分为较小的分片，每个分片放到不同的服务器上。

当你查询的索引分布在多个分片上时，ES会把查询发送给每个相关的分片，并将结果组合在一起，而应用程序并不知道分片的存在。即：这个过程对用户来说是透明的。

通过分片机制，ES实现了分布式搜索。

需要注意：在创建索引的时候就确定好主分片的数量，并且永远不能改变这个数量。

![x](http://121.196.182.26:6100/public/images/tempsnip.png)

比如上图所示，开始设置为5个分片，在单个节点上，后来扩容到5个节点，每个节点有一个分片。如果继续扩容，是不能自动切分进行数据迁移的。官方文档的说法是分片切分成本和重新索引的成本差不多，所以建议干脆通过接口重新索引。

4）Replia：副本

为提高查询吞吐量或实现高可用性，可以使用分片副本。副本是一个分片的精确复制，每个分片可以有零个或多个副本。ES中可以有许多相同的分片，其中之一被选择更改索引操作，这种特殊的分片称为主分片。

当主分片丢失时，如：该分片所在的数据不可用时，集群将副本提升为新的主分片。

ES 禁止同一个分片的主分片和副本分片在同一个节点上，所以如果部署的是单节点的ES是不能有副本的。

它在节点失败的情况下提供高可用性。由于这个原因，需要注意的是，副本分片永远不会分配到与主分片相同的节点上。

![x](http://121.196.182.26:6100/public/images/es_cluster.png)

**简单类比**

- 关系型数据库中的数据库（DataBase），等价于ES中的索引（Index）

- 一个数据库下面有N张表（Table），等价于1个索引Index下面有N多类型（Type）

- 一个数据库表（Table）下的数据由多行（ROW）多列（column，属性）组成，等价于1个Type由多个文档（Document）和多Field组成。

  > 一个索引是一个文档的集合。每个索引有唯一的名字（必须全部是小写字母的），通过这个名字来操作它。一个集群中可以有任意多个索引。索引作动词时，指索引数据、或对数据进行索引。
  >
  > Type 类型：指在一个索引中，可以索引不同类型的文档，如用户数据、博客数据。从6.0.0 版本起已废弃，一个索引中只存放一类数据。
  >
  > ES概念里的索引概念名词而不是动词，一个索引就是一个拥有相似特征的文档的集合。比如说，可以有一个客户数据的索引，另外有一个产品目录的索引，还有一个订单数据的索引。
  >
  > 文档：是被索引的一条数据，索引的基本信息单元，以JSON格式来表示。比如，可以查询某一个客户的文档、某一个产品的一个文档、某个订单的一个文档。这里的文档就是一条JSON格式的记录，而JSON是一个到处存在的数据交互格式，所以很容易解析和传播。在一个index/type里面，你可以存储任意多的文档。（注意，一个文档物理上存在于一个索引之中，但文档必须被索引/赋予一个索引的type）。

- 在一个关系型数据库里面，schema（模式）定义了表、每个表的字段，还有表和字段之间的关系。与之对应的，在 ES 中：Mapping 定义索引下的 Type 的字段处理规则，即索引如何建立、索引类型、是否保存原始索引 JSON 文档、是否压缩原始 JSON 文档、是否需要分词处理、如何进行分词处理等。

- 在数据库中的增insert、删delete、改update、查search操作等价于ES中的增PUT/POST、删Delete、改_update、查GET.

**逻辑架构**

1. Gateway是ES用来存储索引的文件系统，支持多种类型。

2. Gateway的上层是一个分布式的Lucene框架。Lucene被称为目前市场上开源的最好全文检索引擎工具。

3. Lucene之上是ES的模块，包括：索引模块、搜索模块、映射解析模块等

4. ES模块之上是 Discovery、Scripting和第三方插件。

   Discovery是ES的节点发现模块，不同机器上的ES节点要组成集群需要进行消息通信，集群内部需要选举master节点，这些工作都是由Discovery模块完成。支持多种发现机制，如 Zen 、EC2、gce、Azure。

   Scripting用来支持在查询语句中插入javascript、python等脚本语言，scripting模块负责解析这些脚本，使用脚本语句性能稍低。ES也支持多种第三方插件。

5. 再上层是ES的传输模块和JMX。传输模块支持多种传输协议，如 Thrift、memecached、http，默认使用http。JMX 是 java 的管理框架，用来管理 ES 应用。

6. 最上层是 ES 提供给用户的接口，可以通过 RESTful 接口和 ES 集群进行交互。



---

**简单示例**

第一个需求是存储员工数据。 这会以“员工文档“的形式存储：一个文档代表一个员工。存储数据到 ES 的行为叫做 索引，但在索引一个文档之前，需要确定将文档存储在哪里。

一个 ES 集群可以 包含多个索引，每个索引可以包含多个类型 。 这些不同的类型存储着多个文档，每个文档又有 多个 属性 。

对于员工目录，我们可以做如下操作：

- 每个员工索引一个文档，文档包含该员工的所有信息。
- 每个文档都将是 `employee` *类型* 。
- 该类型位于索引 `leadchina` 内。
- 该索引保存在我们的 ES 中。



search返回结果不仅告知匹配了哪些文档，还包含了整个文档本身：显示搜索结果给最终用户所需的全部信息。

接下来，我们搜索姓氏为“张“的雇员。这个一般涉及到一个 查询字符串（query-string）搜索，我们通过一个URL参数来传递查询信息给搜索接口。

Query-string 搜索通过命令非常方便地进行临时性的即席搜索 ，但它有自身的局限性（）。Elasticsearch 提供一个丰富灵活的查询语言叫做“查询表达式“，它支持构建更加复杂和健壮的查询。

领域特定语言（DSL）：使用 JSON 构造了一个请求。我们可以像这样重写之前的查询所有姓为 “张” 的搜索。

ES 默认按照相关性得分排序，即每个文档跟查询的匹配程度。第一个最高得分的结果很明显：张三 的 `about` 属性清楚地写着 “高强” 。

但为什么 张三丰 也作为结果返回了呢？原因是他的 `about` 属性里提到了 “高” 。因为只有 “高” 而没有 “强” ，所以他的相关性得分低于张三的。

这个案例，阐明了 ES 如何 在 全文属性上搜索并返回相关性最强的结果。ES 中的  `相关性` 概念非常重要，也是完全区别于传统关系型数据库的一个概念，数据库中的一条记录要么匹配要么不匹配。

找出一个属性中的独立单词是没有问题的，但有时候想要精确匹配一系列单词或者短语。比如，刚才查询的“兄弟”，为此对 `match` 查询稍作调整，使用一个叫做 `match_phrase` 的查询，毫无悬念，返回结果仅有“张三”的文档。

许多应用都倾向于在每个搜索结果中 **高亮** 部分文本片段，以便让用户知道为何该文档符合查询条件。在 ES 中检索出高亮片段只需要增加一个新的 `highlight` 参数。当执行该查询时，返回结果与之前一样，与此同时结果中还多了一个叫做 `highlight` 的部分。这个部分包含了 `about` 属性匹配的文本片段，并以 HTML 标签 `<em></em>` 封装。

ES 有一个功能叫聚合（aggregations），允许我们基于数据生成一些精细的分析结果。聚合与 SQL 中的 `GROUP BY` 类似但更强大。举个例子，挖掘出员工中最受欢迎的兴趣爱好：



---

**分片原理**

一个运行中的 ES 实例称为一个节点，而集群是由一个或者多个拥有相同"集群名称"的节点组成， 它们共同承担数据和负载的压力。当有节点加入集群中或者从集群中移除节点时，集群将会重新平均分布所有的数据。

当一个节点被选举成为主节点时， 它将负责管理集群范围内的所有变更，例如增加、删除索引，或者增加、删除节点等。

作为用户，我们可以将请求发送到 集群中的任何节点。 每个节点都知道任意文档所处的位置，并且能够将我们的请求直接转发到存储我们所需文档的节点。无论我们将请求发送到哪个节点，它都能负责从各个包含我们所需文档的节点收集回数据，并将最终结果返回給客户端。

ES 的集群监控信息中包含了许多的统计数据，其中最为重要的一项就是集群健康状态，它在 `status` 字段中展示。

这个字段指示当前集群在总体上是否工作正常。它的三种颜色含义如下：

- **`green`**

  所有的主分片和副本分片都正常运行。

- **`yellow`**

  所有的主分片都正常运行，但不是所有的副本分片都正常运行。

- **`red`**

  有主分片没能正常运行。

我们往 ES 添加数据时需要用到索引 —— 保存相关数据的地方。索引实际上是指向一个或者多个物理分片的逻辑命名空间。

一个分片是一个底层的工作单元，它仅保存了全部数据中的一部分。我们的文档被存储和索引到分片内，但是应用程序是直接与索引而不是与分片进行交互。

ES 是利用分片将数据分发到集群内各处的。分片是数据的容器，文档保存在分片内，分片又被分配到集群内的各个节点里。当集群规模扩大或者缩小时，ES 会自动的在各节点中迁移分片，使得数据仍然均匀分布在集群里。

一个分片可以是主分片或者副本分片。索引内任意一个文档都归属于一个主分片，所以主分片的数目决定着索引能够保存的最大数据量。

一个副本分片只是一个主分片的拷贝。副本分片作为硬件故障时保护数据不丢失的冗余备份，并为搜索和返回文档等读操作提供服务。

在索引建立的时候就已经确定了主分片数，但是副本分片数可以随时修改。

我们在包含一个空节点的集群内创建名为 `projects` 的索引。 (索引在默认情况下会被分配5个主分片）为了演示，我们将分配3个主分片和一份副本（一份副本的意思是：每个主分片拥有一个副本分片）。

集群的健康状况为 `yellow` 则表示全部 主分片都正常运行（集群可以正常服务所有请求），但是副本分片没有全部处在正常状态。实际上，所有3个副本分片都是 `unassigned` —— 它们都没有被分配到任何节点。 在同一个节点上既保存原始数据又保存副本是没有意义的，因为一旦失去了那个节点，我们也将丢失该节点上的所有副本数据。

当集群中只有一个节点在运行时，会有单点故障问题。所以，我们需再启动一个节点加入集群防止数据丢失。

当第二个节点加入到集群后，3个副本分片将会分配到这个节点上——每个主分片对应一个副本分片。 这意味着当集群内任何一个节点出现问题时，我们的数据都完好无损。

所有新近被索引的文档都将会保存在主分片上，然后被并行的复制到对应的副本分片上。这就保证了我们既可以从主分片又可以从副本分片上获得文档。

集群健康状态现在是`green`，这表示所有6个分片（包括3个主分片和3个副本分片）都在正常运行。此时的集群不仅正常运行，而且是高可用的。

我们的也可以为正在增长中的应用程序按需扩容。当启动第三个节点并加入集群时，`Node 1` 和 `Node 2` 上各有一个分片被迁移到了新的 `Node 3` 节点，现在每个节点上都拥有2个分片。这表示每个节点的硬件资源（CPU, RAM, I/O）将被更少的分片所共享，每个分片的性能将会得到提升。

分片是一个功能完整的搜索引擎，它拥有使用一个节点上的所有资源的能力。我们这个拥有6个分片（3个主分片和3个副本分片）的索引可以最大扩容到6个节点，每个节点上存在一个分片，并且每个分片拥有所在节点的全部资源。

在运行中的集群上是可以动态调整副本分片数目的，我们可以按需伸缩集群。让我们把副本数从默认的 1 增加到 2，那么现在`projects` 索引拥有9个分片：3个主分片和6个副本分片。这表示我们可以将集群扩容到9个节点，每个节点上一个分片。相比原来3个节点时，集群搜索性能可以提升3倍。但是更多的副本分片数也提高了数据冗余量，占用了更多硬件资源。按照现在3个节点9个分片的配置，我们可以在失去任意2个节点的情况下不丢失任何数据。

现在我们模拟一个节点故障，关闭一个主节点。而集群必须拥有一个主节点来保证正常工作，所以集群情首先会选举一个新的主节点：Node 2 。

在我们关闭 Node 1 的同时也失去了主分片 1 和 2 ，并且在缺失主分片的时候索引也不能正常工作。 如果此时来检查集群的状况，我们看到的状态将会为 red ：不是所有主分片都在正常工作。

幸运的是，在其它节点上存在着这两个主分片的完整副本， 所以新的主节点立即将这些分片在 Node 2 和 Node 3 上对应的副本分片提升为主分片， 此时集群的状态将会为 yellow 。 这个提升主分片的过程是瞬间发生的，如同按下一个开关一般。

为什么我们集群状态是 yellow 而不是 green 呢？ 虽然我们拥有所有的三个主分片，但是同时设置了每个主分片需要对应2份副本分片，而此时只存在一份副本分片。 所以集群不能为 green 的状态，不过我们不必过于担心：如果我们同样关闭了 Node 2 ，我们的程序 依然 可以保持在不丢任何数据的情况下运行，因为 Node 3 为每一个分片都保留着一份副本。

如果我们重新启动 Node 1 ，集群可以将缺失的副本分片再次进行分配。如果 Node 1 依然拥有着之前的分片，ES会去重用它们，同时仅从主分片复制发生了修改的数据文件。



---

**索引原理**

一个文档不仅仅包含业务数据 ，也包含元数据 —— 有关文档本身的信息。这就是三个元数据元素：

- **`_index`**

  文档在哪存放

- **`_type`**

  文档表示的对象类别

- **`_id`**

  文档唯一标识

当索引一个文档的时候，文档会被存储到一个主分片中。ES 如何知道一个文档应该存放到哪个分片中呢？这个过程是根据一个哈希公式决定的：

```sh
shard = hash(routing) % number_of_primary_shards
```

> `routing` 是一个可变值，默认是文档的 `_id` ，也可以设置成一个自定义的值。 `routing` 通过 hash 函数生成一个数字，然后这个数字再除以 `number_of_primary_shards` （主分片的数量）后得到 **余数** 。这个分布在 `0` 到 `number_of_primary_shards-1` 之间的余数，就是我们所寻求的文档所在分片的位置。

这就解释了为什么我们要在创建索引的时候就确定好主分片的数量，并且永远不会改变这个数量：因为如果数量变化了，那么所有之前路由的值都会无效，文档也就找不到了。

我们可以发送请求到集群中的任一节点。 每个节点都有能力处理任意请求。 每个节点都知道集群中任一文档位置，所以可以直接将请求转发到需要的节点上。

假设我们将客户端所有的请求发送到 `Node 1` ，我们将其称为**协调节点(coordinating node)** 。新建、索引和删除请求都是写操作，必须在主分片上面完成之后才能被复制到相关的副本分片，所以协调节点首先会将请求转发到主分片所在节点。主分片保存数据并将数据发送给副本，一旦所有的副本分片都报告成功，主分片节点将向协调节点报告成功，协调节点向客户端报告成功。

在客户端收到成功响应时，文档变更已经在主分片和所有副本分片执行完成，所以变更是安全的。

下面是从主分片或者副本分片检索文档的步骤顺序，重要的一个环节是：在处理读取请求时，协调结点都会通过轮询所有的副本分片来达到负载均衡的效果，提高检索性能。

接下来看一下更新文档的步骤：

1. 客户端向协调节点发送更新请求。
2. 它将请求转发到主分片所在的节点。
3. 主分片检索出待更新的文档，修改 `_source` 字段中的 JSON ，并且尝试重新索引主分片的文档。如果文档已经被另一个进程修改，它会进行重试，超过 `retry_on_conflict` 次后放弃。
4. 如果主分片成功更新了文档，它会将新版本的文档并行转发到所有的副本分片上，重新建立索引。 一旦所有副本分片都返回成功，主分片向协调节点也返回成功，协调节点向客户端返回成功。



---

**基本搜索**

ES 真正强大之处在于可以从无规律的数据中找出有意义的信息——从“大数据”到“大信息”。它在存储文档时，为了便于搜索，会为文档添加索引，这也是为什么要使用结构化的 JSON 文档。文档中的每个字段都会被索引并且可以被查询。

搜索API的最基础的形式是没有指定任何查询的空搜索，它简单地返回集群中所有索引下的所有文档：

**hits**

返回结果中最重要的部分是 `hits` ，它包含 `total` 字段来表示匹配到的文档总数，并且一个 `hits` 数组包含所查询结果的前十个文档。

在 `hits` 数组中每个结果包含文档的 `_index` 、 `_type` 、 `_id` ，加上 `_source` 字段。这意味着我们可以直接从返回的搜索结果中使用整个文档。

每个结果还有一个 `_score` ，它衡量了文档与查询的匹配程度。默认情况下，首先返回最相关的文档结果，就是说，返回的文档是按照 `_score` 降序排列的。在这个例子中，我们没有指定任何查询，故所有的文档具有相同的相关性，因此对所有的结果而言 `1` 是中性的 `_score` 。

`max_score` 值是与查询所匹配文档的 `_score` 的最大值。

**took**

`took` 值告诉我们执行整个搜索请求耗费了多少毫秒。

**shards**

`_shards` 部分告诉我们在查询中参与分片的总数，以及这些分片成功了多少个失败了多少个。

**timeout**

`timed_out` 值告诉我们查询是否超时。

常见情况下，我们想在一个或多个特殊的索引中进行搜索。我们可以通过在URL中指定特殊的索引和类型达到这种效果，这是一些基本的查询API。

要在ES查询时自定义分页信息怎么办呢？和 MYSQL 使用 `LIMIT` 关键字返回单个 `page` 结果的方法相同，ES 接受 `from` 和 `size` 参数：

- **`size`**

  显示应该返回的结果数量，默认是 `10`

- **`from`**

  显示应该跳过的初始结果数量，默认是 `0`

如果每页展示 5 条结果，可以用下面方式请求得到 1 到 3 页的结果。

我们通过查询结果看到，只要字段中包含查询值，ES就能检索出来。那么 ES 不指定字段，是如何在多个不同的字段中查找到结果的呢？

当索引一个文档的时候，ES 取出所有字段的值拼接成一个大的字符串，作为 `_all` 字段进行索引。例如，当索引这个文档时，这就好似增加了一个名叫 `_all` 的额外字段，这个字段是所有字段值拼接而成。除非设置特定字段，否则查询字符串就使用 `_all` 字段进行搜索。

当测试索引里面的数据时，我们发现一些奇怪的事情：在我们的索引中有12条推文，其中只有一条包含日期 `2014-09-15` ，但是看一看下面查询命中的总数（total）：

为什么在 `_all` 字段查询日期返回所有推文，而在 `date` 字段只查询年份却没有返回结果？为什么我们在 `_all` 字段和 `date` 字段的查询结果有差别？

这是因为数据在 `_all` 字段与 `date` 字段的索引方式不同。所以，通过请求 `gb` 索引中 `tweet` 类型的映射，让我们看一看 ES 是如何解释我们文档结构的：

什么是**映射（Mapping）**？就是描述数据在每个字段内如何存储。

查询结果告诉我们 `date` 字段被认为是 `date` 类型的。由于 `_all` 是默认字段，所以没有提及它，但是我们知道 `_all` 字段是拼接的字符串，所以必然是 `string` 类型的。`date` 字段和 `string` 字段索引方式不同，因此搜索结果也不一样。

ES 中的数据可以概括的分为两类：精确值和全文。

精确值如它们听起来那样精确。例如日期或者用户 ID，字符串也可以表示精确值，例如用户名或邮箱地址。对于精确值来讲，`Foo` 和 `foo` 是不同的，`2014` 和 `2014-09-15` 也是不同的。

另一方面，全文是指文本数据（通常以人类容易识别的语言书写），例如一个推文的内容或一封邮件的内容。

精确值很容易查询：要么匹配，要么不匹配。这种查询很容易用 SQL 表示。

查询全文数据时，一般我们问的不只是“匹配查询”，而是“匹配查询的程度有多大？”换句话说，该文档与给定查询的相关性如何？

比如说

- 搜索 `UK` ，会返回包含 `United Kindom` 的文档。
- 搜索 `jump` ，会匹配 `jumped` ， `jumps` ， `jumping` ，甚至是 `leap` 。

为了进行查询，ES 首先分析文档。什么是分析？就是全文如何处理使之可以被搜索。分析之后根据结果创建**倒排索引** 。

ES 使用一种称为**倒排索引**的结构，它适用于快速的全文搜索。一个倒排索引由文档中所有不重复词的列表构成，对于其中每个词，有一个包含它的文档列表。

例如，假设我们有两个文档，每个文档的 `content` 域包含如下内容，

为了创建倒排索引，ES 首先将每个文档的 `content` 域拆分成单独的 词（称为 `词条` 或 `tokens` ），创建一个包含所有不重复词条的排序列表，然后列出每个词条出现在哪个文档。结果如下所示：

现在，如果我们想搜索 `quick brown` ，我们只需要查找包含每个词条的文档。

但是，目前的倒排索引有一些问题：

- `Quick` 和 `quick` 以独立的词条出现，然而用户可能认为它们是相同的词。
- `fox` 和 `foxes` 非常相似, 就像 `dog` 和 `dogs` ；他们有相同的词根。
- `jumped` 和 `leap`, 尽管没有相同的词根，但他们的意思很相近。他们是同义词。

因为只能搜索在索引中出现的词条，所以索引文本和查询字符串必须标准化为相同的格式。分词和标准化的过程称为**分析**。 

分析包含下面的过程：

- 首先，将一块文本分成适合于倒排索引的独立的词条，
- 之后，将这些词条统一化为标准格式以提高它们的“可搜索性”

分析器执行上面的工作。分析器实际上是将三个功能封装到了一个包里：

- **字符过滤器**

  首先，字符串按顺序通过每个字符过滤器*。他们的任务是在分词前整理字符串。一个字符过滤器可以用来去掉HTML，或者将 `&` 转化成 `and`。

- **分词器**

  其次，字符串被分词器分为单个的词条。一个简单的分词器遇到空格和标点的时候，可能会将文本拆分成词条。

- **Token 过滤器**

  最后，词条按顺序通过每个 token 过滤器。这个过程可能会改变词条（例如，小写化 `Quick` ），删除词条（例如， 像 `a`， `and`， `the` 等无用词），或者增加词条（例如，像 `jump` 和 `leap` 这种同义词）。

ES 提供了开箱即用的字符过滤器、分词器和 token 过滤器。

**什么时候使用分析器？**

当我们索引一个文档，它的全文字段被分析成词条以用来创建倒排索引。但是，当我们在全文字段搜索的时候，我们需要将查询字符串通过相同的分析过程，以保证我们搜索的词条格式与索引中的词条格式一致。

全文查询，理解每个字段是如何定义的，因此它们可以做正确的事：

- 当你查询一个全文字段时， 会对查询字符串应用相同的分析器，以产生正确的搜索词条列表。
- 当你查询一个精确值字段时，不会分析查询字符串，而是搜索指定的精确值。

现在我们可以理解在开始的查询中为什么返回那样的结果：

- `date` 字段包含一个精确值：单独的词条 `2014-09-15`。
- `_all` 字段是一个全文字段，所以分词进程将日期转化为三个词条： `2014`， `09`， 和 `15`。

为了能够将时间字段视为时间，数字字段视为数字，字符串字段视为全文或精确值字符串， ES 需要知道每个字段中数据的类型。这个信息包含在映射中。

ES 支持这些简单字段类型，当索引一个包含新字段的文档（之前未曾出现）时ES 会使用动态映射，通过JSON中基本数据类型，尝试猜测字段类型。

通过 `/_mapping` ，我们可以查看 ES 在一个或多个索引中的一个或多个类型的映射。

`string` 字段映射的两个最重要属性是 `index` 和 `analyzer` 。

`string` 字段 `index` 属性默认是 `analyzed` 。如果我们想映射这个字段为一个精确值，我们需要设置它为 `not_analyzed` 。

其他简单类型（例如 `long` ， `double` ， `date` 等）也接受 `index` 参数，但有意义的值只有 `no` 和 `not_analyzed` ， 因为它们永远不会被分析。

对于 `analyzed` 字符串字段，用 `analyzer` 属性指定在搜索和索引时使用的分析器。默认，ES使用 `standard` 分析器， 但你可以指定一个内置的分析器替代它，例如 `whitespace` 、 `simple` 和 `english`，也可以使用自定义分析器。

我们可以更新一个映射来添加一个新字段，但不能将一个存在的字段从 `analyzed` 改为 `not_analyzed` 。因为如果一个字段的映射已经存在，那么该字段的数据可能已经被索引。如果你修改这个字段的映射，索引的数据很可能出错，不能被正常的搜索。

ES 中有两种形式的搜索API：一种是 “轻量的” 查询字符串版本，要求在查询字符串中传递所有的参数，另一种是更完整的请求体版本，要求使用 JSON 格式和更丰富的查询表达式作为搜索语言。

一个带请求体的查询允许我们使用**查询领域特定语言（query domain-specific language，Query DSL ）**来写查询语句。

DSL中区分了查询与过滤：

- 过滤：简单的检查包含或者排除，不计算相关性评分（结果会被缓存到内存中以便快速读取，所以有各种各样的手段来优化查询结果）。
- 查询：不仅仅要找出匹配的文档，还要计算每个匹配文档的相关性评分（计算相关性使得它们比不评分查询费力的多，同时，查询结果并不缓存）。

通常的使用规则是，查询（query）语句来进行全文搜索或者任何需要影响相关性得分的搜索。除此以外的都使用过滤（filters)。

 ES 自带了很多的查询，经常用到的是这么几个：

`match_all`：匹配所有文档。在没有指定查询方式时，它是默认的查询

`match`：无论你在任何字段上进行的是全文搜索还是精确查询，`match` 查询都是可用的标准查询。如果在一个全文字段上使用 `match` 查询，在执行查询前，它会用正确的分析器去分析查询字符串；如果在一个精确值的字段上使用它，例如数字、日期、布尔或者一个 `not_analyzed` 字符串字段，那么它将会精确匹配给定的值。

`multi_match`：可以在多个字段上执行相同的 `match` 查询

`range`：找出那些落在指定区间内的数字或者时间。允许的操作符如下：

- **`gt`**：大于
- **`gte`**：大于等于
- **`lt`**：小于
- **`lte`**：小于等于

`term`：被用于精确值匹配，对于输入的文本不分析。这些精确值可能是数字、时间、布尔或者那些 `not_analyzed` 的字符串。

`terms`：和 `term` 查询一样，但它允许你指定多值进行匹配。如果这个字段包含了指定值中的任何一个值，那么这个文档满足条件。类似SQL语句中的in

`exists`：和 `missing` 查询被用于查找那些指定字段中有值 (`exists`) 或无值 (`missing`) 的文档。与SQL中的 `IS_NULL` (`missing`) 和 `NOT IS_NULL` (`exists`) 类似

如果需要在多个字段上查询多种文本，并且根据一系列的标准来过滤。那么为了构建类似的高级查询，就需要一种能够将多查询组合成单一查询的方法。

ES 中可以用 `bool` 来实现这个需求，它将多查询组合在一起，接收这些参数：

- **`must`**：文档 必须匹配这些条件才能被包含进来。
- **`must_not`**：文档必须不匹配这些条件才能被包含进来。
- **`should`**：如果满足这些语句中的任意语句，将增加 `_score` ，否则，无任何影响。它们主要用于修正每个文档的相关性得分。
- **`filter`**：必须匹配，但它以不评分、过滤模式来进行。这些语句对评分没有贡献，只是根据过滤标准来排除或包含文档。

如果没有 `must` 语句，那么至少需要能够匹配其中的一条 `should` 语句。但，如果存在至少一条 `must` 语句，则对 `should` 语句的匹配没有要求。

我们看一个示例：......

通过混合布尔查询，我们可以在查询请求中灵活地编写查询逻辑，实现复杂的查询。

尽管没有 `bool` 查询使用这么频繁，`constant_score` 查询也是你工具箱里有用的查询工具。它将一个不变的常量评分应用于所有匹配的文档。它被经常用于你只需要执行一个 filter 而没有其它查询（例如，评分查询）的情况下。

可以使用它来取代只有 filter 语句的 `bool` 查询。在性能上是完全相同的，但对于提高查询简洁性和清晰度有很大帮助。

查询变复杂时，理解起来就有点困难了。不过 ES 提供了专门的`validate-query` API 可以用来验证查询是否合法。

我们可以将 `explain` 参数加到查询字符串中，找出查询不合法的原因；对于合法查询，使用 `explain` 参数将返回可读的描述，这对理解 ES 是如何解析 query 是非常有用。

从 `explanation` 中可以看出，匹配 `really powerful` 的 `match` 查询被重写为两个针对 `tweet` 字段的 single-term 查询，一个single-term查询对应查询字符串分出来的一个term。

当然，对于索引 `us` ，这两个 term 分别是 `really` 和 `powerful` ，而对于索引 `gb` ，term 则分别是 `realli` 和 `power` 。之所以出现这个情况，是由于我们将索引 `gb` 中 `tweet` 字段的分析器修改为 `english` 分析器。

---





路由一个文档到一个分片：

当索引一个文档的时候，文档会被存储到一个主分片中。ES 如何知道一个文档应该存放到哪个分片中呢？当我们创建文档时，它如何决定这个文档应当被存储在分片 1 还是分片 2 中呢？

首先这肯定不会是随机的，否则将来要获取文档的时候我们就不知道从何处寻找了。实际上，这个过程是根据下面这个公式决定的：

shard = hash(routing) % number_of_primary_shards

routing 是一个可变值，唯一不可重复，默认是文档的 _id ，也可以设置成一个自定义的值。routing 通过 hash 函数生成一个数字，然后这个数字再除以 number_of_primary_shards（主分片的数量）后得到余数。这个分布在 0 到 number_of_primary_shards - 1 之间的余数，就是我们所寻求的文档所在分片的位置。

这就解释了为什么我们要在创建索引的时候就确定好主分片的数量 并且永远不会改变这个数量：因为如果数量变化了，那么所有之前路由的值都会无效，文档也再也找不到了。

所有的文档 API(get、index、delete、bulk、update 以及 mget)都接受一个叫做 routing 的路由参数，通过这个参数我们可以自定义文档到分片的映射。一个自定义的路由参数可以用来确保所有相关的文档——例如所有属于同一个用户的文档——都被存储到同一个分片中。

详解：

1、我们能够发送请求给集群中任意一个节点。每个节点都有能力处理任意请求。每个节点都知道任意文档所在的节点

2、新建索引和删除请求都是写操作，它们必须在主分片上成功完成才能赋值到相关的复制分片上

3、在主分片和复制分片上成功新建、索引或删除一个文档必要的顺序步骤：

- 客户端给 Node1 发送新建、索引或删除请求。
- 节点使用文档的 _id 确定文档属于分片0，转发请求到 Node3，分片0位于这个节点上。
- Node3 在主分片上执行请求，如果成功，它转发请求到相应的位于 Node1 和 Node2 的复制节点上。当所有的复制节点报告成功，Node3 报告成功到请求的节点，请求的节点再报告给客户端。
- 客户端接收到成功响应的时候，文档的修改已经被用于主分片和所有的复制分片，修改生效了。



ES分片复制：

复制默认的值是 sync。这将导致主分片得到复制分片的成功响应后才返回。

如果你设置 replication 为 async，请求在主分片上被执行后就会返回给客户端。它依旧会转发给复制节点，但你将不知道复制节点成功与否。

上面的这个选项不建议使用。默认的 sync 复制允许 ES 强制反馈传输。async 复制可能会因为在不等待其它分片就绪的情况下发送过多的请求而使 ES 过载。

全文检索

全文检索就是对一篇文章进行索引，可以根据关键字搜索，类似于 mysql 里的 like 语句。

全文索引就是把内容根据词的意义进行分词，然后分别创建索引，例如 “你们的激情是因为什么事情来的” 可能会被分词成：“你们”，“激情”，“什么事情”，“来” 等token，这样当你搜索 “你们” 或者 “激情” 都会把这句搜出来。





term索引词：在elasticsearch中索引词(term)是一个能够被索引的精确值。foo，Foo几个单词是不相同的索引词。索引词(term)是可以通过term查询进行准确搜索。
text文本：是一段普通的非结构化文字，通常，文本会被分析称一个个的索引词，存储在elasticsearch的索引库中，为了让文本能够进行搜索，文本字段需要事先进行分析；当对文本中的关键词进行查询的时候，搜索引擎应该根据搜索条件搜索出原文本。
analysis：分析是将文本转换为索引词的过程，分析的结果依赖于分词器，比如： FOO BAR, Foo-Bar, foo bar这几个单词有可能会被分析成相同的索引词foo和bar，这些索引词存储在elasticsearch的索引库中。当用 FoO:bAR进行全文搜索的时候，搜索引擎根据匹配计算也能在索引库中搜索出之前的内容。这就是elasticsearch的搜索分析。
routing路由：当存储一个文档的时候，他会存储在一个唯一的主分片中，具体哪个分片是通过散列值的进行选择。默认情况下，这个值是由文档的id生成。如果文档有一个指定的父文档，从父文档ID中生成，该值可以在存储文档的时候进行修改。
type类型：在一个索引中，你可以定义一种或多种类型。一个类型是你的索引的一个逻辑上的分类/分区，其语义完全由你来定。通常，会为具有一组相同字段的文档定义一个类型。比如说，我们假设你运营一个博客平台 并且将你所有的数据存储到一个索引中。在这个索引中，你可以为用户数据定义一个类型，为博客数据定义另一个类型，当然，也可以为评论数据定义另一个类型。
template：索引可使用预定义的模板进行创建,这个模板称作Index templatElasticSearch。模板设置包括settings和mappings。
mapping：映射像关系数据库中的表结构，每一个索引都有一个映射，它定义了索引中的每一个字段类型，以及一个索引范围内的设置。一个映射可以事先被定义，或者在第一次存储文档的时候自动识别。
field：一个文档中包含零个或者多个字段，字段可以是一个简单的值（例如字符串、整数、日期），也可以是一个数组或对象的嵌套结构。字段类似于关系数据库中的表中的列。每个字段都对应一个字段类型，例如整数、字符串、对象等。字段还可以指定如何分析该字段的值。
source field：默认情况下，你的原文档将被存储在_source这个字段中，当你查询的时候也是返回这个字段。这允许您可以从搜索结果中访问原始的对象，这个对象返回一个精确的json字符串，这个对象不显示索引分析后的其他任何数据。
id：一个文件的唯一标识，如果在存库的时候没有提供id，系统会自动生成一个id，文档的index/type/id必须是唯一的。
recovery：代表数据恢复或叫数据重新分布，ElasticSearch在有节点加入或退出时会根据机器的负载对索引分片进行重新分配，挂掉的节点重新启动时也会进行数据恢复。
River：代表ElasticSearch的一个数据源，也是其它存储方式（如：数据库）同步数据到ElasticSearch的一个方法。它是以插件方式存在的一个ElasticSearch服务，通过读取river中的数据并把它索引到ElasticSearch中，官方的river有couchDB的，RabbitMQ的，Twitter的，Wikipedia的，river这个功能将会在后面的文件中重点说到。
gateway：代表ElasticSearch索引的持久化存储方式，ElasticSearch默认是先把索引存放到内存中，当内存满了时再持久化到硬盘。当这个ElasticSearch集群关闭再重新启动时就会从gateway中读取索引数据。ElasticSearch支持多种类型的gateway，有本地文件系统(默认), 分布式文件系统，Hadoop的HDFS和amazon的s3云存储服务。
discovery.zen：代表ElasticSearch的自动发现节点机制，ElasticSearch是一个基于p2p的系统，它先通过广播寻找存在的节点，再通过多播协议来进行节点之间的通信，同时也支持点对点的交互。
Transport：代表ElasticSearch内部节点或集群与客户端的交互方式，默认内部是使用tcp协议进行交互，同时它支持http协议（json格式）、thrift、servlet、memcached、zeroMQ等的传输协议（通过插件方式集成）。



参考：

- https://www.cnblogs.com/cdchencw/p/12449500.html