package org.merscwog.app

import spock.lang.Specification

import java.util.concurrent.TimeoutException

class PreparedChallengeSpec extends Specification {
    void 'simple math test to confirm result object'() {
        given: 'An empty challenge'
        Challenge challenge = new Challenge()

        when: 'Compiled and run'
        Object result = PreparedChallenge.prepare(challenge).compileAndRun(scriptText)

        then:
        result == expectedResult

        where:
        scriptText   | expectedResult
        '1 + 2 == 3' | true
        '1 + 2 == 4' | false
    }

    // FIXME: Takes 3+ seconds to run because of global timeout, need to make it configurable to be faster
    void 'properly handles timeout'() {
        given: 'An empty challenge'
        Challenge challenge = new Challenge()

        when: 'Compiled and run'
        PreparedChallenge.prepare(challenge).compileAndRun('''
            boolean k = true
            int i = 0
            while (k) {
                i += 1
            }
        ''')

        then:
        thrown(TimeoutException)
    }

    // FIXME: Takes 3+ seconds to run because of global timeout, need to make it configurable to be faster
    void 'properly handles static method timeout'() {
        given: 'An empty challenge'
        Challenge challenge = new Challenge().tap {
            allowedMethods = [~'Something#.*']
        }

        when: 'Compiled and run'
        // FIXME: Crap it doesn't work if use static method, which is consistent with TimedInterrupt docs
        PreparedChallenge.prepare(challenge).compileAndRun('''
            class Something {
                // FIXME: Might just have to disallow static keyword for methods... if possible
                //        Or invoke in new Thread, and call stop() since we do not care about variable state
                //        Or truly tweak the AST as it is being compiled...
                /*static*/ boolean found() {
                    boolean k = true
                    int i = 0
                    while (k) {
                        i += 1
                    }
                    k
                }
            }

            new Something().found()
        ''')

        then:
        thrown(TimeoutException)
    }
}
