# Annode 프로젝트

## 프로젝트 기획이유

"Pospace"는 음성 기반 커뮤니티 서비스로, 기본적인 컨텐츠는 글과 사진에 기반한 SNS입니다. 사용자는 SNS에서 Follow하고있는 친구들이 올린 게시글을 보게되고 이를
클릭하여 접근하게되면, 그 게시글을 실시간으로 함께 보고 있는 사람들과 그룹을 형성하게됩니다 이때 게시글을 보고있는 그룹단위로 음성대화와 채팅을 활용하여 교류를 할 수 있습니다.
이런 서비스를 선정한 이유는 SNS가 사용자 간의 교류와 사회적인 연결고리를 만들어주기 위한 의미가 점차 퇴색되어가고,
단순 반복적인 미디어의 소비만 하는 곳으로 바뀌는 추세가 보이기 때문입니다.
때문에 모호한 거리에 있는 사람들 간의 사회적 교류와 교감으로 네트워크를 형성시키는것을 목표로 Pospace를 만들게되었습니다.

또한, 프로젝트 개발을 통한 경험적인 측면에서도 많은 것을 얻을 수 있었습니다.

1. 웹소켓을 활용해 서버와 클라이언트 간의 양방향 통신을 구축해볼수있었습니다.

2. WebRTC를 사용하여 음성과 영상을 실시간으로 처리하고,
   P2P 방식을 이용한 사용자 간 실시간 대화를 통해 서버 비용을 최소화하면서도 서비스를 유지할 수 있었습니다.

3. AWS를 사용하여 다양한 클라우드 인프라들을 사용하여 실제 서비스를 배포하였고, 이를 통해 분산 환경의 실제 프로덕션 환경을 경험할 수 있었습니다.

4. 웹사이트뿐만 아니라 안드로이드 어플리케이션도 제작하여, 앱에서만 할수있는 기능인 연락처와 안드로이드 알림 등을 활용한 웹뷰 기반의 프로젝트도 진행해 보았습니다.

5. 웹사이트는 플랫폼이 명확하지 않아 개발과 배포에만 집중되는 경향이 있는데, 실제 운영 단계도 중요한 경험이라고 생각했습니다. 그래서 Google Play 스토어에 직접
   출시하고 검증을 받을 수 있었습니다.

이런 다양하고 의미 있는 경험을 통해 Annode를 기획하게 되었습니다.

## 사용한 기술

### 백엔드

- Spring Boot 3.0.6
- Spring Data JPA
- Spring Security
- Spring Websocket ( with SockJs )

### 데이터베이스

- Mysql 8.0.33

### 프론트엔드

- React.js
- SockJs-client
- Styled Components

### 개발언어

- Java
- JavaScript

## Android 앱 & 웹사이트

<a href='https://play.google.com/store/apps/details?id=com.kic.pospace'><img alt='Google Play에서 받기' src='https://play.google.com/intl/ko/badges/static/images/badges/ko_badge_web_generic.png' width='200'/></a>

[웹사이트로 방문](https://annode-kic.com)

- https://annode-kic.com

## [ 시스템 구성도 ]

![시스템구성도](https://user-images.githubusercontent.com/112735947/258618768-6599c07e-54ee-48cf-86e4-283f271e443d.png)

사용자의 접근방식은 아래 2가지 방식이있습니다

1. 일반적인 웹사이트 접속
2. 안드로이드 웹뷰를 이용한 Application 접속

## 프로젝트 진행 중 주요 문제해결 과정

### [01. 스케일 아웃을 고려한 시스템 구성도](https://cuboid-butterkase-b4a.notion.site/5462665955a0422eb1972c1303a87bac?pvs=4)

### [02. 시그널링 서버 구축 및 분산환경에서의 웹소켓 시그널링 ](https://cuboid-butterkase-b4a.notion.site/8dcda672a3bd4d49ba67ea7cf7c78357?pvs=4)

## 주요 구현 기능

### 소셜

#### Follow 기능

각 유저들은 다른 유저에게 팔로우를 신청할수있으며 팔로우 관계를 맺게되면 메인 화면에서 조회되는 게시글에 팔로우유저의 게시글을 자동조회합니다

팔로우는 A유저가 B유저에게 신청을하고 수락하면 A유저는 B유저의 게시글을 볼수있지만

B유저도 A유저의 게시글을 확인하려면 맞팔로우 관계가 되어야합니다

만약 Follow중인 유저라면 유저정보에서 UnFollow가 활성화됩니다

#### 유저 기능

특정 유저를 조회하여 팔로우를 신청하고싶거나 유저정보를 보고싶다면 특정유저의 고유코드번호 or 전화번호 or 이메일로 조회를 할수있습니다

그리고 내 정보에서 유저의 NickName이나 프로필사진을 수정할수있으며, 만약 회원탈퇴시에는 유저가 작성했던 댓글 , 게시글 , 팔로우관계의 엔티티가 모두 제거됩니다

### 게시글

사진과 텍스트내용을 입력하여 기본적인 게시글을 내용을 작성합니다 또한 다양한 설정 옵션을 제공하여 사용자가 원하는대로 게시글을 만들수있습니다

#### 설정옵션

게시글의 공개범위 : 전체공개 , 팔로우관계 , 맞팔로우관계를 선택하여 게시글을 조회할수있는 유저의 범위를 설정하였습니다
실시간 음성대화 허용 : 작성된 게시글에 실시간 음성대화기능의 사용유무를 선택하도록하였습니다
실시간 음성대화의 최대그룹 인원수 : 2 , 4 , 6 한정된 사용자의 기기성능을 고려하여 2명부터 최대 6명으로 선택하게 설정하였습니다
유저 태그 : 게시글을 작성시 맞팔로우 관계에 있는 친한 유저들을 태그에 넣을수있도록 하였습니다 이때 태그된 유저들에게는 푸시 알림을 보냅니다

#### 게시글 부가기능

추가 기능으로는 게시글 신고 삭제 수정(내용 및 옵션) 기능이 있습니다

유저들은 다양한 게시글에대해 좋아요를 할수있습니다 이때 여러 유저가 하나의 게시글에 좋아요를 누를때
동시성 문제를 방지하기위해 엔티티에 @Version을 부여하여 낙관적락을 걸어 방지하게 설계하였습니다

게시글에서 댓글을 작성하거나 제거할수있습니다 그리고 이때 게시글 엔티티와 댓글 , 유저태그정보는 별도의 테이블로 분리되어 1:N관계로 저장됩니다

#### 게시글 무한스크롤링 조회

게시글을 메인홈에서 조회할때에는 무한스크롤 방식으로 조회하게되며 이때 가져오는 게시글의 기준은
로그인중인 유저가 조회가 가능한 게시물을 공개범위를 기준으로 나눠서 가져오게됩니다
전체공개인 게시물과 Follow관계인 유저의 게시물을 기본적으로 조회하며
서로 팔로우중인 맞팔로우 게시물을 필터링하여 총 3개의 타입의 게시물들을 조합하여 게시물을 무한스크롤링 조회합니다
이때 페이지 기반이 아닌 커서기반방식으로 마지막 게시물의 id를 기반으로 조회합니다

### 회원가입 , 로그인 , 인가

유저의 회원가입은 이메일과 sms인증 두가지 과정을 거치게됩니다 이때 인증번호는 유효시간만큼만 존재하기에 빠르게 생성되고 제거될수있도록
DBMS가 아닌 인메모리 저장소인 Redis로 저장하였습니다 또한 실제로 서비스를 운영할때 단일서버로는 한계가있기에 여러 분산 클러스터에서
데이터의 일관성을 지키기위해 하나의 인메모리 저장소를 사용하여 동일하게 인증로직을 처리하였습니다

유저의 로그인 방식은 Spring Security , jwt방식을 사용하였으며 이로인해 서버는 각 유저의 정보를 기억하는 세션없이 Stateless하게 처리할수있었습니다
또한 인가방식에서 jwt를 통해 요청을 처리하기때문에 HTTP메세지 뿐만아니라 웹소켓기반 메세지를 시그널링 서버에서도 원활하게 인가를 처리할수있었습니다
Spring Security에서 제공하는 인가가 필요한 엔드포인트를 유저 권한별로 구별할수있었습니다

### 실시간 그룹 음성통화 및 채팅 , 시그널링서버

게시글이 하나의 대화공간처럼 사용할수있도록 사용자들이 음성통화를 할수있게 webRTC로 음성스트림을 생성하며 피어커넥션을 통해 음성통화를 할수있습니다
이때 각 유저간의 연결을 맺어주기위하여 각 유저의 연결을 위한 데이터인 SDP를 webSocket으로 시그널링서버에 전달하여 게시물을 하나의 채널로 정해두고
이 채널에 속해있는 유저들간의 SDP 전달을 시그널링 서버에서 중계하게 하였습니다 이때 스케일 아웃을위해 분산된 서버들이 각자 자신의 메모리에
웹소켓세션을 저장하게됩니다 이때 여러 노드들간의 분산된 웹소켓세션끼리 시그널링이 되도록 Redis의 PUB/SUB 기능을 사용하여 클라이언트로부터 전달받은 메세지를 Redis의
채널로 메세지를 발행하고 이를 각 클러스터 멤버들이 구독받게되어 분산된 환경에서도 시그널링이 되도록 설계하였습니다

#### 채널 Token

게시글이 음성대화를 할수있는 채널의 역할을 하기에 각 게시글에는 채널Token이 존재하고 이를 통해 채널에 입장하게됩니다
이때 채널Token이 노출되더라도 함부로 남용될수없도록 게시글의 첫 입장시 인가에서 사용되는 jwt에서 이메일을 추출하여 이를통해 채널Token을 암호화하고
이후 웹소켓세션이 시그널링 서버에 연결될때 jwt를 통해 이메일로 복호화를 하여 채널 Token으로 입장이 올바른지 유효성검증을 수행합니다

#### 게시글 음성채널 입장

만약 게시글에서 설정한 실시간 음성대화의 최대그룹 인원수를 넘어설경우 추가로 들어오는 유저들도 대화를 가능하게 하도록 게시글에서 추가로 채널들이
생성되게 하였습니다 이때 각 유저들이 게시글에 입장을 할때 가장 적은 대화인원수를 가진 채널부터 Redis의 시그널링 메세지를 발행하여 입장을 시키게됩니다

### 안드로이드를 위한 웹뷰방식 및 어플 알림 , 연락처기반 Follow기능

리액트와 스프링으로 만든 웹사이트를 안드로이드 환경에서도 사용할수있도록 웹뷰방식의 어플로 래핑하였습니다
또한 SNS서비스 특성상 어플 알림 및 연락처를 통한 Follow기능이 필요하였습니다
때문에 안드로이드 네이티브단에서 권한과 데이터를 얻게되면 이를 리액트레벨에서 활용할수있도록 @JavascriptInterface를 사용하여 각 플랫폼간 연계를 하였습니다

이때 리액트에서 안드로이드단의 웹뷰 JavascriptInterface가 감지된다면 네이티브 기능을 호출하도록 하였습니다

1. 안드로이드 네이티브 레벨에서 연락처 데이터를 조회한뒤 JavascriptInterface를 통하여 리액트로 넘기고 이를 통해 서버에 전달하여
   연락처에 저장된 유저를 조회하여 아는사람들간의 Follow를 원활히 할수있도록 기능을 설계하였습니다

2. 네이티브 어플레벨에서 웹뷰를 사용할때 마이크 , 디바이스 저장소 , 연락처 , 알림을 사용할수있도록 권한요청을 수행하였습니다

3. 작성자 게시글에 댓글이나 좋아요가 생성되거나 특정 게시글에 자신이 태그될때 유저가 어플을 키지않더라도 알림을 전송하게하였습니다
   이때 파이어베이스의 FCM토큰을 통해 기능을 만들었으며 안드로이드 MainActivity가 onCreate되고, 리액트의 베이스 컴포넌트가 첫 마운트된다면 FCM토큰을 서버로
   전송하게되어 서버는 이를 DB에 저장해두어 푸시알림기능을 사용할수있게됩니다 또한 어플이 제거 , 업데이트 , 데이터삭제 될경우를 대비하여 어플의 onCreate
   라이프사이클마다 FCM토큰을 항시 갱신시키게됩니다
