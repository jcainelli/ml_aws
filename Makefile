deploy:
	cdk deploy --parameters Rds:databasePassword=maria123456 Vpc Cluster Rds MlService

list:
	cdk list

destroy:
	cdk destroy Vpc Cluster Rds MlService