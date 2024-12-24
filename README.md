로컬에서 카프카 실행 방법

1.주키퍼를 먼저 실행시킨다

docker-compose -f common.yml -f zookeeper.yml up

2. 주키퍼 헬스체크

- 윈도우 경우
  - ubuntu(WSL) 환경에서 echo ruok | nc localhost 2181
- MAC Os 경우
   - 별도의 환경 필요없음 그냥  echo ruok | nc localhost 2181 입력
  헬스 체크 참고 : https://zookeeper.apache.org/doc/r3.1.2/zookeeperAdmin.html#sc_zkCommands
3. 카프카 클러스터 실행
  docker-compose -f common.yml -f kafka_cluster.yml up

4.  Kafka 클러스터에서 작성한 작업(기존의 특정 토픽을 삭제하고, 새로운 토픽을 생성하는 자동화된 초기화 작업이 수행) 실행
  docker-compose -f common.yml -f init_kafka.yml up

5. localhost:9000 으로 접속후 확인
   초기에는 아무것도 없으므로 클러스터 임의 생성후 화면
   ![1](https://github.com/user-attachments/assets/8cb86ec9-880c-419b-840a-eea95fd702a3)
   ![2](https://github.com/user-attachments/assets/d2a23971-979e-4729-8eba-07dd5077d246)

이렇게 로컬 카프카 실행 가능!

----

## 카프카의 동작 방식

Producer가 메시지를 특정 Topic에 게시
Broker는 메시지를 받아 파티션에 순서대로 저장
Consumer Group에 속한 Consumer들이 자신이 할당받은 파티션에서 메시지를 소비합
Consumer는 소비한 메시지를 처리하고, 오프셋을 업데이트하여 다음 메시지를 소비할 위치를 기억

## 카프카의 주요 구성 요소

Producer: 데이터를 카프카 클러스터에 생산하여 보내는 역할을 합니다. 다양한 시스템에서 발생하는 이벤트나 데이터를 카프카 토픽에 게시
Broker: 메시지를 저장하고 전달하는 서버입니다. 여러 개의 브로커가 클러스터를 구성하여 고가용성과 확장성을 제공
Topic: 메시지를 논리적으로 분류하는 카테고리입니다. 각 토픽은 파티션으로 나뉘어져 있으며, 각 파티션은 순서대로 메시지를 저장
Consumer: 토픽에서 메시지를 소비하는 역할을 합니다. 소비자는 자신이 관심 있는 토픽을 구독하고 메시지를 처리
Consumer Group: 여러 개의 소비자를 그룹으로 묶어서 토픽의 파티션을 분할하여 소비하게 합니다. 각 파티션은 하나의 소비자 그룹 내에서 단 한 명의 소비자만 소비

추가 설명 링크(본인 기술 블록그 작성글): https://velog.io/@ddang0103/Kafka 
