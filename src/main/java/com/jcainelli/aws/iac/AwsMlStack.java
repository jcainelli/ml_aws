package com.jcainelli.aws.iac;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

public class AwsMlStack extends Stack {
    public AwsMlStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsMlStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here
    }
}
