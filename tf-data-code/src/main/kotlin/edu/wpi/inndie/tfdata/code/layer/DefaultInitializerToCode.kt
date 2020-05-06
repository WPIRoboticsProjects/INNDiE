package edu.wpi.inndie.tfdata.code.layer

import arrow.core.right
import edu.wpi.inndie.tfdata.code.namedArguments
import edu.wpi.inndie.tfdata.layer.Initializer

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

        is Initializer.TruncatedNormal -> makeNewInitializer(
            "TruncatedNormal",
            listOf(
                "mean" to initializer.mean,
                "stddev" to initializer.stddev,
                "seed" to initializer.seed
            )
        ).right()

        is Initializer.VarianceScaling -> makeNewInitializer(
            "VarianceScaling",
            listOf(
                "scale" to initializer.scale,
                "mode" to initializer.mode.value,
                "distribution" to initializer.distribution.value,
                "seed" to initializer.seed
            )
        ).right()

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
