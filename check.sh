# need to set PGHOST, PGPORT, PGPASSWORD and PGUSER
psql -f drop.sql && \
psql -f create.sql && \
psql -f functions.sql && \
psql -f triggers.sql && \
psql -f insert.sql
