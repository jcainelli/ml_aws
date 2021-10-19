# Welcome to IaC 
This project is Infrastructure as code (IaC) to working with the AWS CDK in Java. 

It's a [Maven](https://maven.apache.org/) based project to working with [AWS CDK](https://aws.amazon.com/pt/cdk/) so you can create or destroy stacks or resources necessary for your applications.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

 * `cdk deploy Vpc Cluster MlService`      deploy this stack to your default AWS account/region
 * `cdk destroy Vpc Cluster MlService`        drop stack
 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
 
## Example of use
#### Create RDS Stack with parameters:

`$ cdk deploy --parameters Rds:databasePassword=maria123456 Rds`
