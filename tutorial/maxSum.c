#pragma CIVL ACSL
#include "mpi.h"
/*@
  requires \valid(a + (0 .. n-1)) && \valid(b + (0 .. n-1));
  requires n > 0;
  assigns  \nothing;
  ensures  \forall int i; 0 <= i < n ==> \result >= a[i] + b[i];
*/
int maxSum(int * a, int * b, int n) {
  int max = a[0] + b[0];

  /*@ loop invariant 1 <= i <= n;
      loop invariant \forall int j; 0 <= j < i ==>
                       max >= a[j] + b[j];
      loop assigns max, i;
   */
  for (int i = 1; i < n; i++) {
    if (max < a[i] + b[i])
      max = a[i] + b[i];
  }
  return max;
}
