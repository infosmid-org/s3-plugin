package org.infosmid.plugins.gradle.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

class S3PluginFunctionalTest {
    @Test
    void uploadAndDownloadTasksRunInTestMode() throws IOException {
        Path projectDir = Path.of("build/functionalTest");
        if (Files.exists(projectDir)) {
            try (Stream<Path> walk = Files.walk(projectDir)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
        Files.createDirectories(projectDir);
        copy(Path.of("src/test/resources"), projectDir);
        Files.writeString(projectDir.resolve("settings.gradle"), "");
        Files.writeString(projectDir.resolve("build.gradle"), """
            plugins { id 'org.infosmid.plugins.s3' }
            org.infosmid.gradle.s3.S3BaseConfig.setTesting(true)
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
                    sourceDir = 'f1'
                    keyPrefix = '/'
                }
            }
            task myUpload(type: org.infosmid.gradle.s3.S3Upload) {
                sourceDir = 'f1'
                keyPrefix = 'some-folder'
            }
            task myDownload(type: org.infosmid.gradle.s3.S3Download) {
                key = 'somefilename.txt'
                file = 'localFileName'
            }
            task deploy(dependsOn: [myUpload, myDownload, siteUploadTask, docsDownloadTask])
            """);

        // withDebug runs the build in this JVM, so setTesting(true) is seen by the tasks.
        BuildResult result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("--stacktrace", "-i", "deploy")
            .withDebug(true)
            .forwardOutput()
            .build();

        assertThat(result.tasks(TaskOutcome.FAILED)).isEmpty();
        assertThat(result.task(":deploy").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    private static void copy(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : (Iterable<Path>) walk::iterator) {
                Path destination = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination);
                }
            }
        }
    }
}
