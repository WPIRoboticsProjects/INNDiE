package edu.wpi.axon.ui.service

import com.vaadin.flow.data.provider.DataProvider
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.Database
import kotlin.random.Random

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
        for (i in 1..10) {
            jobs.create(Random.nextJob())
        }
    }

    private fun Random.nextJob() = Job(
        RandomStringUtils.randomAlphanumeric(10),
        TrainingScriptProgress.Completed,
        RandomStringUtils.randomAlphanumeric(10),
        RandomStringUtils.randomAlphanumeric(10),
        nextDataset(),
        Optimizer.Adam(
            nextDouble(),
            nextDouble(),
            nextDouble(),
            nextDouble(),
            nextBoolean()
        ),
        Loss.SparseCategoricalCrossentropy,
        setOf(
            RandomStringUtils.randomAlphanumeric(10),
            RandomStringUtils.randomAlphanumeric(10)
        ),
        nextInt(),
        nextBoolean()
    )

    fun Random.nextDataset(): Dataset {
        return if (nextBoolean()) {
            Dataset.ExampleDataset::class.sealedSubclasses.let {
                it[nextInt(it.size)].objectInstance!!
            }
        } else {
            Dataset.Custom(
                RandomStringUtils.randomAlphanumeric(20),
                RandomStringUtils.randomAlphanumeric(20)
            )
        }
    }
}
