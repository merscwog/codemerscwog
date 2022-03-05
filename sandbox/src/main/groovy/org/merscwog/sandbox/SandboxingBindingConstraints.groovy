package org.merscwog.sandbox

import groovy.transform.Immutable
import org.codehaus.groovy.ast.ClassNode

import java.util.regex.Pattern

/**
 * Constains the explicit compilation and binding constraints allowed for Scripts.
 */
@Immutable
class SandboxingBindingConstraints {
    public static final ThreadLocal<SandboxingBindingConstraints> ACTIVE_VALUES =
            ThreadLocal.withInitial { new SandboxingBindingConstraints() }

    Map<String, ClassNode> variableTypes = [:]
    Set<Pattern> allowedMethods = []
}
