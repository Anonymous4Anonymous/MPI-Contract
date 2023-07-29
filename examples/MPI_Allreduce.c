/*  
  If comm is an intra-communicator, MPI_ALLREDUCE behaves the same as
  MPI_REDUCE except that the result appears in the receive bu↵er of
  all the group members.

  The “in place” option for intra-communicators is specified by
  passing the value MPI_IN_PLACE to the argument sendbuf at all
  processes.
 */
#include<mpi.h>
#pragma CIVL ACSL


#define BUF \mpi_buf(buf, count, datatype)

/*@ mpi uses comm;
    mpi collective(comm):
      requires 0 <= root < \mpi_comm_size && \mpi_agree(root);
      requires \mpi_agree(count * \mpi_sig(datatype)) && 0 <= count;
      requires \mpi_nonoverlapping(datatype);
      requires \separated(BUF, {&count, &datatype, &root, &comm});
      ensures \mpi_agree(*BUF);
      behavior root:
        assumes \mpi_comm_rank == root;
        requires \valid_read(BUF);
        assigns \nothing;
      behavior nonroot:
        assumes \mpi_comm_rank != root;
        requires \valid(BUF);
        assigns *BUF;
        waitsfor { root | int i ; count > 0 };
      complete behaviors;
      disjoint behaviors;
*/
int bcast(void * buf, int count, MPI_Datatype datatype, int root,
	      MPI_Comm comm);

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
        requires sbuf != MPI_IN_PLACE ==> \valid_read(SBUF);
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
	       MPI_Op op, int root, MPI_Comm comm);

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
              MPI_Datatype datatype, MPI_Op op, MPI_Comm comm) { 
  int rank;
  
  MPI_Comm_rank(comm, &rank);
  // sbuf of non-zero cannot be MPI_IN_PLACE for reduce:
  if (sbuf == MPI_IN_PLACE && rank != 0) { 
    reduce(rbuf, NULL, count, datatype, op, 0, comm);
  } else
    reduce(sbuf, rbuf, count, datatype, op, 0, comm);
  bcast(rbuf, count, datatype, 0, comm);
  return 0;
}
