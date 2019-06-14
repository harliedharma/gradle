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

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.builder;

import org.gradle.api.artifacts.ModuleIdentifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class SelectorOverrides {

    private final SelectorOverrides parent;
    private final Map<ModuleIdentifier, DependencyState> overrides = new LinkedHashMap<>();

    public SelectorOverrides(Map<ModuleIdentifier, DependencyState> overrides, SelectorOverrides parent) {
        this.parent = parent;
        this.overrides.putAll(overrides);
        if (parent != null) {
            this.overrides.putAll(parent.overrides);
        }
    }

    public boolean isParent(SelectorOverrides candidate) {
        if (parent == null) {
            return false;
        }
        if (parent == candidate) {
            return true;
        }
        return parent.isParent(parent);
    }

    public DependencyState getOverride(ModuleIdentifier moduleIdentifier) {
        return overrides.get(moduleIdentifier);
    }
}
