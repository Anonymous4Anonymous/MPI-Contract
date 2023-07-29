/*
  
  The routine is called by all group members using the same arguments
  for count, datatype, op, root and comm.  Furthermore, the datatype
  and op given for predefined operators must be the same on all
  processes.

  The datatype argument of MPI_REDUCE must be compatible with op.
  
*/

#include<mpi.h>
#include<civl-mpi.cvh>
#include<pointer.cvh>
#include<string.h>
#include<stdlib.h>
#pragma CIVL ACSL

#define SBUF \mpi_buf(sbuf, count, datatype)
#define RBUF \mpi_buf(rbuf, count, datatype)

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
      requires 0 <= count && \mpi_nonoverlapping(datatype);
      requires \mpi_agree(count) && \mpi_agree(datatype) && \mpi_agree(op);
      behavior root:
        assumes  \mpi_comm_rank == root;
	requires \valid(RBUF);
        requires sbuf == MPI_IN_PLACE || \valid_read(SBUF); 
        requires sbuf != MPI_IN_PLACE ==>                                                                                 
                   \separated(RBUF, {SBUF, &count, &datatype, &op, &root, &comm});
        requires sbuf == MPI_IN_PLACE ==>
                   \separated(RBUF, {&count, &datatype, &op, &root, &comm}); 
        
        assigns  *RBUF;

        ensures  \mpi_reduce(*RBUF, 0, \mpi_comm_size, op, 
                           \lambda integer t; 
                           \mpi_on(sbuf != MPI_IN_PLACE ? *SBUF : \old(*RBUF), t));
        waitsfor {i | int i; 0 <= i < \mpi_comm_size && count > 0};
      behavior non_root:
        assumes  \mpi_comm_rank != root;
        requires \valid_read(SBUF);
        assigns  \nothing;
        waitsfor \nothing;
      disjoint behaviors;
      complete behaviors;
 */
int reduce(const void *sbuf, void *rbuf, int count, MPI_Datatype datatype,
           MPI_Op op, int root, MPI_Comm comm) {
  int rank;
  int tag = 0;
  
  MPI_Comm_rank(comm, &rank);
  if (rank != root)
    MPI_Send(sbuf, count, datatype, root, tag, comm);
  else {
    int nprocs;
    int size = sizeofDatatype(datatype);
    char *rdcbuf = (char*)malloc(sizeof(char) * size * count);
    
    MPI_Comm_size(comm, &nprocs);
    if (sbuf != MPI_IN_PLACE)
      memcpy(rdcbuf, sbuf, count*size);
    else
      memcpy(rdcbuf, rbuf, count*size);
    for (int i = 0; i<nprocs; i++) {
      if (i != root) {
	MPI_Recv(rbuf, count, datatype, i, tag, comm, MPI_STATUS_IGNORE);
	
	$mem_apply(rdcbuf, rbuf, op, count, size, rdcbuf);
      }
    }
    //    memcpy(rbuf, rdcbuf, count*size);
    free(rdcbuf);
  }
  return 0;
}
