Чат, построенный на akka cluster

### Сборка и запуск из исходников
Создать Docker-image и начать сборку проекта
```bash
./docker_from_sources.sh
```
Запуск кластера с произвольным числом нод
```bash
docker-compose up -d --scale node=<number>
```
