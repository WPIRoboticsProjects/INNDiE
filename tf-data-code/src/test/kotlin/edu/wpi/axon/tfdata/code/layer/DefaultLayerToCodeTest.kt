package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.Left
import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import arrow.core.Tuple2
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Constraint
import edu.wpi.axon.tfdata.layer.DataFormat
import edu.wpi.axon.tfdata.layer.Initializer
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.layer.PoolingPadding
import edu.wpi.axon.tfdata.layer.Regularizer
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

internal class DefaultLayerToCodeTest : KoinTestFixture() {

    @ParameterizedTest
    @MethodSource("layerSource")
    fun `test layers`(layer: Layer, expected: Either<String, String>, module: Module?) {
        startKoin {
            module?.let { modules(it) }
        }

        DefaultLayerToCode().makeNewLayer(layer) shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("activationSource")
    fun `test activations`(activation: Activation, expected: String) {
        startKoin {}
        DefaultLayerToCode().makeNewActivation(activation) shouldBe expected
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun layerSource() = listOf(
            Arguments.of(
                Layer.Dense("name", None, 3, Activation.ReLu),
                """tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.Dense("name", Some(setOf("input_name")), 3, Activation.ReLu),
                """tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.Dense("name", None, 3, Activation.ReLu).trainable(),
                """tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.InputLayer("name", listOf(3), 4, null, true),
                """tf.keras.Input(shape=(3,), batch_size=4, dtype=None, sparse=True)""".right(),
                null
            ),
            Arguments.of(
                Layer.InputLayer("name", listOf(224, 224, 3), null, null, false),
                """tf.keras.Input(shape=(224,224,3), batch_size=None, dtype=None, sparse=False)""".right(),
                null
            ),
            Arguments.of(
                Layer.Dropout("name", Some(setOf("in1")), 0.2),
                """tf.keras.layers.Dropout(0.2, noise_shape=None, seed=None, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.Dropout("name", Some(setOf("in1")), 0.2, listOf(1, 2, 3), 2),
                """tf.keras.layers.Dropout(0.2, noise_shape=(1,2,3), seed=2, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.UnknownLayer("", None),
                """Cannot construct an unknown layer: UnknownLayer(name=, inputs=None)""".left(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    None,
                    Left(1),
                    Left(2),
                    PoolingPadding.Valid,
                    null
                ),
                """tf.keras.layers.MaxPooling2D(pool_size=1, strides=2, padding="valid", data_format=None, name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    None,
                    Left(1),
                    null,
                    PoolingPadding.Same,
                    DataFormat.ChannelsLast
                ),
                """tf.keras.layers.MaxPooling2D(pool_size=1, strides=None, padding="same", data_format="channels_last", name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    None,
                    Right(Tuple2(1, 2)),
                    Right(Tuple2(3, 4)),
                    PoolingPadding.Valid,
                    DataFormat.ChannelsFirst
                ),
                """tf.keras.layers.MaxPooling2D(pool_size=(1, 2), strides=(3, 4), padding="valid", data_format="channels_first", name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.BatchNormalization(
                    name = "name",
                    inputs = None,
                    axis = -1,
                    momentum = 0.99,
                    epsilon = 0.001,
                    center = true,
                    scale = true,
                    betaInitializer = Initializer.Zeros,
                    gammaInitializer = Initializer.Ones,
                    movingMeanInitializer = Initializer.Zeros,
                    movingVarianceInitializer = Initializer.Ones,
                    betaRegularizer = Regularizer.L1L2(),
                    gammaRegularizer = Regularizer.L1L2(),
                    betaConstraint = Constraint.NonNeg,
                    gammaConstraint = Constraint.NonNeg,
                    renorm = false,
                    renormClipping = null,
                    renormMomentum = 0.99,
                    fused = null,
                    virtualBatchSize = null
                ),
                ("tf.keras.layers.BatchNormalization(axis=-1, momentum=0.99, epsilon=0.001, " +
                    "center=True, scale=True, beta_initializer=1, gamma_initializer=1, " +
                    "moving_mean_initializer=1, moving_variance_initializer=1, beta_regularizer=2, " +
                    "gamma_regularizer=2, beta_constraint=3, gamma_constraint=3, renorm=False, " +
                    "renorm_clipping=None, renorm_momentum=0.99, fused=None, " +
                    """virtual_batch_size=None, adjustment=None, name="name")""").right(),
                module {
                    single {
                        mockk<ConstraintToCode> {
                            every { makeNewConstraint(any()) } returns Right("3")
                        }
                    }

                    single {
                        mockk<InitializerToCode> {
                            every { makeNewInitializer(any()) } returns Right("1")
                        }
                    }

                    single {
                        mockk<RegularizerToCode> {
                            every { makeNewRegularizer(any()) } returns Right("2")
                        }
                    }
                }
            ),
            Arguments.of(
                Layer.Flatten(
                    "name",
                    None,
                    DataFormat.ChannelsFirst
                ),
                ("""tf.keras.layers.Flatten(data_format="channels_first", name="name")""").right(),
                null
            ),
            Arguments.of(
                Layer.AveragePooling2D(
                    "name",
                    None,
                    Right(Tuple2(2, 2)),
                    Left(3),
                    PoolingPadding.Valid,
                    DataFormat.ChannelsLast
                ),
                Right("""tf.keras.layers.AvgPool2D(pool_size=(2, 2), strides=3, padding="valid", data_format="channels_last", name="name")"""),
                null
            )
        )

        @JvmStatic
        @Suppress("unused")
        fun activationSource() = listOf(
            Arguments.of(Activation.ReLu, "tf.keras.activations.relu"),
            Arguments.of(Activation.SoftMax, "tf.keras.activations.softmax")
        )
    }
}
