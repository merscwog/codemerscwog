package org.merscwog.app

import org.codehaus.groovy.ast.ClassNode

import java.util.regex.Pattern

class Challenge {
    Map<String, ClassNode> variableTypes = [:]
    Set<Pattern> allowedMethods = []
    Set<Pattern> allowedProperties = []

    String description = '''Please return the value 'one' as a long value'''
    List<String> defaultCodeBlocks = ['// Nothing to see here!!!!']
    Object expectedResult = 1L
}
