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

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class S3Plugin implements Plugin<Project> {

    public void apply(Project project) {
        S3Extension extension = project.getExtensions().create("s3", S3Extension.class);
        NamedDomainObjectContainer<S3UploadConfig> uploadContainer =
            project.container(S3UploadConfig.class, new NamedDomainObjectFactory<S3UploadConfig>() {
                public S3UploadConfig create(String name) {
                    return new S3UploadConfig(name, project.getObjects());
                }
            });
        project.getExtensions().add("s3Uploads", uploadContainer);
        NamedDomainObjectContainer<S3DownloadConfig> downloadContainer =
            project.container(S3DownloadConfig.class, new NamedDomainObjectFactory<S3DownloadConfig>() {
                public S3DownloadConfig create(String name) {
                    return new S3DownloadConfig(name, project.getObjects());
                }
            });
        project.getExtensions().add("s3Downloads", downloadContainer);
        uploadContainer.all(new Action<S3UploadConfig>() {
            @Override
            public void execute(S3UploadConfig config) {
                project.getLogger().info(config.getName() + ":upload:config:" + config);
                project.getTasks().register(config.getName() + "UploadTask", S3Upload.class, new Action<S3Upload>() {
                    @Override
                    public void execute(S3Upload task) {
                        task.setBucket(config.getBucket().getOrElse(getExt(project).getBucket()));
                        task.setAwsAccessKeyId(config.getAwsAccessKeyId()
                            .getOrElse(getExt(project).getAwsAccessKeyId()));
                        task.setAwsSecretAccessKey(config.getAwsSecretAccessKey()
                            .getOrElse(getExt(project).getAwsSecretAccessKey()));
                        task.setSourceDir(config.getSourceDir().getOrNull());
                        task.setOverwrite(config.getOverwrite().getOrElse(false));
                        task.setKeyPrefix(config.getKeyPrefix().getOrNull());
                        task.setFile(config.getFile().getOrNull());
                        task.setKey(config.getKey().getOrNull());
                        task.setSkipError(config.getSkipError().getOrElse(false));
                        if (task.getAwsAccessKeyId() != null && task.getAwsSecretAccessKey() == null) {
                            throw new GradleException("Expected awsSecretAccessKey when awsAccessKeyId provided");
                        }
                        if ((task.getKey() != null && task.getFile() == null) ||
                            (task.getFile() != null && task.getKey() == null)) {
                            throw new GradleException("Expected key and file for single file upload");
                        }
                        if ((task.getSourceDir() != null && task.getKeyPrefix() == null) ||
                            (task.getSourceDir() == null && task.getKeyPrefix() != null)) {
                            throw new GradleException("Expected sourceDir and keyPrefix for directory upload");
                        }
                        project.getLogger().info(task.getName() + ":registered:" + config);
                    }
                });
            }
        });
        downloadContainer.all(new Action<S3DownloadConfig>() {
            @Override
            public void execute(S3DownloadConfig config) {
                project.getLogger().info(config.getName() + ":download:config:" + config);
                project.getTasks()
                    .register(config.getName() + "DownloadTask", S3Download.class, new Action<S3Download>() {
                        @Override
                        public void execute(S3Download task) {
                            task.setBucket(config.getBucket().getOrElse(getExt(project).getBucket()));
                            task.setAwsAccessKeyId(config.getAwsAccessKeyId()
                                .getOrElse(getExt(project).getAwsAccessKeyId()));
                            task.setAwsSecretAccessKey(config.getAwsSecretAccessKey()
                                .getOrElse(getExt(project).getAwsSecretAccessKey()));
                            task.setKeyPrefix(config.getKeyPrefix().getOrNull());
                            task.setKey(config.getKey().getOrNull());
                            task.setFile(config.getFile().getOrNull());
                            task.setDestDir(config.getDestDir().getOrNull());
                            task.setSkipError(config.getSkipError().getOrElse(false));
                            if (task.getAwsAccessKeyId() != null && task.getAwsSecretAccessKey() == null) {
                                throw new GradleException("Expected awsSecretAccessKey when awsAccessKeyId provided");
                            }
                            if ((task.getKey() != null && task.getFile() == null) ||
                                (task.getFile() != null && task.getKey() == null)) {
                                throw new GradleException("Expected key and file for single file download");
                            }
                            if ((task.getDestDir() != null && task.getKeyPrefix() == null) ||
                                (task.getDestDir() == null && task.getKeyPrefix() != null)) {
                                throw new GradleException("Expected destDir and keyPrefix for directory upload");
                            }
                            project.getLogger().info("S3Download:registered:" + task.getName() + ":" + config);
                        }
                    });
            }
        });
    }

    private S3Extension getExt(Project project) {
        S3Extension s3Extension = project.getExtensions().findByType(S3Extension.class);
        if (s3Extension == null) {
            throw new GradleException("Expected s3");
        }
        return s3Extension;
    }
}
