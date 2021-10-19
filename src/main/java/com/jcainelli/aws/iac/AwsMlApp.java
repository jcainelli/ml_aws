package com.jcainelli.aws.iac;

import software.amazon.awscdk.core.App;

public class AwsMlApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpcStack = new  VpcStack(app, "Vpc");

        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency( vpcStack );

        RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        MlServiceStack mlServiceStack = new MlServiceStack(app, "MlService", clusterStack.getCluster());
        mlServiceStack.addDependency(clusterStack);
        mlServiceStack.addDependency(rdsStack);

        app.synth();
    }
}
