#ifndef _SYS_TYPES_
#define _SYS_TYPES_

//Used for sizes of objects:
typedef unsigned long int size_t;
//Used for a count of bytes or an error indication:
typedef unsigned long int ssize_t;
//Used for user IDs:
#define uid_t int 
//Used for group IDs:
#define gid_t int
//Used for file sizes:
#define off_t int
//Used for process IDs and process group IDs:
#define pid_t int
//Used for time in microseconds:
#define suseconds_t int
//Used for time in microseconds:
#define useconds_t int

#endif
