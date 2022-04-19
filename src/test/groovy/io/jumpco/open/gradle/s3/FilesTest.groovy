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


import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Files
import java.util.stream.Collectors

class FilesTest {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()

    @Test
    public void testFiles() {
        File dir = new File("src/test/resources/f1");
        Set<File> files = Files.walk(dir.toPath(), Integer.MAX_VALUE).filter { file -> !Files.isDirectory(file) }
                .map { it.toFile() }
                .collect(Collectors.toSet());
        System.out.println("Files:" + files);
        assert files.size() == 3
        File f11 = new File("src/test/resources/f1/f11.txt")
        assert files.contains(f11)
        File localTest = makeFile(dir, f11, "prod")
        File f11Prod = new File("prod", "f11.txt");
        assert f11Prod == localTest
        File portionTest = new File("f11.txt");
        File f11Portion = makeFile(dir, f11, null)
        assert portionTest == f11Portion
    }

    private File makeFile(File file, File sourceFile, String keyPrefix) {
        if (sourceFile.getPath().startsWith(file.getPath())) {
            def portion = sourceFile.getPath().substring(file.getPath().length())
            if (portion.startsWith(File.separator)) {
                portion = portion.substring(1)
            }
            return keyPrefix != null && keyPrefix.length() > 0 ? new File(keyPrefix, portion) : new File(portion);
        }
        return sourceFile;
    }
}
