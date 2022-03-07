package org.merscwog.sandbox

import groovy.transform.stc.POJO
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode

/**
 * Convenience class to be able to create a normal Binding for a Script to run, and to also assign the
 * types to bind the variables to for static compilation.
 */
@POJO
class StaticTypedBinding extends Binding {
    Map<String, ClassNode> variablesToTypes

    StaticTypedBinding() {
        super()
    }

    StaticTypedBinding(Map<String, Object> variables, Map<String, ClassNode> variablesToTypes) {
        super(variables)
        this.variablesToTypes = variablesToTypes
    }

    StaticTypedBinding(String[] args) {
        super(args)
    }

    boolean hasTypeForVariable(String variableName) {
        variablesToTypes?.containsKey(variableName)
    }

    Map<String, ClassNode> getVariablesToTypes() {
        variablesToTypes
    }

    void setTypeForVariable(String variableName, Class clazz) {
        setTypeForVariable(variableName, ClassHelper.make(clazz))
    }

    void setTypeForVariable(String variableName, ClassNode classNode) {
        if (variablesToTypes == null) {
            variablesToTypes = [:]
        }

        variablesToTypes[variableName] = classNode
    }

    void removeTypeForVariable(String variableName) {
        variablesToTypes?.remove(variableName)
    }

    void setVariableWithType(String variableName, Object value, ClassNode classNode) {
        super.setVariable(variableName, value)
        setTypeForVariable(variableName, classNode)
    }

    void setVariableWithType(String variableName, Object value, Class clazz) {
        super.setVariable(variableName, value)
        setTypeForVariable(variableName, clazz)
    }

    void setVariablesWithInferredTypes(Map<String, Object> variablesToInferredTypes) {
        variablesToInferredTypes.each { Map.Entry<String,Object> entry ->
            setVariableWithInferredType(entry.key, entry.value)
        }
    }

    void setVariableWithInferredType(String variableName, boolean value) {
        setVariableWithType(variableName, value, ClassHelper.boolean_TYPE)
    }

    void setVariableWithInferredType(String variableName, char value) {
        setVariableWithType(variableName, value, ClassHelper.char_TYPE)
    }

    void setVariableWithInferredType(String variableName, byte value) {
        setVariableWithType(variableName, value, ClassHelper.byte_TYPE)
    }

    void setVariableWithInferredType(String variableName, short value) {
        setVariableWithType(variableName, value, ClassHelper.short_TYPE)
    }

    void setVariableWithInferredType(String variableName, int value) {
        setVariableWithType(variableName, value, ClassHelper.int_TYPE)
    }

    void setVariableWithInferredType(String variableName, long value) {
        setVariableWithType(variableName, value, ClassHelper.long_TYPE)
    }

    void setVariableWithInferredType(String variableName, float value) {
        setVariableWithType(variableName, value, ClassHelper.float_TYPE)
    }

    void setVariableWithInferredType(String variableName, double value) {
        setVariableWithType(variableName, value, ClassHelper.double_TYPE)
    }

    /**
     * Will use the exact class type the object is when this method is called.
     *
     * NOTE: Boolean, Long, Double, and all of the other 'primitive' classes will by default get coerced into
     *       the other setVariableWithInferredType() primitive methods.  If the object form is absolutely
     *       necessary (such as to support a null value) then call one of the setVariableWithType() variants
     *       instead.
     *
     * @param variableName the variable name to bind the value to.
     * @param value the value to assign.
     */
    void setVariableWithInferredType(String variableName, Object value) {
        setVariableWithType(variableName, value, value.class)
    }
}
