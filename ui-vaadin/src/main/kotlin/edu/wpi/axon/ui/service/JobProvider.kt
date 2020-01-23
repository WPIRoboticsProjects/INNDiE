package edu.wpi.axon.ui.service

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import java.util.stream.Stream

class JobProvider(
    private val jobs: JobDb
) : AbstractBackEndDataProvider<Job, Void>() {

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
