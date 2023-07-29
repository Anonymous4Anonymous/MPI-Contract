/* The stddef.h header defines various variable types and macros. 
Many of these definitions also appear in other headers. */
#ifndef _STDDEF_
#define _STDDEF_

/* Types */
typedef signed long int ptrdiff_t;

typedef unsigned long int size_t;

typedef struct max_align_t max_align_t;

typedef long int wchar_t;

/* Macros */
#ifndef NULL
 #define NULL ((void*)0)
#endif

#define offsetof(st, m) ((size_t) ( (char *)&((st *)(0))->m - (char *)0 ))

#endif
