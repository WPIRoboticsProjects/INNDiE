package edu.wpi.axon.db.data

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.examplemodel.ExampleModel
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableTuple2II
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.ModelDeploymentTarget
import edu.wpi.inndie.util.FilePath
import edu.wpi.inndie.util.getOutputModelName
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils

fun Random.nextDataset(): Dataset {
    return if (nextBoolean()) {
        Dataset.ExampleDataset::class.sealedSubclasses.let {
            it[nextInt(it.size)].objectInstance!!
        }
    } else {
        Dataset.Custom(
            FilePath.S3(RandomStringUtils.randomAlphanumeric(20)),
            RandomStringUtils.randomAlphanumeric(20)
        )
    }
}

fun Random.nextTrainingScriptProgress(): TrainingScriptProgress =
    when (nextInt(TrainingScriptProgress::class.sealedSubclasses.count())) {
        0 -> TrainingScriptProgress.NotStarted
        1 -> TrainingScriptProgress.Creating
        2 -> TrainingScriptProgress.Initializing
        3 -> {
            val epochs = nextInt(1, 10)
            val totalEpochs = nextInt(epochs, epochs + 10)
            TrainingScriptProgress.InProgress(
                epochs.toDouble() / totalEpochs,
                "epochs\n${(0..epochs).joinToString("\n")}"
            )
        }
        4 -> TrainingScriptProgress.Completed
        5 -> TrainingScriptProgress.Error(RandomStringUtils.randomAlphanumeric(50))
        else -> error("Missing a TrainingScriptProgress case.")
    }

fun Random.nextTrainingMethod(): InternalJobTrainingMethod =
    when (nextInt(InternalJobTrainingMethod::class.sealedSubclasses.count())) {
        0 -> InternalJobTrainingMethod.EC2(RandomStringUtils.randomAlphabetic(10))
        1 -> InternalJobTrainingMethod.Local
        2 -> InternalJobTrainingMethod.Untrained
        else -> error("Missing a JobTrainingMethod case.")
    }

fun Random.nextTarget(): ModelDeploymentTarget =
    when (nextInt(ModelDeploymentTarget::class.sealedSubclasses.count())) {
        0 -> ModelDeploymentTarget.Desktop
        1 -> ModelDeploymentTarget.Coral(nextDouble(0.0, 1.0))
        else -> error("Missing a ModelDeploymentTarget case.")
    }

fun Random.nextPlugin(): Plugin {
    val data = RandomStringUtils.randomAlphanumeric(5)
    return if (nextBoolean()) {
        Plugin.Official("Official $data", """print("Hello from official $data")""")
    } else {
        Plugin.Unofficial("Unofficial $data", """print("Hello from unofficial $data")""")
    }
}

fun nextExampleModel() = ExampleModel(
    name = RandomStringUtils.randomAlphanumeric(10),
    fileName = "${RandomStringUtils.randomAlphanumeric(10)}.h5",
    url = "https://${RandomStringUtils.randomAlphanumeric(10)}",
    description = RandomStringUtils.randomAlphanumeric(10),
    freezeLayers = emptyMap()
)

fun Random.nextFilePath(): FilePath =
    when (nextInt(FilePath::class.sealedSubclasses.count())) {
        0 -> FilePath.S3(RandomStringUtils.randomAlphanumeric(10))
        1 -> FilePath.Local(RandomStringUtils.randomAlphanumeric(10))
        else -> error("Missing a FilePath case.")
    }

fun Random.nextModelSource(): ModelSource =
    when (nextInt(ModelSource::class.sealedSubclasses.count())) {
        0 -> ModelSource.FromExample(nextExampleModel())
        1 -> ModelSource.FromFile(nextFilePath())
        else -> error("Missing a ModelSource case.")
    }

fun Random.nextJob(
    jobDb: JobDb,
    name: String = RandomStringUtils.randomAlphanumeric(10),
    status: TrainingScriptProgress = nextTrainingScriptProgress(),
    userOldModelPath: ModelSource = nextModelSource(),
    userDataset: Dataset = nextDataset(),
    userOptimizer: Optimizer = Optimizer.Adam(
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextBoolean()
    ),
    userLoss: Loss = Loss.SparseCategoricalCrossentropy,
    userMetrics: Set<String> = setOf(
        RandomStringUtils.randomAlphanumeric(10),
        RandomStringUtils.randomAlphanumeric(10)
    ),
    userEpochs: Int = nextInt(1, Int.MAX_VALUE),
    userNewModel: Model = Model.Sequential(
        RandomStringUtils.randomAlphanumeric(10),
        (1..3).map { nextInt(128) },
        setOf(
            Layer.Dense(RandomStringUtils.randomAlphanumeric(10), null, 10).isTrainable(),
            Layer.Conv2D(
                RandomStringUtils.randomAlphanumeric(10),
                null,
                9,
                SerializableTuple2II(3, 3),
                Activation.SoftMax
            ).isTrainable(),
            Layer.AveragePooling2D(RandomStringUtils.randomAlphanumeric(10), null).untrainable()
        )
    ),
    userNewModelPath: String = getOutputModelName(
        userOldModelPath.filename
    ),
    generateDebugComments: Boolean = nextBoolean(),
    trainingMethod: InternalJobTrainingMethod = nextTrainingMethod(),
    target: ModelDeploymentTarget = nextTarget(),
    datasetPlugin: Plugin = nextPlugin()
) = jobDb.create(
    name,
    status,
    userOldModelPath,
    userDataset,
    userOptimizer,
    userLoss,
    userMetrics,
    userEpochs,
    userNewModel,
    userNewModelPath,
    generateDebugComments,
    trainingMethod,
    target,
    datasetPlugin
)
