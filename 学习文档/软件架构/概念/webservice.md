# webservice

**WebService**

**1.** **动机**

答：1) 今天，万维网的主要用途是交互式的访问文档和应用程序;

  2) 大多数时候，这些访问是通过浏览器、音频播放器或其它交互式的前-后端系统;

  3) W3C: “假如万维网支持应用程序间的交互，Web在能力及应用范围上能得到引人注目的增长”

**2.** **技术基础**

答：Web services = XML + HTTP

  XML：通用数据描述语言;

  HTTP：被浏览器和Web servers广泛支持地一种传输协议;

**3.** **什么是****Web Service****？**

答：1) Web service最近成为IT业一个被过度传播的术语;

  2) Web service是自我包含、自我描述、模块化的程序，它能发布、定位以及通过Web调用;

  3) 一个Web service的例子是提供股价或处理信用卡交易。一旦一个web service被布署，其它应用程序即可发现和调用这个服务。

  4) 还有什么产业能使用web service？

**4. web service****理念**

答：Web service基于这样的理念：构建应用程序的时候通过发现以及调用网络上现在的应用去实现某现功能;

**5.** **自我包含**

答：1) 在客户端，无须附加的软件;

  2) 只须XML和HTTP协议客户端支持即可开始;

  3) 在服务器端，仅需要一个Web服务器和servlet引擎;

  4) 对于Web service使一个既存的系统重新可用而无须写一行代码是可行的;

**6.** **自我描述**

答：1) 无论是客户端还是服务器端除了格式和请求内容以及响应信息外无须关注任何事情;

  2) 信息格式定义通过消息传输;

  3) 无额外的无素贮藏库或代码产生工具需要;

**7. Web services****是模块化的**

答：1) Web services标准框架提供了一个组件模型;

  2) Web services是一种技术，用于部署和提供Web上的商业功能访问;

  3) J2EE、CORBA和其它标准是实现这些Web services的技术;

**8.** **发布、定位以及通过****Web****调用**

答：所需的一些额外的标准：

  **SOAP**：Simple Object Access Protocol、也可理解为 service-oriented architecture protocol，基于RPC和通讯协议的XML。

  **WSDL**：Web Service Description Language, 一个描述性的接口和协议绑定语言。

  **UDDI**：Universal Description, Discovery，and Integration，一种注册机制，用于查找Web service描述。

**9.** **语言无关和互操作性**

答：1) 客户端和服务器端能在不同环境下被实现;

  2) 既存的环境为了实现Web service无须进行改动;

  3) 但是在现在，我们假设Java是Web service客户端和服务器端的实现语言;

**10.** **基于开放的标准**

答：1) XML和HTTP是Web services的技术基础;

  2) 很大部分Web service技术使用开源项目构建;

  3) 因此，供应商无关以及互操作性是这时的现实目标。

**11. Web services****是动态的**

答：通过使用Web Services，动态电子商务变得很现实。因为，使用UDDI和WSDL，Web service描述和发现可以自动进行。

**12. Web services****是组合的**

答：简单的Web services能组合成更复杂的Web services，无论是使用工作流技术或是调用更底层的Web services。

**13.** **基于成熟技术构建**

答：1) XML + HTML

  2) 和其它分布式计算框架相比，有很多相同点也有很多基础性的不同。例如，传输协议基于文本而非二进制。

**14.** **因此****...****新的机会到来了**

答：1) 在这个时刻，Web service受到大量关注，产生了许多工作机会;

  2) 你应该理解并迅速掌握这项技术便能把握住这些机会。

**15. Web Service****角色**

答：1) service provider创建web service并发布它的接口和访问信息到服务登记处;

  2) service broker（也称为service registry）有责任使Web service接口和实现访问信息对任何潜在的service requestor可用;

  3) service requestor为了使用Web service，使用各种查找操作在broker登记处定义入口以及绑定到service provider。

**16. Web services****架构体系**

答：1) Web services通过service provider部署到Web上;

  2) Web service提供的功能使用WSDL描述;

  3) service broker帮助service provider和service requestor能互相找到对方;

  4) service requestor使用UDDI API从service broker处寻找它所需要的服务;

  5) 当service broker返回查找的结果，service requestor可使用这些结果绑定到一个特定服务;

  6) Web service描述由service provider创建和发布;

  7) Web service由service broker组织和查找;

  8) Web service由service requester定位和调用;

**17. Web services****组件**

答：前面显示了Web service中用到的三种主要的组件：

  1) Service provider: 提供服务并使这些服务可用;

  2) Service broker: 为service provider和service requestor配对;

  3) Service requester: 使用service broker查找Web service，然后调用这些服务去创建应用程序;

**18. Service provider****子角色**

答：1) WSDL规范由二部分组成：服务接口和服务实现;

  2) 服务接口提供者和服务实现者是service provider的子角色;

  3) 二个角色可以，但非必须被同一个事务承担;

**19. Web service****操作**

答：1) 发布/取消发布

​    发布服务至登记处;

​    移除这些登记的条款

​    service provider联系service broker发布/取消服务

  2) 查找操作由service requestor和service broker共同完成: service requestor描述他们查找的服务种类; service broker递交最匹配的请求结果。

  3) 绑定发生在service requestor和service provider间，他们会协议好以便requestor能访问和调用service provider提供的服务。

**20. WSDL****——****Web****服务描述语言**

答：1) WSDL是以XML为基础的接口定义语言，它提供了一种分类和描述Web service的方式;

  2) WSDL定义了：

​    Web service的接口，包括：

​     a. 操作方式(单向、通知、请求-响应);

​     b. 定义了Web service的消息;

​     c. 数据类型(XML schema);

​    Web service访问协议(SOAP over HTTP);

​    Web service联系的终点(Web service URL);

​    符合要求的服务端应用程序必须支持这些接口，客户端用户能从这份文档中得知如何访问一个服务。

**21. UDDI——****统一查找、描述以及综合**

答：1) UDDI提供了一种找到可用Web service的方式;

  2) UDDI提供了一个全球的、平台无关的、开放式框架，使得商业应用能：

​    相互查找;

​    定义它们通过Web交互的方式;

​    在一个全球注册场所共享信息;

  3) 在Web上存在三种开放的UDDI注册场所, 由IBM、Microsoft和HP发起;

  4) 注册是免费的，在任一注册处注册的内容被其它注册处所复制;

  5) 在UDDI商业注册处提供的信息由三部分组成：

​    “白皮书”：包括地址、联系以及标识符;

​    “黄皮书”：包括基于标准分类学的各产业分类;

​    “绿皮书”：所提供的service的技术信息;

  6) Web service provider和requester使用SOAP API和UDDI注册处交流;

**22. SOAP——****简单对象访问协议****(Simple Object Access Protocol)**

答：1) SOAP是一个网络中立的、轻量级的协议，用于交换两个远端应用程序的信息;

  2) SOAP是一个基于XML的协议，由三部分组成：

​    一个定义了一个框架的封套(envelope)，这个框架描述了信息的内容以及如何去处理它。

​    一系列的编码规则，用于表现系统定义的数据类型实例;

​    一个协定，用于表现远端处理调用和响应

**23. SOAP****范例**

答：1) 这个例子是一个SOAP请求以及响应的范例;

  2) 这些例子显示了一个客户端查询IBM股价的SOAP请求以及响应;

**24. SOAP****请求**

答：POST /soapsamples/servlet/rpcrouter HTTP/1.0

  Host: localhost

  Content-Type: text/xml:charset=utf-8

  Content-Length: 460

  SOAPAction: ""

  <?xml version='1.0' encoding='UTF-8'?>

  <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"

​    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

​    xmlns:xsd="http://www.w3.org/2001/XMLSchema">

​    <SOAP-ENV:Body>

​      <nsl:getQuote xmlns:nsl="urn:xmltoday-delayed-quotes"

​         SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">

​         <symbol xsi:type="xsd:string">IBM</symbol>

​      </nsl:getQuote>

​     </SOAP-ENV:Body>

  </SOAP-ENV:Envelope>

  1) SOAP请求表明getQuote方法从以下地址调用：http://localhost/soapsamples/servlet/rpcrouter

  2) SOAP协议并没有指定如何处理请求，服务提供者可运行一个CGI脚本，调用servlet或执行其它产生对应响应的处理;

  3) 响应包含于一个XML文档格式的表单内，该表单包含了处理的结果，在我们这个范例中是IBM的股价;

**25. SOAP****响应**

答：HTTP/1.1 200 OK

  Server: IBM HTTP SERVER/1.3.19 Apache/1.3.20 (Win32)

  Content-Length: 479

  Connection: close

  Content-Type: text/xml; charset = utf-8

  Content-Language: en

  <?xml version='1.0' encoding='UTF-8'?>

  <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"

​    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

​    xmlns:xsd="http://www.w3.org/2001/XMLSchema">

​    <SOAP-ENV:Body>

​      <nsl:getQuoteResponse xmlns:nsl="urn:xmltoday-delayed-quotes"

​        SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">

​        <return xsi:type="xsd:float">108.53</return>

​      </nsl:getQuoteResponse>

​    </SOAP-ENV:Body>

  </SOAP-ENV:Envelope>

  结果所位于的元素名称在请求方法名后加后缀"Response",例请求方法名为：getQuote, 响应方法名为：getQuoteResponse。

**26. Http****响应状态**

答：1) 1XX——information

  2) 2XX——success

  3) 3XX——redirection

  4) 4XX——client error

  5) 5XX——sever error

**27. Web service****的好处**

答：专注于核心商业逻辑，使用Web service应用于非核心商业逻辑从而以一个很低的成本快速发布新的IT解决方案;

  通过使用Web service封装以前软件系统到当前系统中可保护既有投资;

  以最少的费用将用户和伙伴的商业系统结合到一块;

  **好处****——****促进协同工作能力**

  1) service provider和service requester之间的沟通设计为平台和语言无关;

  2) 这个交互需要一份WSDL文档，这份文档定义了接口以及描述了相应的服务，连同网络协议在一起(通常是HTTP);

  **好处****——****快速发布新的****IT****解决方案**

  1) 当service requester使用service broker寻找service provider，这种发现是自动发生的。

  2) 一旦requester和provider相互找到，provider的WSDL文档用于将requester和服务绑定到一块。

  3) 这意味着requester、provider和broker一块创建的系统是自我设置、自我适应以及强健的。

  **好处****——****通过封装降低了复杂性**

  1) service requester和provider只关心必要的接口;

  2) service requester并不关心service provider如何实现服务;

  3) 这些细节都在requester和provider方封装好，这种封装对于降低复杂性非常重要;

  **好处****——****给遗留系统以新的生机**

  1) 对于一个遗留系统、产生一个SOAP包装，然后产生一个WSDL文档将应用程序作为一个web service;

  2) 这意味着遗留系统能用于新的方面;

  3) 此外，与遗留系统相联系的基础设施能封装成一系列的服务;