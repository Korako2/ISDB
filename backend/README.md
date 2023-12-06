# ISDB

# How to run Web server
1. start postgresql on port `15432` either by yourself or using docker-compose: `docker-compose up`
2. Start Spring application
3. Go to [http://localhost:8080/index](http://localhost:8080/index)


# How to generate data
```sh
gradle bootJar
java -Xmx13192m -jar build/libs/isbd-curs-0.0.1-SNAPSHOT.jar 1000 10000 sql/csv_data
```
`1000` -- drivers count
`10000` -- customers count

Find output csv files in `sql/csv_data` folder

# How to fill db with data
1. cd into sql folder
`cd sql`

## Prepare
### Local
1. run postgres (for example using `docker-compose up`). Replace `PGPORT` with postgres port
2. export env vars
```sh
export PGUSER=postgres && export PGPASSWORD=postgres && export PGPORT=15432 && export PGHOST=127.0.0.1 && export PGDATABASE=postgres
```

### or Helios
1. create `~/.pgpass` with following contents: `*:*:*:replace-with-user:replace-with-your-password`
2. forward port:
```sh
ssh -L 35432:127.0.0.1:5432 -p 2222 -N s******@se.ifmo.ru
```
3. export env variables
```sh
export PGUSER=s**** && export PGPORT=35432 && export PGHOST=127.0.0.1 && export PGDATABASE=studs
```

## Fill tables with data
*it will drop all data* and create all tables from scratch
```sh
./fill-tables.sh
```

## Finally
create functions, triggers and indices
```sh
psql -f functions.sql
psql -f triggers.sql
psql -f indices.sql
```
