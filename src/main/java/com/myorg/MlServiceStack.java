package com.myorg;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa o ML project
 * */
public class MlServiceStack extends Stack {

    private static final String DOCKER_IMAGE = "jeancainelli/ml:1.0.0";
    private static final String CONTAINER_NAME = "jeancainelli/ml";
    private static final int INSTANCES_NUMBER = 2;
    private static final String CLOUD_WHATCH_LOGS = "Service01LogGroup";
    private static final String LOGS_GROUP_NAME = "MlService";
    private static final String PREFIX_LOGS_STREAM = "MlService01";
    private static final String HEALTH_PATH = "/actuator/health";
    private static final String HEALTH_PORT = "8080";
    private static final String HEALTHY_HTTP_CODE_SUCESS = "200";
    private static final boolean LOAD_BALANCE_IS_PUBLIC = true;
    private static final int SCALING_PERCENTAGE_USE = 50;
    private static final int SCALING_PERCENTAGE_SECONDS = 60;
    private static final int SCALING_SECONDS_DESTROY_INSTANCE = 60;
    private static final int SCALLING_MIN_PODS = 2;
    private static final int SCALLING_MAX_PODS = 4;
    private static final String SERVICE_NAME_ML = "service-ml";
    private static final String LOAD_BALLANCE_NAME = "ALB01";
    private static final String SCALE_CPU_UTILIZATION_NAME = "MlAutoScaling";
    private static final int CONTAINER_PORT = 8080;
    public static final String SCHEME_DB_NAME = "aws_project01";

    public MlServiceStack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public MlServiceStack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> rdsVariables = loadRdsVariables();

        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, LOAD_BALLANCE_NAME)
                .serviceName(SERVICE_NAME_ML)
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(INSTANCES_NUMBER)
                .listenerPort(8080)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName(CONTAINER_NAME)
                                .image(ContainerImage.fromRegistry(DOCKER_IMAGE))
                                .containerPort(CONTAINER_PORT)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, CLOUD_WHATCH_LOGS)
                                                .logGroupName(LOGS_GROUP_NAME)
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix(PREFIX_LOGS_STREAM)
                                        .build()))
                                .environment(rdsVariables)
                                .build())
                .publicLoadBalancer(LOAD_BALANCE_IS_PUBLIC)
                .build();

        configureHealthCheck(service01);

        ScalableTaskCount scalableTaskCount = configureMixAndMaxPods(service01);

        configurePercentageUsePodsScale(scalableTaskCount);

    }

    private Map<String, String> loadRdsVariables() {
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue(RdsStack.RDS_ENDPOINT)
                + ":" + RdsStack.RDS_PORT +"/" + SCHEME_DB_NAME + "?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", RdsStack.RDS_USER);
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue(RdsStack.RDS_PASSWORD));
        return envVariables;
    }

    private void configurePercentageUsePodsScale(ScalableTaskCount scalableTaskCount) {
        scalableTaskCount.scaleOnCpuUtilization(SCALE_CPU_UTILIZATION_NAME, CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(SCALING_PERCENTAGE_USE)
                .scaleInCooldown(Duration.seconds(SCALING_PERCENTAGE_SECONDS))
                .scaleOutCooldown(Duration.seconds(SCALING_SECONDS_DESTROY_INSTANCE))
                .build());
    }

    @NotNull
    private ScalableTaskCount configureMixAndMaxPods(ApplicationLoadBalancedFargateService service01) {
        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(SCALLING_MIN_PODS)
                .maxCapacity(SCALLING_MAX_PODS)
                .build());
        return scalableTaskCount;
    }

    private void configureHealthCheck(ApplicationLoadBalancedFargateService service01) {
        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path(HEALTH_PATH)
                .port(HEALTH_PORT)
                .healthyHttpCodes(HEALTHY_HTTP_CODE_SUCESS)
                .build());
    }
}
