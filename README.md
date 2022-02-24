## Spring 게시판 API 만들기 

<br/>

<br/>

### 사용 기술

##### Spring,   SpringBoot,   Security,   DATA JPA,   QueryDSL,   WebMVC, Data Redis, AOP

<br/>

<br/>

### 상세한 내용 - 블로그 확인

https://ttl-blog.tistory.com/265?category=910686

<br/>

<br/>

### 구현 기능

##### Security & JWT를 이용한 Authentication, Redis를 이용한 로그인 정보 Cache,

##### 기본적인 게시글, 댓글, 대댓글의 CRUD와, 대댓글 까지만 허용하는 댓글의 계층구조 구현,

##### AOP를 사용하여 로그 기록 남기기

##### Dev, Prod, Test 환경별 분리

<br/>

<br/>



### 구조

─boardexample
   └─myboard
       ├─domain
       │  ├─commnet
       │  │  ├─controller
       │  │  ├─dto
       │  │  ├─exception
       │  │  ├─repository
       │  │  └─service
       │  ├─member
       │  │  ├─controller
       │  │  ├─dto
       │  │  ├─exception
       │  │  ├─repository
       │  │  └─service
       │  └─post
       │      ├─cond
       │      ├─controller
       │      ├─dto
       │      ├─exception
       │      ├─repository
       │      └─service
       └─global
           ├─aop
           ├─cache
           ├─config
           ├─exception
           ├─file
           │  ├─exception
           │  └─service
           ├─jwt
           │  ├─filter
           │  └─service
           ├─log
           ├─login
           │  ├─filter
           │  └─handler
           └─util
               └─security



