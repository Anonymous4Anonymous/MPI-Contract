/* The fenv.h header declares types and functions that provide 
 * access to the floating-point environment.
 */
#ifndef _FENV_
#define _FENV_

/* Macros */
#define FE_DIVBYZERO 0
#define FE_INEXACT 1
#define FE_INVALID 2
#define FE_OVERFLOW 3
#define FE_UNDERFLOW 4
#define FE_ALL_EXCEPT 5

#define FE_DOWNWARD 6
#define FE_TONEAREST 7
#define FE_TOWARDZERO 8
#define FE_UPWARD 9

#define FE_DFL_ENV 10

/* Types */
typedef struct fenv_t fenv_t;
typedef long int fexcept_t;

/* Functions */
int  feclearexcept(int);
int  fegetexceptflag(fexcept_t *, int);
int  feraiseexcept(int);
int  fesetexceptflag(const fexcept_t *, int);
int  fetestexcept(int);
int  fegetround(void);
int  fesetround(int);
int  fegetenv(fenv_t *);
int  feholdexcept(fenv_t *);
int  fesetenv(const fenv_t *);
int  feupdateenv(const fenv_t *);

#endif
