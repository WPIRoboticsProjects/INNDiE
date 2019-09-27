package edu.wpi.axon.training

import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.DefaultLayersToGraph
import edu.wpi.axon.tflayerloader.LoadLayersFromHDF5
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import java.io.File

internal class MobilenetIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with mobilenet`() {
        startKoin {
            modules(defaultModule())
        }

        val modelName = "mobilenetv2_1.00_224.h5"
        val localModelPath = this::class.java.getResource(modelName).toURI().path
        val layers = LoadLayersFromHDF5(DefaultLayersToGraph()).load(File(localModelPath))

        layers.attempt().unsafeRunSync().shouldBeRight { model ->
            model.shouldBeInstanceOf<Model.General> {
                TrainGeneral(
                    userModelPath = localModelPath,
                    userDataset = Dataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it
                ).generateScript().shouldBeValid {
                    it.a shouldBe """
                    |import tensorflow as tf
                    |
                    |model = tf.keras.models.load_model("$modelName")
                    |
                    |var1 = tf.keras.Input(shape=(224,224,3), batch_size=None, dtype=None, sparse=False)
                    |var2 = model.get_layer("Conv1_pad")(var1)
                    |var3 = model.get_layer("Conv1")(var2)
                    |var4 = model.get_layer("bn_Conv1")(var3)
                    |var5 = model.get_layer("Conv1_relu")(var4)
                    |var6 = model.get_layer("expanded_conv_depthwise")(var5)
                    |var7 = model.get_layer("expanded_conv_depthwise_BN")(var6)
                    |var8 = model.get_layer("expanded_conv_depthwise_relu")(var7)
                    |var9 = model.get_layer("expanded_conv_project")(var8)
                    |var10 = model.get_layer("expanded_conv_project_BN")(var9)
                    |var11 = model.get_layer("block_1_expand")(var10)
                    |var12 = model.get_layer("block_1_expand_BN")(var11)
                    |var13 = model.get_layer("block_1_expand_relu")(var12)
                    |var14 = model.get_layer("block_1_pad")(var13)
                    |var15 = model.get_layer("block_1_depthwise")(var14)
                    |var16 = model.get_layer("block_1_depthwise_BN")(var15)
                    |var17 = model.get_layer("block_1_depthwise_relu")(var16)
                    |var18 = model.get_layer("block_1_project")(var17)
                    |var19 = model.get_layer("block_1_project_BN")(var18)
                    |var20 = model.get_layer("block_2_expand")(var19)
                    |var21 = model.get_layer("block_2_expand_BN")(var20)
                    |var22 = model.get_layer("block_2_expand_relu")(var21)
                    |var23 = model.get_layer("block_2_depthwise")(var22)
                    |var24 = model.get_layer("block_2_depthwise_BN")(var23)
                    |var25 = model.get_layer("block_2_depthwise_relu")(var24)
                    |var26 = model.get_layer("block_2_project")(var25)
                    |var27 = model.get_layer("block_2_project_BN")(var26)
                    |var28 = model.get_layer("block_2_add")([var19, var27])
                    |var29 = model.get_layer("block_3_expand")(var28)
                    |var30 = model.get_layer("block_3_expand_BN")(var29)
                    |var31 = model.get_layer("block_3_expand_relu")(var30)
                    |var32 = model.get_layer("block_3_pad")(var31)
                    |var33 = model.get_layer("block_3_depthwise")(var32)
                    |var34 = model.get_layer("block_3_depthwise_BN")(var33)
                    |var35 = model.get_layer("block_3_depthwise_relu")(var34)
                    |var36 = model.get_layer("block_3_project")(var35)
                    |var37 = model.get_layer("block_3_project_BN")(var36)
                    |var38 = model.get_layer("block_4_expand")(var37)
                    |var39 = model.get_layer("block_4_expand_BN")(var38)
                    |var40 = model.get_layer("block_4_expand_relu")(var39)
                    |var41 = model.get_layer("block_4_depthwise")(var40)
                    |var42 = model.get_layer("block_4_depthwise_BN")(var41)
                    |var43 = model.get_layer("block_4_depthwise_relu")(var42)
                    |var44 = model.get_layer("block_4_project")(var43)
                    |var45 = model.get_layer("block_4_project_BN")(var44)
                    |var46 = model.get_layer("block_4_add")([var37, var45])
                    |var47 = model.get_layer("block_5_expand")(var46)
                    |var48 = model.get_layer("block_5_expand_BN")(var47)
                    |var49 = model.get_layer("block_5_expand_relu")(var48)
                    |var50 = model.get_layer("block_5_depthwise")(var49)
                    |var51 = model.get_layer("block_5_depthwise_BN")(var50)
                    |var52 = model.get_layer("block_5_depthwise_relu")(var51)
                    |var53 = model.get_layer("block_5_project")(var52)
                    |var54 = model.get_layer("block_5_project_BN")(var53)
                    |var55 = model.get_layer("block_5_add")([var46, var54])
                    |var56 = model.get_layer("block_6_expand")(var55)
                    |var57 = model.get_layer("block_6_expand_BN")(var56)
                    |var58 = model.get_layer("block_6_expand_relu")(var57)
                    |var59 = model.get_layer("block_6_pad")(var58)
                    |var60 = model.get_layer("block_6_depthwise")(var59)
                    |var61 = model.get_layer("block_6_depthwise_BN")(var60)
                    |var62 = model.get_layer("block_6_depthwise_relu")(var61)
                    |var63 = model.get_layer("block_6_project")(var62)
                    |var64 = model.get_layer("block_6_project_BN")(var63)
                    |var65 = model.get_layer("block_7_expand")(var64)
                    |var66 = model.get_layer("block_7_expand_BN")(var65)
                    |var67 = model.get_layer("block_7_expand_relu")(var66)
                    |var68 = model.get_layer("block_7_depthwise")(var67)
                    |var69 = model.get_layer("block_7_depthwise_BN")(var68)
                    |var70 = model.get_layer("block_7_depthwise_relu")(var69)
                    |var71 = model.get_layer("block_7_project")(var70)
                    |var72 = model.get_layer("block_7_project_BN")(var71)
                    |var73 = model.get_layer("block_7_add")([var64, var72])
                    |var74 = model.get_layer("block_8_expand")(var73)
                    |var75 = model.get_layer("block_8_expand_BN")(var74)
                    |var76 = model.get_layer("block_8_expand_relu")(var75)
                    |var77 = model.get_layer("block_8_depthwise")(var76)
                    |var78 = model.get_layer("block_8_depthwise_BN")(var77)
                    |var79 = model.get_layer("block_8_depthwise_relu")(var78)
                    |var80 = model.get_layer("block_8_project")(var79)
                    |var81 = model.get_layer("block_8_project_BN")(var80)
                    |var82 = model.get_layer("block_8_add")([var73, var81])
                    |var83 = model.get_layer("block_9_expand")(var82)
                    |var84 = model.get_layer("block_9_expand_BN")(var83)
                    |var85 = model.get_layer("block_9_expand_relu")(var84)
                    |var86 = model.get_layer("block_9_depthwise")(var85)
                    |var87 = model.get_layer("block_9_depthwise_BN")(var86)
                    |var88 = model.get_layer("block_9_depthwise_relu")(var87)
                    |var89 = model.get_layer("block_9_project")(var88)
                    |var90 = model.get_layer("block_9_project_BN")(var89)
                    |var91 = model.get_layer("block_9_add")([var82, var90])
                    |var92 = model.get_layer("block_10_expand")(var91)
                    |var93 = model.get_layer("block_10_expand_BN")(var92)
                    |var94 = model.get_layer("block_10_expand_relu")(var93)
                    |var95 = model.get_layer("block_10_depthwise")(var94)
                    |var96 = model.get_layer("block_10_depthwise_BN")(var95)
                    |var97 = model.get_layer("block_10_depthwise_relu")(var96)
                    |var98 = model.get_layer("block_10_project")(var97)
                    |var99 = model.get_layer("block_10_project_BN")(var98)
                    |var100 = model.get_layer("block_11_expand")(var99)
                    |var101 = model.get_layer("block_11_expand_BN")(var100)
                    |var102 = model.get_layer("block_11_expand_relu")(var101)
                    |var103 = model.get_layer("block_11_depthwise")(var102)
                    |var104 = model.get_layer("block_11_depthwise_BN")(var103)
                    |var105 = model.get_layer("block_11_depthwise_relu")(var104)
                    |var106 = model.get_layer("block_11_project")(var105)
                    |var107 = model.get_layer("block_11_project_BN")(var106)
                    |var108 = model.get_layer("block_11_add")([var99, var107])
                    |var109 = model.get_layer("block_12_expand")(var108)
                    |var110 = model.get_layer("block_12_expand_BN")(var109)
                    |var111 = model.get_layer("block_12_expand_relu")(var110)
                    |var112 = model.get_layer("block_12_depthwise")(var111)
                    |var113 = model.get_layer("block_12_depthwise_BN")(var112)
                    |var114 = model.get_layer("block_12_depthwise_relu")(var113)
                    |var115 = model.get_layer("block_12_project")(var114)
                    |var116 = model.get_layer("block_12_project_BN")(var115)
                    |var117 = model.get_layer("block_12_add")([var108, var116])
                    |var118 = model.get_layer("block_13_expand")(var117)
                    |var119 = model.get_layer("block_13_expand_BN")(var118)
                    |var120 = model.get_layer("block_13_expand_relu")(var119)
                    |var121 = model.get_layer("block_13_pad")(var120)
                    |var122 = model.get_layer("block_13_depthwise")(var121)
                    |var123 = model.get_layer("block_13_depthwise_BN")(var122)
                    |var124 = model.get_layer("block_13_depthwise_relu")(var123)
                    |var125 = model.get_layer("block_13_project")(var124)
                    |var126 = model.get_layer("block_13_project_BN")(var125)
                    |var127 = model.get_layer("block_14_expand")(var126)
                    |var128 = model.get_layer("block_14_expand_BN")(var127)
                    |var129 = model.get_layer("block_14_expand_relu")(var128)
                    |var130 = model.get_layer("block_14_depthwise")(var129)
                    |var131 = model.get_layer("block_14_depthwise_BN")(var130)
                    |var132 = model.get_layer("block_14_depthwise_relu")(var131)
                    |var133 = model.get_layer("block_14_project")(var132)
                    |var134 = model.get_layer("block_14_project_BN")(var133)
                    |var135 = model.get_layer("block_14_add")([var126, var134])
                    |var136 = model.get_layer("block_15_expand")(var135)
                    |var137 = model.get_layer("block_15_expand_BN")(var136)
                    |var138 = model.get_layer("block_15_expand_relu")(var137)
                    |var139 = model.get_layer("block_15_depthwise")(var138)
                    |var140 = model.get_layer("block_15_depthwise_BN")(var139)
                    |var141 = model.get_layer("block_15_depthwise_relu")(var140)
                    |var142 = model.get_layer("block_15_project")(var141)
                    |var143 = model.get_layer("block_15_project_BN")(var142)
                    |var144 = model.get_layer("block_15_add")([var135, var143])
                    |var145 = model.get_layer("block_16_expand")(var144)
                    |var146 = model.get_layer("block_16_expand_BN")(var145)
                    |var147 = model.get_layer("block_16_expand_relu")(var146)
                    |var148 = model.get_layer("block_16_depthwise")(var147)
                    |var149 = model.get_layer("block_16_depthwise_BN")(var148)
                    |var150 = model.get_layer("block_16_depthwise_relu")(var149)
                    |var151 = model.get_layer("block_16_project")(var150)
                    |var152 = model.get_layer("block_16_project_BN")(var151)
                    |var153 = model.get_layer("Conv_1")(var152)
                    |var154 = model.get_layer("Conv_1_bn")(var153)
                    |var155 = model.get_layer("out_relu")(var154)
                    |var156 = model.get_layer("global_average_pooling2d")(var155)
                    |var157 = model.get_layer("Logits")(var156)
                    |newModelVar = tf.keras.Model(inputs=[var1], outputs=[var157])
                    |newModelVar.get_layer("Conv1_pad").trainable = True
                    |newModelVar.get_layer("Conv1").trainable = True
                    |newModelVar.get_layer("bn_Conv1").trainable = True
                    |newModelVar.get_layer("Conv1_relu").trainable = True
                    |newModelVar.get_layer("expanded_conv_depthwise").trainable = True
                    |newModelVar.get_layer("expanded_conv_depthwise_BN").trainable = True
                    |newModelVar.get_layer("expanded_conv_depthwise_relu").trainable = True
                    |newModelVar.get_layer("expanded_conv_project").trainable = True
                    |newModelVar.get_layer("expanded_conv_project_BN").trainable = True
                    |newModelVar.get_layer("block_1_expand").trainable = True
                    |newModelVar.get_layer("block_1_expand_BN").trainable = True
                    |newModelVar.get_layer("block_1_expand_relu").trainable = True
                    |newModelVar.get_layer("block_1_pad").trainable = True
                    |newModelVar.get_layer("block_1_depthwise").trainable = True
                    |newModelVar.get_layer("block_1_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_1_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_1_project").trainable = True
                    |newModelVar.get_layer("block_1_project_BN").trainable = True
                    |newModelVar.get_layer("block_2_expand").trainable = True
                    |newModelVar.get_layer("block_2_expand_BN").trainable = True
                    |newModelVar.get_layer("block_2_expand_relu").trainable = True
                    |newModelVar.get_layer("block_2_depthwise").trainable = True
                    |newModelVar.get_layer("block_2_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_2_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_2_project").trainable = True
                    |newModelVar.get_layer("block_2_project_BN").trainable = True
                    |newModelVar.get_layer("block_2_add").trainable = True
                    |newModelVar.get_layer("block_3_expand").trainable = True
                    |newModelVar.get_layer("block_3_expand_BN").trainable = True
                    |newModelVar.get_layer("block_3_expand_relu").trainable = True
                    |newModelVar.get_layer("block_3_pad").trainable = True
                    |newModelVar.get_layer("block_3_depthwise").trainable = True
                    |newModelVar.get_layer("block_3_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_3_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_3_project").trainable = True
                    |newModelVar.get_layer("block_3_project_BN").trainable = True
                    |newModelVar.get_layer("block_4_expand").trainable = True
                    |newModelVar.get_layer("block_4_expand_BN").trainable = True
                    |newModelVar.get_layer("block_4_expand_relu").trainable = True
                    |newModelVar.get_layer("block_4_depthwise").trainable = True
                    |newModelVar.get_layer("block_4_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_4_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_4_project").trainable = True
                    |newModelVar.get_layer("block_4_project_BN").trainable = True
                    |newModelVar.get_layer("block_4_add").trainable = True
                    |newModelVar.get_layer("block_5_expand").trainable = True
                    |newModelVar.get_layer("block_5_expand_BN").trainable = True
                    |newModelVar.get_layer("block_5_expand_relu").trainable = True
                    |newModelVar.get_layer("block_5_depthwise").trainable = True
                    |newModelVar.get_layer("block_5_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_5_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_5_project").trainable = True
                    |newModelVar.get_layer("block_5_project_BN").trainable = True
                    |newModelVar.get_layer("block_5_add").trainable = True
                    |newModelVar.get_layer("block_6_expand").trainable = True
                    |newModelVar.get_layer("block_6_expand_BN").trainable = True
                    |newModelVar.get_layer("block_6_expand_relu").trainable = True
                    |newModelVar.get_layer("block_6_pad").trainable = True
                    |newModelVar.get_layer("block_6_depthwise").trainable = True
                    |newModelVar.get_layer("block_6_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_6_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_6_project").trainable = True
                    |newModelVar.get_layer("block_6_project_BN").trainable = True
                    |newModelVar.get_layer("block_7_expand").trainable = True
                    |newModelVar.get_layer("block_7_expand_BN").trainable = True
                    |newModelVar.get_layer("block_7_expand_relu").trainable = True
                    |newModelVar.get_layer("block_7_depthwise").trainable = True
                    |newModelVar.get_layer("block_7_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_7_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_7_project").trainable = True
                    |newModelVar.get_layer("block_7_project_BN").trainable = True
                    |newModelVar.get_layer("block_7_add").trainable = True
                    |newModelVar.get_layer("block_8_expand").trainable = True
                    |newModelVar.get_layer("block_8_expand_BN").trainable = True
                    |newModelVar.get_layer("block_8_expand_relu").trainable = True
                    |newModelVar.get_layer("block_8_depthwise").trainable = True
                    |newModelVar.get_layer("block_8_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_8_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_8_project").trainable = True
                    |newModelVar.get_layer("block_8_project_BN").trainable = True
                    |newModelVar.get_layer("block_8_add").trainable = True
                    |newModelVar.get_layer("block_9_expand").trainable = True
                    |newModelVar.get_layer("block_9_expand_BN").trainable = True
                    |newModelVar.get_layer("block_9_expand_relu").trainable = True
                    |newModelVar.get_layer("block_9_depthwise").trainable = True
                    |newModelVar.get_layer("block_9_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_9_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_9_project").trainable = True
                    |newModelVar.get_layer("block_9_project_BN").trainable = True
                    |newModelVar.get_layer("block_9_add").trainable = True
                    |newModelVar.get_layer("block_10_expand").trainable = True
                    |newModelVar.get_layer("block_10_expand_BN").trainable = True
                    |newModelVar.get_layer("block_10_expand_relu").trainable = True
                    |newModelVar.get_layer("block_10_depthwise").trainable = True
                    |newModelVar.get_layer("block_10_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_10_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_10_project").trainable = True
                    |newModelVar.get_layer("block_10_project_BN").trainable = True
                    |newModelVar.get_layer("block_11_expand").trainable = True
                    |newModelVar.get_layer("block_11_expand_BN").trainable = True
                    |newModelVar.get_layer("block_11_expand_relu").trainable = True
                    |newModelVar.get_layer("block_11_depthwise").trainable = True
                    |newModelVar.get_layer("block_11_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_11_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_11_project").trainable = True
                    |newModelVar.get_layer("block_11_project_BN").trainable = True
                    |newModelVar.get_layer("block_11_add").trainable = True
                    |newModelVar.get_layer("block_12_expand").trainable = True
                    |newModelVar.get_layer("block_12_expand_BN").trainable = True
                    |newModelVar.get_layer("block_12_expand_relu").trainable = True
                    |newModelVar.get_layer("block_12_depthwise").trainable = True
                    |newModelVar.get_layer("block_12_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_12_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_12_project").trainable = True
                    |newModelVar.get_layer("block_12_project_BN").trainable = True
                    |newModelVar.get_layer("block_12_add").trainable = True
                    |newModelVar.get_layer("block_13_expand").trainable = True
                    |newModelVar.get_layer("block_13_expand_BN").trainable = True
                    |newModelVar.get_layer("block_13_expand_relu").trainable = True
                    |newModelVar.get_layer("block_13_pad").trainable = True
                    |newModelVar.get_layer("block_13_depthwise").trainable = True
                    |newModelVar.get_layer("block_13_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_13_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_13_project").trainable = True
                    |newModelVar.get_layer("block_13_project_BN").trainable = True
                    |newModelVar.get_layer("block_14_expand").trainable = True
                    |newModelVar.get_layer("block_14_expand_BN").trainable = True
                    |newModelVar.get_layer("block_14_expand_relu").trainable = True
                    |newModelVar.get_layer("block_14_depthwise").trainable = True
                    |newModelVar.get_layer("block_14_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_14_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_14_project").trainable = True
                    |newModelVar.get_layer("block_14_project_BN").trainable = True
                    |newModelVar.get_layer("block_14_add").trainable = True
                    |newModelVar.get_layer("block_15_expand").trainable = True
                    |newModelVar.get_layer("block_15_expand_BN").trainable = True
                    |newModelVar.get_layer("block_15_expand_relu").trainable = True
                    |newModelVar.get_layer("block_15_depthwise").trainable = True
                    |newModelVar.get_layer("block_15_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_15_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_15_project").trainable = True
                    |newModelVar.get_layer("block_15_project_BN").trainable = True
                    |newModelVar.get_layer("block_15_add").trainable = True
                    |newModelVar.get_layer("block_16_expand").trainable = True
                    |newModelVar.get_layer("block_16_expand_BN").trainable = True
                    |newModelVar.get_layer("block_16_expand_relu").trainable = True
                    |newModelVar.get_layer("block_16_depthwise").trainable = True
                    |newModelVar.get_layer("block_16_depthwise_BN").trainable = True
                    |newModelVar.get_layer("block_16_depthwise_relu").trainable = True
                    |newModelVar.get_layer("block_16_project").trainable = True
                    |newModelVar.get_layer("block_16_project_BN").trainable = True
                    |newModelVar.get_layer("Conv_1").trainable = True
                    |newModelVar.get_layer("Conv_1_bn").trainable = True
                    |newModelVar.get_layer("out_relu").trainable = True
                    |newModelVar.get_layer("global_average_pooling2d").trainable = True
                    |newModelVar.get_layer("Logits").trainable = True
                    |
                    |checkpointCallback = tf.keras.callbacks.ModelCheckpoint(
                    |    "mobilenetv2_1.00_224-weights.{epoch:02d}-{val_loss:.2f}.hdf5",
                    |    monitor="val_loss",
                    |    verbose=1,
                    |    save_best_only=False,
                    |    save_weights_only=True,
                    |    mode="auto",
                    |    save_freq="epoch",
                    |    load_weights_on_restart=False
                    |)
                    |
                    |newModelVar.compile(
                    |    optimizer=tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False),
                    |    loss=tf.keras.losses.sparse_categorical_crossentropy,
                    |    metrics=["accuracy"]
                    |)
                    |
                    |earlyStoppingCallback = tf.keras.callbacks.EarlyStopping(
                    |    monitor="val_loss",
                    |    min_delta=0,
                    |    patience=10,
                    |    verbose=1,
                    |    mode="auto",
                    |    baseline=None,
                    |    restore_best_weights=False
                    |)
                    |
                    |(xTrain, yTrain), (xTest, yTest) = tf.keras.datasets.mnist.load_data()
                    |
                    |newModelVar.fit(
                    |    xTrain,
                    |    yTrain,
                    |    batch_size=None,
                    |    epochs=50,
                    |    verbose=2,
                    |    callbacks=[checkpointCallback, earlyStoppingCallback],
                    |    validation_data=(xTest, yTest),
                    |    shuffle=True
                    |)
                    """.trimMargin()
                }
            }
        }
    }
}
