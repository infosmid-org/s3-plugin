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
