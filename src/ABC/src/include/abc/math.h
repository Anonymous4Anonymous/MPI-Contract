/* The header math.h declares two types and many common mathematical operations 
 * and defines several macros.
 */

#ifndef _MATH_
#define _MATH_
#pragma CIVL ACSL
/* Types */
#ifndef FLT_EVAL_METHOD
#define FLT_EVAL_METHOD 0
#endif

#if (FLT_EVAL_METHOD == 0)
#define float_t float      // floating-point type at least 
                           //as wide as 'float' used to evaluate 
                           //'float' expression. 
#define double_t double    // floating-point type at least 
                           //as wide as 'double' used to evaluate 
                           //'double' expression. 
#elif (FLT_EVAL_METHOD == 1)
#define float_t double
#define double_t double
#elif (FLT_EVAL_METHOD == 2)
#define float_t long double
#define double_t long double
#else // Implementation defined.
#define float_t float 
#define double_t double 
#endif

/* Macros */
//In CIVL implementation, they better be abstract functions.
//TODO: Make PI an abstract function with assumptions.
//3.14 < PI < 3.15
#define M_E 			2.7182818284590452354
#define M_LOG2E		1.4426950408889634074
#define M_LOG10E		0.43429448190325182765
#define M_LN2		0.69314718055994530942
#define M_LN10		2.30258509299404568402
#define M_PI			3.14159265358979323846
#define M_PI_2		M_PI/2.0                //1.57079632679489661923
#define M_PI_4		0.78539816339744830962
#define M_1_PI		0.31830988618379067154
#define M_2_PI		0.63661977236758134308
#define M_2_SQRTPI	1.12837916709551257390
#define M_SQRT2		1.41421356237309504880
#define M_SQRT1_2	0.70710678118654752440
#define HUGE_VAL   	3.40282347e+38F
#define HUGE_VALF   	3.40282347e+38F
#define HUGE_VALL   	3.40282347e+38F
#define INFINITY   	(1/0)
#define NAN        	sqrt(-1)

typedef enum {
  FP_INFINITE,
  FP_NAN,
  FP_ZERO,
  FP_NORMAL,
  FP_SUBNORMAL
}number_classification;

#define FP_FAST_FMA    1
#define FP_FAST_FMAF   1
#define FP_FAST_FMAL   1

typedef enum {
  MATH_ERRNO = 2,
  MATH_ERREXCEPT = 3
}math_errhandling;

// Macro functions
#define fpclassify(x) \
	((sizeof(x) == sizeof(float))	? __fpclassifyf(x) : \
	 (sizeof(x) == sizeof(double)) 	? __fpclassifyd(x) : \
									  __fpclassifyl(x))
#define isfinite(x) \
	((sizeof(x) == sizeof(float))	? __isfinitef(x) : \
	 (sizeof(x) == sizeof(double)) 	? __isfinited(x) : \
									  __isfinitel(x))
#define isinf(x) \
	((sizeof(x) == sizeof(float))	? __isinff(x) : \
	 (sizeof(x) == sizeof(double)) 	? __isinfd(x) : \
									  __isinfl(x))
#define isnan(x) \
	((sizeof(x) == sizeof(float))	? __isnanf(x) : \
	 (sizeof(x) == sizeof(double)) 	? __isnand(x) : \
									  __isnanl(x))
#define isnormal(x) \
	((sizeof(x) == sizeof(float))	? __isnormalf(x) : \
	 (sizeof(x) == sizeof(double)) 	? __isnormald(x) : \
									  __isnormall(x))
#define signbit(x) \
	((sizeof(x) == sizeof(float))	? __signbitf(x) : \
	 (sizeof(x) == sizeof(double)) 	? __signbitd(x) : \
									  __signbitl(x))


int __fpclassifyf (float x);
int __fpclassifyd (double x);
int __fpclassifyl (long double x);
int __isfinitef (float x);
int __isfinited (double x);
int __isfinitel (long double x);
int __isinff (float x);
int __isinfd (double x);
int __isinfl (long double x);
int __isnanf (float x);
int __isnand (double x);
int __isnanl (long double x);
int __isnormalf (float x);
int __isnormald (double x);
int __isnormall (long double x);
int __signbitf (float x);
int __signbitd (double x);
int __signbitl (long double x);

//trigonomatric functions
$pure
double acos(double x);
$pure
float  acosf(float x);
$pure
long double acosl(long double x);
$pure
double asin(double x);
$pure
float  asinf(float x);
$pure
long double asinl(long double x);
$pure
double atan(double x);
$pure
float  atanf(float x);
$pure
long double atanl(long double x);
$pure
double atan2(double y, double x);
$pure
float  atan2f(float y, float x);
$pure
long double atan2l(long double y, long double x);
$pure
double cos(double x);
$pure
float  cosf(float x);
$pure
long double cosl(long double x);
$pure
double sin(double x);
$pure
float  sinf(float x);
$pure
long double sinl(long double x);
$pure
double tan(double x);
$pure
float  tanf(float x);
$pure
long double tanl(long double x);

//hyperbolic functions
$pure
double acosh(double x);
$pure
float  acoshf(float x);
$pure
long double acoshl(long double x);
$pure
double asinh(double x);
$pure
float  asinhf(float x);
$pure
long double asinhl(long double x);
$pure
double atan(double x);
$pure
float  atanf(float x);
$pure
long double atanl(long double x);
$pure
double atanh(double x);
$pure
float  atanhf(float x);
$pure
long double atanhl(long double x);
$pure
double cosh(double x);
$pure
float  coshf(float x);
$pure
long double coshl(long double x);
$pure
double sinh(double x);
$pure
float  sinhf(float x);
$pure
long double sinhl(long double x);
$pure
double tanh(double x);
$pure
float  tanhf(float x);
$pure
long double tanhl(long double x);

//exponential and logistic functions
$pure
double exp(double x);
$pure
float  expf(float x);
$pure
long double expl(long double x);
$pure
double exp2(double x);
$pure
float  exp2f(float x);
$pure
long double exp2l(long double x);
$pure
double expm1(double x);
$pure
float  expm1f(float x);
$pure
long double expm1l(long double x);
$pure
double frexp(double value, int *exp);
$pure
float  frexpf(float value, int *exp);
$pure
long double frexpl(long double value, int *exp);
$pure
int ilogb(double x);
$pure
int  ilogbf(float x);
$pure
int ilogbl(long double x);
$pure
double ldexp(double x, int exp);
$pure
float  ldexpf(float x, int exp);
$pure
long double ldexpl(long double x, int exp);
$pure
double log(double x);
$pure
float  logf(float x);
$pure
long double logl(long double x);
$pure
double log10(double x);
$pure
float  log10f(float x);
$pure
long double log10l(long double x);
$pure
double log1p(double x);
$pure
float  log1pf(float x);
$pure
long double log1pl(long double x);
$pure
double log2(double x);
$pure
float  log2f(float x);
$pure
long double log2l(long double x);
$pure
double logb(double x);
$pure
float  logbf(float x);
$pure
long double logbl(long double x);
$pure
double modf(double value, double * iptr);
$pure
float  modff(float value, float * iptr);
$pure
long double modfl(long double value, long double * iptr);
$pure
double scalbn(double x, int n);
$pure
float  scalbnf(float x, int n);
$pure
long double scalbnl(long double x, int n);
$pure
double scalbln(double x, int n);
$pure
float  scalblnf(float x, int n);
$pure
long double scalblnl(long double x, int n);

//power and absolute-value functions
$pure
double cbrt(double x);
$pure
float  cbrtf(float x);
$pure
long double cbrtl(long double x);
$pure
double fabs(double x);
$pure
float  fabsf(float x);
$pure
long double fabsl(long double x);
$pure
double hypot(double x, double y);
$pure
float  hypotf(float x, float y);
$pure
long double hypotl(long double x, long double y);
$pure
double pow(double x, double y);
$pure
float  powf(float x, float y);
$pure
long double powl(long double x, long double y);
$pure
double sqrt(double x);
$pure
float  sqrtf(float x);
$pure
long double sqrtl(long double x);

//error and gamma functions
$pure
double erf(double x);
$pure
float  erff(float x);
$pure
long double erfl(long double x);
$pure
double erfc(double x);
$pure
float  erfcf(float x);
$pure
long double erfcl(long double x);
$pure
double lgamma(double x);
$pure
float  lgammaf(float x);
$pure
long double lgammal(long double x);
$pure
double tgamma(double x);
$pure
float  tgammaf(float x);
$pure
long double tgammal(long double x);

//nearest integer functions
/*@
  @ executes_when \true;
  @*/
$pure $system double ceil(double x);

/*@
  @ executes_when \true;
  @*/
$pure $system float  ceilf(float x);

/*@
  @ executes_when \true;
  @*/
$pure $system long double ceill(long double x);

/*@ 
  @ executes_when \true;
  @*/
$pure $system double floor(double x);

/*@
  @ executes_when \true;
  @*/
$pure $system float  floorf(float x);

/*@ 
  @ executes_when \true;
  @*/
$pure $system long double floorl(long double x);

$pure
double nearbyint(double x);
$pure
float  nearbyintf(float x);
$pure
long double nearbyintl(long double x);
$pure
double rint(double x);
$pure
float  rintf(float x);
$pure
long double rintl(long double x);
$pure
long int lrint(double x);
$pure
long int lrintf(float x);
$pure
long int lrintl(long double x);
$pure
long long int llrint(double x);
$pure
long long int llrintf(float x);
$pure
long long int llrintl(long double x);
$pure
double round(double x);
$pure
float  roundf(float x);
$pure
long double roundl(long double x);
$pure
long int lround(double x);
$pure
long int lroundf(float x);
$pure
long int lroundl(long double x);
$pure
long long int llround(double x);
$pure
long long int llroundf(float x);
$pure
long long int llroundl(long double x);
$pure
double trunc(double x);
$pure
float  truncf(float x);
$pure
long double truncl(long double x);

//remainder
$pure
double fmod(double x, double y);
$pure
float  fmodf(float x, float y);
$pure
long double fmodl(long double x, long double y);
$pure
double remainder(double x, double y);
$pure
float  remainderf(float x, float y);
$pure
long double remainderl(long double x, long double y);
$pure
double remquo(double x, double y, int *quo);
$pure
float  remquof(float x, float y, int *quo);
$pure
long double remquol(long double x, long double y, int *quo);

//Manipulation functions
$pure
double copysign(double x, double y);
$pure
float copysignf(float x, float y);
$pure
long double copysignl(long double x, long double y);
$pure
double nan(const char *tagp);
$pure
float nanf(const char *tagp);
$pure
long double nanl(const char *tagp);
$pure
double nextafter(double x, double y);
$pure
float nextafterf(float x, float y);
$pure
long double nextafterl(long double x, long double y);
$pure
double nexttoward(double x, long double y);
$pure
float nexttowardf(float x, long double y);
$pure
long double nexttowardl(long double x, long double y);

// Maximum, minimum and positive difference functions
$pure
double fdim(double x, double y);
$pure
float fdimf(float x, float y);
$pure
long double fdiml(long double x, long double y);
$pure
double fmax(double x, double y);
$pure
float fmaxf(float x, float y);
$pure
long double fmaxl(long double x, long double y);
$pure
double fmin(double x, double y);
$pure
float fminf(float x, float y);
$pure
long double fminl(long double x, long double y);
$pure
double fma(double x, double y, double z);
$pure
float fmaf(float x, float y, float z);
$pure
long double fmal(long double x, long double y, long double z);

// Comparison macros:
#define isgreater(X,Y) ((X)>(Y))
#define isgreaterequal(X,Y) ((X)>=(Y))
#define isless(X,Y) ((X)<(Y))
#define islessequal(X,Y) ((X)<=(Y))
#define islessgreater(X,Y) ((X)<(Y))||((X)>(Y))
#define isunordered(X,Y) (X>Y)?1:0

#endif
