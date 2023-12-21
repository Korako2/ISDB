export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/studs"
export SPRING_DATASOURCE_USERNAME="$USER"
export SPRING_DATASOURCE_PASSWORD="$(cat ~/.pgpass | cut -d: -f5)"
export SERVER_PORT=9595

java -jar backend-0.0.1-SNAPSHOT.jar
