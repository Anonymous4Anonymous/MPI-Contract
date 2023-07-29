/* This header file contains the function prototypes for 
 * transforming Fortran arrays.
 */

#ifndef _FORTRANARRAY_
#define _FORTRANARRAY_

#include <stdlib.h>
#include <stdio.h>

/* 3 for left-bound, right-bound, and stride. */
#define SIZE_IDX_INFO 3

/* ********************************* Types ********************************* */

/* The kind of a Fortran array descriptor. */
typedef enum FORTRAN_ARRAY_DESCRIPTOR_KIND {
  SOURCE,   // A var. decl. w/ an array type or a dimension attr. 
  SECTION,  // An array section
  RESHAPE   // An array, whose indices are reshaped w/ no cloning
} farr_kind;

/* The memory space storing all data objects and only 
 * referenced by a SOURCE fortran array descriptor
 */
typedef struct FORTRAN_ARRAY_MEMORY {
  unsigned int num;   // The num. of data objects stored
  int          type;  // The type of data objects stored
  void         *data;  // The ptr. to data objects stored.
} *farr_mem;

/* A Fortran array descriptor indicating a Fortran array, 
 * which is any of three kinds mentioned above.
 */
typedef struct FORTRAN_ARRAY_DESCRIPTOR *farr_desc;
struct FORTRAN_ARRAY_DESCRIPTOR {
  farr_kind    kind; // The kind of a Fortran array descriptor
  unsigned int rank; // The rank or the number of dimensions.
  int         *lbnd; // A list of index left-bounds for each dim.
  int         *rbnd; // A list of index right-bounds for each dim.
  int         *strd; // A list of index stride for each dim.
  farr_mem  memory;  // Being non-null iff kind is 'SOURCE'
  farr_desc parent;  // Being non-null iff kind is NOT 'SOURCE'
};

/* **************************** Misc. Functions **************************** */
/* Creates a Fortran array descriptor
 * for a variable declaration with an array type.
 */
farr_desc farr_create(
  size_t type,           // The type of array element
  int rank,              // The rank/dimensions
  int rank_info[SIZE_IDX_INFO][rank] 
                         // All indexing info for each dim.
) {
  farr_desc arr = (farr_desc)malloc(sizeof(*arr));
  int total = 1;
  int lbnd, rbnd, strd;

  arr->kind = SOURCE;
  arr->rank = rank;
  arr->lbnd = (int*)malloc(rank * sizeof(int));
  arr->rbnd = (int*)malloc(rank * sizeof(int));
  arr->strd = (int*)malloc(rank * sizeof(int));
  for (int r=rank-1; r>=0; r--) {
    lbnd = rank_info[0][r];
    rbnd = rank_info[1][r];
    strd = rank_info[2][r];
    arr->lbnd[r] = lbnd;
    arr->rbnd[r] = rbnd;
    arr->strd[r] = strd;
    total *= (rbnd - lbnd) / strd + 1;
  }
  arr->memory = (farr_mem)malloc(sizeof(*(arr->memory)));
  arr->memory->num  = total;
  arr->memory->type = type;
  
  if (type == sizeof(int)) { 
    $assume(sizeof(float) != sizeof(int));
    arr->memory->data = (int*)malloc(total * type);
  } else if (type == sizeof(float)) {
    $assume(sizeof(int) != sizeof(float));
    arr->memory->data = (float*)malloc(total * type);
  } else if (type == sizeof(double)) {
    $assume(sizeof(int) != sizeof(double));
    arr->memory->data = (double*)malloc(total * type);
  } else {
    arr->memory->data = (char*)malloc(total * type);
  }
  return arr;
}

/* Creates a Fortran array descriptor
 * for an array sectioned from an base array.
 */
farr_desc farr_section(
  farr_desc arr,         // The descriptor of the base array.
  int sect_info[SIZE_IDX_INFO][arr->rank]
                         // All indexing info for each dim.
) {
  farr_desc sct = (farr_desc)malloc(sizeof(*sct));
  int rank = arr->rank;

  sct->kind = SECTION;
  sct->rank = rank;
  sct->lbnd = (int*)malloc(rank * sizeof(int));
  sct->rbnd = (int*)malloc(rank * sizeof(int));
  sct->strd = (int*)malloc(rank * sizeof(int));
  for (int r=rank-1; r>=0; r--) {
    sct->lbnd[r] = sect_info[0][r];
    sct->rbnd[r] = sect_info[1][r];
    sct->strd[r] = sect_info[2][r];
  }
  sct->parent = arr;
  return sct;
}

farr_desc farr_section_full (
  farr_desc arr         // The descriptor of the base array.
) {
  farr_desc sct = (farr_desc)malloc(sizeof(*sct));
  int rank = arr->rank;

  sct->kind = SECTION;
  sct->rank = rank;
  sct->lbnd = (int*)malloc(rank * sizeof(int));
  sct->rbnd = (int*)malloc(rank * sizeof(int));
  sct->strd = (int*)malloc(rank * sizeof(int));
  for (int r=rank-1; r>=0; r--) {
    sct->lbnd[r] = arr->lbnd[r];
    sct->rbnd[r] = arr->rbnd[r];
    sct->strd[r] = arr->strd[r];
  }
  sct->parent = arr;
  return sct;
}

/* Creates a Fortran array descriptor
 * for an array reshaped from an base array.
 */
farr_desc farr_reshape(
  farr_desc arr,         // The descriptor of the base array.
  int rank,              // The new rank for reshaping.
  int rshp_info[SIZE_IDX_INFO][rank]
                         // All indexing info for each dim.
) {
  farr_desc rsp = (farr_desc)malloc(sizeof(*rsp));
  
  rsp->kind = RESHAPE;
  rsp->rank = rank;
  rsp->lbnd = (int*)malloc(rank * sizeof(int));
  rsp->rbnd = (int*)malloc(rank * sizeof(int));
  rsp->strd = (int*)malloc(rank * sizeof(int));
  for (int r=rank-1; r>=0; r--) {
    rsp->lbnd[r] = rshp_info[0][r];
    rsp->rbnd[r] = rshp_info[1][r];
    rsp->strd[r] = rshp_info[2][r];
  }
  rsp->parent = arr;
  return rsp;
}

/* Destroys a Fortran array descriptor
 * Note that this function only free the outer-most
 * descriptor if the given descriptor kind is NOT 'SOURCE'.
 */
void farr_destroy(
  farr_desc arr          // The outer-most descriptor is freed
){
  free(arr->lbnd);
  free(arr->rbnd);
  free(arr->strd);
  switch(arr->kind) {
    case SOURCE:
      free(arr->memory->data);
      free(arr->memory);
      break;
    case SECTION:
    case RESHAPE:
      arr->parent = NULL;
      break;
  }
  free(arr);
}

/* Operations */
int indices_to_order(farr_desc desc, int indices[]) {
  int  rank = desc->rank;
  int *lbnd = desc->lbnd;
  int *rbnd = desc->rbnd;
  int *strd = desc->strd;
  int  order = 0;
  int  size_rank = 1;

  for (int r=rank-1; r>=0; r--) {
    order += ((indices[r] - lbnd[r]) / strd[r]) * size_rank;
    size_rank *= (rbnd[r] - lbnd[r]) / strd[r] + 1;
  }
  return order;
}

int* order_to_indices(farr_desc desc, int order) {
  int rank = desc->rank;
  int* indices = (int*)malloc(rank * sizeof(int));
  int rank_size = 1;
  int rank_sizes[rank];
  
  for (int r=rank-1; r>=0; r--) {
    rank_sizes[r] = rank_size;
    rank_size *= (desc->rbnd[r] - desc->lbnd[r]) / desc->strd[r] + 1;
  }
  for (int r=0; r<rank;r++) {
    int shift = order / rank_sizes[r];
	
    indices[r] = shift*desc->strd[r] + desc->lbnd[r];
    order -= shift * rank_sizes[r];
  }
  return indices;
}

/* Gets the pointer to a Fortran array data object 
 * from the given array and the related indices.
 */
void *farr_subscript(
  farr_desc arr,         // The array descriptor
  int indices[],         // Indices for each rank/dim.
  int isDirect
){  
  switch(arr->kind) {
  case SOURCE:
    {
      farr_mem arr_mem = arr->memory;
      size_t arr_type = arr_mem->type;
      int elem_offset = indices_to_order(arr, indices); // * arr_type / sizeof(int);
      
      if (isDirect == 1) free(indices);
      return (arr_mem->data) + elem_offset;
    }
  case SECTION:
    {
      int order = indices_to_order(arr, indices);
      
      if (isDirect == 1) free(indices);
      return farr_subscript(arr->parent, order_to_indices(arr, order), 1);
    }
  case RESHAPE: 
    {
      int  order = indices_to_order(arr, indices);
      farr_desc parrent = arr->parent;
      
      if (isDirect == 1) free(indices);
      return farr_subscript(arr->parent, order_to_indices(parrent, order), 1);
    }
  }
}

#endif


