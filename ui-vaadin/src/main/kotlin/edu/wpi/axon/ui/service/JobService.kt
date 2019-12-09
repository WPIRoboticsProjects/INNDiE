package edu.wpi.axon.ui.service

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.nextJob
import java.util.stream.Stream
import kotlin.random.Random
import org.jetbrains.exposed.sql.Database

object JobService {
    class JobProvider : AbstractBackEndDataProvider<Job, Void>() {
        override fun sizeInBackEnd(query: Query<Job, Void>?): Int {
            return jobs.count()
        }

        override fun fetchFromBackEnd(query: Query<Job, Void>): Stream<Job> {
            return jobs.fetch(query.limit, query.offset).stream()
        }

        override fun getId(item: Job): Any {
            return item.id
        }
    }

    val jobs = JobDb(
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
    )

    val dataProvider = JobProvider()

    init {
        for (i in 1..20) {
            jobs.create(Random.nextJob())
        }
    }
}
