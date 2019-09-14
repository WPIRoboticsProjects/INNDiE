package edu.wpi.axon.tflayerloader

import edu.wpi.axon.tflayers.Activation
import edu.wpi.axon.tflayers.SealedLayer
import io.kotlintest.matchers.equality.shouldBeEqualToUsingFields
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
            File(LoadLayersFromHDF5Test::class.java.getResource("saved_tf_model.h5").toURI())
        ).shouldBeEqualToUsingFields(
            setOf(
                SealedLayer.UnknownLayer("conv2d_16"),
                SealedLayer.UnknownLayer("conv2d_17"),
                SealedLayer.UnknownLayer("max_pooling2d_8"),
                SealedLayer.UnknownLayer("dropout_19"),
                SealedLayer.UnknownLayer("flatten_8"),
                SealedLayer.Dense("dense_22", 128, Activation.ReLu),
                SealedLayer.UnknownLayer("dropout_20"),
                SealedLayer.Dense("dense_23", 10, Activation.SoftMax)
            )
        )
    }
}
