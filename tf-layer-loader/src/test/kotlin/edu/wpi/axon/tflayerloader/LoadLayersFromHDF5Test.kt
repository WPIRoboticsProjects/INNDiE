package edu.wpi.axon.tflayerloader

import arrow.core.Tuple2
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.SealedLayer
import edu.wpi.axon.tfdata.layer.trainable
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

internal class LoadLayersFromHDF5Test {

    /*
    {"class_name": "Sequential",
             "config": {"name": "sequential_11",
              "layers": [{"class_name": "Conv2D",
                "config": {"name": "conv2d_16",
                 "trainable": true,
                 "batch_input_shape": [null, 28, 28, 1],
                 "dtype": "float32",
                 "filters": 32,
                 "kernel_size": [3, 3],
                 "strides": [1, 1],
                 "padding": "valid",
                 "data_format": "channels_last",
                 "dilation_rate": [1, 1],
                 "activation": "relu",
                 "use_bias": true,
                 "kernel_initializer": {"class_name": "GlorotUniform",
                  "config": {"seed": null, "dtype": "float32"}},
                 "bias_initializer": {"class_name": "Zeros",
                  "config": {"dtype": "float32"}},
                 "kernel_regularizer": null,
                 "bias_regularizer": null,
                 "activity_regularizer": null,
                 "kernel_constraint": null,
                 "bias_constraint": null}},
               {"class_name": "Conv2D",
                "config": {"name": "conv2d_17",
                 "trainable": true,
                 "dtype": "float32",
                 "filters": 64,
                 "kernel_size": [3, 3],
                 "strides": [1, 1],
                 "padding": "valid",
                 "data_format": "channels_last",
                 "dilation_rate": [1, 1],
                 "activation": "relu",
                 "use_bias": true,
                 "kernel_initializer": {"class_name": "GlorotUniform",
                  "config": {"seed": null, "dtype": "float32"}},
                 "bias_initializer": {"class_name": "Zeros",
                  "config": {"dtype": "float32"}},
                 "kernel_regularizer": null,
                 "bias_regularizer": null,
                 "activity_regularizer": null,
                 "kernel_constraint": null,
                 "bias_constraint": null}},
               {"class_name": "MaxPooling2D",
                "config": {"name": "max_pooling2d_8",
                 "trainable": true,
                 "dtype": "float32",
                 "pool_size": [2, 2],
                 "padding": "valid",
                 "strides": [2, 2],
                 "data_format": "channels_last"}},
               {"class_name": "Dropout",
                "config": {"name": "dropout_19",
                 "trainable": true,
                 "dtype": "float32",
                 "rate": 0.25,
                 "noise_shape": null,
                 "seed": null}},
               {"class_name": "Flatten",
                "config": {"name": "flatten_8",
                 "trainable": true,
                 "dtype": "float32",
                 "data_format": "channels_last"}},
               {"class_name": "Dense",
                "config": {"name": "dense_22",
                 "trainable": true,
                 "dtype": "float32",
                 "units": 128,
                 "activation": "relu",
                 "use_bias": true,
                 "kernel_initializer": {"class_name": "GlorotUniform",
                  "config": {"seed": null, "dtype": "float32"}},
                 "bias_initializer": {"class_name": "Zeros",
                  "config": {"dtype": "float32"}},
                 "kernel_regularizer": null,
                 "bias_regularizer": null,
                 "activity_regularizer": null,
                 "kernel_constraint": null,
                 "bias_constraint": null}},
               {"class_name": "Dropout",
                "config": {"name": "dropout_20",
                 "trainable": true,
                 "dtype": "float32",
                 "rate": 0.5,
                 "noise_shape": null,
                 "seed": null}},
               {"class_name": "Dense",
                "config": {"name": "dense_23",
                 "trainable": true,
                 "dtype": "float32",
                 "units": 10,
                 "activation": "softmax",
                 "use_bias": true,
                 "kernel_initializer": {"class_name": "GlorotUniform",
                  "config": {"seed": null, "dtype": "float32"}},
                 "bias_initializer": {"class_name": "Zeros",
                  "config": {"dtype": "float32"}},
                 "kernel_regularizer": null,
                 "bias_regularizer": null,
                 "activity_regularizer": null,
                 "kernel_constraint": null,
                 "bias_constraint": null}}]}}
     */

    @Test
    fun `load from test file 1`() {
        LoadLayersFromHDF5().load(
            File(LoadLayersFromHDF5Test::class.java.getResource("model1.h5").toURI())
        ).shouldBeInstanceOf<Model.Sequential> {
            it.name shouldBe "sequential_11"
            it.batchInputShape.shouldContainExactly(null, 28, 28, 1)
            it.layers.shouldContainExactly(
                setOf(
                    SealedLayer.Conv2D("conv2d_16", 32, Tuple2(3, 3), Activation.ReLu).trainable(),
                    SealedLayer.Conv2D("conv2d_17", 64, Tuple2(3, 3), Activation.ReLu).trainable(),
                    SealedLayer.UnknownLayer("max_pooling2d_8").trainable(),
                    SealedLayer.UnknownLayer("dropout_19").trainable(),
                    SealedLayer.UnknownLayer("flatten_8").trainable(),
                    SealedLayer.Dense("dense_22", 128, Activation.ReLu).trainable(),
                    SealedLayer.UnknownLayer("dropout_20").trainable(),
                    SealedLayer.Dense("dense_23", 10, Activation.SoftMax).trainable()
                )
            )
        }
    }

    @Test
    fun `load from test file 2`() {
        LoadLayersFromHDF5().load(
            File(LoadLayersFromHDF5Test::class.java.getResource("model2.h5").toURI())
        ).also { println(it) }.shouldBeInstanceOf<Model.Sequential> {
            it.name shouldBe "mobilenetv2_1.00_224"
            it.batchInputShape.shouldContainExactly(null, 224, 224, 3)
            it.layers.shouldHaveSize(157)
        }
    }
}
