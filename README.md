[![Build Status](https://dev.azure.com/wpilib/DesktopTools/_apis/build/status/wpilibsuite.Axon?branchName=master)](https://dev.azure.com/wpilib/DesktopTools/_build/latest?definitionId=34&branchName=master)
[![Github Releases](https://img.shields.io/github/downloads/wpilibsuite/Axon/total.svg)](https://github.com/wpilibsuite/Axon/releases/latest)
[![codecov](https://codecov.io/gh/wpilibsuite/Axon/branch/master/graph/badge.svg)](https://codecov.io/gh/wpilibsuite/Axon)

# Axon

Axon - A Graphical Neural Network Editor

A Computer Science and Robotic Engineering Major Qualifying Project submitted to
the Faculty of Worcester Polytechnic Institute in partial fulfillment of the
requirements for the degree of Bachelor of Science.

## Core

The core project is responsible for understanding the operations the user wants
to complete and executing them in the correct order. Typically, the output of
the user's commands takes the form of code generation.

### Code Generation

This project has a DSL which models the generated code. Project `dsl-interface`
provides the interface for the components of the DSL. Project `dsl` provides
the implementation. Other offshoot projects, such as `tasks-yolov3`, add
model-specific implementation details that can be used with the DSL.

The flow of information through the DSL to the generated code is structured as
follows:

1. Information is input using the DSL via the `ScriptGenerator`. This forms a
"program" configuration which completely describes the code which will be
generated.
2. This configuration is checked for correctness by the `ScriptGenerator` inside
`ScriptGenerator.code`.
3. The configuration is parsed into a `Code` dependency graph using `CodeGraph`,
which performs further correctness checks to verify the dependency graph is a
singular DAG.
4. That graph is then traversed and parsed into a program by appending
`Code.code` segments in traversal order, predecessors first.

If at any point the `ScriptGenerator` or any of its dependencies determines
that the configuration is invalid, an error type is returned.

### Adding a Layer

To add a layer,

1. Add it in `SealedLayer` as a sealed subclass. Any parameter validation should be tested.
2. Add test cases that use it in `DefaultLayerToCodeTest` and then add it as a case in
`DefaultLayerToCode`.
3. Use it in `LoadLayersFromHDF5::parseLayer` and amend any tests that now fail because they
previously did not understand the new layer type. If no tests fail, add a test that loads a model
containing the new layer type.

### Adding an Initializer

To add an initializer,

1. Add it in `Initializer` as a sealed subclass.
2. Add a case for it in `DefaultInitializerToCode`. Add test cases in
`DefaultInitializerToCodeTest`.
3. Add a case for it in `LoadLayersFromHDF5::initializer`. Generate new models that use each
variance of the new `Initializer` and add a test in `LoadLayersWithInitializersIntegrationTest` that
loads each one.

### Plugins

Axon uses a simple plugin system to generalize over many different dataset and models.

#### Dataset Plugins

You can use dataset plugins to control how datasets are processed before training. In the training
script, after a dataset is loaded, it is given to a dataset plugin for processing, before being
given to the model during training. This is when any model-specific processing or formatting should
be done. Dataset plugins must implement this function:
```python
def process_dataset(x, y):
    # Do some data processing in here and return the results.
    return (x, y)
```

In that function, `x` is the dataset and `y` is the label set.

#### Test Data Loading Plugin

At the start of a test run, the test data must be loaded by a plugin. You can control how the test
data is loaded and processed before being given to the model during inference using a test data
loading plugin. These plugins must implement this function:
```python
def load_test_data(path):
    # Load the dataset from `path`, process it, and return it
    # along with `steps`.
    return (data, steps)
```

In that function, `path` is the file path to the test data that the user requested be loaded. In the
return statement, `data` is the final dataset that will be given directly to the model and `steps`
is the number of steps to give to TensorFlow when running inference; consult [the TensorFlow
documentation](https://www.tensorflow.org/versions/r1.15/api_docs/python/tf/keras/Model#predict)
for how to use `steps`.

#### Test Output Processing Plugin

After the inference step of a test run has completed, the input and output to/from the model is
given to a test output processing plugin. This plugin is responsible for interpreting the output of
the model and writing any test results to a folder in the current directory called `output`. Any
files put into this directory will be presented to the user in Axon's test view UI. These plugins
must implement this function:
```python
def process_model_output(model_input, model_output):
    from pathlib import Path
    # Write test result files into the `output` directory.
    Path("output/file1.txt").touch()
    Path("output/file2.txt").touch()
```

In that function, `model_input` is the data given to the model for inference and `model_output` is
the output directly from the model.

## AWS Integration

### S3 Directories Managed by Axon

Inside Axon's autogenerated S3 bucket (named with a prefix `axon-autogenerated-` followed by some
random alphanumeric characters for uniqueness), Axon manages these directories:

- axon-untrained-models
    - Contains “untrained” models that the user can use to create a new Job with
    - These models cannot be used for testing because they are assumed to not contain any weights (or at least not any meaningful weights)
    - EC2 pulls untrained models from here when running a training job
- axon-training-results
    - File format is `axon-training-results/{job id}/{results files...}`
    - Contains all results from running a training script
    - EC2 uploads trained models here after finishing a training job
- axon-test-data
    - Contains test data files that can be used with the test view
    - Lambda pulls test data files from here
- axon-datasets
    - Contains the user's datasets
    - EC2 pulls datasets from here for training
- axon-training-scripts
    - Contains generated training scripts
    - EC2 pulls training scripts from here
- axon-training-progress
    - Contains training progress files that Axon polls to get training progress updates
    - EC2 writes training progress into here
    - File format is `axon-training-progress/{model name}/{dataset name}/progress.txt`
- axon-plugins
    - Contains directories for each plugin cache
    - Unofficial plugins are stored here

### AWS Configuration

- Security Group for ECS named `axon-autogenerated-ecs-sg`
- Security Group for EC2 named `axon-autogenerated-ec2-sg`
- Security Group for RDS named `axon-autogenerated-rds-sg`
- Task role for ECS named `axon-autogenerated-ecs-task-role`
- IAM role for EC2 named `axon-autogenerated-ec2-role`
- Instance profile for EC2 named `axon-autogenerated-ec2-instance-profile`
- ECS Cluster named `axon-autogenerated-cluster`
- ECS Task Definition named `axon-autogenerated-task-family`
