package org.shipkit.internal.version

import spock.lang.Specification
import spock.lang.Subject

class VersionBumperTest extends Specification {

    @Subject v = new VersionBumper()

    def "increments version"() {
        expect:
        v.incrementVersion("1.0.0") == "1.0.1"
        v.incrementVersion("0.0.0") == "0.0.1"
        v.incrementVersion("1.10.15") == "1.10.16"

        v.incrementVersion("1.0.0-beta.3") == "1.0.0-beta.4"
        v.incrementVersion("1.10.15-beta.5") == "1.10.15-beta.6"
        v.incrementVersion("2.0.0-RC.1") == "2.0.0-RC.2"
    }

    def "increments only 3 numbered versions"() {
        when:
        v.incrementVersion(unsupported)

        then:
        thrown(IllegalArgumentException)

        where:
        unsupported << ["1.0", "2", "1.0.0.0", "1.0.1-beta"]
    }
}
