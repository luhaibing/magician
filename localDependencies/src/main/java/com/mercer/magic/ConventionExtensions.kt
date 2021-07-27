package com.mercer.magic

import groovy.lang.Closure
import groovy.lang.GroovyObject
import groovy.lang.MetaClass
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.Convention
import kotlin.reflect.KClass

inline fun <ConventionType : Any, ReturnType> Any.withConvention(
    conventionType: KClass<ConventionType>,
    function: ConventionType.() -> ReturnType
): ReturnType =
    conventionOf(this).getPlugin(conventionType.java).run(function)

fun conventionOf(target: Any): Convention = when (target) {
    is Project -> target.convention
    is HasConvention -> target.convention
    else -> throw IllegalStateException("Object `$target` doesn't support conventions!")
}

/**
 * Executes the given [builder] against this object's [GroovyBuilderScope].
 *
 * @see [GroovyBuilderScope]
 */
inline fun <T> Any.withGroovyBuilder(builder: GroovyBuilderScope.() -> T): T =
    GroovyBuilderScope.of(this).builder()


/**
 * Provides a dynamic dispatching DSL with Groovy semantics for better integration with
 * plugins that rely on Groovy builders such as the core `maven` plugin.
 *
 * It supports Groovy keyword arguments and arbitrary nesting, for instance, the following Groovy code:
 *
 * ```Groovy
 * repository(url: "scp://repos.mycompany.com/releases") {
 *   authentication(userName: "me", password: "myPassword")
 * }
 * ```
 *
 * Can be mechanically translated to the following Kotlin with the aid of `withGroovyBuilder`:
 *
 * ```Kotlin
 * withGroovyBuilder {
 *   "repository"("url" to "scp://repos.mycompany.com/releases") {
 *     "authentication"("userName" to "me", "password" to "myPassword")
 *   }
 * }
 * ```
 *
 * @see [withGroovyBuilder]
 */
interface GroovyBuilderScope : GroovyObject {

    companion object {

        /**
         * Creates a [GroovyBuilderScope] for the given [value].
         */
        fun of(value: Any): GroovyBuilderScope =
            when (value) {
                is GroovyObject -> GroovyBuilderScopeForGroovyObject(value)
                else -> GroovyBuilderScopeForRegularObject(value)
            }
    }

    /**
     * The delegate of this [GroovyBuilderScope].
     */
    val delegate: Any

    /**
     * Invokes with Groovy semantics and [arguments].
     */
    operator fun String.invoke(vararg arguments: Any?): Any?

    /**
     * Invokes with Groovy semantics and no arguments.
     */
    operator fun String.invoke(): Any? =
        invoke(*emptyArray<Any>())

    /**
     * Invokes with Groovy semantics, [arguments] and provides a nested [GroovyBuilderScope].
     */
    operator fun <T> String.invoke(
        vararg arguments: Any?,
        builder: GroovyBuilderScope.() -> T
    ): Any? =
        invoke(*arguments, closureFor(builder))

    /**
     * Invokes with Groovy semantics, no arguments, and provides a nested [GroovyBuilderScope].
     */
    operator fun <T> String.invoke(builder: GroovyBuilderScope.() -> T): Any? =
        invoke(closureFor(builder))

    /**
     * Invokes with Groovy semantics, named [keywordArguments], and provides a nested [GroovyBuilderScope].
     */
    operator fun <T> String.invoke(
        vararg keywordArguments: Pair<String, Any?>,
        builder: GroovyBuilderScope.() -> T
    ): Any? =
        invoke(keywordArguments.toMap(), closureFor(builder))

    /**
     * Invokes with Groovy semantics and named [keywordArguments].
     */
    operator fun String.invoke(vararg keywordArguments: Pair<String, Any?>): Any? =
        invoke(keywordArguments.toMap())

    private
    fun <T> closureFor(builder: GroovyBuilderScope.() -> T): Closure<Any?> =
        object : Closure<Any?>(this, this) {
            @Suppress("unused")
            fun doCall() = delegate.withGroovyBuilder(builder)
        }
}

private class GroovyBuilderScopeForGroovyObject(override val delegate: GroovyObject) :
    GroovyBuilderScope, GroovyObject by delegate {

    override fun String.invoke(vararg arguments: Any?): Any? =
        delegate.invokeMethod(this, arguments)
}


private class GroovyBuilderScopeForRegularObject(override val delegate: Any) : GroovyBuilderScope {

    private val groovyMetaClass: MetaClass by unsafeLazy {
        InvokerHelper.getMetaClass(delegate)
    }

    override fun invokeMethod(name: String, args: Any?): Any? =
        groovyMetaClass.invokeMethod(delegate, name, args)

    override fun setProperty(propertyName: String, newValue: Any?) =
        groovyMetaClass.setProperty(delegate, propertyName, newValue)

    override fun getProperty(propertyName: String): Any =
        groovyMetaClass.getProperty(delegate, propertyName)

    override fun setMetaClass(metaClass: MetaClass?) =
        throw IllegalStateException()

    override fun getMetaClass(): MetaClass =
        groovyMetaClass

    override fun String.invoke(vararg arguments: Any?): Any? =
        groovyMetaClass.invokeMethod(delegate, this, arguments)
}

/**
 * Thread unsafe version of [lazy].
 *
 * @see LazyThreadSafetyMode.NONE
 */
internal fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)