/*  
  If comm is an intra-communicator, MPI_ALLREDUCE behaves the same as
  MPI_REDUCE except that the result appears in the receive bu↵er of
  all the group members.

  The “in place” option for intra-communicators is specified by
  passing the value MPI_IN_PLACE to the argument sendbuf at all
  processes.
 */
#include<mpi.h>
#include<pointer.cvh>
#include<string.h>
#include<stdlib.h>
#include<civl-mpi.cvh>

#pragma CIVL ACSL

#define SBUF \mpi_buf(sbuf, count, datatype)
#define RBUF \mpi_buf(rbuf, count, datatype)


int my_log2(int n) {
  int c = 0;
  while (n / 2 > 0) {
    n = n / 2;
    c++;
  }
  return c;
}

int my_pow2(int n) {
  int r = 1;
  while (n > 0) {
    r = 2 * r;
    n--;
  }
  return r;
}

void reduce(void * inout, void * in, int count, int size, MPI_Op op) {
  $mem_apply(inout, in, ($operation)op, count, size, inout);
}


/*@
   mpi uses comm;
   mpi collective(comm):
     requires \valid(RBUF) && count >= 0;
     requires \mpi_agree(count) && \mpi_agree(datatype) && \mpi_agree(op);
     requires \mpi_nonoverlapping(datatype);
     requires \separated(RBUF, { {SBUF | int i; sbuf != MPI_IN_PLACE},
                                 &count, &datatype, &op, &comm });

     assigns *RBUF;

     waitsfor {i | int i; 0 <= i < \mpi_comm_size && count > 0};
     behavior not_in_place:
       assumes sbuf != MPI_IN_PLACE;
       requires \mpi_agree(sbuf != MPI_IN_PLACE);
       requires \valid_read(SBUF);
       ensures  \mpi_reduce(*RBUF, 0, \mpi_comm_size, op,
                            \lambda integer t; \mpi_on(*SBUF, t));
     behavior in_place:
       assumes sbuf == MPI_IN_PLACE;
       requires \mpi_agree(sbuf);
       ensures  \mpi_reduce(*RBUF, 0, \mpi_comm_size, op,
                            \lambda integer t; \mpi_on(\old(*RBUF), t));
   disjoint behaviors;
   complete behaviors;

*/
int allreduce(const void *sbuf, void *rbuf, int count,
              MPI_Datatype datatype, MPI_Op op, MPI_Comm comm) {
  int elementsize = sizeofDatatype(datatype);
  int datasize = elementsize * count;
  int mask = 1;
  int pof2;
  int rem;
  int nprocs, rank, myrank; 

  MPI_Comm_size(comm, &nprocs);
  MPI_Comm_rank(comm, &rank);
   // handle nprocs == 1:
  if (nprocs == 1) {
    if (sbuf != MPI_IN_PLACE)
      memcpy(rbuf, sbuf, datasize);
    return 0;
  }

  void * global = (char*)malloc(sizeof(char)*datasize);//$mpi_malloc(count, datatype);
  
  pof2 = my_log2(nprocs);
  pof2 = my_pow2(pof2);
  rem = nprocs - pof2;
  if (sbuf == MPI_IN_PLACE)
    memcpy(global, rbuf, datasize);
  else
    memcpy(global, sbuf, datasize);
  if (rank < 2 * rem) {
    if (rank % 2 == 0) {
      MPI_Send(global, count, datatype, rank + 1, 0, comm);
      myrank = -1;
    } else {
      MPI_Recv(rbuf, count, datatype, rank - 1, 0, comm, MPI_STATUS_IGNORE);
      reduce(global, rbuf, count, elementsize, op);
      myrank = rank / 2;
    }
  } else
    myrank = rank - rem;
  if (myrank != -1) 
    while (mask < pof2) {
      int newdst = myrank ^ mask;
      int dst;
      
      if (newdst < rem)
	dst = newdst * 2 + 1;
      else
	dst = newdst + rem;
      MPI_Sendrecv(global, count, datatype, dst, 0, 
		   rbuf, count, datatype, dst, 0, comm, MPI_STATUS_IGNORE);
      reduce(global, rbuf, count, elementsize, op);
      mask = mask << 1;
    }
  if (rank < 2 * rem)
    if (rank % 2 != 0)
      MPI_Send(global, count, datatype, rank - 1, 0, comm);
    else
      MPI_Recv(global, count, datatype, rank + 1, 0, comm, MPI_STATUS_IGNORE);
  memcpy(rbuf, global, datasize);
  free(global);
  return 0;
}
