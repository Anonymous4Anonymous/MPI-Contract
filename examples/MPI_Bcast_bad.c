#include <mpi.h>
#pragma CIVL ACSL
#define BUF \mpi_buf(buf, count, datatype)

/*@ mpi uses comm;
    mpi collective(comm):
      requires 0 <= root < \mpi_comm_size && \mpi_agree(root);
      requires \mpi_agree(count * \mpi_sig(datatype)) && 0 <= count;
      requires \separated(BUF, {&count, &datatype, &root, &comm});
      requires \mpi_nonoverlapping(datatype);
      ensures \mpi_agree(*BUF) && 0;
      //ensures \mpi_back_messages <= \nothing;
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
	      MPI_Comm comm) {
  int nprocs, rank;
  int tag = 999;

  MPI_Comm_size(comm, &nprocs);
  MPI_Comm_rank(comm, &rank);
  if (rank == root) {
    for (int i = 0; i < nprocs; i++)
      if (i != root)
	MPI_Send(buf, count, datatype, i, tag, comm);
  } else
    MPI_Recv(buf, count, datatype, root, tag, comm,
	     MPI_STATUS_IGNORE);
  return 0;
}
