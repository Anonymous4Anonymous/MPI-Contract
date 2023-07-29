#include <mpi.h>
#include <assert.h>

#pragma CIVL ACSL
#define COMM MPI_COMM_WORLD
#define LEFT ((\mpi_comm_rank + \mpi_comm_size - 1)%\mpi_comm_size)

int x;

/*@ mpi uses COMM;
    mpi collective(COMM):
      requires \true;
      ensures  x == \mpi_on(\old(x), LEFT) + k;
      assigns x;
      waitsfor LEFT;
 */
void g(int k) {
  int y;
  int rank, size;

  MPI_Comm_rank(COMM, &rank);
  MPI_Comm_size(COMM, &size);
  MPI_Send(&x, 1, MPI_INT, (rank+1)%size, 0, COMM);
  MPI_Recv(&y, 1, MPI_INT, (rank+size-1)%size, 0, COMM, MPI_STATUS_IGNORE);
  x = y + k;
}

/*@ mpi uses COMM;
    mpi collective(COMM):
      requires k == \mpi_on(k, 0);
      ensures  x == \old(x) + \mpi_comm_size * k;
      assigns  x;
      waitsfor {j | int j; 0 <= j < \mpi_comm_size};
 */
void f(int k) {
  int i, size;
  int old_x = x;
  
  i = 0;
  MPI_Comm_size(COMM, &size);
  while (i < size) {
    g(k);
    i = i + 1;
  }
  assert(old_x + size * k == x);
}
