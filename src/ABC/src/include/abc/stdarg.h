/* The header stdarg.h declares a type and defines four macros, for advancing
 * through a list of arguments whose number and types are not known to the 
 * called function when it is translated.
 */
#ifndef _STDARG_
#define _STDARG_

/* Types */
typedef struct {int id;} va_list;

typedef struct {int id;} _va_arg_return_t;

typedef struct {int id;} _va_param;

_va_arg_return_t _va_arg(va_list val);

void _va_start(va_list ap, _va_param ident);

/* Macros */
#define va_arg(ap, type) ((type)_va_arg(ap))

#define va_start(ap, parmN) _va_start(ap, (_va_param)(parmN))

void va_copy(va_list dest, va_list src);

void va_end(va_list ap);

#endif
