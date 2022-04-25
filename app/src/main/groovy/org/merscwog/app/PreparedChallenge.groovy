package org.merscwog.app

import groovy.transform.CompileStatic
import groovy.transform.TimedInterrupt
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import org.merscwog.sandbox.SandboxingBindingConstraints

import java.util.concurrent.TimeoutException

class PreparedChallenge {
    private static final String FAKE_SCRIPT_FILE_NAME = 'line'
    private static CompilerConfiguration compilerConfig = generateCompileConfiguration()

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

    String getDescription() {
        underlyingChallenge.description
    }

    void assertExpectedResult(Object actualResult) throws PowerAssertionError {
        Object expectedResult = underlyingChallenge.expectedResult
        assert actualResult == expectedResult
    }

    Object compileAndRun(String scriptText) throws CompilationFailedException, TimeoutException {
        SandboxingBindingConstraints.ACTIVE_VALUES.set(new SandboxingBindingConstraints(
                underlyingChallenge.staticTypedBinding.variablesToTypes,
                underlyingChallenge.allowedMethods,
                underlyingChallenge.allowedProperties))

        Script script = preparedShell.parse(scriptText, FAKE_SCRIPT_FILE_NAME)
        script.run()
    }

    void compileAndRunAndAssertExpectedResult(String scriptText) throws CompilationFailedException, PowerAssertionError, TimeoutException {
        Object result = compileAndRun(scriptText)
        assertExpectedResult(result)
    }
}
