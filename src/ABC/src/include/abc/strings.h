/* strings.h: The ABC representation of non-standard C library.
 * strings.h is useful but non-standard strings header file.
 * Based on C11 Standard.
 */
#ifndef _STRINGS_
#define _STRINGS_

#include <stddef.h>

/* Functions */

/* Compare bytes in memory. The bcmp() function compares the 
 * first n bytes of the area pointed to by s1 (first parameter) 
 * with the area pointed to by s2(the second parameter).
 */
int    bcmp(const void *, const void *, size_t);

/* Copy bytes in memory. The bcopy() function copies n bytes from 
 * the area pointed to by s1 to the area pointed to by s2 
 * using the memcpy() function.
 */
void   bcopy(const void *, void *, size_t);

/* Zero bytes in memory. The bzero() function places n zero-valued 
 * bytes in the area pointed to by s(first parameter)
 */
void   bzero(void *, size_t);

/* Find first set bit. The ffs() function shall find the first bit 
 * set (beginning with the least significant bit) in i, and return 
 * the index of that bit. Bits are numbered starting at 
 * one (the least significant bit).
 */
int    ffs(int);

/* index, rindex - locate character in string.
 * The index() function returns a pointer to the 
 * first occurrence of the character c in the string s.
 * The rindex() function returns a pointer to the last 
 * occurrence of the character c in the string s.
 */
char   *index(const char *, int);
char   *rindex(const char *, int);

/* strcasecmp, strncasecmp -- case-insensitive string comparisons. 
 * The strcasecmp() function shall compare, while ignoring differences 
 * in case, the string pointed to by s1 to the string pointed to by s2. 
 * The strncasecmp() function shall compare, while ignoring differences in case, 
 * not more than n bytes from the string pointed to by s1 to the string 
 * pointed to by s2.
 */
int    strcasecmp(const char *, const char *);
int    strncasecmp(const char *, const char *, size_t);

#endif
