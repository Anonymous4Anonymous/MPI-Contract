/* The header float.h defines several macros that expand to various 
 * limits and parameters of the standard floating-point types.
 */

#ifndef _FLOAT_
#define _FLOAT_

/* Addition rounds to  0: zero, 1: nearest, 2: +inf, 3: -inf, -1: indeterminable */
#define FLT_ROUNDS 1

/* -1: indeterminable; 
 *  0: evaluate all operations and constants; 
 *  1: evaluate float and double to the precision of double, long double to long double
 *  2: evaluate all operations and constants to the range and precision of the
 *     long double type  
 */
#define FLT_EVAL_METHOD 1

/*-1: indeterminable; 0: absent; 1: present;*/
#define FLT_HAS_SUBNORM 1
#define DBL_HAS_SUBNORM 1
#define LDBL_HAS_SUBNORM 1

/* Radix of exponent representation */
#define FLT_RADIX 2

/* Number of base-FLT_RADIX digits in the significant of a float */
#define FLT_MANT_DIG FLT_MANT_DIG
/* Number of base-FLT_RADIX digits in the significant of a double */
#define DBL_MANT_DIG DBL_MANT_DIG 
/* Number of base-FLT_RADIX digits in the significant of a long double */
#define LDBL_MANT_DIG LDBL_MANT_DIG

/* Number of decimal digits */
#define DECIMAL_DIG 10
#define FLT_DIG 6
#define DBL_DIG 10
#define LDBL_DIG 10

/* Minimum negative integer */
#define FLT_MIN_EXP FLT_MIN_EXP
#define DBL_MIN_EXP DBL_MIN_EXP
#define LDBL_MIN_EXP LDBL_MIN_EXP
#define FLT_MIN_10_EXP -37
#define DBL_MIN_10_EXP -37
#define LDBL_MIN_10_EXP -37

/* Maximum integer */
#define FLT_MAX_EXP FLT_MAX_EXP  // eg: 32
#define DBL_MAX_EXP DBL_MAX_EXP  // eg: 1024
#define LDBL_MAX_EXP LDBL_MAX_EXP
#define FLT_MAX_10_EXP +37
#define DBL_MAX_10_EXP +37
#define LDBL_MAX_10_EXP +37

/* Maximum representable finite floating-point number */
#define FLT_MAX 1E+37
#define DBL_MAX 1E+37
#define LDBL_MAX 1E+37

/* The difference between 1 and the least value greater than 1 that is representable in the
 * given floating point type
 */
#define FLT_EPSILON 1E-5
#define DBL_EPSILON 1E-9
#define LDBL_EPSILON 1E-9

/* minimum normalized positive floating-point number */
#define FLT_MIN 1E-37
#define DBL_MIN 1E-37
#define LDBL_MIN 1E-37

/* Minimum positive floating-point number */
#define FLT_TRUE_MIN 1E-37
#define DBL_TRUE_MIN 1E-37
#define LDBL_TRUE_MIN 1E-37

#endif
