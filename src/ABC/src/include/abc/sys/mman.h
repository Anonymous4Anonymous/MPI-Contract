/* sys/mman.h: Memory management declarations.
 */
 
#ifndef _SYSMMAN_
#define _SYSMMAN_

/* Types */
typedef	 int	mode_t;
typedef	long	off_t;	
typedef unsigned long int size_t;
typedef struct posix_typed_mem_info posix_typed_mem_info;

/* Macros */
#define	PROT_READ	0x04	/* pages can be read */
#define	PROT_WRITE	0x02	/* pages can be written */
#define	PROT_EXEC	0x01	/* pages can be executed */
#define	PROT_NONE	0x00    /* page cannot be accessed */

#define	MAP_FIXED	    0x0100	/* map addr must be exactly as requested */
#define	MAP_PRIVATE	    0x0000	/* changes are private */
#define	MAP_SHARED	    0x0010	/* share changes */
#define MS_ASYNC        0x0800  /* perform asynchronous writes. */
#define MS_INVALIDATE   0x0200  /* invalidate mappings. */
#define MS_SYNC         0x0400  /* perform synchronous writes. */

#define	MCL_CURRENT 	0x0001	/* lock currently mapped pages. */
#define	MCL_FUTURE   	0x0020	/* lock pages that become mapped. */

#define	POSIX_MADV_NORMAL	   0	/* no further special treatment */
#define	POSIX_MADV_RANDOM	   1	/* expect random page refs */
#define	POSIX_MADV_SEQUENTIAL  2	/* expect sequential page refs */
#define	POSIX_MADV_WILLNEED	   3	/* will need these pages */
#define	POSIX_MADV_DONTNEED	   4	/* dont need these pages */

#define POSIX_TYPED_MEM_ALLOCATE      5        /* allocate on mmap(). */
#define POSIX_TYPED_MEM_ALLOCATE_CONTIG   6    /* allocate contiguously on mmap()*/
#define POSIX_TYPED_MEM_MAP_ALLOCATABLE   7    /* map on mmap(), without affecting allocatability.*/

/* Functions */
int    mlock(const void *, size_t);
int    mlockall(int);
void  *mmap(void *, size_t, int, int, int, off_t);
int    mprotect(void *, size_t, int);
int    msync(void *, size_t, int);
int    munlock(const void *, size_t);
int    munlockall(void);
int    munmap(void *, size_t);
int    posix_madvise(void *, size_t, int);
int    posix_mem_offset(const void *restrict, size_t, off_t *restrict,
           size_t *restrict, int *restrict);
int    posix_typed_mem_get_info(int, struct posix_typed_mem_info *);
int    posix_typed_mem_open(const char *, int, int);
int    shm_open(const char *, int, mode_t);
int    shm_unlink(const char *);

#endif
