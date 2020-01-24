package edu.wpi.axon.ui.service

import com.vaadin.flow.component.UI
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.ui.view.HasNotifications
import java.util.stream.Stream
import org.koin.core.KoinComponent
import org.koin.core.inject

class JobProvider : AbstractBackEndDataProvider<Job, Void>(), KoinComponent, HasNotifications {

    private val jobs: JobDb by inject()

    init {
        val ui = UI.getCurrent()
        jobs.subscribe {
            ui.access {
                refreshItem(it)
            }
        }
    }

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
