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

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class S3BaseConfig {
    private static boolean testing = false;

    protected final String name;

    private Property<String> awsAccessKeyId;

    private Property<String> awsSecretAccessKey;

    private Property<String> bucket;

    private Property<String> key;

    private Property<String> file;

    private Property<String> keyPrefix;

    private Property<Boolean> skipError;

    public S3BaseConfig(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.bucket = objectFactory.property(String.class);
        this.file = objectFactory.property(String.class);
        this.key = objectFactory.property(String.class);
        this.keyPrefix = objectFactory.property(String.class);
        this.awsAccessKeyId = objectFactory.property(String.class);
        this.awsSecretAccessKey = objectFactory.property(String.class);
        this.skipError = objectFactory.property(Boolean.class);
    }

    public static boolean isTesting() {
        return testing;
    }

    public static void setTesting(boolean t) {
        testing = t;
    }

    public Property<String> getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket.set(bucket);
    }

    public Property<String> getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public Property<String> getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file.set(file);
    }

    public Property<String> getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix.set(keyPrefix);
    }

    public Property<String> getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId.set(awsAccessKeyId);
    }

    public Property<String> getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey.set(awsSecretAccessKey);
    }

    public String getName() {
        return name;
    }

    public Property<Boolean> getSkipError() {
        return skipError;
    }

    public void setSkipError(Boolean skipError) {
        this.skipError.set(skipError);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        S3BaseConfig that = (S3BaseConfig) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "S3BaseConfig{" +
            "awsAccessKeyId=" + awsAccessKeyId +
            ", awsSecretAccessKey=" + (awsSecretAccessKey.getOrNull() != null ? "******" : "null") +
            ", bucket=" + bucket +
            ", name='" + name + '\'' +
            ", key=" + key +
            ", file=" + file +
            ", skipError='" + skipError + '\'' +
            ", keyPrefix=" + keyPrefix +
            '}';
    }
}
