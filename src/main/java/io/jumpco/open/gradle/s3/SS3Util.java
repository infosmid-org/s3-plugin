/*
    Copyright 2019-2021 Open JumpCO

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
    documentation files (the "Software"), to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
    and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
    THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
    CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.
 */
package io.jumpco.open.gradle.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import java.util.ArrayList;
import java.util.List;

public class SS3Util {
    private final Project project;

    private final String bucket;

    protected String awsAccessKeyId;

    protected String awsSecretAccessKey;

    public SS3Util(Project project, String bucket) {
        this.project = project;
        this.bucket = bucket;
    }

    public SS3Util(Project project, String bucket, String awsAccessKeyId, String awsSecretAccessKey) {
        this.project = project;
        this.bucket = bucket;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    S3Extension getExt() {
        return project.getExtensions().findByType(S3Extension.class);
    }

    public String getBucket() {
        if (bucket == null) {
            return getExt().getBucket();
        }
        return bucket;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public AmazonS3 getS3Client() {
        List<AWSCredentialsProvider> credentialsProviders = new ArrayList<>();
        if (awsAccessKeyId != null) {
            if (awsSecretAccessKey == null) {
                throw new GradleException("Expected awsSecretAccessKey when awsAccessKeyId was provided");
            }
            project.getLogger().info("s3:awsAccessKeyId:" + awsAccessKeyId);
            credentialsProviders.add(new SimpleWSCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId,
                awsSecretAccessKey)));
        }
        credentialsProviders.add(new EnvironmentVariableCredentialsProvider());
        credentialsProviders.add(new SystemPropertiesCredentialsProvider());
        String profile = getExt().getProfile();
        if (profile != null) {
            project.getLogger().info("s3:profile: " + profile);
            credentialsProviders.add(new ProfileCredentialsProvider(profile));
        }
        credentialsProviders.add(new EC2ContainerCredentialsProviderWrapper());
        AWSCredentialsProviderChain credentialsProviderChain = new AWSCredentialsProviderChain(
            credentialsProviders.toArray(new AWSCredentialsProvider[0])
        );

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(credentialsProviderChain);

        String region = getExt().getRegion();
        if (region != null) {
            builder.withRegion(region);
        }
        return builder.build();
    }

    private static class SimpleWSCredentialsProvider implements AWSCredentialsProvider {
        private final AWSCredentials credentials;

        public SimpleWSCredentialsProvider(AWSCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public AWSCredentials getCredentials() {
            return credentials;
        }

        @Override
        public void refresh() {
        }
    }
}
