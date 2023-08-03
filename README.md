# Manage_Employee_App
1. Spring Bean, Spring Component: cần phân biệt được @Bean & @Component

Spring profile & configuration: biết cách get value từ file config, cần biết cách sử dụng nhiều file config (dev, alpha, beta, stg, prod)

IOC Containers, DI: cần phân biệt được các cách Inject/Autowired

Bean Scopes, Bean Life cycle

Entity, ORM, Relationship, Fetch Type (Lazy vs Eager): cần phân biệt được các loại Cascade, các FetchType, relationship

DTO <-> Entity, Mapstruct, Mapper

Controller, HTTP Methods (POST, GET, PUT, DELETE): cần biết các thành phần request, response, các annotation của request

Authentication vs Authorization: tìm hiểu về tầng filter, generate JWT, validate JWT, refesh token, secure API

Spring Security, User service

Service, Scheduler/Cronjob, Transactional
(Tìm hiểu xem config Scheduler/Cronjob động, tức là server đã chạy rồi muốn thay đổi thời gian scheduler thì phải làm thế nào?)

Email service, Thymeleaf, multilanguage: hiểu các cách config email
11.1 Annotation @Async, Spring event

Repository: hiểu các cách query data
12.1 Automatic Custom Queries
12.2 Manual Custom Queries (JPQL)
12.3 Native query
12.4 Customizing the Result with Class Constructors
12.5 Customizing the Result with Spring Data Projection (open/close)
12.6 Join table
12.7 Sort, Paging với JPQL & native query, so sanh voi slice
12.8 Cache query

Mock, Unit test, Integration test: tìm hiểu về Mockito.mock() vs @Mock vs @MockBean, test controller, test service, test repo

Spring resttemplate vs Webclient
