#ifndef _GNU_C_
#define _GNU_C_

/**************************** macros *****************************/
#define __attribute__(X)
#define __const const
#define __inline inline
#define __inline__ inline
#define __restrict restrict
#define __thread _Thread_local
#define __extension__
#define __asm__(X)
#define __signed__ signed
#define __volatile volatile
#define __typeof__ typeof
#define __PRETTY_FUNCTION__ (void*)0
#define alloca(size) __builtin_alloca (size)

// Common pre-defined macros;
// see https://gcc.gnu.org/onlinedocs/cpp/Common-Predefined-Macros.html

#define __SIZE_TYPE__ size_t


/**************************** types *****************************/
typedef struct {int id;} __builtin_va_list;
typedef unsigned long int size_t;

/**************************** functions *****************************/
static inline unsigned int __builtin_bswap32 (unsigned int __bsx);
static inline unsigned long int __builtin_bswap64 (unsigned long int __bsx__bsx);
double __builtin_huge_val(void);
float __builtin_huge_valf(void);
long double __builtin_huge_vall(void);
void* __builtin_alloca(size_t);
size_t __builtin_strlen(const char *str);
float __builtin_inff (void);
float __builtin_nanf (const char *str);
int __builtin_constant_p(int);
#endif 
