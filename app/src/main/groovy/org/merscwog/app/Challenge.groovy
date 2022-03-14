package org.merscwog.app

import org.merscwog.sandbox.StaticTypedBinding

import java.util.regex.Pattern

class Challenge {
    StaticTypedBinding staticTypedBinding = new StaticTypedBinding()
    Set<Pattern> allowedMethods = []
    Set<Pattern> allowedProperties = []

    String description = ''''''
    Object expectedResult = 1L
}
