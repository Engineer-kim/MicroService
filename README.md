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
