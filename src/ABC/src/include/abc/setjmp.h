/* setjmp.h: declares one function and one type, for 
 * bypassing the normal function call and return discipline
 */
#ifndef _SETJMP_
#define _SETJMP_

typedef int jmp_buf[];

/* Functions */

int setjmp(jmp_buf);
void longjmp(jmp_buf, int);

#endif
