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
