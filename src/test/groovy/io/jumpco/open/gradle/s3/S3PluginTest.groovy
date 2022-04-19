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
package io.jumpco.open.gradle.s3


import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class S3PluginTest {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File packetReader

    @Before
    public void setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << "plugins { id 'io.jumpco.open.gradle.s3' }"

        copy(new File('src/test/resources'), buildFile.parentFile)
    }

    @Test
    public void testUpload() {
        buildFile << """
            io.jumpco.open.gradle.s3.S3BaseConfig.setTesting(true)
            s3 {
                bucket = 'some-bucket'
                awsAccessKeyId = '123456789'
                awsSecretAccessKey = 'secret'
            }
            s3Downloads {
                docs {
                    destDir = 'sample'
                    keyPrefix = '/'
                } 
            }
            s3Uploads {
                site {
                    bucket = 'some-other-bucket'
                    sourceDir = 'site'
                    keyPrefix = '/'
                }
            }
            task myUpload(type: io.jumpco.open.gradle.s3.S3Upload) {
                sourceDir = 'my-dir'
                keyPrefix = 'some-folder'
            }
            task myDownload(type: io.jumpco.open.gradle.s3.S3Download) {
                key = 'somefilename.txt'
                file = 'localFileName'
            }
            task deploy(dependsOn: [myUpload, myDownload, siteUploadTask, docsDownloadTask]) {
            }            
        """
        S3BaseConfig.setTesting(true)
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('--stacktrace', '-i', 'deploy')

                .withDebug(true)
                .build()
        println("Temp folder=$buildFile.parent")
        copy(buildFile.parentFile, new File('output'))
        assert result.tasks(TaskOutcome.FAILED).isEmpty()
    }

    def copy(File source, File target) {
        if (source.isDirectory()) {
            source.eachFile {
                copy(it, new File(target, it.name))
            }
        } else {
            if (!target.parentFile.exists()) {
                target.parentFile.mkdirs()
            }
            println("Copy $source.path -> $target.path")
            target.bytes = source.bytes
        }
    }
}
