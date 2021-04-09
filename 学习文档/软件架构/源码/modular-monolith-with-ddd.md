一个最近由 Kamil Grzybek 在 Github 发布的项目，给出了使用领域驱动设计（DDD，Domain-Driven Design）方法设计并实现一个单体（monolith）应用的详细介绍。该项目的目标，就是展示如何以模块化方式设计并实现一个单体应用。此外，Grzybek 还基于他在应用开发实践的收获，给出了一些有用的架构建议和设计模式。

Grzybek 是华沙 ITSG Global 的一位系统架构师，并担任团队负责人。他指出，该项目的目标并非是要创建一个极简应用，或是一个验证原型（PoC，proof of concept），而是给出一种适用于生产环境的完整实现。该项目的动机，来自于 Grzybek 对一些类似的但并未取得成功的项目的审视。在他看来，大多数示例应用过于简单，或是不够完整。他一直认为，这些应用至少在某些部分上存在着设计和实现上的问题，或是存在着相关文档缺失的问题。

Grzybek 强调指出，他的实现只是解决类似业务问题的许多方法之一。系统的软件架构需考虑多种因素，例如功能要求、质量属性和技术约束等，另一方面也会受开发人员的经验、技术约束、时间和预算等因素的影响，所有这些因素都会影响解决方案。

该应用针对的是为大多数开发人员所认可的会议组（meeting group）领域。应用实现中考虑了额外的复杂性，因此相比基于 CRUD 的常规应用更具意义。Grzybek 将主领域进一步划分为四个子域，即会议、支付、管理，以及用户访问。

为给出领域所需的功能，Grzybek 采用了一种称为“事件风暴”（ Event Storming ）的方法。该方法由 Alberto Brandolini 建立，用于探索复杂业务领域。Grzybek 使用该方法在主领域的各子域中发现行为和事件。

从更高层次上看，该架构中定义了一个 API 层、包含存储的四个模块（分别对应于所发现的四个子域），以及一个用于通信的公共事件总线（EventBus），如下图所示。

![x](../../Resources/modular-monolith-with-ddd01.jpg)

模块也相应地划分为四个子模块，并分别实现为独立的二进制文件，分别为：处理所有请求的 Application、实现领域逻辑的 Domain、实现基础架构代码的 Infrastructure，以及在 EventBus 上发布事件并且是模块间唯一共享组件的 IntegrationEvents。Grzybek 使用了 Decorator 模式，实现添加工作单元（ Unit of Work ）和日志等交叉关注点（cross-cutting）。

为分离应用内部的命令和查询，Grzybek 使用并实现了 CQRS 的一种变体。该 CQRS 变体针对命令所涉及的同一数据库表，在查询中使用了原始 SQL 和视图。虽然 CQRS 还具体其他变体，但 Grzybek 力图避免使应用过于复杂化。

模块间的集成是基于异步事件传输的。事件传输使用了“发件箱模式”/“收件箱模式”（ outbox and inbox pattern ），以及基于内存中的 EventBus 代理。为存储要发布的事件，发件箱模式在数据存储中添加了独立的表。事件的添加，实现中通过执行任务的命令，以及等同于命令的事务。此后，这些事件通过单独的流程，转发到另一个模块的收件箱中。在该项目中，事件传输是通过各模块中的后台 Worker 实现的。该实现提供了多次交付和处理。

最后，Grzybek 强调该项目仍在开发中，欢迎贡献者的加入。项目是使用 C# 编写的.NET Core 应用，使用了像 Autofac （用于 IoC ）、 Dapper (一种用于读取模型的 MicroORM ）之类的类库。项目中还包括基于 Arrang-Act-Assert 模式的测试，使用 NUnit 实现。

在与 InfoQ 的访谈中，Grzybek 进一步详细介绍了他的设计理念。

InfoQ：相比基于微服务的设计，您如何评价单体应用的模块化设计？

**Kamil Grzybek：**与微服务体系架构相比，模块化单体应用的主要差别之处在于部署方法和模块间通信。

在微服务架构中，每个模块都以独立的进程运行。模块间的通信必须使用网络实现，并且通常通过同步服务 API 调用（即 RPC，远程过程调用），或是使用代理（即消息传递）实现。微服务架构是一种分布式系统，具有分布式的所有优点和缺点。对于模块化单体应用，则无需考虑分布式系统。所有模块均以同一进程运行，无需使用网络即可相互通信。各模块可以通过方法调用直接同步引用内存中对象，或是异步地使用运行于同一进程中的某个中介者（Mediator）。

InfoQ：相比其他解决方案，例如一些消息传递平台，使用 EventBus 和发件箱 / 收件箱模式具有哪些优缺点？

**Grzybek：**模块化单体应用的主要优点，是开发人员无需使用任何消息平台，因为大多数功能都可以使用现有的设计模式在内存中实现。模块化单体应用本身可担当此类平台。当然，对于更高级的系统，使用独立的平台可能是更好的解决方案。但做出此决定时，必须谨慎。

InfoQ：您在 CQRS 模式的实现中，使用了视图和原始 SQL，而非单独的表。对此您能详细介绍一下吗？

**Grzybek：**我的方法虽然是一种最简单的 CQRS 实现，但其功能强大，通常完全够用。使用视图是应用和数据库之间的一种抽象和契约形式。开发人员可随时进入 CQRS 实现的下一层，读取应用逻辑可以保持不变。

下一层是物化视图。物化视图加快了读取的速度，但会导致写入性能略有下降。一些最先进的系统对于扩展性要求极高，它们引入了 CQRS 模式的最高层实现，并异步更新读取模型。这就是“最终一致性”。

每个实现层，都会增加解决方案的复杂性，因此，只有在真正需要时，我们才应去动更高的层。



## 简介

此项目的主要目标：

- 展示如何以**模块化**方式实现**整体**应用程序
- 介绍应用程序 的完整实施
  - 这不是另一个简单的应用程序
  - 这不是另一种概念证明（PoC）
  - 目的是提出可以在生产环境中运行的应用程序的实现
- 展示**最佳实践**和**面向对象编程原则的应用**
- 介绍**设计模式**的使用。何时，如何以及为什么可以使用它们
- 介绍一些**架构上的**考虑因素，决策，方法
- 使用**领域驱动设计**方法（**战术**模式）的实现的介绍
- 演示领域模型的**单元测试**的实现（考虑可测试的设计）
- 介绍**集成测试**
- 介绍**事件溯源**的实施

没有：

- 业务需求收集和分析
- 系统分析
- 领域探索
- 领域蒸馏
- 域驱动设计**策略**模式
- 架构评估，质量属性分析
- 集成，系统测试
- 项目管理
- 基础设施
- 容器化
- 软件工程过程
- 部署过程
- 维护
- 文献资料



## 领域

**定义：**

> 领域：知识，影响或活动的范围。用户提供的应用程序的主题范围就是软件的领域。[领域驱动设计参考](http://domainlanguage.com/ddd/reference/)，Eric Evans

**Meetings**

主要业务实体`Member`，`Meeting Group`和`Meeting`。`Member`可以创建`Meeting Group`，成为`Meeting Group`的一员，也可以参加`Meeting`。

`Meeting Group Member`可以是组织者`Organizer`，也可以是普通`Member`。

只有`Organizer`可以创建一个新的`Meeting`。

一个`Meeting`有参与者，非参与者（`Members`中宣布不参加该`Meeting`的人）和等候区`Waitlist`的`Members`。

一个`Meeting`可以有与会者限制。如果达到限制，`Members`则只能在等候区`Waitlist`签到。

`Meeting Attendee`可以带客人到`Meeting`，允许的客人人数由`Meeting`属性限制，可以禁止带客人。

`Meeting Attendee`可以具有以下两个角色之一：`Attendee`或`Host`。一个`Meeting`必须至少有一个`Host`。`Host`是`Meeting`的管理员，可以编辑`Meeting`信息或更改与会者名单。

每个`Meeting Group`必须有一个组织者，该组织者有预定会议`Subscription`的权限（收费权限）。一位组织者最多可以在3个`Meeting Groups`中预定会议。

此外，会议组织者可以设置一个`Event Fee`。每个`Meeting Attendee`都有义务支付费用。所有客人也应由`Meeting Attendee`付款。

**Administration**

要创建一个新的`Meeting Group`，`Member`需要向`group`提出。一个建议`Meeting Group Proposal`将发送给`Administrators`。`Administrator`可以接受或拒绝这个`Meeting Group Proposal`。如果接受，一个`Meeting Group`会被创建。

**Payments**

一个`Member`如果是付款人`Payer`，则可以买`Subscription`服务。他需要支付`Subscription Payment`。`Subscription`可以到期，因此续订`Subscription Renewal`是必要的（通过`Subscription Renewal Payment`付款保持`Subscription`有效）。

当`Meeting`需要费用时，`Payer`需要支付`Meeting Fee`（通过`Meeting Fee Payment`）。

**Users**

每一个`Administrator`，`Member`和`Payer`都是一个`User`。成为`User`，必须通过注册成为`User`，并且还要确认。

每个`User`分配一个或多个`User Role`。

每个`User Role`都有一套`Permissions`。一个`Permission`决定`User`是否可以调用特定动作。

### 概念模型

**定义：**

> 概念模型：一个系统的表示，由以下概念组成：用于帮助人们认识，理解或模拟模型所代表的主题。[维基百科-概念模型](https://en.wikipedia.org/wiki/Conceptual_model)

![x](../../Resources/Conceptual_Model.png)

### 事件风暴

虽然概念模型关注于结构和它们之间的关系，但是在我们的领域中发生的**行为**和**事件**更为重要。

有很多显示行为和事件的方法。其中一种是称为[Event Storming](https://www.eventstorming.com/)的轻量级技术，这种技术正变得越来越流行。下面介绍了使用此技术的3个主要业务流程：用户注册，会议组创建和会议组织。

注意：Event Storming是一个轻量级的现场研讨会。这里介绍了该研讨会的可能输出之一。即使您不参加Event Storming研讨会，这种过程演示对您和您的利益相关者也可能非常有价值。

**用户注册流程**

![x](../../Resources/User_Registration.jpg)

**会议组创建**

![x](../../Resources/Meeting_Group_Creation.jpg)

**会议组织**

![x](../../Resources/Meeting_Organization.jpg)

**支付**

![x](../../Resources/Payments_EventStorming_Design.jpg)



## 架构

### 高级视图

![x](../../Resources/Architecture_high_level.png)

模块描述：

- API：很薄的一层REST API应用
  - 接受请求
  - 通过用户权限模块认证请求
  - 给特定模块委托工作（命令 或 查询）
  - 返回响应

- 用户认证：负责用户身份验证，授权和注册
- 会议：实现会议界限上下文：创建会议组，会议
- 管理员：实现管理边界上下文：实现管理任务，例如会议组提案验证
- 付款：实现支付边界上下文：实现与支付相关的所有功能
- 内存事件总线：发布/订阅实现，以使用事件异步集成所有模块（“[事件驱动的体系结构”](https://en.wikipedia.org/wiki/Event-driven_architecture)）。

优点：

1. API不包含应用逻辑
2. API通过一个简单接口和模块交互，发送查询和命令请求
3. 每个模块有自己的接口，供API使用
4. 模块之间只使用事件总线异步调用，不允许直接方法调用
5. 每个模块有自己独立的数据，不允许共享数据；如果需要，可以将模块数据移动到单独的数据库中
6. 每个模块只依赖其它模块的集成事件（请参阅[模块级别视图](https://github.com/kgrzybek/modular-monolith-with-ddd#32-module-level-view)）
7. 每个模块有自己的聚合根，每个模块有自己的控制反转容器
8. 每个模块有自己的初始化方法，API中需要初始化每个模块
9. 每个模块高内聚，只有必需的类型和成员是公开的，其余是内部或私有的

### 模块级视图

![x](../../Resources/Module_level_diagram.png)

每个模块都有一个清洁的架构，包含：

- 应用程序：负责处理请求的应用程序逻辑子模块：用例，领域事件，集成事件，内部命令。
- 领域：领域模型实现了适用的[有界上下文](https://martinfowler.com/bliki/BoundedContext.html)
- 基础设施：基础结构代码，负责模块初始化，后台处理，数据访问，与事件总线和其他外部组件或系统的通信
- 集成事件：**契约**发布到活动总线; 只有此程序集可以被其他模块调用

**注意：**应用程序，域和基础设施可以合并为一个程序集。有些人喜欢水平分层或更多分解功能，有些则不喜欢。在单独的程序集中实现域模型或基础结构允许使用[`internal`](https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/internal)关键字进行封装。有时，边界上下文逻辑不值得，因为它太简单了。一如既往，务实并采取任何您喜欢的方法。

### API和模块通信

API通过两种方式与模块交互：

1. 模块初始化
2. 请求处理

**模块初始化**

每个模块都有一个静态`Initialize`方法，该方法在API项目`Startup`类中调用。此模块所需的所有配置都应作为此方法的参数提供。在初始化期间配置所有服务，并使用“控制反转”容器创建聚合根。

```c#
public static void Initialize(
    string connectionString,
    IExecutionContextAccessor executionContextAccessor,
    ILogger logger,
    EmailsConfiguration emailsConfiguration)
{
    var moduleLogger = logger.ForContext("Module", "Meetings");

    ConfigureCompositionRoot(connectionString, executionContextAccessor, moduleLogger, emailsConfiguration);

    QuartzStartup.Initialize(moduleLogger);

    EventsBusStartup.Initialize(moduleLogger);
}
```

**请求处理**

每个模块都有暴露给API的相同接口签名。它包含3种方法：带结果的命令，不带结果的命令和查询。

```c#
public interface IMeetingsModule
{
    Task<TResult> ExecuteCommandAsync<TResult>(ICommand<TResult> command);

    Task ExecuteCommandAsync(ICommand command);

    Task<TResult> ExecuteQueryAsync<TResult>(IQuery<TResult> query);
}
```

**注意：**有人说处理命令不应返回结果。这是一种可以理解的论述，但有时不切实际，尤其是当您要立即返回新创建的资源的ID时。有时，命令和查询之间的边界是模糊的。一个示例是`AuthenticateCommand`，返回令牌，但它不是查询，因为有副作用。

### 通过CQRS处理模块请求

命令和查询的处理通过应用体系结构样式[CQRS](https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs)实现

![x](../../Resources/CQRS.jpg)

1、命令通过写对象（领域驱动设计战术模式实现）处理

```c#
internal class CreateNewMeetingGroupCommandHandler : ICommandHandler<CreateNewMeetingGroupCommand>
{
    private readonly IMeetingGroupRepository _meetingGroupRepository;
    private readonly IMeetingGroupProposalRepository _meetingGroupProposalRepository;

    internal CreateNewMeetingGroupCommandHandler(
        IMeetingGroupRepository meetingGroupRepository,
        IMeetingGroupProposalRepository meetingGroupProposalRepository)
    {
        _meetingGroupRepository = meetingGroupRepository;
        _meetingGroupProposalRepository = meetingGroupProposalRepository;
    }

    public async Task<Unit> Handle(CreateNewMeetingGroupCommand request, CancellationToken cancellationToken)
    {
        var meetingGroupProposal = await _meetingGroupProposalRepository.GetByIdAsync(request.MeetingGroupProposalId);

        var meetingGroup = meetingGroupProposal.CreateMeetingGroup();

        await _meetingGroupRepository.AddAsync(meetingGroup);

        return Unit.Value;
    }
}
```

2、查询通过读对象（在数据库视图上使用原生SQL）处理

```c#
internal class GetAllMeetingGroupsQueryHandler : IQueryHandler<GetAllMeetingGroupsQuery, List<MeetingGroupDto>>
{
    private readonly ISqlConnectionFactory _sqlConnectionFactory;

    internal GetAllMeetingGroupsQueryHandler(ISqlConnectionFactory sqlConnectionFactory)
    {
        _sqlConnectionFactory = sqlConnectionFactory;
    }

    public async Task<List<MeetingGroupDto>> Handle(GetAllMeetingGroupsQuery request, CancellationToken cancellationToken)
    {
        var connection = _sqlConnectionFactory.GetOpenConnection();

        const string sql = "SELECT " +
                           "[MeetingGroup].[Id], " +
                           "[MeetingGroup].[Name], " +
                           "[MeetingGroup].[Description], " +
                           "[MeetingGroup].[LocationCountryCode], " +
                           "[MeetingGroup].[LocationCity]" +
                           "FROM [meetings].[v_MeetingGroups] AS [MeetingGroup]";
        var meetingGroups = await connection.QueryAsync<MeetingGroupDto>(sql);

        return meetingGroups.AsList();
    }
}
```

优点：

- 解决方案与问题匹配：读写需求通常是不同的

- [单一职责原则](https://en.wikipedia.org/wiki/Single_responsibility_principle)（SRP）：一次只做一件事
- [接口隔离原则](https://en.wikipedia.org/wiki/Interface_segregation_principle)（ISP）：每个处理程序仅使用一种方法实现接口
- [参数对象模式](https://refactoring.com/catalog/introduceParameterObject.html)：命令和查询是易于序列化/反序列化的对象
- 易于通过[装饰器模式](https://en.wikipedia.org/wiki/Decorator_pattern)实现面向切口编程
- 通过[中介者模式](https://en.wikipedia.org/wiki/Mediator_pattern)实现松耦合：将请求的调用者与请求的处理程序分开

坏处：

- 中介者模式引入了额外的间接处理，并且更难于推断哪个类处理了请求

有关更多信息：[具有原始SQL和DDD的简单CQRS实现](https://www.kamilgrzybek.com/design/simple-cqrs-implementation-with-raw-sql-and-ddd/)

### 领域模型的原理和属性

领域模型是系统中最重要的部分，要特别注意设计。以下是一些适用于每个模块的领域模型的关键原理和属性：

1. 高度封装：默认情况下全部成员都是`private`，然后是`internal`，只有在必要时才标识为`public`。
2. 高水平的PI（Persistence Ignorance，持续性无知）：所有类都是[POCO](https://en.wikipedia.org/wiki/Plain_old_CLR_object)（简单对象），不依赖基础设施、数据库
3. 充血模型，行为丰富：所有业务逻辑都位于领域模型中，不会泄漏到应用程序层或其他地方。
4. 不偏执于基本类型：实体的原始属性都聚合在值对象（ValueObjects）中
5. 业务语言：所有类，方法和成员均以边界上下文中使用的业务语言命名。
6. 易于测试：领域模型是系统的关键部分，因此应该易于测试（可测试设计）。

```c#
public class MeetingGroup : Entity, IAggregateRoot
{
    public MeetingGroupId Id { get; private set; }

    private string _name;

    private string _description;

    private MeetingGroupLocation _location;

    private MemberId _creatorId;

    private List<MeetingGroupMember> _members;

    private DateTime _createDate;

    private DateTime? _paymentDateTo;

    internal static MeetingGroup CreateBasedOnProposal(
        MeetingGroupProposalId meetingGroupProposalId,
        string name,
        string description,
        MeetingGroupLocation location, MemberId creatorId)
    {
        return new MeetingGroup(meetingGroupProposalId, name, description, location, creatorId);
    }

     public Meeting CreateMeeting(
            string title,
            MeetingTerm term,
            string description,
            MeetingLocation location,
            int? attendeesLimit,
            int guestsLimit,
            Term rsvpTerm,
            MoneyValue eventFee,
            List<MemberId> hostsMembersIds,
            MemberId creatorId)
        {
            this.CheckRule(new MeetingCanBeOrganizedOnlyByPayedGroupRule(_paymentDateTo));

            this.CheckRule(new MeetingHostMustBeAMeetingGroupMemberRule(creatorId, hostsMembersIds, _members));

            return new Meeting(this.Id,
                title,
                term,
                description,
                location,
                attendeesLimit,
                guestsLimit,
                rsvpTerm,
                eventFee,
                hostsMembersIds,
                creatorId);
        }
```

### 面向切面概念

用装饰器模式实现了面向切面编程，满足单一职责和“不重复造轮子”原则。每个Command处理类都被日志、认证和工作单元3个装饰器装饰。

![x](../../Resources/Decorator.jpg)

**Logging**

日志装饰器记录每个命令的执行，参数和处理。这样，处理器内部的每个日志都具有处理命令的日志上下文。

```c#
internal class LoggingCommandHandlerDecorator<T> : ICommandHandler<T> where T:ICommand
{
    private readonly ILogger _logger;
    private readonly IExecutionContextAccessor _executionContextAccessor;
    private readonly ICommandHandler<T> _decorated;

    public LoggingCommandHandlerDecorator(
        ILogger logger,
        IExecutionContextAccessor executionContextAccessor,
        ICommandHandler<T> decorated)
    {
        _logger = logger;
        _executionContextAccessor = executionContextAccessor;
        _decorated = decorated;
    }
    public async Task<Unit> Handle(T command, CancellationToken cancellationToken)
    {
        if (command is IRecurringCommand)
        {
            return await _decorated.Handle(command, cancellationToken);
        }
        using (
            LogContext.Push(
                new RequestLogEnricher(_executionContextAccessor),
                new CommandLogEnricher(command)))
        {
            try
            {
                this._logger.Information(
                    "Executing command {Command}",
                    command.GetType().Name);

                var result = await _decorated.Handle(command, cancellationToken);

                this._logger.Information("Command {Command} processed successful", command.GetType().Name);

                return result;
            }
            catch (Exception exception)
            {
                this._logger.Error(exception, "Command {Command} processing failed", command.GetType().Name);
                throw;
            }
        }
    }

    private class CommandLogEnricher : ILogEventEnricher
    {
        private readonly ICommand _command;

        public CommandLogEnricher(ICommand command)
        {
            _command = command;
        }
        public void Enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory)
        {
            logEvent.AddOrUpdateProperty(new LogEventProperty("Context", new ScalarValue($"Command:{_command.Id.ToString()}")));
        }
    }

    private class RequestLogEnricher : ILogEventEnricher
    {
        private readonly IExecutionContextAccessor _executionContextAccessor;
        public RequestLogEnricher(IExecutionContextAccessor executionContextAccessor)
        {
            _executionContextAccessor = executionContextAccessor;
        }
        public void Enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory)
        {
            if (_executionContextAccessor.IsAvailable)
            {
                logEvent.AddOrUpdateProperty(new LogEventProperty("CorrelationId", new ScalarValue(_executionContextAccessor.CorrelationId)));
            }
        }
    }
}
```

**Validation**

认证装饰器执行命令数据验证。它使用FluentValidation库对照Command参数检查规则。

```c#
internal class ValidationCommandHandlerDecorator<T> : ICommandHandler<T> where T:ICommand
{
    private readonly IList<IValidator<T>> _validators;
    private readonly ICommandHandler<T> _decorated;

    public ValidationCommandHandlerDecorator(
        IList<IValidator<T>> validators,
        ICommandHandler<T> decorated)
    {
        this._validators = validators;
        _decorated = decorated;
    }

    public Task<Unit> Handle(T command, CancellationToken cancellationToken)
    {
        var errors = _validators
            .Select(v => v.Validate(command))
            .SelectMany(result => result.Errors)
            .Where(error => error != null)
            .ToList();

        if (errors.Any())
        {
            var errorBuilder = new StringBuilder();

            errorBuilder.AppendLine("Invalid command, reason: ");

            foreach (var error in errors)
            {
                errorBuilder.AppendLine(error.ErrorMessage);
            }

            throw new InvalidCommandException(errorBuilder.ToString(), null);
        }

        return _decorated.Handle(command, cancellationToken);
    }
}
```

**Unit Of Work**

所有 Command 都有副作用。为了避免在每个 handler 上调用commit，使用了`UnitOfWorkCommandHandlerDecorator`装饰器。它还标记`InternalCommand`为已处理（如果它是内部命令），并调度所有领域事件（作为[工作单元的](https://martinfowler.com/eaaCatalog/unitOfWork.html)一部分）。

```c#
public class UnitOfWorkCommandHandlerDecorator<T> : ICommandHandler<T> where T:ICommand
{
    private readonly ICommandHandler<T> _decorated;
    private readonly IUnitOfWork _unitOfWork;
    private readonly MeetingsContext _meetingContext;

    public UnitOfWorkCommandHandlerDecorator(
        ICommandHandler<T> decorated,
        IUnitOfWork unitOfWork,
        MeetingsContext meetingContext)
    {
        _decorated = decorated;
        _unitOfWork = unitOfWork;
        _meetingContext = meetingContext;
    }

    public async Task<Unit> Handle(T command, CancellationToken cancellationToken)
    {
        await this._decorated.Handle(command, cancellationToken);

        if (command is InternalCommandBase)
        {
            var internalCommand =
                await _meetingContext.InternalCommands.FirstOrDefaultAsync(x => x.Id == command.Id,
                    cancellationToken: cancellationToken);

            if (internalCommand != null)
            {
                internalCommand.ProcessedDate = DateTime.UtcNow;
            }
        }

        await this._unitOfWork.CommitAsync(cancellationToken);

        return Unit.Value;
    }
}
```

### 模块集成

使用集成事件和内存中事件总线作为代理，模块之间的集成是严格**异步的**。这样，模块之间的耦合极少，并且仅存在于集成事件的结构上。

**模块不共享数据，**因此不可能也不希望创建跨多个模块的事务。为了确保最大的可靠性，使用了[发件箱/收件箱模式](http://www.kamilgrzybek.com/design/the-outbox-pattern/)。因此，此模式提供*“至少一次发送”*和*“至少一次处理”*。

![x](../../Resources/OutboxInbox.jpg)

使用两个SQL表和每个模块的后台工作程序实现发件箱和收件箱。后台工作器是使用Quartz.NET库实现的。

**保存到发件箱：**

![x](../../Resources/OutboxSave.png)

**处理发件箱：**

![x](../../Resources/OutboxProcessing.png)

### 内部处理

该系统的主要原理是您只能通过调用特定的命令来更改其状态。

命令不仅可以由API调用，还可以由处理模块本身调用。实现此机制的主要用例是，当我们想要以不同的流程和事务处理某些事物时，将以最终一致性进行数据处理。例如，收件箱处理，因为我们要基于收件箱中的集成事件来执行某些操作（称为命令）。

这个想法取材于Alberto和Brandolini的Event Storming图片（名字为“几乎解释了所有内容的图片”），该图片表明，每个副作用（领域事件）都是通过调用“聚合命令”创建的。有关更多详细信息，请参见[EventStorming备忘单](https://xebia.com/blog/eventstorming-cheat-sheet/)文章。

内部处理的实现与发件箱和收件箱的实现非常相似。一个SQL表和一个后台工作者进行处理。每个内部处理的Command必须从`InternalCommandBase`类继承：

```c#
internal abstract class InternalCommandBase : ICommand
{
    public Guid Id { get; }

    protected InternalCommandBase(Guid id)
    {
        this.Id = id;
    }
}
```

这很重要，因为`UnitOfWorkCommandHandlerDecorator`必须在提交期间将内部命令标记为已处理：

```c#
public async Task<Unit> Handle(T command, CancellationToken cancellationToken)
{
    await this._decorated.Handle(command, cancellationToken);

    if (command is InternalCommandBase)
    {
        var internalCommand =
            await _meetingContext.InternalCommands.FirstOrDefaultAsync(x => x.Id == command.Id,
                cancellationToken: cancellationToken);

        if (internalCommand != null)
        {
            internalCommand.ProcessedDate = DateTime.UtcNow;
        }
    }

    await this._unitOfWork.CommitAsync(cancellationToken);

    return Unit.Value;
}
```

### 安全性