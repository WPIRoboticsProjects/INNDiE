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
