package com.autonomousapps.internal.utils

import org.gradle.api.GradleException
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier

internal fun ComponentIdentifier.asString(): String {
  return when (this) {
    is ProjectComponentIdentifier -> projectPath
    is ModuleComponentIdentifier -> {
      // flat JAR/AAR files have no group. I don't trust that, if absent, it will be blank rather
      // than null.
      @Suppress("UselessCallOnNotNull")
      if (moduleIdentifier.group.isNullOrBlank()) moduleIdentifier.name
      else moduleIdentifier.toString()
    }
    // e.g. "Gradle API"
    is OpaqueComponentIdentifier -> displayName
    // for a file dependency
    is OpaqueComponentArtifactIdentifier -> displayName
    else -> throw GradleException("Cannot identify ComponentIdentifier subtype. Was ${javaClass.simpleName}, named $this")
  }.intern()
}

internal fun ComponentIdentifier.resolvedVersion(): String? {
  return when (this) {
    is ProjectComponentIdentifier -> null
    is ModuleComponentIdentifier -> {
      // flat JAR/AAR files have no version, but rather than null, it's empty.
      if (version.isNotBlank()) version else null
    }
    // e.g. "Gradle API"
    is OpaqueComponentIdentifier -> null
    // for a file dependency
    is OpaqueComponentArtifactIdentifier -> null
    else -> throw GradleException("Cannot identify ComponentIdentifier subtype. Was ${javaClass.simpleName}, named $this")
  }?.intern()
}

internal fun DependencySet.toIdentifiers(): Set<String> = mapNotNullToSet {
  when (it) {
    is ProjectDependency -> it.dependencyProject.path
    is ModuleDependency -> {
      // flat JAR/AAR files have no group.
      if (it.group != null) "${it.group}:${it.name}" else it.name
    }
    // Don't have enough information, so ignore it
    is SelfResolvingDependency -> null
    else -> throw GradleException("Unknown Dependency subtype: \n$it\n${it.javaClass.name}")
  }?.intern()
}
