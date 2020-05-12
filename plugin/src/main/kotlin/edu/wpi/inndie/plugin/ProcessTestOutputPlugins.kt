package edu.wpi.inndie.plugin

object ProcessTestOutputPlugins {

    val imageClassificationModelOutputPlugin =
        Plugin.Official(
            "Image Classification",
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
        |    with open("output/classification.txt", "w+") as f:
        |        f.write("Expected class: {}\n".format(np.argmax(expected_output)))
        |        f.write("Predicted class: {}\n".format(np.argmax(model_output)))
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

    val autoMpgRegressionOutputPlugin = Plugin.Official(
        "Auto MPG Regression",
        """
        |def process_model_output(model_input, expected_output, model_output):
        |    import numpy as np
        |    import matplotlib
        |    matplotlib.use('Agg')
        |    import matplotlib.pyplot as plt
        |
        |    test_predictions = model_output.flatten()
        |    plt.scatter(expected_output, test_predictions)
        |    plt.xlabel('True Values [MPG]')
        |    plt.ylabel('Predictions [MPG]')
        |    plt.axis('equal')
        |    plt.axis('square')
        |    plt.xlim([0,plt.xlim()[1]])
        |    plt.ylim([0,plt.ylim()[1]])
        |    _ = plt.plot([-100, 100], [-100, 100])
        |    plt.savefig("output/model_output.png")
        """.trimMargin()
    )
}
