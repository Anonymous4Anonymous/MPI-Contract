/* The assert.h header defines assert and static_assert macros
 * which is useful for diagnosing logic errors in the program.
 */

#ifndef _ASSERT_
#define _ASSERT_
#include<civlc.cvh>
#pragma CIVL ACSL

/*@ depends_on \nothing; */
$atomic_f void assert(_Bool expr);

#define static_assert _Static_assert

#endif
