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

public class S3UploadConfig extends S3BaseConfig {

    private Property<String> sourceDir;

    private Property<Boolean> overwrite;

    private Property<Boolean> compareContent;

    public S3UploadConfig(String name, ObjectFactory objectFactory) {
        super(name, objectFactory);

        this.overwrite = objectFactory.property(Boolean.class);
        this.overwrite.set(false);

        this.compareContent = objectFactory.property(Boolean.class);
        this.compareContent.set(false);

        this.sourceDir = objectFactory.property(String.class);
    }

    public Property<String> getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir.set(sourceDir);
    }

    public Property<Boolean> getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite.set(overwrite);
    }

    public Property<Boolean> getCompareContent() {
        return compareContent;
    }

    public void setCompareContent(Boolean compareContent) {
        this.compareContent.set(compareContent);
    }

    @Override
    public String toString() {
        return "S3UploadExtension{" +
            "sourceDir='" + sourceDir + '\'' +
            ", overwrite=" + overwrite +
            ", compareContent=" + compareContent +
            super.toString() +
            '}';
    }
}
