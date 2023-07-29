#include<mpi.h>
#include<civl-mpi.cvh>
#include<string.h>
#pragma CIVL ACSL

#define SBUF         \mpi_buf(sbuf, scount, stype)
#define SBUF_OF(id)  \mpi_on(SBUF, (id))

#define RBUF         \mpi_buf(rbuf, rcount * \mpi_comm_size, rtype)
#define RBUF_OF(id)  (\mpi_buf(rbuf, rcount, rtype) + (id)*rcount)

#define SSIG         (\mpi_sig(stype) * scount)
#define SSIG_OF(id)  \mpi_on(SSIG, (id))
#define RSIG         (\mpi_sig(rtype) * rcount)

/*@
  mpi uses comm;
  mpi collective(comm) :
    requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
//    ensures  \mpi_back_messages <= \nothing;
    behavior root:
      assumes  \mpi_comm_rank == root;
      requires \valid(RBUF) && \mpi_nonoverlapping(rtype);
      requires \forall int id; 0 <= id < \mpi_comm_size ==>
                  id != root ==> SSIG_OF(id) == RSIG;
      requires sbuf == MPI_IN_PLACE || \valid_read(SBUF);
      requires sbuf != MPI_IN_PLACE ==> (SSIG == RSIG);
      requires \separated(RBUF, 
                          { { SBUF | int i; sbuf != MPI_IN_PLACE }, 
                          &scount, &stype, &rcount, &rtype, &root, &comm });
                 
      assigns  *RBUF;

      ensures  sbuf != MPI_IN_PLACE ==> *RBUF_OF(root) == \old(*RBUF_OF(root));
      ensures  sbuf == MPI_IN_PLACE ==> *RBUF_OF(root) == \old(*RBUF_OF(root));
      ensures  \forall int id; 0 <= id < \mpi_comm_size ==>
                 id != root ==> *RBUF_OF(id) == *SBUF_OF(id);
      waitsfor {i | int i; 0 <= i < \mpi_comm_size && rcount > 0};
    behavior not_root:
      assumes  \mpi_comm_rank != root;
      requires \valid_read(SBUF);
      assigns  \nothing;
      waitsfor \nothing;
  disjoint behaviors;
  complete behaviors;
*/
int gather(const void* sbuf, int scount, MPI_Datatype stype, 
	   void* rbuf, int rcount, MPI_Datatype rtype,
	   int root, MPI_Comm comm) {
  int rank, nprocs;
  MPI_Status status;
  int tag = 998;

  MPI_Comm_rank(comm, &rank);
  MPI_Comm_size(comm, &nprocs);
  if(root == rank) { 
    if(sbuf != MPI_IN_PLACE) {
      void *ptr;
      
      ptr = $mpi_pointer_add(rbuf, root * rcount, rtype);
      memcpy(ptr, sbuf, rcount * sizeofDatatype(rtype));
    }
  } else
    MPI_Send(sbuf, scount, stype, root, tag, comm);
  if(rank == root) {
    int real_recvcount;
    int offset;

    for(int i=0; i<nprocs; i++) {
      if(i != root) {
	void * ptr;

	offset = i * rcount;
	ptr = $mpi_pointer_add(rbuf, offset, rtype);
	MPI_Recv(ptr, rcount, rtype, i, tag, comm,
		 &status);
      }
    }
  }
  return 0;
}
