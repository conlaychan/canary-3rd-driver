package com.chenye.iot.canary.utils

import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.net.URL

object ClassScanner {

    /**
     * 扫描指定包（包括子包）内的所有Class。
     */
    fun scanByPackages(packages: Collection<String>, classLoader: ClassLoader): Set<Class<*>> {
        val classes: MutableSet<Class<*>> = HashSet()
        for (packageName in packages) {
            val path = packageName.replace('.', '/')
            val children: List<String> = list(path, classLoader)
            for (fqn in children) {
                if (fqn.endsWith(".class")) {
                    val externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.')
                    val type = classLoader.loadClass(externalName)
                    classes.add(type)
                }
            }
        }
        return classes
    }

    private fun preserveSubpackageName(baseUrlString: String, resource: Resource, rootPath: String): String {
        return """${rootPath}${if (rootPath.endsWith("/")) "" else "/"}${
            resource.url.toString().substring(baseUrlString.length)
        }"""
    }

    private fun list(url: URL, path: String): List<String> {
        val urlString = url.toString()
        val baseUrlString = if (urlString.endsWith("/")) urlString else "$urlString/"
        val resources = PathMatchingResourcePatternResolver(javaClass.classLoader).getResources(
            "$baseUrlString**/*.class"
        )
        return resources.map { resource ->
            preserveSubpackageName(baseUrlString, resource, path)
        }
    }

    private fun list(path: String, classLoader: ClassLoader): List<String> {
        val names: MutableList<String> = ArrayList()
        for (url in classLoader.getResources(path)) {
            names.addAll(list(url, path))
        }
        return names
    }
}
