#pragma CIVL ACSL
#include <mpi.h>

/*@
   mpi uses comm;
   mpi collective(comm):
     assigns \nothing;
     ensures \mpi_agree(\result);
     ensures  \forall int i; 0 <= i < \mpi_comm_size ==> \result >= \mpi_on(sval, i);
     waitsfor {i | int i; 0 <= i < \mpi_comm_size};
*/
int maxPar(int sval, MPI_Comm comm);


/*@
  mpi uses MPI_COMM_WORLD;
  mpi collective(MPI_COMM_WORLD):
    requires \mpi_comm_size == n;
    requires \forall int i; 0 <= i < n ==> \mpi_agree(a[i]) && \mpi_agree(b[i]);
    requires \valid(a + (0 .. n-1)) && \valid(b + (0 .. n-1));
    requires n > 0;
    assigns  \nothing;
    ensures  \forall int i; 0 <= i < n ==> \result >= a[i] + b[i];
    waitsfor {i | int i; 0 <= i < n};
*/
int maxSumPar(int * a, int * b, int n) {
  int rank;
  int local;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  local = a[rank] + b[rank];
  return maxPar(local, MPI_COMM_WORLD);
}
