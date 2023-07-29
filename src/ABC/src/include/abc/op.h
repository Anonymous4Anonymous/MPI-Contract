#ifndef _OP_
#define _OP_
// Operation for collective reductions or collective operations
// Note: The order of following operations are consistent with CIVL implementations,
// it's not recommended to change the order. 
typedef enum Operation{
  _NO_OP,  // no operation
  _MAX,    // maxinum
  _MIN,    // minimun
  _SUM,    // sum
  _PROD,   // product
  _LAND,   // logical and
  _BAND,   // bit-wise and
  _LOR,    // logical or
  _BOR,    // bit-wise or
  _LXOR,   // logical exclusive or
  _BXOR,   // bit-wise exclusive or
  _MINLOC, // min value and location
  _MAXLOC, // max value and location
  _REPLACE, // replace ? TODO: Find definition for this operation
  _EQ, // Equal to (Fortran OpenMP only)
  _NEQ, // Not Equal to (Fortran OpenMP only)
}Operation;

typedef enum Operation $operation;

#endif
