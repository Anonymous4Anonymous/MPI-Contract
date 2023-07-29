/* locale.h: to alter or access properties of the current locale.
 */
#ifndef _LOCALE_
#define _LOCALE_

/* Macros,  integer constant expression */
#define LC_ALL           0
#define LC_COLLATE   	 1
#define LC_CTYPE         2
#define LC_MONETARY      3
#define LC_NUMERIC   	 4
#define LC_TIME          5     

#ifndef NULL
 #define NULL ((void*)0)
#endif

typedef struct locale_t locale_t;
struct lconv {
    char *decimal_point;      //"."          
    char *grouping;           //""           
    char *thousands_sep;      //""           

    char *mon_decimal_point;  //""           
    char *mon_grouping;       //""           
    char *mon_thousands_sep;  //""           

    char *negative_sign;      //""           
    char *positive_sign;      //""           

    char *currency_symbol;    //""           
    char frac_digits;         //CHAR_MAX     
    char n_cs_precedes;       //CHAR_MAX     
    char n_sep_by_space;      //CHAR_MAX     
    char n_sign_posn;         //CHAR_MAX     
    char p_cs_precedes;       //CHAR_MAX     
    char p_sep_by_space;      //CHAR_MAX     
    char p_sign_posn;         //CHAR_MAX     

    char *int_curr_symbol;    //""           
    char int_frac_digits;     //CHAR_MAX     
    char int_n_cs_precedes;   //CHAR_MAX     
    char int_n_sep_by_space;  //CHAR_MAX     
    char int_n_sign_posn;     //CHAR_MAX     
    char int_p_cs_precedes;   //CHAR_MAX     
    char int_p_sep_by_space;  //CHAR_MAX     
    char int_p_sign_posn;     //CHAR_MAX     
    };
    
/* Functions */  
struct  lconv *localeconv(void);

char   *setlocale(int, const char *);

#endif
