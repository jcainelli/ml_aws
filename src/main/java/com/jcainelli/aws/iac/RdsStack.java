package com.jcainelli.aws.iac;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;

import java.util.Collections;


public class RdsStack extends Stack {

    public static final String RDS_INSTANCE_NAME = "aws-ml-db";
    public static final boolean RDS_MULTI_ZONE = false;
    public static final int LENGTH_DISC = 10;
    public static final String RDS_ENDPOINT = "rds-endpoint";
    public static final String RDS_PASSWORD = "rds-password";
    public static final String RDS_USER = "admin";
    public static final int RDS_PORT = 3306;

    public RdsStack(final Construct scope, final String id, Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public RdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        CfnParameter databasePassword = CfnParameter.Builder.create(this, "databasePassword")
                .type("String")
                .description("The RDS Instance password")
                .build();

        ISecurityGroup iSecurityGroup = addSecurityGroupInVpc(id, vpc);

        addRulePortAccessInSecurityGroup(iSecurityGroup);

        DatabaseInstance databaseInstance = DatabaseInstance.Builder
                .create(this, "Rds01")
                .instanceIdentifier(RDS_INSTANCE_NAME)
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_5_7)
                        .build()))
                .vpc(vpc)
                .credentials(Credentials.fromUsername(RDS_USER,
                        CredentialsFromUsernameOptions.builder()
                                .password(SecretValue.plainText(databasePassword.getValueAsString()))
                                .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .multiAz(RDS_MULTI_ZONE)
                .allocatedStorage(LENGTH_DISC)
                .securityGroups(Collections.singletonList(iSecurityGroup))
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build())
                .build();

        exportParametersRDS(databasePassword, databaseInstance);
    }

    private void exportParametersRDS(CfnParameter databasePassword, DatabaseInstance databaseInstance) {
        CfnOutput.Builder.create(this, RDS_ENDPOINT)
                .exportName(RDS_ENDPOINT)
                .value(databaseInstance.getDbInstanceEndpointAddress())
                .build();

        CfnOutput.Builder.create(this, RDS_PASSWORD)
                .exportName(RDS_PASSWORD)
                .value(databasePassword.getValueAsString())
                .build();
    }

    @NotNull
    private ISecurityGroup addSecurityGroupInVpc(String id, Vpc vpc) {
        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        return iSecurityGroup;
    }

    private void addRulePortAccessInSecurityGroup(ISecurityGroup iSecurityGroup) {
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(RDS_PORT));
    }
}
