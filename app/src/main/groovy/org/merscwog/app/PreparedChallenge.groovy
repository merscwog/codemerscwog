package org.merscwog.app

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.merscwog.sandbox.SandboxingBindingConstraints

class PreparedChallenge {
     static CompilerConfiguration compilerConfig = generateCompileConfiguration()

     Challenge underlyingChallenge
     GroovyShell preparedShell

     Object compileAndRun(String scriptText) throws CompilationFailedException {
          SandboxingBindingConstraints.ACTIVE_VALUES.set(new SandboxingBindingConstraints(
                  underlyingChallenge.staticTypedBinding.variablesToTypes,
                  underlyingChallenge.allowedMethods,
                  underlyingChallenge.allowedProperties))

          Script script = preparedShell.parse(scriptText)
          script.run()
     }

     static PreparedChallenge prepare(Challenge challenge) {
          new PreparedChallenge().tap {
               underlyingChallenge = challenge
               preparedShell = new GroovyShell(challenge.staticTypedBinding, compilerConfig)
          }
     }

     static CompilerConfiguration generateCompileConfiguration() {
          CompilerConfiguration compilerConfig = new CompilerConfiguration()
          ASTTransformationCustomizer customizer = new ASTTransformationCustomizer(
                  ['extensions': 'org.merscwog.sandbox.SandboxingTypeCheckingExtension'],
                  CompileStatic)
          compilerConfig.addCompilationCustomizers(customizer)
     }
}
