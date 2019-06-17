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

package org.gradle.instantexecution


class InstantExecutionClassloadingIntegrationTest extends AbstractInstantExecutionIntegrationTest {


    def "can capture and restore types from init script classpath dependencies"() {

        given:
        def jar = file("init-cp.jar")
        jarWithClasses(["some/SampleType": """
            package some;
            import java.io.Serializable; // for input snapshots
            public class SampleType implements Serializable {}
        """], jar)

        and:
        def initScript = file("some.init.gradle") << """

            import some.SampleType

            initscript {
                dependencies {
                    classpath(files("$jar.absolutePath"))
                }
            }
            
            rootProject { 
                tasks.register("some") {
                    inputs.property("some", new SampleType())
                    outputs.upToDateWhen { false }
                    doLast {
                        println("some")
                    }
                }
            }
        """

        and:
        buildFile << ""

        when:
        instantRun "some", "-I", initScript.absolutePath

        then:
        instantRun "some", "-I", initScript.absolutePath
    }
}
