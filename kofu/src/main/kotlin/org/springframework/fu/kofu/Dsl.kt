/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.fu.kofu

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.Environment

@DslMarker
internal annotation class DslMarker

@DslMarker
interface Dsl : ApplicationContextInitializer<GenericApplicationContext> {

	var context: GenericApplicationContext

	val env: Environment
		get() = context.environment

	val profiles: Array<String>
		get() = env.activeProfiles
}

/**
 * Get a reference to the bean by type or type + name with the syntax
 * `ref<Foo>()` or `ref<Foo>("foo")`. When leveraging Kotlin type inference
 * it could be as short as `ref()` or `ref("foo")`.
 * @param name the name of the bean to retrieve
 * @param T type the bean must match, can be an interface or superclass
 */
inline fun <reified T : Any> Dsl.ref(name: String? = null): T = when (name) {
	null -> context.getBean(T::class.java)
	else -> context.getBean(name, T::class.java)
}

abstract class AbstractDsl : Dsl {

	override lateinit var context: GenericApplicationContext

	@PublishedApi
	internal val initializers = mutableSetOf<ApplicationContextInitializer<GenericApplicationContext>>()

	override fun initialize(context: GenericApplicationContext) {
		this.context = context
		register(context)
		for (child in initializers) {
			child.initialize(context)
		}
	}

	internal abstract fun register(context: GenericApplicationContext)

}