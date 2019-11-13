package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import org.apache.commons.lang3.RandomStringUtils
import kotlin.random.Random

fun Random.nextDataset(): Dataset {
    return if (nextBoolean()) {
        Dataset.ExampleDataset::class.sealedSubclasses.let {
            it[nextInt(it.size)].objectInstance!!
        }
    } else {
        Dataset.Custom(RandomStringUtils.randomAlphanumeric(20))
    }
}
