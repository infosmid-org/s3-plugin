/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infosmid.gradle.s3;

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
