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
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.readClass
import org.gradle.instantexecution.serialization.readList
import org.gradle.instantexecution.serialization.writeClass
import org.gradle.instantexecution.serialization.writeCollection
import java.util.EnumSet


internal
object EnumSetCodec : Codec<EnumSet<*>> {

    override fun WriteContext.encode(value: EnumSet<*>) {
        writeClass(value.enumType)
        writeCollection(value)
    }

    override fun ReadContext.decode(): EnumSet<*>? =
        newEmptyEnumSet(readClass())
            .addAll(readList())

    private
    val EnumSet<*>.enumType: Class<*>
        get() {
            val nonEmptyEnumSet = takeIf { isNotEmpty() }
                ?: complementOf.invoke(null, this) as EnumSet<*>
            return nonEmptyEnumSet.iterator().next().javaClass
        }

    private
    val complementOf = EnumSet::class.java.getDeclaredMethod("complementOf", EnumSet::class.java)

    private
    fun newEmptyEnumSet(enumType: Class<*>): EnumSet<*> =
        noneOf.invoke(null, enumType) as EnumSet<*>

    private
    val noneOf = EnumSet::class.java.getDeclaredMethod("noneOf", Class::class.java)

    private
    fun EnumSet<*>.addAll(list: List<Any?>): EnumSet<*> = apply {
        collectionAddAll.invoke(this, list)
    }

    private
    val collectionAddAll = Collection::class.java.getDeclaredMethod("addAll", Collection::class.java)
}
