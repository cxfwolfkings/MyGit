# kafka代码

1. 概念
2. 特征
3. 代码



## 概念

- Producer：消息的生产者
- Consumer：消息的消费者
- ConsumerGroup：消费者组，实现单播和广播的手段
- Broker：kafak服务集群节点，Kafka集群中的一台或多台服务器统称broker
- Topic：Kafka处理资源的消息源(feeds of messages)的不同分类
- Partition：Topic 物理上的分组，一个topic可以分为多个partion，每个partion是一个有序的队列。partion中每条消息都会被分配一个有序的Id(offset)
- Message：消息，是通信的基本单位，每个producer可以向一个topic（主题）发布 一些消息
- Producers：消息和数据生成者，向Kafka的一个topic发布消息的过程叫做producers
- Consumers：消息和数据的消费者，订阅topic并处理其发布的消费过程叫做consumers



## 特征

- 重播场景

- 高吞吐

  kafak高吞吐原因分析：

  1. 借助操作系统的文件处理，底层使用page cache 加快读写，所以kafka 服务器要加大page cache 设置提高吞吐
  2. 本身顺序读写，降低复杂度，提高吞吐
  3. 二进制格式而不是json避免序列化反序列化
  4. 对消息的批处理和压缩

- 拉数据模式，可高自定义的消费逻辑

- 适用于所有发布订阅的场景

###### Kafka 使用场景

- Time travel/durable/commit log
- Many consumers for the same message
- High throughput (millions of messages per second)
- Stream processing
- Replicability
- High availability
- Message order

###### RabbitMQ场景

- flexible routing
- Priority Queue
- A standard protocol message queue

**生产者：**

生产者生产消息不仅必须指定Topic，还可按照需求指定发往特定的分区

**消费者：**

- Kafak消费消息后不会删除消息
- 消费者是通过offset偏移量来控制消费消息，offset持久化在消费者一方
- 一个Topic可被一个或多个消费者消费
- 一个消费者可消费不同的多个topic
- 消费者不仅可以指定要消费的Topic，还可指定消费的分区
- 同一个Group可以定义一个或多个消费者
- 同一个Group中的多个消费者只会有一个收到消息
- 不同Group相同Topic的消费者都会收到消息(fanout)

Kafka 只保证分区内的记录是有序的，而不保证主题中不同分区的顺序

- Kafka作为一个集群，运行在一台或者多台服务器上.
- Kafka 通过 topic 对存储的流数据进行分类。
- 每条记录中包含一个key，一个value和一个timestamp（时间戳）。

Kafak争抢模式实现：多个消费者，同一个Topic同一个Group

Kafak广播模式实现：多个消费者，同一个Topic不同Group



## 代码





##### 注意

重新分配分区后，新增分区要等几分钟后才可被触发使用。

多分区场景下，kafka 服务端 lag 有负数情况，目前官方修复为最多-1，此bug并不影响数据的准确性，客户端消费依然正常。生成的消息指定相同的key，此消息将发往同一个分区。消费者数量一定要小于分区数，否则：多出来的消费者永远无法消费到消息！

![x](../../../Resources/md/mq20.png)

**为什么Kafka使用的是磁盘反而最终强于依靠内存的rabbitmq？**

1. 顺序写入

   因为硬盘是机械结构，每次读写都会寻址->写入，其中寻址是一个“机械动作”，它是最耗时的。所以硬盘最“讨厌”随机I/O，最喜欢顺序I/O。为了提高读写硬盘的速度，Kafka就是使用顺序I/O。如果一个topic建立多个分区，那么每个parathion都是一个文文件，收到消息后Kafka会把数据插入到文件末尾。

2. Memory Mapped Files（内存映射文件）

   64位操作系统中一般可以表示20G的数据文件，它的工作原理是直接利用操作系统的Page来实现文件到物理内存的直接映射。完成映射之后你对物理内存的操作会被同步到硬盘上

   Kafka高效文件存储设计特点：Kafka把topic中一个parition大文件分成多个小文件段，通过多个小文件段，就容易定期清除或删除已经消费完的文件，减少磁盘占用。通过索引信息可以快速定位message和确定response的最大大小。通过index元数据全部映射到memory（内存映射文件），可以避免segment file的IO磁盘操作。通过索引文件稀疏存储，可以大幅降低index文件元数据占用空间大小。

**Kafka数据存储**

- 数据文件的分段

  Kafka解决查询效率的手段之一是将数据文件分段，比如有100条Message，它们的offset是从0到99。假设将数据文件分成5段，第一段为0-19，第二段为20-39，以此类推，每段放在一个单独的数据文件里面，数据文件以该段中最小的offset命名。这样在查找指定offset的Message的时候，用二分查找就可以定位到该Message在哪个段中。

- 为数据文件建索引

  数据文件分段使得可以在一个较小的数据文件中查找对应offset的Message了，但是这依然需要顺序扫描才能找到对应offset的Message。为了进一步提高查找的效率，Kafka为每个分段后的数据文件建立了索引文件，文件名与数据文件的名字是一样的，只是文件扩展名为.index。

  索引文件中包含若干个索引条目，每个条目表示数据文件中一条Message的索引。索引包含两个部分（均为4个字节的数字），分别为相对offset和position。

  相对offset：因为数据文件分段以后，每个数据文件的起始offset不为0，相对offset表示这条Message相对于其所属数据文件中最小的offset的大小。举例，分段后的一个数据文件的offset是从20开始，那么offset为25的Message在index文件中的相对offset就是25-20 = 5。存储相对offset可以减小索引文件占用的空间。

  position：表示该条Message在数据文件中的绝对位置。只要打开文件并移动文件指针到这个position就可以读取对应的Message了。

  index文件中并没有为数据文件中的每条Message建立索引，而是采用了稀疏存储的方式，每隔一定字节的数据建立一条索引。这样避免了索引文件占用过多的空间，从而可以将索引文件保留在内存中。但缺点是没有建立索引的Message也不能一次定位到其在数据文件的位置，从而需要做一次顺序扫描，但是这次顺序扫描的范围就很小了。



**参考：**

- [官方文档](http://kafka.apachecn.org/documentation.html#producerapi)

- [C#客户端使用](https://docs.confluent.io/current/clients/dotnet.html)

- [Apache kafka 工作原理介绍](https://www.ibm.com/developerworks/cn/opensource/os-cn-kafka/index.html)

- [从Kafka读取数据](https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html)

- [Kafka为什么这么快](https://www.freecodecamp.org/news/what-makes-apache-kafka-so-fast-a8d4f94ab145/)

