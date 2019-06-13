/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.jvm;

import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.internal.file.copy.DefaultZipCompressor;
import org.gradle.api.internal.file.copy.ZipCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.gradle.api.internal.file.archive.ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES;

/**
 * Transforms a classes directory into a jar. This can lead to better performance on some systems
 * when there are a lot of class files on the classpath.
 */
public abstract class ClassesToJarTransform implements TransformAction<TransformParameters.None> {
    private ZipCompressor compressor = new DefaultZipCompressor(false, ZipEntry.DEFLATED);

    @InputArtifact
    public abstract File getClassesFolder();

    public void transform(TransformOutputs outputs) {
        File classesFolder = getClassesFolder();
        File jarFile = outputs.file(classesFolder.getName() + ".jar");
        zip(classesFolder, jarFile);
    }

    private void zip(File classesDir, File jarFile) {
        try(ZipOutputStream zipOutStr = compressor.createArchiveOutputStream(jarFile)) {
            zipOutStr.setEncoding("UTF-8");
            File[] files = classesDir.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    zipEntries(childFile, childFile.getName(), zipOutStr);
                }
            }
        } catch (Exception e) {
            throw new GradleException(String.format("Could not create classes JAR '%s'.", jarFile), e);
        }

    }

    private void zipEntries(File fileToZip, String fileName, ZipOutputStream zipOutStr) throws IOException {
        if (fileToZip.isDirectory()) {
            ZipEntry entry = new ZipEntry(fileName + "/");
            entry.setTime(CONSTANT_TIME_FOR_ZIP_ENTRIES);
            zipOutStr.putNextEntry(entry);
            zipOutStr.closeEntry();

            File[] files = fileToZip.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    zipEntries(childFile, fileName + "/" + childFile.getName(), zipOutStr);
                }
            }
        } else {
            ZipEntry entry = new ZipEntry(fileName);
            entry.setTime(CONSTANT_TIME_FOR_ZIP_ENTRIES);
            zipOutStr.putNextEntry(entry);
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                IOUtils.copyLarge(fis, zipOutStr);
            }
            zipOutStr.closeEntry();
        }
    }
}

