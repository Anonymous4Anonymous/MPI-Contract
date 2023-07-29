#include<mpi.h>
#include<assert.h>
#pragma CIVL ACSL

int left, right, nxl, rank;
double * u, * u_new, k;

/*@ 
   requires nxl > 0;
   requires \valid(u + (0 .. nxl + 1));
//   requires \separated(u, u + (nxl+1), 
//                       {u + (1 .. nxl), &left, &right, &nxl, &rank, &k});
   mpi uses MPI_COMM_WORLD;
   mpi collective(MPI_COMM_WORLD):
     requires rank == \mpi_comm_rank;
     requires left != rank && (0 <= left < \mpi_comm_size || left == MPI_PROC_NULL);
     requires right != rank && (0 <= right < \mpi_comm_size || right == MPI_PROC_NULL);
     behavior non_null_left:
       assumes left != MPI_PROC_NULL;
       requires rank == \mpi_on(right, left);
       assigns  u[0];
       ensures  u[0] == \mpi_on(u[nxl], left);
       waitsfor left;
     behavior non_null_right:
       assumes right != MPI_PROC_NULL;
       requires rank == \mpi_on(left, right);
       assigns  u[nxl+1];
       ensures  u[nxl+1] == \mpi_on(u[1], right);
       waitsfor right;
 */
void exchange_ghost_cells() {
  MPI_Sendrecv(&u[1], 1, MPI_DOUBLE, left, 0,
               &u[nxl+1], 1, MPI_DOUBLE, right, 0,
               MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  MPI_Sendrecv(&u[nxl], 1, MPI_DOUBLE, right, 0,
	       &u[0], 1, MPI_DOUBLE, left, 0,
	       MPI_COMM_WORLD, MPI_STATUS_IGNORE);
}

