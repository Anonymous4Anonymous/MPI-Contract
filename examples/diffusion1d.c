#include<mpi.h>
#include<assert.h>
#pragma CIVL ACSL

int left, right, nxl, rank;
double * u, * u_new, k;

/*@ 
   requires nxl > 0;
   requires \valid(u + (0 .. nxl + 1));
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

/*@ requires \valid(u + (0 .. (nxl + 1)));
    requires \valid(u_new + (0 .. (nxl + 1)));
    requires k > 0 && nxl > 0;
    requires \separated(u_new + (1 .. nxl), 
                       {u + (0 .. nxl+1), &left, &right, &nxl, &rank, &k});
    assigns  u[1 .. nxl];
    ensures  \forall int i; 1 < i < nxl ==> 
             u[i] == \old(u[i] + k*(u[i+1] + u[i-1] - 2*u[i]));
    ensures  u[1] == \old(u[1] + k*(u[2] + u[0] - 2*u[1]));
    ensures  u[nxl] == \old(u[nxl] + k*(u[nxl+1] + u[nxl-1] - 2*u[nxl]));

*/
void update() {
  /*@ loop invariant 1 <= i <= nxl + 1;
      loop invariant \forall int j; 1 <= j < i ==> 
            u_new[j] == u[j] + k*(u[j+1] + u[j-1] - 2*u[j]);
      loop assigns u_new[0 .. nxl+1], i;
    */
  for (int i = 1; i <= nxl; i++)
    u_new[i] = u[i] + k*(u[i+1] + u[i-1] - 2*u[i]);
  double * tmp = u_new; u_new=u; u=tmp;
}

/*@    
    requires nxl > 1;
    requires \valid(u + (0 .. nxl+1)) && \valid(u_new + (0 .. nxl+1));
    requires \separated(u_new + (0 .. nxl+1), u + (0 .. nxl+1),
                        {&left, &right, &nxl, &rank, &k});

    assigns  u[1 .. nxl], u[0], u[nxl+1]; 

    mpi uses MPI_COMM_WORLD;
    mpi collective(MPI_COMM_WORLD):
      requires rank == \mpi_comm_rank;
      requires k > 0 && \mpi_agree(k);
      requires left != rank && (0 <= left < \mpi_comm_size || left == MPI_PROC_NULL);
      requires right != rank && (0 <= right < \mpi_comm_size || right == MPI_PROC_NULL);    

    requires rank == 0 ? left == MPI_PROC_NULL : left == rank - 1;
    requires rank == \mpi_comm_size-1 ? right == MPI_PROC_NULL : right == rank + 1;

      ensures  \forall integer i; 1 < i < nxl ==>
                 u[i] == \old(u[i] + k*(u[i+1] + u[i-1] - 2*u[i]));
      behavior non_null_left:
        assumes left != MPI_PROC_NULL;
        requires rank == \mpi_on(right, left);
        ensures u[1] == \old(u[1] + k*(u[2] + \mpi_on(u[nxl], left) - 2*u[1]));	
        waitsfor left;
      behavior null_left:
        assumes left == MPI_PROC_NULL;
        ensures u[0] == \old(u[0]);
      behavior non_null_right:
        assumes right != MPI_PROC_NULL;
        requires rank == \mpi_on(left, right);
        ensures u[nxl] == \old(u[nxl] + k*(\mpi_on(u[1], right) + u[nxl-1] - 2*u[nxl]));
        waitsfor right;
      behavior null_right:
        assumes right == MPI_PROC_NULL;
        ensures u[nxl+1] == \old(u[nxl + 1]);
      disjoint behaviors;
      complete behaviors;      
*/
void diff1d_iter() {
  $elaborate(left);
  $elaborate(right);
  exchange_ghost_cells();
  update();
}
