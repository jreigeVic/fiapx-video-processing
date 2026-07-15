#!/bin/bash
set -e

# Creates one logical database per service, driven entirely by the
# POSTGRES_MULTIPLE_DATABASES environment variable (comma-separated list).
# Adding a new microservice database only requires updating .env — this
# script never needs to change.

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
	echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
	IFS=',' read -ra DATABASES <<< "$POSTGRES_MULTIPLE_DATABASES"
	for db in "${DATABASES[@]}"; do
		db_trimmed="$(echo -n "$db" | xargs)"
		[ -z "$db_trimmed" ] && continue
		echo "Ensuring database '$db_trimmed' exists"
		psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
			SELECT 'CREATE DATABASE "$db_trimmed"'
			WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db_trimmed')\gexec
		EOSQL
	done
	echo "Multiple databases created"
fi
