# need to set PGHOST, PGPORT, PGPASSWORD and PGUSER
cat drop.sql create.sql functions.sql triggers.sql insert.sql call-functions.sql | \
  psql -1 -v ON_ERROR_STOP=1 -q -f -
