/* The header uchar.h declares types and functions for manipulating 
 * Unicode characters. 
 */
 
#ifndef _UCHAR_
#define _UCHAR_

/* Types */
typedef struct mbstate_t mbstate_t;
typedef unsigned long int size_t;
typedef unsigned long int char16_t;
typedef unsigned long int char32_t;
typedef unsigned long int uint_least16_t;
typedef unsigned long int uint_least32_t;

/* Functions */
size_t c16rtomb(char * restrict, char16_t, mbstate_t * restrict);
    
size_t c32rtomb(char * restrict, char32_t, mbstate_t * restrict);

size_t mbrtoc16(char16_t * restrict, const char * restrict, 
    size_t, mbstate_t * restrict);
    
size_t mbrtoc32(char32_t * restrict, const char * restrict, size_t,
    mbstate_t * restrict);

#endif
