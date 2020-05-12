package edu.wpi.inndie.aws

/**
 * A [TrainingScriptCanceller] that is designed for an [EC2TrainingScriptRunner].
 *
 * @param ec2Manager Used to interface with EC2.
 */
class EC2TrainingScriptCanceller(
    private val ec2Manager: EC2Manager
) : TrainingScriptCanceller {

    private val instanceIds = mutableMapOf<Int, String>()

    /**
     * Adds a Job.
     *
     * @param jobId The Job ID.
     * @param instanceId The EC2 instance ID for the instance that was started to run the training
     * script.
     */
    fun addJob(jobId: Int, instanceId: String) {
        instanceIds[jobId] = instanceId
    }

    override fun cancelScript(jobId: Int) {
        require(jobId in instanceIds.keys)
        ec2Manager.terminateInstance(instanceIds[jobId]!!)
    }
}
