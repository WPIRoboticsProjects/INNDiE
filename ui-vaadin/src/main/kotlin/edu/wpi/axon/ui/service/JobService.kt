package edu.wpi.axon.ui.service

import com.vaadin.flow.data.provider.DataProvider
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.nextJob
import kotlin.random.Random
import org.jetbrains.exposed.sql.Database

object JobService {

    val jobs = JobDb(
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
    )

    val dataProvider = DataProvider.fromCallbacks<Job>(
        { jobs.fetch(it.limit, it.offset).stream() },
        { jobs.count() }
    )

    init {
        for (i in 1..20) {
            jobs.create(Random.nextJob())
        }
    }
}
