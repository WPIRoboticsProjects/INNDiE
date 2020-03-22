package edu.wpi.axon.plugin

object ProcessTestOutputPlugins {

    val serializeModelOutputPlugin = Plugin.Official(
        "Serialize Model Output",
        """
        |def process_model_output(model_input, model_output):
        |    import json
        |    with open("output/model_output.json", "w+") as f:
        |        json.dump(model_output, f)
        """.trimMargin()
    )
}
