package org.merscwog.app

import groovy.transform.CompileStatic
import groovy.transform.TimedInterrupt
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.merscwog.sandbox.SandboxingBindingConstraints

class PreparedChallenge {
    static CompilerConfiguration compilerConfig = generateCompileConfiguration()

    private Challenge underlyingChallenge
    private GroovyShell preparedShell

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
        // FIXME: Not sure what maximum runtime of script should be, but 3 seconds is pretty long
        //        Maybe need to make this customizable, which would no longer allow it to be static
        ASTTransformationCustomizer timedInterruptCustomizer = new ASTTransformationCustomizer(
                ['value': 3L],
                TimedInterrupt)
        compilerConfig.addCompilationCustomizers(customizer, timedInterruptCustomizer)
    }

    Object compileAndRun(String scriptText) throws CompilationFailedException {
        SandboxingBindingConstraints.ACTIVE_VALUES.set(new SandboxingBindingConstraints(
                underlyingChallenge.staticTypedBinding.variablesToTypes,
                underlyingChallenge.allowedMethods,
                underlyingChallenge.allowedProperties))

        Script script = preparedShell.parse(scriptText)
        script.run()
    }

}
