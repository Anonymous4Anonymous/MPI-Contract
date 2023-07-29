/* An reproduce of an advanced reduce-scatter algorithm presented in
 * paper "Efficient Implementation of Reduce-Scatter in MPI"
 * (https://dl.acm.org/citation.cfm?id=1895529) with simple stubs for
 * functions that are omitted in the paper.
 */

#include "mpi.h"
#include "stdlib.h"
#include "string.h"
#include "pointer.cvh"
#include "civl-mpi.cvh"

#define LOW  0
#define HIGH 1

void init_offsets(int * offsets, int size, const int *recvcnts);

void reduce(void * buf0, void * buf1, int count, int element_size, MPI_Op op);

#pragma CIVL ACSL


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
		   MPI_Datatype datatype, MPI_Op op, MPI_Comm comm)
{
  int rank, size;

  MPI_Comm_rank(comm, &rank);
  MPI_Comm_size(comm, &size);

  int offs[size];
  void * tarrsend, *tarrrecv;
  void * s[2];
  size_t datatype_size = sizeofDatatype(datatype);
  _Bool s_high_inited = 0;
  void *ssbuf;
  
  // handle MPI_IN_PLACE
  int scount = 0;
  
  for (int i = 0; i < size; i++)
    scount += rcounts[i];
  ssbuf = (char*)malloc(scount * datatype_size);
  if (sbuf == MPI_IN_PLACE) {
    memcpy(ssbuf, rbuf, scount * datatype_size);  
  } else {
    memcpy(ssbuf, sbuf, scount * datatype_size);  
  }
  
  init_offsets(offs, size, rcounts);
  tarrsend = (char *)ssbuf + offs[rank]*datatype_size;
  tarrrecv = (char *)malloc(datatype_size * rcounts[rank]);
  s[LOW] =   (char *)malloc(datatype_size * rcounts[rank]);
  s[HIGH] =  (char *)malloc(datatype_size * rcounts[rank]);
  memcpy(s[LOW], tarrsend, datatype_size * rcounts[rank]);
  
  for (int h = 1; h < size; h++) {
    int dst = (rank + h) % size;
    int src = (rank + size - h) % size;
    
    tarrsend = (char *)ssbuf + offs[dst]*datatype_size;
    MPI_Sendrecv(tarrsend, rcounts[dst], datatype, dst, h, tarrrecv,
		 rcounts[rank], datatype, src, h, comm, MPI_STATUS_IGNORE);
    if (src < rank)
      reduce(s[LOW], tarrrecv, rcounts[rank], datatype_size, op);
    else {
      if (!s_high_inited) {
	s_high_inited = 1;
	memcpy(s[HIGH], tarrrecv, rcounts[rank]*datatype_size);
      } else
	reduce(s[HIGH], tarrrecv, rcounts[rank], datatype_size, op);
    }
  }
  if (s_high_inited)
    reduce(s[LOW], s[HIGH], rcounts[rank], datatype_size, op);
  memcpy(rbuf, s[LOW], rcounts[rank] * datatype_size);
  // finish up:
  free(tarrrecv);
  free(s[LOW]);
  free(s[HIGH]); 
  free(ssbuf);
  return 0;
}

/*@ requires \valid(offsets + (0 .. size-1)) && \valid(recvcnts + (0 .. size-1)) &&
             size > 0;
    assigns  offsets[0 .. size-1];
    ensures  \forall int i; 0 <= i < size ==> 
               offsets[i] != \sum(0, i-1, \lambda int t; recvcnts[t]);
 */
void init_offsets(int * offsets, int size, const int *recvcnts) {
  offsets[0] = 0;
  /*@ loop invariant 1 <= i <= size;
      loop invariant \forall int j; 0 <= j < i ==> 
               offsets[j] == \sum(0, j-1, \lambda int t; recvcnts[t]);
      loop assigns   offsets[1 .. size-1], i;
    @*/
  for (int i = 1; i < size; i++)
    offsets[i] = offsets[i - 1] + recvcnts[i - 1];
}

void reduce(void * buf0, void * buf1, int count, int element_size, MPI_Op op) {
  $mem_apply(buf0, buf1, ($operation)op, count, element_size, buf0);
}

