package org.ka.jenkins.masterless.image.junkins


import spock.lang.Specification

import java.util.concurrent.TimeUnit

class JenkinsBenchmarksTest extends Specification implements JenkinsHome {

    static final long LIMIT_10_SECONDS = 10

    void 'time to get jenkins running < 10 sec'() {
        given:
        def root = newDefaultJenkinsHome()

        when:
        def start = System.nanoTime()
        def junkins = Junkins.builder()
                .rootDir(root)
                .warExplodedDir(new File("./build/war"))
                .build()
        def stop = System.nanoTime()

        def duration = TimeUnit.NANOSECONDS.toSeconds(stop - start)

        then:
        duration < LIMIT_10_SECONDS

        cleanup:
        junkins?.stop()
        root?.deleteDir()
    }
}
