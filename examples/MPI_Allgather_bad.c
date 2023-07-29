/*
   The type signature associated with sendcount, sendtype, at a
   process must be equal to the type signature associated with
   recvcount, recvtype at any other process.

   The “in place” option for intra-communicators is specified by passing
   the value MPI_IN_PLACE to the argument sendbuf at all processes.  
*/
#include<mpi.h>
#include<civl-mpi.cvh>
#include<string.h>
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

      ensures  sbuf != MPI_IN_PLACE ==> *RBUF_OF(root) == *SBUF;
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
	       int root, MPI_Comm comm);


/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_nonoverlapping(rtype);
      requires rcount >= 0 && \valid(RBUF);
      requires \separated(RBUF, 
                          { { SBUF | int i; sbuf != MPI_IN_PLACE },
                          &scount, &stype, &rcount, &rtype, &comm });

      assigns  *RBUF;

      waitsfor {i | int i; 0 <= i < \mpi_comm_size && rcount > 0};
      behavior not_in_place:
        assumes sbuf != MPI_IN_PLACE;
        requires \mpi_agree(sbuf != MPI_IN_PLACE);
        requires \forall int i; 0 <= i < \mpi_comm_size ==> SSIG == \mpi_on(RSIG, i);
        requires scount >= 0 && \valid_read(SBUF);
	ensures  \forall int i; 0 <= i < \mpi_comm_size ==>  *RBUF_OF(i) == \mpi_on(*SBUF, i); 
     behavior in_place:
        assumes sbuf == MPI_IN_PLACE;
	requires \mpi_agree(sbuf == MPI_IN_PLACE);
        requires \forall int i; 0 <= i < \mpi_comm_size ==> RSIG == \mpi_on(RSIG, i);
      	ensures  \forall int i; 0 <= i < \mpi_comm_size ==>  *RBUF_OF(i) == \old(\mpi_on(*RBUF_OF(i), i)); 
	ensures 0;
     disjoint behaviors;
     complete behaviors;
 */
int allgather(const void *sbuf, int scount, MPI_Datatype stype,
	      void *rbuf, int rcount, MPI_Datatype rtype, MPI_Comm comm) {
  int place;
  int nprocs;
  
  MPI_Comm_rank(comm, &place);
  MPI_Comm_size(comm, &nprocs);
  if (sbuf != MPI_IN_PLACE) {
    gather(sbuf, scount, stype,
	   rbuf, rcount, rtype,
	   0, comm);
  } else {
    void * buf;
    
    if (place == 0)
      buf = MPI_IN_PLACE;
    else
      buf = $mpi_pointer_add(rbuf, rcount * place, rtype);
    gather(buf, rcount, rtype,
	       rbuf, rcount, rtype,
	       0, comm);    
  }
  bcast(rbuf, rcount*nprocs, rtype, 0, comm);
  return 0;
}
