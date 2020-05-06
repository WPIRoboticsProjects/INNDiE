package edu.wpi.inndie.tfdata

import edu.wpi.inndie.util.FilePath
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
sealed class Dataset : Comparable<Dataset> {

    abstract val displayName: String
    abstract val progressReportingName: String

    @Serializable
    sealed class ExampleDataset(val name: String, override val displayName: String) : Dataset() {

        final override val progressReportingName = name

        init {
            require(progressReportingName.matches(Regex("""[\w-]+""")))
        }

        @Serializable
        object BostonHousing : ExampleDataset("boston_housing", "Boston Housing")

        @Serializable
        object Cifar10 : ExampleDataset("cifar10", "CIFAR-10")

        @Serializable
        object Cifar100 : ExampleDataset("cifar100", "CIFAR-100")

        @Serializable
        object FashionMnist : ExampleDataset("fashion_mnist", "Fashion MNIST")

        @Serializable
        object IMDB : ExampleDataset("imdb", "IMBD")

        @Serializable
        object Mnist : ExampleDataset("mnist", "MNIST")

        @Serializable
        object Reuters : ExampleDataset("reuters", "Reuters")

        @Serializable
        object AutoMPG : ExampleDataset("auto-mpg", "Auto MPG")
    }

    @Serializable
    data class Custom(val path: FilePath, override val displayName: String) : Dataset() {

        override val progressReportingName = path.filename
            .replace(".", "_")
            .replace("-", "_")
            .replace(" ", "_")

        init {
            require(progressReportingName.matches(Regex("""\w+""")))
        }

        val baseNameWithExtension = path.filename
        val baseNameWithoutExtension = path.filename.substringBeforeLast(".")
    }

    override fun compareTo(other: Dataset) = COMPARATOR.compare(this, other)

    fun serialize(): String = Json(
        JsonConfiguration.Stable
    ).stringify(serializer(), this)

    companion object {
        private val COMPARATOR = Comparator.comparing<Dataset, String> { it.displayName }

        fun deserialize(data: String) = Json(
            JsonConfiguration.Stable
        ).parse(serializer(), data)
    }
}
