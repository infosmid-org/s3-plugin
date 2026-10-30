package org.infosmid.plugins.gradle.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FilesTest {
    @Test
    void testFiles() throws IOException {
        File dir = new File("src/test/resources/f1");
        Set<File> files;
        try (Stream<java.nio.file.Path> walk = Files.walk(dir.toPath())) {
            files = walk.filter(file -> !Files.isDirectory(file)).map(java.nio.file.Path::toFile)
                .collect(Collectors.toSet());
        }
        assertThat(files).hasSize(3);
        File f11 = new File("src/test/resources/f1/f11.txt");
        assertThat(files).contains(f11);
        assertThat(makeFile(dir, f11, "prod")).isEqualTo(new File("prod", "f11.txt"));
        assertThat(makeFile(dir, f11, null)).isEqualTo(new File("f11.txt"));
    }

    private File makeFile(File file, File sourceFile, String keyPrefix) {
        if (sourceFile.getPath().startsWith(file.getPath())) {
            String portion = sourceFile.getPath().substring(file.getPath().length());
            if (portion.startsWith(File.separator)) {
                portion = portion.substring(1);
            }
            return keyPrefix != null && !keyPrefix.isEmpty() ? new File(keyPrefix, portion) : new File(portion);
        }
        return sourceFile;
    }
}
