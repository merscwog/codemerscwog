package org.merscwog.sandbox

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys
import org.codehaus.groovy.transform.stc.ExtensionMethodNode
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.codehaus.groovy.transform.stc.TypeCheckingContext

import java.util.regex.Pattern

/**
 * Specialized AST checking extension specifically to stop certain methods and binding variables from
 * being used at compile time by Scripts.
 */
class SandboxingTypeCheckingExtension extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    @Override
    Object run() {
        SandboxingBindingConstraints activeConstraints = SandboxingBindingConstraints.ACTIVE_VALUES.get()
        Map<String, ClassNode> variableTypes = activeConstraints.variableTypes
        Set<Pattern> allowedMethods = activeConstraints.allowedMethods
        Set<Pattern> allowedProperties = activeConstraints.allowedProperties

        Closure onMethodSelectionClosure = { Expression expr, MethodNode methodNode ->
            if (expr.columnNumber > 0 && expr.lineNumber > 0) {
                String descr = toMethodDescriptor(methodNode)

                if (!allowedMethods.any { Pattern pattern ->  pattern.matcher(descr).matches() }) {
                    invokeMethod('addStaticTypeError', ["Disallowed method: '${descr}'", expr] as Object[])
                }
            }
        }

        invokeMethod('onMethodSelection', [onMethodSelectionClosure] as Object[])

        Closure unresolvedVariableClosure = { VariableExpression varExpr ->
            if (invokeMethod('isDynamic', [varExpr] as Object[])) {
                ClassNode varClassNode = variableTypes?[varExpr.name]

                if (varClassNode) {
                    invokeMethod('storeType', [varExpr, varClassNode] as Object[])
                    invokeMethod('setHandled', [true] as Object[])
                }
            }
        }

        invokeMethod('unresolvedVariable', [unresolvedVariableClosure] as Object[])

        TypeCheckingContext typeCheckingContext = (TypeCheckingContext)getProperty('context')

        Closure afterVisitMethodClosure = { MethodNode methodNode ->
            PropertyExpressionChecker visitor = new PropertyExpressionChecker(typeCheckingContext.source, methodNode.declaringClass, allowedProperties)
            visitor.visitMethod(methodNode)
        }

        invokeMethod('afterVisitMethod', [afterVisitMethodClosure] as Object[])
    }

    private static String prettyPrintClassNode(ClassNode classNode) {
        classNode.isArray() ? "${prettyPrintClassNode(classNode.componentType)}[]" : classNode.toString(false)
    }

    private static String toMethodDescriptor(MethodNode methodNode) {
        if (methodNode instanceof ExtensionMethodNode) {
            return toMethodDescriptor(methodNode.extensionMethodNode)
        }

        String methodParameters = methodNode.parameters.collect { Parameter parameter ->
            prettyPrintClassNode(parameter.originType)
        }.join(',')
        "${methodNode.declaringClass.toString(false)}#${methodNode.name}(${methodParameters})"
    }

    /**
     * Explicitly handle property invocations (i.e. implicit method invocations)
     */
    private static class PropertyExpressionChecker extends ClassCodeVisitorSupport {

        private final SourceUnit sourceUnit
        private final Set<Pattern> allowedProperties
        private final Hack hack

        PropertyExpressionChecker(final SourceUnit sourceUnit, final ClassNode classNode, final Set<Pattern> allowedProperties) {
            this.sourceUnit = sourceUnit
            this.allowedProperties = allowedProperties
            this.hack = new Hack(sourceUnit, classNode)
        }

        @Override
        void visitPropertyExpression(PropertyExpression expression) {
            super.visitPropertyExpression(expression)

            ClassNode owner = expression.objectExpression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER)
            if (owner) {
                if (expression.spreadSafe && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(owner, ClassHelper.make(Collection))) {
                    owner = hack.passThruInferComponentType(owner, ClassHelper.int_TYPE)
                }
                String descr = "${prettyPrintClassNode(owner)}#${expression.propertyAsString}"

                // FIXME: properties need different regex pattern, although maybe always caught by method checks anyway
                //        Not sure why I'd ever want the method to be allowed but _NOT_ the property
                if (! allowedProperties.any { Pattern pattern -> pattern.matcher(descr).matches() }) {
                    hack.passThruAddStaticTypeError("Property is not allowed: ${descr}", expression)
                }
            }
        }

        @Override
        protected SourceUnit getSourceUnit() {
            sourceUnit
        }

        /**
         * inferComponentType() is only accessible by a StaticTypeCheckingVisitor, so need a way to construct
         * one with proper SourceUnit and ClassNode.
         *
         * Since it already needs to be created, might as well use it to invoke direct calls to
         * addStaticTypeError() that otherwise would require an uglier invokeMethod() call.
         */
        private static class Hack extends StaticTypeCheckingVisitor {
            Hack(SourceUnit source, ClassNode classNode) {
                super(source, classNode)
            }

            ClassNode passThruInferComponentType(final ClassNode containerType, final ClassNode indexType) {
                super.inferComponentType(containerType, indexType)
            }

            void passThruAddStaticTypeError(final String msg, final ASTNode expr) {
                super.addStaticTypeError(msg, expr)
            }
        }
    }
}
