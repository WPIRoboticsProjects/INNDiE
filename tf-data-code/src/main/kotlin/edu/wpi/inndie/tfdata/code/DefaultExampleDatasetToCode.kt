package edu.wpi.inndie.tfdata.code

import edu.wpi.axon.tfdata.Dataset

class DefaultExampleDatasetToCode : ExampleDatasetToCode {

    override fun datasetToCode(dataset: Dataset.ExampleDataset) = when (dataset) {
        Dataset.ExampleDataset.BostonHousing, Dataset.ExampleDataset.Cifar10,
        Dataset.ExampleDataset.Cifar100, Dataset.ExampleDataset.FashionMnist,
        Dataset.ExampleDataset.IMDB, Dataset.ExampleDataset.Mnist,
        Dataset.ExampleDataset.Reuters -> "    return tf.keras.datasets.${dataset.name}.load_data()"

        Dataset.ExampleDataset.AutoMPG -> """
            |    import pandas as pd
            |
            |    dataset_path = tf.keras.utils.get_file("auto-mpg.data", "https://archive.ics.uci.edu/ml/machine-learning-databases/auto-mpg/auto-mpg.data")
            |
            |    column_names = ['MPG','Cylinders','Displacement','Horsepower','Weight',
            |                    'Acceleration', 'Model Year', 'Origin']
            |    raw_dataset = pd.read_csv(dataset_path, names=column_names,
            |                          na_values = "?", comment='\t',
            |                          sep=" ", skipinitialspace=True)
            |
            |    dataset = raw_dataset.copy()
            |    dataset = dataset.dropna()
            |    origin = dataset.pop('Origin')
            |    dataset['USA'] = (origin == 1)*1.0
            |    dataset['Europe'] = (origin == 2)*1.0
            |    dataset['Japan'] = (origin == 3)*1.0
            |
            |    train_dataset = dataset.sample(frac=0.8,random_state=0)
            |    test_dataset = dataset.drop(train_dataset.index)
            |
            |    train_stats = train_dataset.describe()
            |    train_stats.pop("MPG")
            |    train_stats = train_stats.transpose()
            |
            |    train_labels = train_dataset.pop('MPG')
            |    test_labels = test_dataset.pop('MPG')
            |
            |    def norm(x):
            |        return (x - train_stats['mean']) / train_stats['std']
            |    normed_train_data = norm(train_dataset)
            |    normed_test_data = norm(test_dataset)
            |
            |    return (normed_train_data, train_labels), (normed_test_data, test_labels)
        """.trimMargin()
    }
}
