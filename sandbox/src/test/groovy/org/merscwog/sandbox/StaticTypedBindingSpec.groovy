package org.merscwog.sandbox

import org.codehaus.groovy.ast.ClassHelper
import spock.lang.Specification
import spock.lang.Unroll

class StaticTypedBindingSpec extends Specification {

    @Unroll
    void 'inferred types are as expected'() {
        given:
        StaticTypedBinding typedBinding = new StaticTypedBinding()

        when:
        typedBinding.setVariableWithInferredType('something', value)

        then:
        typedBinding.variablesToTypes['something'] == expectedType

        where:
        value            | expectedType
        false            | ClassHelper.boolean_TYPE
        8 as byte        | ClassHelper.byte_TYPE
        'a' as char      | ClassHelper.char_TYPE
        24 as short      | ClassHelper.short_TYPE
        3                | ClassHelper.int_TYPE
        45L              | ClassHelper.long_TYPE
        32.0f            | ClassHelper.float_TYPE
        96.3d            | ClassHelper.double_TYPE
        'word'           | ClassHelper.STRING_TYPE
        Boolean.FALSE    | ClassHelper.boolean_TYPE
        3 as Byte        | ClassHelper.byte_TYPE
        'a' as Character | ClassHelper.char_TYPE
        24 as Short      | ClassHelper.short_TYPE
        3 as Integer     | ClassHelper.int_TYPE
        45 as Long       | ClassHelper.long_TYPE
        32.0 as Float    | ClassHelper.float_TYPE
        96.3 as Double   | ClassHelper.double_TYPE
    }

    @Unroll
    void 'explicit types are as expected'() {
        given:
        StaticTypedBinding typedBinding = new StaticTypedBinding()

        when:
        typedBinding.setVariableWithType('something', Boolean.FALSE, clazz)

        then:
        typedBinding.variablesToTypes['something'] == expectedType

        where:
        value            | clazz     | expectedType
        Boolean.FALSE    | Boolean   | ClassHelper.Boolean_TYPE
        3 as Byte        | Byte      | ClassHelper.Byte_TYPE
        'a' as Character | Character | ClassHelper.Character_TYPE
        24 as Short      | Short     | ClassHelper.Short_TYPE
        3 as Integer     | Integer   | ClassHelper.Integer_TYPE
        45 as Long       | Long      | ClassHelper.Long_TYPE
        32.0 as Float    | Float     | ClassHelper.Float_TYPE
        96.3 as Double   | Double    | ClassHelper.Double_TYPE
    }
}
