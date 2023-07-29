#include "mpi.h"
#include "stdio.h"
#pragma CIVL ACSL

#define T      double
#define MPI_T  MPI_DOUBLE
#define RANK   \mpi_comm_rank
#define NPROCS \mpi_comm_size

int id, n;     // id: process rank, n: sorting array length
T myvalue;     // element hold by the process
MPI_Comm comm;

$assume($forall (int i) (i % 2 == 0 || i % 2 == 1) && (i % 2 != (i+1) % 2));

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(i);
      requires 0 <= i < n && id == RANK && n == NPROCS && \mpi_nonoverlapping(MPI_T);
      assigns  myvalue;
      ensures  (id % 2 == 0 && id < n-1 ==> myvalue <= \mpi_on(myvalue, id + 1)) ||
               (id % 2 == 1 && id < n-1 ==> myvalue <= \mpi_on(myvalue, id + 1));                 // sortness
      ensures  \sum(0, n-1, \lambda int t; \mpi_on(myvalue, t) == myvalue ? 1 : 0) ==             // permutation for non-trivial size case
               \sum(0, n-1, \lambda int t; \old(\mpi_on(myvalue, t)) == myvalue ? 1 : 0);
 */
void oddEvenParIter(int i) {
  T othervalue;
  
  if ((i+id)%2 == 0) {
    if (id<n-1) {
      MPI_Send(&myvalue, 1, MPI_T, id+1, 0, comm);
      MPI_Recv(&othervalue, 1, MPI_T, id+1, 0, comm, MPI_STATUS_IGNORE);
      if (othervalue < myvalue)
	myvalue = othervalue;
    }
  } else { // odd
    if (id>0) {
      MPI_Recv(&othervalue, 1, MPI_T, id-1, 0, comm, MPI_STATUS_IGNORE);
      MPI_Send(&myvalue, 1, MPI_T, id-1, 0, comm);
      if (othervalue > myvalue)
        myvalue = othervalue;
    }
  }
}
