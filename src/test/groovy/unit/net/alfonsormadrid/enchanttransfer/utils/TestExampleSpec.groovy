import net.alfonsormadrid.enchanttransfer.utils.TestExample
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class TestExampleSpec extends Specification {

    @Subject
    TestExample testClass = new TestExample()

    def 'Message property is filled with hello message'() {
        when:
        testClass.helloMessage()

        then:
        testClass.message == "Hello world"
    }

    @Unroll
    def 'Message property is filled with custom message'() {
        when:
        testClass.customMessage(message)

        then:
        testClass.message == message

        where:
        message                       | _
        "This is a custom message"    | _
        "Another custom message"      | _
        "Great custom message!!!"     | _
        "No more custome messages..." | _
    }

}