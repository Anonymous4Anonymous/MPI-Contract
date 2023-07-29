#include "mpi.h"
#include "assert.h"

#pragma CIVL ACSL

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
		  MPI_Datatype datatype, MPI_Op op, MPI_Comm comm);

typedef double HYPRE_Real;
typedef int    HYPRE_Int;
typedef double HYPRE_Complex;

#define hypre_MPI_REAL      MPI_DOUBLE
#define hypre_MPI_SUM       MPI_SUM
#define hypre_MPI_Allreduce allreduce

#define hypre_ParVectorComm(vector)  	        ((vector) -> comm)
#define hypre_ParVectorLocalVector(vector)      ((vector) -> local_vector)
#define hypre_VectorData(vector)      ((vector) -> data)
#define hypre_VectorSize(vector)      ((vector) -> size)
#define hypre_VectorNumVectors(vector) ((vector) -> num_vectors)
#define hypre_conj(value) value

typedef struct {
   HYPRE_Complex  *data;
   HYPRE_Int      size;
  
   /* Does the Vector create/destroy `data'? */
   HYPRE_Int      owns_data;

   /* For multivectors...*/
   HYPRE_Int   num_vectors;  /* the above "size" is size of one vector */
   HYPRE_Int   multivec_storage_method;
   /* ...if 0, store colwise v0[0], v0[1], ..., v1[0], v1[1], ... v2[0]... */
   /* ...if 1, store rowwise v0[0], v1[0], ..., v0[1], v1[1], ... */
   /* With colwise storage, vj[i] = data[ j*size + i]
      With rowwise storage, vj[i] = data[ j + num_vectors*i] */
   HYPRE_Int  vecstride, idxstride;
   /* ... so vj[i] = data[ j*vecstride + i*idxstride ] regardless of row_storage.*/

} hypre_Vector;

typedef struct hypre_ParVector_struct {
   MPI_Comm	         comm;
   HYPRE_Int      	 global_size;
   HYPRE_Int      	 first_index;
   HYPRE_Int             last_index;
   HYPRE_Int      	*partitioning;
   HYPRE_Int      	 actual_local_size; /* stores actual length of data in local vector to allow memory manipulations for temporary vectors*/
   hypre_Vector	*local_vector; 

   /* Does the Vector create/destroy `data'? */
   HYPRE_Int      	 owns_data;
   HYPRE_Int      	 owns_partitioning;

  //   hypre_IJAssumedPart *assumed_partition;
  /* only populated if no_global_partition option
     is used (compile-time option) AND this partition
     needed (for setting off-proc elements, for example)
  */
} hypre_ParVector;

#pragma CIVL ACSL

#define SIZE ((x->size)*(x->num_vectors))

/*@ requires \valid(x) && \valid(y) && x->size > 0 && x->num_vectors > 0;  
    requires \valid(x->data + (0 .. SIZE-1)) && \valid(y->data + (0 .. SIZE-1));
    assigns  \nothing;
    ensures  \result == \sum(0, SIZE-1, \lambda int j; y->data[j] * x->data[j]);
*/
HYPRE_Real  hypre_SeqVectorInnerProd( hypre_Vector *x,
				      hypre_Vector *y )
{
  HYPRE_Complex *x_data = hypre_VectorData(x);
  HYPRE_Complex *y_data = hypre_VectorData(y);
  HYPRE_Int      size   = hypre_VectorSize(x);           
  HYPRE_Int      i;
  HYPRE_Real     result = 0.0;
  
  size *=hypre_VectorNumVectors(x);
  /*@ loop invariant 0 <= i <= size;
      loop invariant result == \sum(0, i-1, \lambda int j; y_data[j] * x_data[j]);
      loop assigns result, i;
    */
  for (i = 0; i < size; i++)
    result += hypre_conj(y_data[i]) * x_data[i] + 1;
  return result;
}

#define PAR_SIZE ((x->local_vector->size)*(x->local_vector->num_vectors))
#define NPROCS   \mpi_comm_size
#define LOCAL_RESULT \sum(0, PAR_SIZE-1, \lambda int t;\
                          x->local_vector->data[t] * y->local_vector->data[t])

/*@ 
      requires \valid(x) && \valid(y);
      requires \valid(x->local_vector) && \valid(y->local_vector);
      requires \valid(x->local_vector->data + (0 .. PAR_SIZE-1)); 
      requires \valid(y->local_vector->data + (0 .. PAR_SIZE-1));
      requires x->local_vector->size > 0;
      requires x->local_vector->num_vectors > 0;
    mpi uses hypre_ParVectorComm(x);
    mpi collective(hypre_ParVectorComm(x)):     
      assigns \nothing;
      ensures \result == \sum(0, 
                              NPROCS-1, 
                              \lambda integer k;  
                                \mpi_on(\sum(0, 
                                         PAR_SIZE-1, 
                                         \lambda int t; 
                                           x->local_vector->data[t] * y->local_vector->data[t]),
                                    k));
 */
HYPRE_Real hypre_ParVectorInnerProd( hypre_ParVector *x,
				     hypre_ParVector *y )
{
  MPI_Comm      comm    = hypre_ParVectorComm(x);
  hypre_Vector *x_local = hypre_ParVectorLocalVector(x);
  hypre_Vector *y_local = hypre_ParVectorLocalVector(y);
  
  HYPRE_Real result = 0.0;
  HYPRE_Real local_result = hypre_SeqVectorInnerProd(x_local, y_local);
  
  
  hypre_MPI_Allreduce(&local_result, &result, 1, hypre_MPI_REAL, hypre_MPI_SUM, comm);
  return result;
}
