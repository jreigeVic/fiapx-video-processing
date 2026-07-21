# Single RDS instance hosting the four logical databases (auth_db,
# video_db, processing_db, notification_db) - AWS Academy budget
# constraint, recorded in ADR-014. Each service still owns its database
# exclusively, so database-per-service (ADR-004) holds at the logical
# level. The databases themselves are created by the bootstrap Job in the
# Helm phase (Flyway migrates each schema on service startup).

# Latest PostgreSQL 16.x, matching the postgres:16 image used by
# docker-compose locally.
data "aws_rds_engine_version" "postgres" {
  engine  = "postgres"
  version = "16"
  latest  = true
}

resource "random_password" "db" {
  length  = 24
  special = false # keep it safe inside JDBC URLs and shell commands
}

resource "aws_security_group" "rds" {
  name        = "fiapx-rds"
  description = "PostgreSQL access from inside the default VPC (EKS nodes)"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "PostgreSQL from the VPC"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.default.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "postgres" {
  identifier     = "fiapx-postgres"
  engine         = "postgres"
  engine_version = data.aws_rds_engine_version.postgres.version
  instance_class = var.db_instance_class

  allocated_storage = 20
  storage_type      = "gp3"

  username = var.db_username
  password = random_password.db.result

  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period = 0 # lab environment
  skip_final_snapshot     = true
  deletion_protection     = false
  apply_immediately       = true
}
