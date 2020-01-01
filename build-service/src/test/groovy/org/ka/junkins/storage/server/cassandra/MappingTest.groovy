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
        def jobId = UUID.randomUUID().toString()
        def buildId = UUID.randomUUID().toString()
        int number = 1
        def startedTs = System.currentTimeMillis()

        when:
        buildDao.updateBuild(jobId, number, buildId, 'RUNNING', null, startedTs, 0)
        def build = buildDao.findBuild(jobId, number)

        then:
        build.present
        build.get().buildId == buildId
        build.get().jobId == jobId
        build.get().number == number
        build.get().startedTs == startedTs
        build.get().lastUpdateTs != 0
        build.get().finishedTs != null
        build.get().result == null
        build.get().status == 'RUNNING'
    }

    void 'build finished'() {
        given:
        def jobId = UUID.randomUUID().toString()
        def buildId = UUID.randomUUID().toString()
        int number = 2
        def startedTs = System.currentTimeMillis()

        when:
        buildDao.updateBuild(jobId, number, buildId, 'RUNNING', null, startedTs, 0)
        def finishedTs = 3
        buildDao.updateBuild(jobId, number, buildId, 'DONE', 'SUCCESS', startedTs, finishedTs)

        def build = buildDao.findBuild(jobId, number)

        then:
        build.present
        build.get().buildId == buildId
        build.get().jobId == jobId
        build.get().number == number
        build.get().startedTs == startedTs
        build.get().lastUpdateTs != 0
        build.get().finishedTs == finishedTs
        build.get().result == 'SUCCESS'
        build.get().status == 'DONE'
    }

    void 'can find builds by job_id'() {
        given:
        def jobId = UUID.randomUUID().toString()

        when:
        10.times {
            buildDao.updateBuild(jobId, it, UUID.randomUUID().toString(), 'RUNNING', null, System.currentTimeMillis(), 0)
        }
        def builds = buildDao.findBuildByJob(jobId).all()

        then:
        builds.size() == 10
    }

    void 'insert new step'() {
        given:
        def buildId = UUID.randomUUID().toString()
        int stepId = 1
        int parentId = 0
        def startedTs = System.currentTimeMillis()

        when:
        buildStepDao.newBuildStep(buildId, stepId, parentId, 'name', startedTs)
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().buildId == buildId
        step.get().stepId == stepId
        step.get().parentId == parentId
        step.get().result == null
        step.get().log == null
        step.get().startedTs == startedTs
        step.get().finishedTs == null
        step.get().lastUpdateTs != null
    }

    void 'initiate a step, update log, finish build'() {
        given:
        def buildId = UUID.randomUUID().toString()
        int stepId = 1
        int parentId = 0
        def startedTs = System.currentTimeMillis()

        when:
        buildStepDao.newBuildStep(buildId, stepId, parentId, 'name', startedTs)
        buildStepDao.updateBuildLog(buildId, stepId, 'LOG1 LOG2')
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().log == 'LOG1 LOG2'
        step.get().startedTs != null
        step.get().finishedTs == null
        step.get().lastUpdateTs != null

        when:
        def finishedTs = System.currentTimeMillis()
        buildStepDao.finishBuildStep(buildId, stepId, 'DONE', 'LOG3', finishedTs)
        def finishedStep = buildStepDao.findBuildStep(buildId, stepId)

        then:
        finishedStep.present
        finishedStep.get().log == 'LOG3'
        finishedStep.get().startedTs == startedTs
        finishedStep.get().finishedTs == finishedTs
        finishedStep.get().lastUpdateTs != 0
    }

    void 'insert step that has finished'() {
        given:
        def buildId = UUID.randomUUID().toString()
        int stepId = 1
        int parentId = 0
        def startedTs = System.currentTimeMillis()
        def finishedTs = startedTs

        when:
        buildStepDao.insertFinishedBuildStep(buildId, stepId, parentId, 'name', 'DONE', 'LOG', startedTs, finishedTs)
        def step = buildStepDao.findBuildStep(buildId, stepId)

        then:
        step.present
        step.get().buildId == buildId
        step.get().stepId == stepId
        step.get().parentId == parentId
        step.get().name == 'name'
        step.get().result == 'DONE'
        step.get().log == 'LOG'
        step.get().startedTs == startedTs
        step.get().finishedTs == finishedTs
        step.get().lastUpdateTs != 0
    }

    void 'query all build steps by build id'() {
        given:
        def buildId = UUID.randomUUID().toString()
        int parentId = 0
        def startedTs = System.currentTimeMillis()
        def finishedTs = startedTs

        when:
        10.times { stepId ->
            buildStepDao.insertFinishedBuildStep(buildId, stepId, parentId, 'name', 'DONE', 'LOG', startedTs, finishedTs)
        }
        def steps = buildStepDao.allBuildSteps(buildId).all()

        then:
        steps.size() == 10
        steps.each { step ->
            assert step.buildId == buildId
            assert step.name == 'name'
            assert step.result == 'DONE'
            assert step.log == 'LOG'
            assert step.startedTs == startedTs
            assert step.finishedTs == finishedTs
            assert step.lastUpdateTs != 0
        }
    }
}
