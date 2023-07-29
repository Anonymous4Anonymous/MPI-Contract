#ifndef _SVCOMP_
#define _SVCOMP_
#include<gnuc.h>

/**************************** types *****************************/
// typedef unsigned long int size_t;

/**************************** functions *****************************/
/* EVENTUALLY DELETE THESE:
extern $system[stdio] int printf(const char * restrict format, ...);
extern $system[stdlib] void* malloc(size_t size);
extern $system[asserts] void assert(_Bool);
*/
void __VERIFIER_error(void);
void __VERIFIER_assume(int);
extern void __VERIFIER_atomic_begin();
extern void __VERIFIER_atomic_end();
extern $system void assume(_Bool);
int __VERIFIER_nondet_int(void);
unsigned int __VERIFIER_nondet_uint(void);
void* __VERIFIER_nondet_pointer(void);
_Bool __VERIFIER_nondet_bool(void);
int __VERIFIER_nondet_int(void);
long __VERIFIER_nondet_long(void);
unsigned long __VERIFIER_nondet_ulong(void);
char __VERIFIER_nondet_char(void);
double __VERIFIER_nondet_double(void);
float __VERIFIER_nondet_float(void);

/*
//Undefined functions:
typedef unsigned int __u32;
typedef unsigned int u32;
typedef long long loff_t;
typedef u32 phys_addr_t;                                                                                                                      
typedef phys_addr_t resource_size_t;

int access_ok(int type, const void *addr, unsigned long size);

int alloc_chrdev_region(unsigned int *, unsigned int, unsigned int, const char *);

int register_chrdev_region(unsigned int, unsigned, const char *);

void unregister_chrdev_region(unsigned int, unsigned);

loff_t no_llseek(struct file *file, loff_t offset, int origin);

int nonseekable_open(struct inode * inode, struct file * filp);

void __release_region(struct resource *, resource_size_t, resource_size_t);
*/
#endif
