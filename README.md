# Artifact of "Collective Contracts for Message-Passing Parallel Programs"

## Contents

- `src`: the Java source code of the prototype tool used in the submission

- `examples`: examples of the experiment reported in the publication

- `civl.jar`: the Java executable of the prototype tool used for the
              experiment

- `Makefile`: type `make` to build a new `civl.jar` from the source
  
- `civl_config_used_for_experiment`: the CIVL configuration file we used
                                     in the experiment
  
- `experiment_output_plain_text.txt`: the original output log of the
                                      experiment we reported in the
                                      publication				      

- `tech_report.pdf`: the extended version of the publication

- `abstract.pdf`: the artifact abstract including a quick tutorial on
  how to create new examples

- `tutorial`: containing two extra examples we used in `abstract.pdf`
  for a quick tutorial of how to create new examples

## Build the prototype and run the experiment

- Edit the `src/ABC/build.properties` and `src/CIVL/build.properties`
  so that the `workspace` matches your path.

- To build the prototype, you need Java 17 SDK installed.  Once you
  have Java SDK, you can type `make` from the root directory of this
  repository to build an executable `civl.jar` from the source.

- The prototype uses two SMT provers:
    - z3: https://github.com/Z3Prover/z3
    - cvc4: https://cvc4.github.io/
    
  After installing these two provers, one can type `make config` under
  the root directory of this repository to help the prototype find the
  two provers.  The result is a config file `~/.sarl`. One can edit
  the file to adjust timeouts or the invocation order of the two
  provers.  These edits may affect verification time of the prototype.

- Run the experiment with the following steps:
    1. `cd examples`
    
    2. `make`: run the experiments with up to 3 processes    
       `make full`: run the experiments with up to 5 processes.  This
                    is the setting used in the paper.       
       `make bad`: run examples, in which contract violations were
                   introduced on purpose


## Run the prototype on user-provided programs.

- Please read the artifact abstract for a quick tutorial.

- Use the following incantations to verify user-provided functions.

    - `java -jar civl.jar verify -input_mpi_nprocs_hi=[higher] -input_mpi_nprocs_lo=[lower] -mpiContract=[funcname] [filename]` or
    - `java -jar civl.jar verify -input_mpi_nprocs=[nprocs] -mpiContract=[funcname] [filename]`

   - higher: the higher bound on the number of MPI processes
   - lower: the lower bound on the number of MPI processes
   - nprocs: the exact number of MPI processes
   - funcname: the name of the function in the input program that will
               be verified against its contract.   
   - filename: the file name of the input program.

  Additionally, one may need to pass `-loop` flag to the command if
  the example uses loop invariants.
