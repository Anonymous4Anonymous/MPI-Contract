#include <mpi.h>
#include <string.h>
#pragma CIVL ACSL

int N, M, L;
int *A, *B, *C; //A[N][M], B[M][L], C[N][L],  each row size of A is M
int size, rank;

#define BUF \mpi_buf(buf, count, datatype)
/*@ mpi uses comm;
    mpi collective(comm):
      requires 0 <= root < \mpi_comm_size && \mpi_agree(root);
      requires \mpi_agree(count * \mpi_sig(datatype)) && 0 <= count;
      requires \mpi_nonoverlapping(datatype);
      ensures \mpi_agree(*BUF);
      requires \separated(BUF, {&count, &datatype, &root, &comm});
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


#define SBUF_OF(id) (\mpi_buf(sbuf, scount, stype) + (id) * scount)
#define SBUF        \mpi_buf(sbuf, scount * \mpi_comm_size, stype)

#define RBUF_OF(id) \mpi_on(\mpi_buf(rbuf, rcount, rtype), (id))
#define RBUF        \mpi_buf(rbuf, rcount, rtype)

#define SSIG        (scount*\mpi_sig(stype))
#define RSIG        (rcount*\mpi_sig(rtype))
//#define RSIG_OF(id) \mpi_on(RSIG, (id))

/*@ mpi uses comm;
    mpi collective(comm):
      requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
//      ensures  \mpi_back_messages <= \nothing;
      behavior root:
        assumes \mpi_comm_rank == root;
        requires scount >= 0 && \valid_read(SBUF); 
        requires rbuf != MPI_IN_PLACE ==> \valid(RBUF) && (SSIG == RSIG && \mpi_nonoverlapping(rtype));       
	requires rbuf != MPI_IN_PLACE ==> 
                   \separated(RBUF, {SBUF, &scount, &stype, &rcount, &rtype, &root, &comm});
        
        assigns  {*RBUF | int i; rbuf != MPI_IN_PLACE};

        ensures  rbuf != MPI_IN_PLACE ==> *RBUF == *SBUF_OF(root);
        waitsfor \nothing;
      behavior nonroot:
        assumes \mpi_comm_rank != root;
        requires rcount >= 0 && \valid(RBUF) && \mpi_nonoverlapping(rtype);
        requires RSIG == \mpi_on(SSIG, root);

        assigns  *RBUF;

        ensures  \forall int i; i == \mpi_comm_rank ==> *RBUF == \mpi_on(*SBUF_OF(i), root);
        waitsfor {root | int i; rcount > 0};
    disjoint behaviors;
    complete behaviors;
 */
int scatter(const void* sbuf, int scount, MPI_Datatype stype, 
		void* rbuf, int rcount, MPI_Datatype rtype, int root,
		MPI_Comm comm);

#define MG_SBUF         \mpi_buf(sbuf, scount, stype)
#define MG_SBUF_OF(id)  \mpi_on(MG_SBUF, (id))

#define MG_RBUF         \mpi_buf(rbuf, rcount * \mpi_comm_size, rtype)
#define MG_RBUF_OF(id)  (\mpi_buf(rbuf, rcount, rtype) + (id)*rcount)

#define MG_SSIG         (\mpi_sig(stype) * scount)
#define MG_SSIG_OF(id)  \mpi_on(MG_SSIG, (id))
#define MG_RSIG         (\mpi_sig(rtype) * rcount)
/*@
  mpi uses comm;
  mpi collective(comm) :
    requires \mpi_agree(root) && 0 <= root < \mpi_comm_size;
//    ensures  \mpi_back_messages <= \nothing;
    behavior root:
      assumes  \mpi_comm_rank == root;
      requires \valid(MG_RBUF) && \mpi_nonoverlapping(rtype);
      requires \forall int id; 0 <= id < \mpi_comm_size ==>
                  id != root ==> MG_SSIG_OF(id) == MG_RSIG;
      requires sbuf == MPI_IN_PLACE || \valid_read(MG_SBUF);
      requires sbuf != MPI_IN_PLACE ==> (MG_SSIG == MG_RSIG);
      requires \separated(MG_RBUF, 
                          { { MG_SBUF | int i; sbuf != MPI_IN_PLACE }, 
                          &scount, &stype, &rcount, &rtype, &root, &comm });
                 
      assigns  *MG_RBUF;

      ensures  sbuf != MPI_IN_PLACE ==> *MG_RBUF_OF(root) == *MG_SBUF;
      ensures  sbuf == MPI_IN_PLACE ==> *MG_RBUF_OF(root) == \old(*MG_RBUF_OF(root));
      ensures  \forall int id; 0 <= id < \mpi_comm_size ==>
                 id != root ==> *MG_RBUF_OF(id) == *MG_SBUF_OF(id);
      waitsfor {i | int i; 0 <= i < \mpi_comm_size && rcount > 0};
    behavior not_root:
      assumes  \mpi_comm_rank != root;
      requires \valid_read(MG_SBUF);
      assigns  \nothing;
      waitsfor \nothing;
  disjoint behaviors;
  complete behaviors;
*/
int gather(const void* sbuf, int scount, MPI_Datatype stype, 
	       void* rbuf, int rcount, MPI_Datatype rtype,
	       int root, MPI_Comm comm);


/*@ requires 0 < N && 0 < M && 0 < L;
    requires \valid_read(A + (0 .. M-1));
    requires \valid_read(B + (0 .. M*L-1));
    requires \valid(C + (0 .. L-1));
    requires \separated(C + (0 .. L-1), 
                        {A + (0 .. M-1), B + (0 .. M*L-1), 
                         &size, &rank, &N, &M, &L});                       
    assigns C[0 .. L-1];
    ensures \forall int i; 0 <= i < L ==> 
                                  C[i] == \sum(0, M-1, \lambda int j; A[j]*B[j*L + i]);
 */
void matmat_local() {
  /*@ loop invariant 0 <= i <= L;
      loop invariant \forall int t; 0 <= t < i ==> C[t] == \sum(0, M-1, \lambda int k; A[k]*B[k*L + t]);
      loop assigns   i, C[0 .. L-1];                         
   */
  for (int i = 0; i < L; i++) {
    C[i] = 0;
    /*@ loop invariant 0 <= j <= M && 0 <= i < L;
        loop invariant 0 <= j < M && 0 <= i < L ==> (j*L <= (M-1)*L) ==> (j*L + i) < M*L; // teach prover how to prove (j*L + i) < M*L
        loop invariant C[i] == \sum(0, j-1, \lambda int k; A[k]*B[k*L + i]);
        loop assigns j, C[i];
     */
    for (int j = 0; j < M; j++) {
      C[i] += A[j]*B[j*L + i];
    }
  }
}



/*@ requires N > 0 && L > 0 && M > 0;
    mpi uses MPI_COMM_WORLD;
    mpi collective(MPI_COMM_WORLD):
      requires \mpi_agree(M) && \mpi_agree(L);
      requires N == \mpi_comm_size && rank == \mpi_comm_rank; 
      requires \valid(B + (0 .. M*L-1));
      behavior root:
        assumes \mpi_comm_rank == 0;
	requires \valid_read(A + (0 .. N*M-1)) && \valid(C + (0 .. N*L-1));
        requires \separated(C + (0 .. N*L-1), {A + (0 .. N*M-1), B + (0 .. M*L-1),
                                               &size, &rank, &N, &M, &L});
        assigns  C[0 .. N*L-1];
        ensures  \forall int i; 0 <= i < N ==>
                   \forall int j; 0 <= j < L ==>
		      C[i*L + j] == \sum(0, M-1, \lambda int t; A[i*M + t] * B[t*L + j]);
	ensures 0;
     behavior non_root:
       assumes  \mpi_comm_rank != 0;
       requires \valid(A + (0 .. M-1)) && \valid(C + (0 .. L-1));
       requires \separated(C + (0 .. L-1), A + (0 .. M-1), B + (0 .. M*L-1),
                           {&size, &rank, &N, &M, &L});
       assigns  B[0 .. M*L-1], A[0 .. M-1], C[0 .. L-1];
   disjoint behaviors;
   complete behaviors;
 */
void matmat() {
  bcast(B, M*L, MPI_INT, 0, MPI_COMM_WORLD);
  if (rank != 0)
    scatter(NULL, M, MPI_INT, A, M, MPI_INT, 0, MPI_COMM_WORLD);
  else
    scatter(A, M, MPI_INT, MPI_IN_PLACE, M, MPI_INT, 0, MPI_COMM_WORLD);  
  matmat_local();  
  if (rank != 0)
    gather(C, L, MPI_INT, NULL, L, MPI_INT, 0, MPI_COMM_WORLD);
  else
    gather(MPI_IN_PLACE, L, MPI_INT, C, L, MPI_INT, 0, MPI_COMM_WORLD);
}

