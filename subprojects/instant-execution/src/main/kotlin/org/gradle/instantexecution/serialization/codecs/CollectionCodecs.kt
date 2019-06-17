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

package org.gradle.instantexecution.serialization.codecs

import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.codec
import org.gradle.instantexecution.serialization.readClass
import org.gradle.instantexecution.serialization.readCollectionInto
import org.gradle.instantexecution.serialization.readList
import org.gradle.instantexecution.serialization.readMapInto
import org.gradle.instantexecution.serialization.writeClass
import org.gradle.instantexecution.serialization.writeCollection
import org.gradle.instantexecution.serialization.writeMap
import java.util.EnumMap
import java.util.TreeMap
import java.util.TreeSet


internal
val listCodec: Codec<List<*>> = codec(
    { writeCollection(it) },
    { readList() }
)


internal
val hashSetCodec: Codec<HashSet<Any?>> = setCodec { HashSet<Any?>(it) }


internal
val linkedHashSetCodec: Codec<LinkedHashSet<Any?>> = setCodec { LinkedHashSet<Any?>(it) }


internal
val treeSetCodec: Codec<TreeSet<Any?>> = codec(
    {
        write(it.comparator())
        writeCollection(it)
    },
    {
        val comparator = read() as Comparator<Any?>?
        readCollectionInto { TreeSet(comparator) }
    })


internal
fun <T : MutableSet<Any?>> setCodec(factory: (Int) -> T) = codec(
    { writeCollection(it) },
    { readCollectionInto(factory) }
)


internal
val hashMapCodec: Codec<HashMap<Any?, Any?>> = mapCodec { HashMap<Any?, Any?>(it) }


internal
val linkedHashMapCodec: Codec<LinkedHashMap<Any?, Any?>> = mapCodec { LinkedHashMap<Any?, Any?>(it) }


internal
val treeMapCodec: Codec<TreeMap<Any?, Any?>> = mapCodec { TreeMap<Any?, Any?>() }


internal
val enumMapCodec: Codec<EnumMap<*, Any?>> = codec(
    { writeMap(it) },
    {
        EnumMap::class.java
            .getDeclaredConstructor(java.util.Map::class.java)
            .newInstance(readMapInto { LinkedHashMap<Any?, Any?>(it) })
            as EnumMap<*, Any?>?
    }
)


internal
fun <T : MutableMap<Any?, Any?>> mapCodec(factory: (Int) -> T): Codec<T> = codec(
    { writeMap(it) },
    { readMapInto(factory) }
)


internal
val arrayCodec: Codec<Array<*>> = codec({
    writeClass(it.javaClass.componentType)
    writeSmallInt(it.size)
    for (element in it) {
        write(element)
    }
}, {
    val componentType = readClass()
    val size = readSmallInt()
    val array = java.lang.reflect.Array.newInstance(componentType, size) as Array<Any?>
    for (i in 0 until size) {
        array[i] = read()
    }
    array
})
