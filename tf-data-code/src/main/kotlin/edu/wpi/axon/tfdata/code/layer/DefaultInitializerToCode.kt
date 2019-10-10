package edu.wpi.axon.tfdata.code.layer

import arrow.core.right
import edu.wpi.axon.tfdata.code.namedArguments
import edu.wpi.axon.tfdata.layer.Initializer

class DefaultInitializerToCode : InitializerToCode {

    override fun makeNewInitializer(initializer: Initializer) = when (initializer) {
        is Initializer.Constant -> makeNewInitializer(
            "Constant",
            listOf("value" to initializer.value)
        ).right()

        is Initializer.Identity -> makeNewInitializer(
            "Identity",
            listOf("gain" to initializer.gain)
        ).right()

        Initializer.Normal -> TODO()

        Initializer.Ones -> makeNewInitializer("Ones", emptyList()).right()

        is Initializer.Orthogonal -> makeNewInitializer(
            "Orthogonal",
            listOf(
                "gain" to initializer.gain,
                "seed" to initializer.seed
            )
        ).right()

        is Initializer.RandomNormal -> makeNewInitializer(
            "RandomNormal",
            listOf(
                "mean" to initializer.mean,
                "stddev" to initializer.stddev,
                "seed" to initializer.seed
            )
        ).right()

        is Initializer.RandomUniform -> makeNewInitializer(
            "RandomUniform",
            listOf(
                "minval" to initializer.minVal,
                "maxval" to initializer.maxVal,
                "seed" to initializer.seed
            )
        ).right()

        Initializer.TruncatedNormal -> TODO()

        Initializer.Uniform -> TODO()

        Initializer.VarianceScaling -> TODO()

        Initializer.Zeros -> makeNewInitializer("Zeros", emptyList()).right()

        is Initializer.GlorotNormal -> makeNewInitializer(
            "glorot_normal",
            listOf("seed" to initializer.seed)
        ).right()

        is Initializer.GlorotUniform -> makeNewInitializer(
            "glorot_uniform",
            listOf("seed" to initializer.seed)
        ).right()
    }

    private fun makeNewInitializer(
        className: String,
        namedArgs: List<Pair<String, Any?>>
    ) = """tf.keras.initializers.$className(${namedArguments(namedArgs)})"""
}
