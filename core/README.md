# Axon Core

The core project is responsible for understanding the operations the user wants
to complete and executing them in the correct order. Typically, the output of
the user's commands takes the form of code generation.

## Code Generation

This project has a DSL which models the generated code. Project `dsl-interface`
provides the interface for the components of the DSL. Project `dsl` provides
the implementation. Other offshoot projects, such as `tasks-yolov3`, add
model-specific implementation details that can be used with the DSL.

The flow of information through the DSL to the generated code is structured as
follows:

1. Information is input using the DSL via the `ScriptGenerator`. This forms a
"program" configuration which completely described the code which will be
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
