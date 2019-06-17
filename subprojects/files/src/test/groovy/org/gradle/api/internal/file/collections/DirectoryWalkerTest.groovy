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

package org.gradle.api.internal.file.collections

import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.file.collections.jdk7.Jdk7DirectoryWalker
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory
import org.gradle.test.fixtures.file.TestFile
import org.gradle.util.UsesNativeServices

import java.util.concurrent.atomic.AtomicInteger

@UsesNativeServices
class DirectoryWalkerTest extends AbstractDirectoryWalkerTest<DirectoryWalker> {
    @Override
    protected List<DirectoryWalker> getWalkers() {
        return [new Jdk7DirectoryWalker(), new ReproducibleDirectoryWalker()]
    }

    private static generateFilesAndSubDirectories(TestFile parentDir, int fileCount, int dirCount, int maxDepth, int currentDepth, AtomicInteger fileIdGenerator) {
        for (int i = 0; i < fileCount; i++) {
            parentDir.createFile("file" + fileIdGenerator.incrementAndGet()) << ("x" * fileIdGenerator.get())
        }
        if (currentDepth < maxDepth) {
            for (int i = 0; i < dirCount; i++) {
                TestFile subDir = parentDir.createDir("dir" + fileIdGenerator.incrementAndGet())
                generateFilesAndSubDirectories(subDir, fileCount, dirCount, maxDepth, currentDepth + 1, fileIdGenerator)
            }
        }
    }

    def "file walker sees a snapshot of file metadata even if files are deleted after walking has started"() {
        given:
        def rootDir = tmpDir.createDir("root")
        long minimumTimestamp = (System.currentTimeMillis() / 1000 * 1000) - 2000
        def file1 = rootDir.createFile("a/b/1.txt")
        file1 << '12345'
        def file2 = rootDir.createFile("a/b/2.txt")
        file2 << '12345'
        def file3 = rootDir.createFile("a/b/3.txt")
        file3 << '12345'
        def walkerInstance = new Jdk7DirectoryWalker()
        def fileTree = new DirectoryFileTree(rootDir, new PatternSet(), { walkerInstance } as Factory, TestFiles.fileSystem(), false)
        def visitedFiles = []
        def visitedDirectories = []
        def fileVisitor = [visitFile: { visitedFiles << it }, visitDir: { visitedDirectories << it }] as FileVisitor

        when:
        fileTree.visit(fileVisitor)
        rootDir.deleteDir()

        then:
        visitedFiles.size() == 3
        visitedFiles.each {
            assert !it.isDirectory() && it.getSize() == 5 && it.getLastModified() >= minimumTimestamp
        }
        visitedDirectories.size() == 2
        visitedDirectories.every {
            it.isDirectory()
        }
    }

    @Override
    protected List<String> walkDirForPaths(DirectoryWalker walkerInstance, File rootDir, PatternSet patternSet) {
        def visited = []
        def visitClosure = { visited << it.file.absolutePath }
        def fileVisitor = [visitFile: visitClosure, visitDir: visitClosure] as FileVisitor
        def fileTree = new DirectoryFileTree(rootDir, patternSet, { walkerInstance } as Factory, TestFiles.fileSystem(), false)
        fileTree.visit(fileVisitor)
        return visited
    }
}
