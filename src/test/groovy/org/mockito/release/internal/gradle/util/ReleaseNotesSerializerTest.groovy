package org.mockito.release.internal.gradle.util

import org.apache.commons.lang.builder.EqualsBuilder
import org.mockito.release.notes.internal.DefaultImprovement
import org.mockito.release.notes.internal.DefaultImprovementSerializer
import org.mockito.release.notes.internal.DefaultReleaseNotesData
import org.mockito.release.notes.model.Improvement
import org.mockito.release.notes.vcs.DefaultContributionSet
import org.mockito.release.notes.vcs.DefaultContributionSetSerializer
import spock.lang.Specification
import spock.lang.Subject

class ReleaseNotesSerializerTest extends Specification {

    @Subject
    ReleaseNotesSerializer serializer
    DefaultContributionSetSerializer defaultContributionSetSerializer = Mock(DefaultContributionSetSerializer)
    DefaultImprovementSerializer defaultImprovementSerializer = Mock(DefaultImprovementSerializer)

    def setup() {
        serializer = new ReleaseNotesSerializer(defaultContributionSetSerializer, defaultImprovementSerializer)
    }

    def "should serialize and deserialize file"() {
        def improvements = new LinkedList<Improvement>()
        def improvement = new DefaultImprovement(123L, "sampleTitle", "sample.url.com", new LinkedList<String>(), true)
        improvements.add(improvement)
        def contributionSet = new DefaultContributionSet()
        defaultContributionSetSerializer.deserialize(_) >> contributionSet
        defaultImprovementSerializer.deserialize(_) >> improvement
        def releaseNote = new DefaultReleaseNotesData("0.1",
                new Date(),
                contributionSet,
                improvements,
                "0.0.1",
                "0.2")
        def input = [releaseNote]

        when:
        def serializedData = serializer.serialize(input)
        def result = serializer.deserialize(serializedData)

        then:
        result[0].getVersion() == input[0].getVersion()
        result[0].getDate() == input[0].getDate()
        result[0].getPreviousVersionVcsTag() == input[0].getPreviousVersionVcsTag()
        result[0].getVcsTag() == input[0].getVcsTag()
        EqualsBuilder.reflectionEquals(result[0].getContributions(), input[0].getContributions())
        EqualsBuilder.reflectionEquals(result[0].getImprovements(), input[0].getImprovements())
    }

}
