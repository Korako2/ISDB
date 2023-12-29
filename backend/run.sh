# to envaronments: local and remote
# remote if user starts with s
# on remote port  5432, on local 35432
# on remote jar file is in ./backend-0.0.1-SNAPSHOT.jar on local in ./build/libs/backend-0.0.1-SNAPSHOT.jar
# on remote SERVER_PORT=9595 on local SERVER_PORT=8080
#
# Define default values for local dev
export SERVER_PORT=8080
export POSTGRES_PORT=35432
export PG_USER=s336764
export JAR_FILE="./build/libs/backend-0.0.1-SNAPSHOT.jar"

# Check if user starts with 's' for remote environment
if [[ $USER == s* ]]; then
    export SERVER_PORT=9595
    export POSTGRES_PORT=5432
    export JAR_FILE="./backend-0.0.1-SNAPSHOT.jar"
    export PG_USER=$USER
fi

# Set other environment-specific variables
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:$POSTGRES_PORT/studs"
export SPRING_DATASOURCE_USERNAME="$PG_USER"
export SPRING_DATASOURCE_PASSWORD="$(cat ~/.pgpass | cut -d: -f5)"

# Run the Java application
java -jar $JAR_FILE
