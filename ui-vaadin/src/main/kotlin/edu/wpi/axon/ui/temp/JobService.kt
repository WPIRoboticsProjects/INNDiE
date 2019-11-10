package edu.wpi.axon.ui.temp

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.Job
import java.util.stream.Stream

object JobService {
    private val jobs = LinkedHashSet<Job>()

    init {
        jobs.add(Job("Job A", "Not Started", Dataset.FashionMnist))
        jobs.add(Job("Job B", "Training", Dataset.Cifar10))
        jobs.add(Job("Job C", "Finished", Dataset.Cifar100))
        jobs.add(Job("Job D", "Hidden", Dataset.Reuters))
    }

    fun fetchJobs(offset: Int, limit: Int): Stream<Job> {
        return jobs.stream().skip(offset.toLong()).limit(limit.toLong())
    }

    fun getJobCount(): Int {
        return jobs.size
    }

    fun addJob(job: Job): Boolean {
        return jobs.add(job)
    }

    fun deleteJob(job: Job): Boolean {
        return jobs.remove(job)
    }
}