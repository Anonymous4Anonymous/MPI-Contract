#pragma CIVL ACSL
#include <mpi.h>

/*@
  requires \valid(a + (0 .. n-1)) && \valid(b + (0 .. n-1));
  requires n > 0;
  assigns  \nothing;
  ensures  \forall int i; 0 <= i < n ==> \result >= a[i] + b[i];
  ensures  \exists int i; 0 <= i < n ==> \result == a[i] + b[i];  
*/
int maxSum(int * a, int * b, int n) {
  int max = a[0] + b[0];
  
  /*@ loop invariant 1 <= i <= n;
      loop invariant \forall int j; 0 <= j < i ==>
                       max >= a[j] + b[j];
      loop invariant \exists int j; 0 <= j < i ==>
                       max == a[j] + b[j];		       
      loop assigns max, i;
   */
  for (int i = 1; i < n; i++) {
    if (max < a[i] + b[i])
      max = a[i] + b[i];
  }
  return max;
}


#define SBUF \mpi_buf(sbuf, count, datatype)
#define RBUF \mpi_buf(rbuf, count, datatype)
/*@
   mpi uses comm;
   mpi collective(comm):
     requires \valid(RBUF) && count >= 0;
     requires \mpi_agree(count) && \mpi_agree(datatype) && \mpi_agree(op);
     requires \mpi_nonoverlapping(datatype);
     requires \separated(RBUF,
                         { { SBUF | int i; sbuf != MPI_IN_PLACE }, 
                           &count, &datatype, &op, &comm });

     assigns *RBUF;
     ensures \mpi_agree(*RBUF);
     waitsfor {i | int i; 0 <= i < \mpi_comm_size && count > 0};
     behavior not_in_place:
       assumes sbuf != MPI_IN_PLACE;
       requires \mpi_agree(sbuf != MPI_IN_PLACE);
       requires \valid_read(SBUF);
       ensures  \mpi_reduce(*RBUF, 0, \mpi_comm_size, op,
                            \lambda integer t; \mpi_on(*SBUF, t));
     behavior in_place:
       assumes sbuf == MPI_IN_PLACE;
       requires \mpi_agree(sbuf == MPI_IN_PLACE);
       ensures  \mpi_reduce(*RBUF, 0, \mpi_comm_size, op,
                                \lambda integer t; \mpi_on(\old(*RBUF), t));
   disjoint behaviors;
   complete behaviors;
*/
int allreduce(const void *sbuf, void *rbuf, int count,
              MPI_Datatype datatype, MPI_Op op, MPI_Comm comm);


/*@
  mpi uses MPI_COMM_WORLD;
  mpi collective(MPI_COMM_WORLD):
    requires \mpi_comm_size == n;
    requires \valid(a + (0 .. n-1)) && \valid(b + (0 .. n-1));
    requires n > 0;
    assigns  \nothing;
    ensures  \forall int i; 0 <= i < n ==> \result >= \mpi_on(a[i] + b[i], i);
    ensures  \exists int i; 0 <= i < n ==> \result == \mpi_on(a[i] + b[i], i);       
    waitsfor {i | int i; 0 <= i < n};
*/
int maxSumPar(int * a, int * b, int n) {
  int rank;
  int local;
  int max;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  local = a[rank] + b[rank];
  allreduce(&local, &max, 1, MPI_INT, MPI_MAX,
                MPI_COMM_WORLD);
  return max;
}
