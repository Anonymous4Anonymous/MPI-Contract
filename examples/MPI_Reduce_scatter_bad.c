/*
   The “in place” option for intra-communicators is specified by
   passing MPI_IN_PLACE in the sendbuf argument. In this case, the
   input data is taken from the receive buffer. It is not required
   to specify the “in place” option on all processes, since the
   processes for which recvcounts[i] == 0 may not have allocated a
   receive buffer.    

   The routine is called by all group members using the same arguments
   for recvcounts, datatype, op and comm.
                                                       ---- MPI 2020 Draft
 */
#include<mpi.h>
#include<assert.h>
#include<civl-mpi.cvh>
#include<stdlib.h>
#pragma CIVL ACSL


#define RD_SBUF \mpi_buf(sbuf, count, datatype)
#define RD_RBUF \mpi_buf(rbuf, count, datatype)

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
      requires 0 <= count && \mpi_nonoverlapping(datatype);
      requires \mpi_agree(count) && \mpi_agree(datatype) && \mpi_agree(op);
      behavior root:
        assumes  \mpi_comm_rank == root;
	requires \valid(RD_RBUF);
        requires sbuf == MPI_IN_PLACE || \valid_read(RD_SBUF);
        requires sbuf != MPI_IN_PLACE ==>                                                                                 
                   \separated(RD_RBUF, {RD_SBUF, &count, &datatype, &op, &root, &comm});
        requires sbuf == MPI_IN_PLACE ==>
                   \separated(RD_RBUF, {&count, &datatype, &op, &root, &comm}); 

        assigns  *RD_RBUF;

        ensures  \mpi_reduce(*RD_RBUF, 0, \mpi_comm_size, op, 
                           \lambda integer t; 
                           \mpi_on(sbuf != MPI_IN_PLACE ? *RD_SBUF : \old(*RD_RBUF), t));
        waitsfor {i | int i; 0 <= i < \mpi_comm_size && count > 0};
      behavior non_root:
        assumes  \mpi_comm_rank != root;
        requires \valid_read(RD_SBUF);
        assigns  \nothing;
        waitsfor \nothing;
      disjoint behaviors;
      complete behaviors;
 */
int reduce(const void *sbuf, void *rbuf, int count, MPI_Datatype datatype,
	       MPI_Op op, int root, MPI_Comm comm);



#define SV_SBUF_OF(id)    (\mpi_buf(sbuf, scounts[(id)], stype) + displs[(id)])
#define SV_SBUF           \mpi_buf(sbuf, scounts[\mpi_comm_size-1] + displs[\mpi_comm_size-1], stype)

#define SV_RBUF           \mpi_buf(rbuf, rcount, rtype)

#define SV_SSIG_OF(id)    \mpi_on((\mpi_sig(stype) * scounts[(id)]), root)
#define SV_RSIG           (\mpi_sig(rtype) * rcount)

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
      behavior root:
        assumes  \mpi_comm_rank == root;
        requires \valid_read(scounts + (0 .. \mpi_comm_size-1));
        requires \valid_read(displs + (0 .. \mpi_comm_size-1));
        requires \forall int i; 0 <= i < \mpi_comm_size ==> scounts[i] >= 0;
        requires \forall int i, j; 0 <= i < j < \mpi_comm_size ==>
                   (displs[i] + scounts[i] <= displs[j]) ||
                   (displs[j] + scounts[j] <= displs[i]);
	requires \valid(SV_SBUF);
        requires rbuf != MPI_IN_PLACE ==> 
                   \valid_read(SV_SBUF_OF(root)) && \valid(SV_RBUF) && 
                   SV_SSIG_OF(root) == SV_RSIG                      &&
                   \mpi_nonoverlapping(rtype);
        requires rbuf != MPI_IN_PLACE ==>  \separated(SV_RBUF,  {{ SV_SBUF_OF(i) | int i; 0 <= i < \mpi_comm_size },
                                                                 scounts + (0 .. \mpi_comm_size-1),
                                                                 displs + (0 .. \mpi_comm_size-1),  
                                                                 &stype, &rtype, &rcount, &root, &comm});
 
        assigns  {*SV_RBUF | int i; rbuf != MPI_IN_PLACE};

        ensures  rbuf != MPI_IN_PLACE ==> *SV_RBUF == *SV_SBUF_OF(root);
        waitsfor \nothing;
      behavior nonroot:
        assumes  \mpi_comm_rank != root;
        requires  rcount >= 0 && \valid(SV_RBUF) && \mpi_nonoverlapping(rtype);
        requires  \forall integer myrank; myrank == \mpi_comm_rank ==> SV_SSIG_OF(myrank) == SV_RSIG;
        assigns  *SV_RBUF;

	//ensures  \forall int i; i == \mpi_comm_rank ==> *SV_RBUF == \mpi_on(*SV_SBUF_OF(i), root);
        waitsfor {root | int i; 0 <= i < \mpi_comm_size && rcount > 0};
    disjoint behaviors;
    complete behaviors;
 */
int scatterv(const void *sbuf, const int *scounts, const int *displs,
                 MPI_Datatype stype, void *rbuf, int rcount,
                 MPI_Datatype rtype,
                 int root, MPI_Comm comm);


#define MY_RANK                  \mpi_comm_rank
#define SCOUNT                   \sum(0, \mpi_comm_size-1, \lambda int k0; rcounts[k0])
#define COUNTS(i)                (i==0?0:\sum(0, (i-1), \lambda int k1; rcounts[k1]))
#define SBUF                     \mpi_buf(sbuf, SCOUNT, datatype)
#define MY_SBUF_BLK(i)           (\mpi_buf(sbuf, rcounts[(i)], datatype) + COUNTS(i))
#define MY_RBUF_AS_SBUF_BLK(i)   (\mpi_buf(rbuf, rcounts[(i)], datatype) + COUNTS(i))
#define RBUF_AS_SBUF             \mpi_buf(rbuf, SCOUNT, datatype)
#define MY_RBUF                  \mpi_buf(rbuf, rcounts[MY_RANK], datatype)

/*@ mpi uses comm;
    mpi collective(comm):
      requires \valid_read(rcounts + (0 .. \mpi_comm_size-1));
      requires rcounts[MY_RANK] >= 0;
      requires \mpi_agree(datatype);
      requires \forall int i; 0 <= i < \mpi_comm_size ==> \mpi_agree(rcounts[i]);
      requires \mpi_agree(SCOUNT) && \mpi_agree(op);
      requires \mpi_nonoverlapping(datatype);
      requires \separated(MY_RBUF,  
                          { rcounts + (0 .. \mpi_comm_size-1),       
                            &datatype, &op, &comm,     
                            { SBUF | int i; sbuf != MPI_IN_PLACE }
                          }); 

      assigns *MY_RBUF;

      ensures \forall int i; i == \mpi_comm_rank ==>
                 \mpi_reduce(*MY_RBUF, 0, \mpi_comm_size, op, \lambda integer t; 
                             \mpi_on(sbuf != MPI_IN_PLACE ? *MY_SBUF_BLK(i) : \old(*MY_RBUF_AS_SBUF_BLK(i)), t));
      waitsfor {i | int i; rcounts[i] > 0};
      behavior not_in_place:
        assumes  sbuf != MPI_IN_PLACE;
        requires \valid_read(SBUF);
        requires \valid(MY_RBUF);
      behavior in_place:
        assumes sbuf == MPI_IN_PLACE;
        requires \valid(RBUF_AS_SBUF);
    disjoint behaviors;
    complete behaviors;
 */

int reduce_scatter(const void *sbuf, void *rbuf, const int *rcounts,
		   MPI_Datatype datatype, MPI_Op op, MPI_Comm comm) {
  int scount = 0;
  int np, rank; 
  char * tmp = NULL;
  
  MPI_Comm_size(comm, &np);
  MPI_Comm_rank(comm, &rank);

  int displs[np];  

  for (int i = 0; i < np; i++) {
    displs[i] = scount;
    scount += rcounts[i];
  }  
  if (rank == 0) {
    int size = sizeofDatatype(datatype);
    tmp = (char*)malloc(scount*size*sizeof(char));  
  }
  if (sbuf == MPI_IN_PLACE)
    reduce(rbuf, tmp, scount, datatype, op, 0, comm);
  else
    reduce(sbuf, tmp, scount, datatype, op, 0, comm);

  scatterv(tmp, rcounts, displs, datatype,
	       rbuf, rcounts[rank], datatype, 0, comm);
  if (rank == 0)
    free(tmp);
  return 0;
}
