psql -h 127.0.0.1 -p 15432 -U postgres -f drop.sql && \
psql -h 127.0.0.1 -p 15432 -U postgres -f create.sql && \
psql -h 127.0.0.1 -p 15432 -U postgres -f functions.sql && \
psql -h 127.0.0.1 -p 15432 -U postgres -f triggers.sql && \
psql -h 127.0.0.1 -p 15432 -U postgres -f insert.sql
