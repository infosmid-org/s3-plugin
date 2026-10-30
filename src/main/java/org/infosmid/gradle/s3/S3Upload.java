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

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@DisableCachingByDefault(because = "Uploads to a remote service")
public class S3Upload extends DefaultTask {

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
    private String sourceDir;

    @Optional
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    private FileCollection files;

    @Input
    private boolean overwrite = false;

    @Input
    private boolean compareContent = false;

    @Input
    private boolean skipError;
    @Optional
    @Input
    private Integer batchSize = 100;

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

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public FileCollection getFiles() {
        return files;
    }

    public void setFiles(final FileCollection files) {
        this.files = files;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isCompareContent() {
        return compareContent;
    }

    public void setCompareContent(boolean compareContent) {
        this.compareContent = compareContent;
    }

    public boolean isSkipError() {
        return skipError;
    }

    public void setSkipError(final boolean skipError) {
        this.skipError = skipError;
    }

    Integer getBatchSize() {
        return batchSize;
    }

    void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    private Set<File> findExisting(final SS3Util util) {
        final Set<File> existing = new HashSet<>();
        final ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(getBucket())
                .withMaxKeys(batchSize)
                .withPrefix(keyPrefix);
        ListObjectsV2Result result;
        do {
            result = util.getS3Client().listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                if (objectSummary.getKey().startsWith(keyPrefix)) {
                    existing.add(new File(objectSummary.getKey()));
                    getLogger().debug("{}:listing:{}:{}", getName(), getBucket(), objectSummary.getKey());
                }
            }
            String token = result.getNextContinuationToken();
            req.setContinuationToken(token);
        }
        while (result.isTruncated());
        return existing;
    }

    private void uploadFile(final SS3Util util, final File fileToUpload) throws IOException {
        if (!fileToUpload.exists()) {
            throw new GradleException("upload file:" + getFile() + " not found");
        }
        if (util.getS3Client().doesObjectExist(getBucket(), getKey())) {
            if (isOverwrite()) {
                getLogger().lifecycle("{}:{} → s3://{}/{} with overwrite", getName(), getFile(), getBucket(), getKey());
                util.getS3Client().putObject(getBucket(), getKey(), fileToUpload);
            }
            else {
                if (isCompareContent()) {
                    S3ObjectInputStream s3stream = util.getS3Client()
                            .getObject(getBucket(), getKey())
                            .getObjectContent();
                    File target = new File(getKey());
                    getLogger().info("{}:upload:comparing:{} -> {}", getName(), target, fileToUpload);
                    if (IOUtils.contentEquals(s3stream.getDelegateStream(), new FileInputStream(fileToUpload))) {
                        getLogger().lifecycle("{}:upload:equals:skipping:{}", getName(), getKey());
                    }
                    else {
                        getLogger().lifecycle("{}:upload:{} → s3://{}/{}", getName(), getFile(), getBucket(), getKey());
                        util.getS3Client().putObject(getBucket(), getKey(), fileToUpload);
                    }
                }
                getLogger().warn("{}:upload:s3://{}/{} exists, not overwriting", getName(), getBucket(), getKey());
            }
        }
        else {
            getLogger().lifecycle("{}:{} → s3://{}/{}", getName(), getFile(), getBucket(), getKey());
            util.getS3Client().putObject(getBucket(), getKey(), fileToUpload);
        }
    }

    private void uploadFiles(final SS3Util util, final File file, final List<File> sourceFiles) throws IOException {
        final Set<File> existing = findExisting(util);
        for (File sourceFile : sourceFiles) {
            File target = makeFile(file, sourceFile, keyPrefix);
            if (existing.contains(target)) {
                if (isCompareContent()) {
                    S3ObjectInputStream s3stream = util.getS3Client()
                            .getObject(getBucket(), target.getPath())
                            .getObjectContent();
                    getLogger().info("{}:upload:comparing:{} -> {}", getName(), target, sourceFile);
                    if (IOUtils.contentEquals(s3stream.getDelegateStream(), new FileInputStream(sourceFile))) {
                        getLogger().info("{}:upload:equals:skipping:{}", getName(), target.getPath());
                    }
                    else {
                        getLogger().lifecycle(getName() + ":upload:different:adding:" + target.getPath());
                        util.getS3Client()
                                .putObject(getBucket(), target.getPath(), sourceFile);
                        getLogger().info("{}:upload:completed:{}", getName(), sourceFile.getPath());
                    }
                }
                else if (overwrite) {
                    getLogger().lifecycle("{}:upload:exists:overwriting:{}", getName(), target.getPath());
                    util.getS3Client()
                            .putObject(getBucket(), target.getPath(), sourceFile);
                    getLogger().info("{}:upload:completed:{}", getName(), sourceFile.getPath());
                }
            }
            else {
                getLogger().lifecycle(getName() + ":upload:new:" + target.getPath());
                util.getS3Client().putObject(getBucket(), target.getPath(), sourceFile);
                getLogger().info("{}:upload:completed:{}", getName(), sourceFile.getPath());
            }
        }

    }

    @TaskAction
    public void task() throws InterruptedException, IOException {

        if (getBucket() == null) {
            throw new GradleException("Invalid parameters: [bucket] was not provided and/or a default was not set");
        }
        SS3Util util = new SS3Util(getProject(), getBucket(), getAwsAccessKeyId(), getAwsSecretAccessKey());
        if (getKeyPrefix() != null && getSourceDir() != null) {
            if (getKey() != null || getFile() != null || getFiles() != null) {
                throw new GradleException(
                        "Invalid parameters: [key, file, fileCollection] are not valid for S3 Upload directory");
            }
            getLogger().lifecycle("{}:directory:{} → s3://{}/{}",
                    getName(),
                    getProject().file(getSourceDir()),
                    getBucket(),
                    getKeyPrefix());
            if (!S3BaseConfig.isTesting()) {
                File file = getProject().file(getSourceDir());
                if (!file.exists() && isSkipError()) {
                    throw new GradleException("upload sourceDir:" + getSourceDir() + " not found");
                }
                if (isCompareContent() || !isOverwrite()) {
                    final List<File> sourceFiles =
                            Files.walk(file.toPath(), Integer.MAX_VALUE)
                                    .filter(f -> !Files.isDirectory(f))
                                    .map(Path::toFile)
                                    .collect(Collectors.toList());
                    uploadFiles(util, file, sourceFiles);
                }
                else {
                    Transfer transfer = TransferManagerBuilder.standard()
                            .withS3Client(util.getS3Client())
                            .build()
                            .uploadDirectory(getBucket(), getKeyPrefix(), file, true);
                    ProgressListener listener = new S3Listener(transfer, getLogger());
                    transfer.addProgressListener(listener);
                    transfer.waitForCompletion();
                }
            }
            else {
                getLogger().lifecycle("testing:upload:{}", getBucket());
            }
        }
        else if (getKey() != null && getFile() != null) {
            if (!S3BaseConfig.isTesting()) {
                this.uploadFile(util, new File(getFile()));
            }
            else {
                getLogger().lifecycle("{}:upload:{} → s3://{}/{}", getName(), getFile(), getBucket(), getKey());
                getLogger().lifecycle("testing:upload:{}", getName());
            }
        }
        else if (getKeyPrefix() != null && getFiles() != null) {
            if (getKey() != null || getFile() != null || getSourceDir() != null) {
                throw new GradleException(
                        "Invalid parameters: [key, file, sourceDir] are not valid for S3 Upload fileCollection");
            }
            final ArrayList<File> files = new ArrayList<>(getFiles().getFiles());
            final java.util.Optional<File> commonParent = findCommonParent(files);
            if (!commonParent.isPresent()) {
                throw new IllegalArgumentException("Couldn't find common prefix for the provided files");
            }
            final File dir = commonParent.get();
            getLogger().lifecycle("{}: files(size={} under {}) → s3://{}/{}",
                    getName(),
                    files.size(),
                    dir,
                    getBucket(),
                    getKeyPrefix());
            if (!S3BaseConfig.isTesting()) {
                if (isCompareContent() || !isOverwrite()) {
                    uploadFiles(util, dir, files);
                }
                else {
                    Transfer transfer = TransferManagerBuilder.standard()
                            .withS3Client(util.getS3Client())
                            .build()
                            .uploadFileList(getBucket(), getKeyPrefix(), dir, files);
                    ProgressListener listener = new S3Listener(transfer, getLogger());
                    transfer.addProgressListener(listener);
                    transfer.waitForCompletion();
                }
            }
            else {
                getLogger().lifecycle("testing:upload:{}", getBucket());
            }
        }
        else {
            throw new GradleException(
                    "Invalid parameters: one of [key, file] or [keyPrefix, sourceDir] or [keyPrefix, fileCollection] pairs must be specified for S3Upload");
        }
    }

    private File makeFile(File file, File sourceFile, String keyPrefix) {
        if (sourceFile.getPath().startsWith(file.getPath())) {
            String portion = sourceFile.getPath().substring(file.getPath().length());
            if (portion.startsWith(File.separator)) {
                portion = portion.substring(1);
            }
            return keyPrefix != null && keyPrefix.length() > 0 ? new File(keyPrefix, portion) : new File(portion);
        }
        return sourceFile;
    }

    private java.util.Optional<File> findCommonParent(final List<File> files) {
        if (files.isEmpty()) {
            getLogger().info("{}: files is empty", getName());
            return java.util.Optional.empty();
        }
        else {
            java.util.Optional<File> result = java.util.Optional.of(files.get(0)
                    .getParentFile());
            for (final File file : files) {
                result = commonParent(result.get(), file);
                if (result.isPresent()) {
                    return result;
                }
            }
            return result;
        }
    }

    private java.util.Optional<File> commonParent(final File potentialParent, final File file) {
        if (file.getAbsolutePath().startsWith(potentialParent.getAbsolutePath()))
            return java.util.Optional.of(potentialParent);
        else {
            final File parent = potentialParent.getParentFile();
            if (parent == null)
                return java.util.Optional.empty();
            else
                return commonParent(parent, file);
        }
    }
}
