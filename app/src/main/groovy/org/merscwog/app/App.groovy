package org.merscwog.app

/**
 * For now, this is the "App" class, but will need to pull out the scripting stuff to either a library or
 * within some other helper class.
 */
class App {

    private static final String SCRIPT_TEXT = '''
        println 'bob'
        println hi
        //['bob'].indices
        //System.exit(-1)
        2
    '''

    @SuppressWarnings(['Println', 'CatchThrowable'])
    static void main(String[] args) {
        //StaticTypedBinding staticTypedBinding = new StaticTypedBinding()
        //staticTypedBinding.setVariableWithType('hi', 'Hello World', String)
//        Map<String, ClassNode> variableTypes = staticTypedBinding.variablesToTypes
//        Set<Pattern> allowedMethods = [~/groovy\.lang\.Script#println(.*)/]
//        Set<Pattern> allowedProperties = [~/java\.util\.Collection#indices/]

        Challenge challenge = new Challenge().tap {
            staticTypedBinding.setVariableWithType('hi', 'Hello World', String)
            allowedMethods = [~/groovy\.lang\.Script#println(.*)/]
            allowedProperties = [~/java\.util\.Collection#indices/]
        }

        PreparedChallenge preparedChallenge = PreparedChallenge.prepare(challenge)
        Object scriptResult = preparedChallenge.compileAndRun(SCRIPT_TEXT)
        if (scriptResult) {
            try {
                Object expectedResult = challenge.expectedResult
                assert scriptResult == expectedResult
                println 'You PASSED!'
            } catch (Throwable t) {
                println t.message
                println 'You FAILED!'
            }
        } else {
            println 'Need something to return, so FAILED!'
        }
    }
}
