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

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import java.io.File;

public class S3Download extends DefaultTask {
    @Optional
    @Input
    protected String bucket;

    @Optional
    @Input
    protected String awsAccessKeyId;

    @Optional
    @Input
    protected String awsSecretAccessKey;

    @Optional
    @Input
    private String key;

    @Optional
    @Input
    private String file;

    @Optional
    @Input
    private String keyPrefix;

    @Optional
    @Input
    private String destDir;

    @Input
    private boolean skipError;

    @Internal
    @Override
    public String getGroup() {
        return "s3";
    }

    @Internal
    S3Extension getExt() {
        return getProject().getExtensions().findByType(S3Extension.class);
    }

    public String getBucket() {
        if (bucket == null) {
            return getExt().getBucket();
        }
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAwsAccessKeyId() {
        if (awsAccessKeyId == null) {
            return getExt().getAwsAccessKeyId();
        }
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        if (awsSecretAccessKey == null) {
            return getExt().getAwsSecretAccessKey();
        }
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public boolean isSkipError() {
        return skipError;
    }

    public void setSkipError(final boolean skipError) {
        this.skipError = skipError;
    }

    @TaskAction
    public void task() throws InterruptedException {

        Transfer transfer = null;

        if (getBucket() == null) {
            throw new GradleException("Invalid parameters: [bucket] was not provided and/or a default was not set");
        }
        SS3Util util = new SS3Util(getProject(), getBucket(), getAwsAccessKeyId(), getAwsSecretAccessKey());

        if (keyPrefix != null && destDir != null) {
            if (key != null || file != null) {
                throw new GradleException("Invalid parameters: [key, file] are not valid for S3Download recursive");
            }
            getLogger().lifecycle("{}:directory:s3://{}/{} → {}/", getName(), getBucket(), keyPrefix, destDir);
            if (!S3BaseConfig.isTesting()) {
                transfer = TransferManagerBuilder.standard().withS3Client(util.getS3Client()).build()
                    .downloadDirectory(getBucket(), keyPrefix, getProject().file(destDir));
            }
        } else if (key != null && file != null) {
            if (keyPrefix != null || destDir != null) {
                throw new GradleException(
                    "Invalid parameters: [keyPrefix, destDir] are not valid for S3 Download single file");
            }
            getLogger().lifecycle("{}:file:s3://{}/{} → {}", getName(), getBucket(), key, file);
            if (!S3BaseConfig.isTesting()) {
                File f = new File(file);
                if (f.getParentFile() != null && !f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                try {
                    transfer = TransferManagerBuilder.standard().withS3Client(util.getS3Client()).build()
                        .download(getBucket(), key, f);
                } catch (AmazonS3Exception amazonS3Exception) {
                    if (!skipError) {
                        throw amazonS3Exception;
                    }
                    getLogger().lifecycle("{} Skipping error: {}", getName(), amazonS3Exception.getMessage());
                }
            }
        } else {
            throw new GradleException(
                "Invalid parameters: one of [key, file] or [keyPrefix, destDir] pairs must be specified for S3 Download");
        }
        if (!S3BaseConfig.isTesting()) {
            if (transfer == null && !skipError) {
                throw new GradleException("Expected transfer");
            }
            if (transfer != null) {
                ProgressListener listener = new S3Listener(transfer, getLogger());
                transfer.addProgressListener(listener);
                transfer.waitForCompletion();
            }
        } else {
            getLogger().lifecycle("{}testing:{}", getName(), getBucket());
        }
    }
}
