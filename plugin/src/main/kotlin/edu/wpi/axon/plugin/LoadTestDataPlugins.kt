package edu.wpi.axon.plugin

object LoadTestDataPlugins {

    private val processMnistTypeDataset = """
        |            x_test = x_test[:1]
        |            x_test = tf.cast(x_test / 255, tf.float32)
        |            x_test = x_test[..., tf.newaxis]
        |            y_test = tf.keras.utils.to_categorical(y_test)
        |            return (x_test, y_test[:1], 1)
    """.trimMargin()

    // TODO: Don't hardcode which element to return
    val loadExampleDatasetPlugin = Plugin.Official(
        "Load an Example Dataset",
        """
        |def load_test_data(input):
        |    import json
        |    loaded_json = json.loads(input)
        |
        |    try:
        |        type = loaded_json["example_dataset"]
        |        if type == "boston_housing":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.boston_housing.load_data()
        |$processMnistTypeDataset
        |        elif type == "cifar10":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.cifar10.load_data()
        |$processMnistTypeDataset
        |        elif type == "cifar100":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.cifar100.load_data()
        |$processMnistTypeDataset
        |        elif type == "fashion_mnist":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.fashion_mnist.load_data()
        |$processMnistTypeDataset
        |        elif type == "imdb":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.imdb.load_data()
        |$processMnistTypeDataset
        |        elif type == "mnist":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
        |$processMnistTypeDataset
        |        elif type == "reuters":
        |            (x_train, y_train), (x_test, y_test) = tf.keras.datasets.reuters.load_data()
        |$processMnistTypeDataset
        |        elif type == "auto-mpg":
        |            import pandas as pd
        |
        |            dataset_path = tf.keras.utils.get_file("auto-mpg.data", "https://archive.ics.uci.edu/ml/machine-learning-databases/auto-mpg/auto-mpg.data")
        |
        |            column_names = ['MPG','Cylinders','Displacement','Horsepower','Weight',
        |                            'Acceleration', 'Model Year', 'Origin']
        |            raw_dataset = pd.read_csv(dataset_path, names=column_names,
        |                                  na_values = "?", comment='\t',
        |                                  sep=" ", skipinitialspace=True)
        |
        |            dataset = raw_dataset.copy()
        |            dataset = dataset.dropna()
        |            origin = dataset.pop('Origin')
        |            dataset['USA'] = (origin == 1)*1.0
        |            dataset['Europe'] = (origin == 2)*1.0
        |            dataset['Japan'] = (origin == 3)*1.0
        |
        |            train_dataset = dataset.sample(frac=0.8,random_state=0)
        |            test_dataset = dataset.drop(train_dataset.index)
        |
        |            train_stats = train_dataset.describe()
        |            train_stats.pop("MPG")
        |            train_stats = train_stats.transpose()
        |
        |            train_labels = train_dataset.pop('MPG')
        |            test_labels = test_dataset.pop('MPG')
        |
        |            def norm(x):
        |                return (x - train_stats['mean']) / train_stats['std']
        |            normed_train_data = norm(train_dataset)
        |            normed_test_data = norm(test_dataset)
        |
        |            x_test = normed_test_data
        |            y_test = test_labels
        |
        |            return (x_test, y_test, None)
        |        else:
        |            raise RuntimeError("Cannot load the dataset.")
        |    except KeyError:
        |        raise RuntimeError("Cannot load the dataset.")
        """.trimMargin()
    )
}
