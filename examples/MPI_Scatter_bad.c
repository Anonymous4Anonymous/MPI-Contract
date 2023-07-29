#include<mpi.h>
#include<string.h>
#include<civl-mpi.cvh>

#pragma CIVL ACSL

#define SBUF_OF(id) (\mpi_buf(sbuf, scount, stype) + (id) * scount)
#define SBUF        \mpi_buf(sbuf, scount * \mpi_comm_size, stype)

#define RBUF_OF(id) \mpi_on(\mpi_buf(rbuf, rcount, rtype), (id))
#define RBUF        \mpi_buf(rbuf, rcount, rtype)

#define SSIG        (scount*\mpi_sig(stype))
#define RSIG        (rcount*\mpi_sig(rtype))
#define RSIG_OF(id) \mpi_on(RSIG, (id))

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
      behavior root:
        assumes \mpi_comm_rank == root;
        requires scount >= 0 && \valid_read(SBUF); 
	requires rbuf == MPI_IN_PLACE || \valid(RBUF);
        requires rbuf != MPI_IN_PLACE ==> SSIG == RSIG && \mpi_nonoverlapping(rtype);
	requires rbuf != MPI_IN_PLACE ==> 
                   \separated(RBUF, {SBUF, &scount, &stype, &rcount, &rtype, &root, &comm});
        requires \forall int i; 0 <= i < \mpi_comm_size && i != root ==>
                   SSIG == RSIG_OF(i);
        
        assigns  {*RBUF | int i; rbuf != MPI_IN_PLACE};

        ensures  rbuf != MPI_IN_PLACE ==> *RBUF == *SBUF_OF(root);
        waitsfor \nothing;
      behavior nonroot:
        assumes \mpi_comm_rank != root;
        requires rcount >= 0 && \valid(RBUF) && \mpi_nonoverlapping(rtype);
        assigns  *RBUF;
        ensures  \forall int i; i == \mpi_comm_rank ==> *RBUF == \mpi_on(*SBUF_OF(i), root);
        waitsfor {root | int i; rcount > 0};
    disjoint behaviors;
    complete behaviors;
 */
int scatter(const void* sbuf, int scount, MPI_Datatype stype, 
	    void* rbuf, int rcount, MPI_Datatype rtype, int root,
	    MPI_Comm comm) {
  int rank, nprocs;
  int tag = 0;
  
  MPI_Comm_rank(comm, &rank);
  MPI_Comm_size(comm, &nprocs);
  /* MPI_standard requirement: 
   * Only root process can use MPI_IN_PLACE */
  if (rank == root && rbuf != MPI_IN_PLACE) {
    void * ptr;
    
    ptr = $mpi_pointer_add(sbuf, root*scount, stype);
    memcpy(rbuf, ptr, sizeofDatatype(rtype)*rcount);
  }
  /* Root process scatters data to other processes */
  if(rank == root)
    for(int i = 0; i < nprocs; i++){
      if(i != root) {
	void * ptr;
	
	ptr = $mpi_pointer_add(sbuf, i * scount, stype);
	$mpi_send(ptr, scount, stype, i, tag, comm);
      }
    }
  /* Non-root processes receive data */
  if(root != rank){    
    $mpi_recv(rbuf, rcount, rtype, 
	      root, tag, comm, MPI_STATUS_IGNORE);
  }
  if (rbuf != MPI_IN_PLACE) // add an non-sense eror
    $mpi_assigns(rbuf, rcount, rtype);
  return 0;
}
