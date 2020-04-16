package edu.wpi.axon.plugin

object ProcessTestOutputPlugins {

    val serializeModelOutputPlugin = Plugin.Official(
        "Basic View",
        """
        |def process_model_output(model_input, expected_output, model_output):
        |    import numpy as np
        |
        |    with open("output/expected_output.txt", "w+") as f:
        |        np.savetxt(f, expected_output)
        |
        |    with open("output/model_output.txt", "w+") as f:
        |        np.savetxt(f, model_output)
        |
        |    images = tf.image.encode_jpeg(tf.cast(model_input[0]*255, tf.uint8))
        |    fwrite = tf.write_file(
        |        tf.constant("output/model_input.jpeg"),
        |        images
        |    )
        |    with tf.Session() as session:
        |        session.run(fwrite)
        """.trimMargin()
    )
}
