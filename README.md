This is the Artifact for the ICSE 2024 submission: Collective Contracts for Message-Passing Parallel Programs

## Contents
- the extended technical report, including
    - a complete proof for the main theorem
    - a list of contracts for all MPI collective functions

- the source code of the prototype tool used in the submission is
  located at `src`

- the experiment examples are located at `examples`

- `civl_config_used_for_experiment`: the configuration file used in
  the experiment reported in the submission

    - It specifies the versions, the invocation order, and the timeout
      for the two SMT provers: z3 and cvc4.

    - Using a different configuration may results in slight difference
      in experiment results regarding times, number of prover calls,
      or number of explored states.  Significant difference should not
      be observed.


## Build the prototype and run the experiment

- install SMT provers z3 (https://github.com/Z3Prover/z3) and cvc4 (https://github.com/CVC4)

- If this artifact repository is not located at
  `/Users/{username}/Documents`, please update the `root` path with
  respect to the location of this repository in `build.properties` in
  `src/ABC` and `src/CIVL`.

- build the prototype by `make` at the root directory of this repository

- run the experiment with the following steps:
    - `cd examples`
    - `make config`: help CIVL finds the installed provers
    - `make`: run the experiments with up to 5 processes
