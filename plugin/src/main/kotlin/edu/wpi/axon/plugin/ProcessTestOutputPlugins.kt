package edu.wpi.axon.plugin

object ProcessTestOutputPlugins {

    val serializeModelOutputPlugin = Plugin.Official(
        "Serialize Model Output",
        """
        |def process_model_output(model_input, expected_output, model_output):
        |    # import json
        |    import numpy as np
        |    with open("output/expected_output.txt", "w+") as f:
        |        # json.dump(expected_output, f)
        |        np.savetxt(f, expected_output)
        |    with open("output/model_output.txt", "w+") as f:
        |        # json.dump(model_output, f)
        |        np.savetxt(f, model_output)
        """.trimMargin()
    )
}
