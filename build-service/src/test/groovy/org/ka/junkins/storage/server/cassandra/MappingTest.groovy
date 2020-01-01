package org.ka.junkins.storage.server.cassandra


import spock.lang.Shared
import spock.lang.Specification

class MappingTest extends Specification {

    @Shared Cassandra.Module cassandra
    @Shared BuildStorageMapper.BuildDao buildDao
    @Shared BuildStorageMapper.BuildStepDao buildStepDao

    void setupSpec() {
        cassandra = EmbeddedCassandra.create()
        def mapping = Mapping.create(cassandra)

        buildDao = mapping.buildDao()
        buildStepDao = mapping.buildStepDao()
    }

    void cleanupSpec() {
        cassandra?.stop()
    }

    void 'insert a new build and read it'() {
        given:
        def jobId = UUID.randomUUID()
        def buildId = UUID.randomUUID()
        int number = 1
        def startedTs = new Date().toInstant()

        when:
        buildDao.updateBuild(jobId, number, buildId, 'RUNNING', null, startedTs, null)
        def build = buildDao.findBuild(jobId, number)

        then:
        build.present
        build.get().buildId == buildId
        build.get().jobId == jobId
        build.get().number == number
        build.get().startTs == startedTs
        build.get().lastUpdateTs != null
        build.get().finishTs == null
        build.get().result == null
        build.get().status == 'RUNNING'
    }

    void 'build finished'() {
        given:
        def jobId = UUID.randomUUID()
        def buildId = UUID.randomUUID()
        int number = 2
        def startedTs = new Date().toInstant()

        when:
        buildDao.updateBuild(jobId, number, buildId, 'RUNNING', null, startedTs, null)
        def finishTs = new Date().toInstant()
        buildDao.updateBuild(jobId, number, buildId, 'DONE', 'SUCCESS', startedTs, finishTs)

        def build = buildDao.findBuild(jobId, number)

        then:
        build.present
        build.get().buildId == buildId
        build.get().jobId == jobId
        build.get().number == number
        build.get().startTs == startedTs
        build.get().lastUpdateTs != null
        build.get().finishTs == finishTs
        build.get().result == 'SUCCESS'
        build.get().status == 'DONE'
    }

    void 'can find builds by job_id'() {
        given:
        def jobId = UUID.randomUUID()

        when:
        10.times {
            buildDao.updateBuild(jobId, it, UUID.randomUUID(), 'RUNNING', null, new Date().toInstant(), null)
        }
        def builds = buildDao.findBuildByJob(jobId).all()

        then:
        builds.size() == 10
    }

    void 'insert new step'() {
        given:
        def buildId = UUID.randomUUID()
        int stepId = 1
        int parentId = 0
        def startTs = new Date().toInstant()

        when:
        buildStepDao.newBuildStep(buildId, stepId, parentId, 'name', startTs)
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().buildId == buildId
        step.get().stepId == stepId
        step.get().parentId == parentId
        step.get().result == null
        step.get().log == null
        step.get().startTs == startTs
        step.get().finishTs == null
        step.get().lastUpdateTs != null
    }

    void 'initiate a step, update log, finish build'() {
        given:
        def buildId = UUID.randomUUID()
        int stepId = 1
        int parentId = 0
        def startTs = new Date().toInstant()

        when:
        buildStepDao.newBuildStep(buildId, stepId, parentId, 'name', startTs)
        buildStepDao.updateBuildLog(buildId, stepId, 'LOG1 LOG2')
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().log == 'LOG1 LOG2'
        step.get().startTs != null
        step.get().finishTs == null
        step.get().lastUpdateTs != null

        when:
        buildStepDao.finishBuildStep(buildId, stepId, 'DONE', 'LOG3', new Date().toInstant())
        def finishedStep = buildStepDao.findBuildStep(buildId, stepId)

        then:
        finishedStep.present
        finishedStep.get().log == 'LOG3'
        finishedStep.get().startTs != null
        finishedStep.get().finishTs != null
        finishedStep.get().lastUpdateTs != null
    }

    void 'insert step that has finished'() {
        given:
        def buildId = UUID.randomUUID()
        int stepId = 1
        int parentId = 0
        def startTs = new Date().toInstant()
        def finishTs = startTs

        when:
        buildStepDao.insertFinishedBuildStep(buildId, stepId, parentId, 'name', 'DONE', 'LOG', startTs, finishTs)
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().buildId == buildId
        step.get().stepId == stepId
        step.get().parentId == parentId
        step.get().name == 'name'
        step.get().result == 'DONE'
        step.get().log == 'LOG'
        step.get().startTs == startTs
        step.get().finishTs == finishTs
        step.get().lastUpdateTs != null
    }

    void 'query all build steps by build id'() {
        given:
        def buildId = UUID.randomUUID()
        int parentId = 0
        def startTs = new Date().toInstant()
        def finishTs = startTs

        when:
        10.times { stepId ->
            buildStepDao.insertFinishedBuildStep(buildId, stepId, parentId, 'name', 'DONE', 'LOG', startTs, finishTs)
        }
        def steps = buildStepDao.allBuildSteps(buildId).all()

        then:
        steps.size() == 10
        steps.each { step ->
            assert step.buildId == buildId
            assert step.name == 'name'
            assert step.result == 'DONE'
            assert step.log == 'LOG'
            assert step.startTs == startTs
            assert step.finishTs == finishTs
            assert step.lastUpdateTs != null
        }
    }
}
