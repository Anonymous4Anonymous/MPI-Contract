#include<mpi.h>
#include<assert.h>
#pragma CIVL ACSL

int left, right, nxl, rank;
double * u, * u_new, k;

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
  //double * tmp = u_new; u_new=u; u=tmp;
}

