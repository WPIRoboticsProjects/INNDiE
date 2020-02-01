package edu.wpi.axon.tfdata.code.layer

import arrow.core.Either
import arrow.core.Right
import arrow.core.left
import arrow.core.right
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.SerializableEitherITii
import edu.wpi.axon.tfdata.SerializableTuple2II
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Constraint
import edu.wpi.axon.tfdata.layer.DataFormat
import edu.wpi.axon.tfdata.layer.Initializer
import edu.wpi.axon.tfdata.layer.Interpolation
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
        @Suppress("unused", "LongMethod", "StringLiteralDuplication")
        fun layerSource() = listOf(
            Arguments.of(
                Layer.Dense("name", null, 3, Activation.ReLu),
                ("tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, " +
                    "name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.Dense("name", setOf("input_name"), 3, Activation.ReLu),
                ("tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, " +
                    "name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.Dense("name", null, 3, Activation.ReLu).trainable(),
                ("tf.keras.layers.Dense(units=3, activation=tf.keras.activations.relu, " +
                    "name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.InputLayer("name", listOf(3), 4, null, true),
                """tf.keras.Input(shape=(3,), batch_size=4, dtype=None, sparse=True)""".right(),
                null
            ),
            Arguments.of(
                Layer.InputLayer("name", listOf(224, 224, 3), null, null, false),
                ("tf.keras.Input(shape=(224,224,3), batch_size=None, dtype=None, " +
                    "sparse=False)").right(),
                null
            ),
            // Validating the first null is not in the shape for an InputLayer should be handled at
            // a higher level. This test ensures that this level does not try to handle it.
            Arguments.of(
                Layer.InputLayer("name", listOf(null, 224, 224, 3), null, null, false),
                ("tf.keras.Input(shape=(None,224,224,3), batch_size=None, " +
                    "dtype=None, sparse=False)").right(),
                null
            ),
            Arguments.of(
                Layer.Dropout("name", setOf("in1"), 0.2),
                ("tf.keras.layers.Dropout(0.2, noise_shape=None, seed=None, " +
                    "name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.Dropout("name", setOf("in1"), 0.2, listOf(1, 2, 3), 2),
                ("tf.keras.layers.Dropout(0.2, noise_shape=(1,2,3), seed=2, " +
                    "name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.UnknownLayer("", null),
                """Cannot construct an UnknownLayer: UnknownLayer(name=, inputs=null)""".left(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    null,
                    SerializableEitherITii.Left(1),
                    SerializableEitherITii.Left(2),
                    PoolingPadding.Valid,
                    null
                ),
                ("tf.keras.layers.MaxPooling2D(pool_size=1, strides=2, padding=\"valid\", " +
                    "data_format=None, name=\"name\")").right(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    null,
                    SerializableEitherITii.Left(1),
                    null,
                    PoolingPadding.Same,
                    DataFormat.ChannelsLast
                ),
                ("tf.keras.layers.MaxPooling2D(pool_size=1, strides=None, " +
                    """padding="same", data_format="channels_last", name="name")""").right(),
                null
            ),
            Arguments.of(
                Layer.MaxPooling2D(
                    "name",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(1, 2)),
                    SerializableEitherITii.Right(SerializableTuple2II(3, 4)),
                    PoolingPadding.Valid,
                    DataFormat.ChannelsFirst
                ),
                ("tf.keras.layers.MaxPooling2D(pool_size=(1, 2), strides=(3, 4), " +
                    """padding="valid", data_format="channels_first", name="name")""").right(),
                null
            ),
            Arguments.of(
                Layer.BatchNormalization(
                    name = "name",
                    inputs = null,
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
                    "moving_mean_initializer=1, moving_variance_initializer=1, " +
                    "beta_regularizer=2, gamma_regularizer=2, beta_constraint=3, " +
                    "gamma_constraint=3, renorm=False, renorm_clipping=None, " +
                    "renorm_momentum=0.99, fused=None, virtual_batch_size=None, " +
                    "adjustment=None, name=\"name\")").right(),
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
                    null,
                    DataFormat.ChannelsFirst
                ),
                """tf.keras.layers.Flatten(data_format="channels_first", name="name")""".right(),
                null
            ),
            Arguments.of(
                Layer.AveragePooling2D(
                    "name",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    SerializableEitherITii.Left(3),
                    PoolingPadding.Valid,
                    DataFormat.ChannelsLast
                ),
                Right(
                    "tf.keras.layers.AvgPool2D(pool_size=(2, 2), strides=3, padding=\"valid\", " +
                        """data_format="channels_last", name="name")"""
                ),
                null
            ),
            Arguments.of(
                Layer.GlobalMaxPooling2D(
                    "name",
                    null,
                    DataFormat.ChannelsFirst
                ),
                Right(
                    "tf.keras.layers.GlobalMaxPooling2D(data_format=\"channels_first\"," +
                        " name=\"name\")"
                ),
                null
            ),
            Arguments.of(
                Layer.SpatialDropout2D(
                    "name",
                    null,
                    0.2,
                    null
                ),
                Right("""tf.keras.layers.SpatialDropout2D(0.2, data_format=None, name="name")"""),
                null
            ),
            Arguments.of(
                Layer.UpSampling2D(
                    "name",
                    null,
                    SerializableEitherITii.Right(SerializableTuple2II(2, 2)),
                    null,
                    Interpolation.Nearest
                ),
                Right(
                    "tf.keras.layers.UpSampling2D(size=(2, 2), data_format=None, " +
                        "interpolation=\"nearest\", name=\"name\")"
                ),
                null
            ),
            Arguments.of(
                Layer.GlobalAveragePooling2D(
                    "name",
                    null,
                    DataFormat.ChannelsLast
                ),
                Right(
                    "tf.keras.layers.GlobalAveragePooling2D(data_format=\"channels_last\", " +
                        "name=\"name\")"
                ),
                null
            ),
            Arguments.of(
                Layer.Conv2D(
                    "name",
                    null,
                    32,
                    SerializableTuple2II(3, 3),
                    Activation.Linear
                ),
                Right(
                    "tf.keras.layers.Conv2D(32, (3, 3), activation=tf.keras.activations.linear, name=\"name\")"
                ),
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
