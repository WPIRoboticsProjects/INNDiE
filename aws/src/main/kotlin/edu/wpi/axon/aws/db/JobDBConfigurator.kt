package edu.wpi.axon.aws.db

import arrow.fx.IO

/**
 * Configures a Job DB so it is ready to be connected to.
 */
interface JobDBConfigurator {

    /**
     * Ensures the Job DB is properly configured and ready to accept a connection.
     *
     * @return An effect for continuation.
     */
    fun ensureConfiguration(): IO<Unit>
}
