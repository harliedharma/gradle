/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import org.gradle.util.RelativePathUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public class JavaConventionalSourceFileClassNameConverter implements SourceFileClassNameConverter {
    private final CompilationSourceDirs sourceDirs;

    public JavaConventionalSourceFileClassNameConverter(CompilationSourceDirs sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    @Override
    public Collection<String> getClassNames(File sourceFile) {
        List<File> dirs = sourceDirs.getSourceRoots();
        for (File sourceDir : dirs) {
            if (sourceFile.getAbsolutePath().startsWith(sourceDir.getAbsolutePath())) { //perf tweak only
                String relativePath = RelativePathUtil.relativePath(sourceDir, sourceFile);
                if (!relativePath.startsWith("..")) {
                    return Collections.singletonList(relativePath.replaceAll("/", ".").replaceAll("\\.java$", ""));
                }
            }
        }
        throw new IllegalArgumentException(format("Unable to find source class: '%s' because it does not belong to any of the source dirs: '%s'",
            sourceFile, dirs));
    }

    @Override
    public Optional<File> getFile(String fqcn) {
        throw new UnsupportedOperationException();
    }
}
