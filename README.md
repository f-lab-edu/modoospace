
## ModooSpace
스터디룸, 회의실, 연습실, 파티룸, 스튜디오 등 모든 공간을 시간단위로 대여할 수 있는 공간 대여 플랫폼입니다.
> ❗️ 단 호스트의 승인이 있어야 사용이 가능합니다. 

### 프로젝트 목표
- 국내 [SpaceCloud](https://www.spacecloud.kr/)를 모티브로 공간대여 플랫폼을 구현하였습니다.
- 비즈니스 로직을 객체에게 최대한 위임하여 Service Layer에서 객체가 서로 협력하여 요청을 수행할 수 있도록 아키텍처를 구성하였습니다.
- 해당 프로젝트에서는 Mock없는 테스트를 지향하며 Domain 단위테스트, Service 통합테스트를 수행하여 TestCoverage 80%를 달성하였습니다.
- 단순 기능만 구현한 것이 아닌, 성능 테스트를 통해 높은 트래픽을 가정한 상황에서도 안정적인 서비스를 유지할 수 있도록 지속적으로 서버 구조를 개선 중입니다.

### 사용 기술
<img width="637" alt="스크린샷 2024-03-27 오전 12 19 02" src="https://github.com/f-lab-edu/modoospace/assets/48192141/94b581e1-0863-49af-8ea8-bc3f80a29807">

### ERD 구조
<img width="1002" alt="스크린샷 2024-03-26 오후 11 10 16" src="https://github.com/f-lab-edu/modoospace/assets/48192141/8acd7fe9-b624-4081-8a2e-45a54f28831d">

### 1차 서버 아키텍처
![image](https://github.com/f-lab-edu/modoospace/assets/48192141/b8b63d8c-a09b-492f-a825-cd5b981d34e4)

### 주요 기술 Issue
- [CI/CD를 구축해보자1 - NCP서버 생성 및 Docker로 어플리케이션 배포하기](https://velog.io/@gjwjdghk123/CI-CD1)
- [CI/CD를 구축해보자2 - JaCoCo와 GitHub Actions으로 CI/CD구축해보기](https://velog.io/@gjwjdghk123/CI-CD2)
- [ObjectOptimisticLockingFailureException과 고아객체(Orphan) 그리고 한방 쿼리](https://velog.io/@gjwjdghk123/ObjectOptimisticLockingFailureException)
- [nGrinder를 이용한 성능 테스트 및 성능 개선(ElasticSearch, Redis)](https://velog.io/@gjwjdghk123/nGrinder%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%84%B1%EB%8A%A5-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B0%8F-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0ElasticSearch-Redis)
- [ElasticSearch TimeOutException 해결과정](https://velog.io/@gjwjdghk123/ElasticSearch-TimeOutException-%ED%95%B4%EA%B2%B0%EA%B3%BC%EC%A0%95)