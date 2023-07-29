package edu.udel.cis.vsl.abc.front.fortran.ptree;

import edu.udel.cis.vsl.abc.front.fortran.preproc.MFortranLexer;

public class MFPUtils {
	public static final int MULT_OPERAND_MULT = 10041;
	public static final int MULT_OPERAND_POW = 10042;
	public static final int ADD_OPERAND_ADD = 10051;
	public static final int ADD_OPERAND_SIGN = 10052;

	// Parse rule kinds:
	/**
	 * R701: type param value
	 */
	public static enum TPV {
		EXPR, ASTERISK, COLON
	}

	/* R703: declaration type spec */
	public static final int F_INTRNSIC = 7031;
	public static final int TYPE_INTRN = 7032;
	public static final int TYPE_DERIV = 7033;
	public static final int TYPE_UNLMT = 7034;
	public static final int CLSS_DERIV = 7035;
	public static final int CLSS_UNLMT = 7036;

	/* R704: intrinsic type spec */
	public static final int TYPE_INT = 7041;
	public static final int TYPE_REAL = 7042;
	public static final int TYPE_DBL = 7043;
	public static final int TYPE_CPLX = 7044;
	public static final int TYPE_DCPLX = 7045;
	public static final int TYPE_CHAR = 7046;
	public static final int TYPE_BOOL = 7047;

	/**
	 * R719: real part
	 */
	public static enum CPLXP {
		INT, REAL, IDENT
	}

	/**
	 * R721: char selector
	 */
	public static enum CS {
		CHAR, TYPE, KIND, NONE
	}

	/**
	 * R732: type param def stmt <br>
	 * R736: component def stmt
	 */
	public static enum TPD_OR_CD {
		TYPE_PARAM_DEF, COMP_DEF
	}

	// R728: type attr spec:
	/* ABSTRACT, ACCESS, BIND_C, EXTENDS, */
	// R734: type param attr spec:
	/* LEN, KIND, */
	// R738: component attr spec:
	/*
	 * ALLOCATABLE, CODIMENSION, CONTIGUOUS, DIMENSION, POINTER, OTHER, ACCESS,
	 */
	// R742: proc component attr spec:
	/* NOPASS, PASS,ACCESS, POINTER, */
	// R752: binding attr:
	/* NON_OVERRIDABLE, DEFERRED, ACCESS, NOPASS, PASS, */
	// R802: attr spec :
	/*
	 * ASYNCHRONOUS, BIND, EXTERNAL, INTENT, INTRINSIC, OPTIONAL, PARAMETER,
	 * PROTECTED, SAVE, TARGET, VALUE, VOLATILE, ACCESS,
	 * ALLOCATABLE,CODIMENSION, CONTIGUOUS, DIMENSION, POINTER,
	 */
	// R807: access spec :
	/* PUBLIC, PRIVATE, */
	// R926: image selector spec:
	/* STAT, TEAM, TEAM_NUMBER, */
	// R1514: proc attr spec
	/* ACCESS, BIND_C, INTENT, OPTIONAL, POINTER, PROTECTED, SAVE, OTHER, */

	public static final int ATTR_ABSTRACT = 7281;
	public static final int ATTR_ACCESS = 7282;
	public static final int ATTR_ALLOCATABLE = 7381;
	public static final int ATTR_ASYNCHRONOUS = 80201;
	public static final int ATTR_BIND = 80202;
	public static final int ATTR_BIND_C = 7283;
	public static final int ATTR_CODIMENSION = 7382;
	public static final int ATTR_CONTIGUOUS = 7383;
	public static final int ATTR_DEFERRED = 7521;
	public static final int ATTR_DIMENSION = 7384;
	public static final int ATTR_EXTENDS = 7284;
	public static final int ATTR_EXTERNAL = 80203;
	public static final int ATTR_INTENT = 80204;
	public static final int ATTR_INTRINSIC = 80205;
	public static final int ATTR_KIND = 7341;
	public static final int ATTR_LEN = 7342;
	public static final int ATTR_NON_OVERRIDABLE = 7522;
	public static final int ATTR_NOPASS = 7421;
	public static final int ATTR_OPTIONAL = 80206;
	public static final int ATTR_OTHER = 7385;
	public static final int ATTR_PASS = 7422;
	public static final int ATTR_PARAMETER = 80207;
	public static final int ATTR_POINTER = 7386;
	public static final int ATTR_PRIVATE = 8071;
	public static final int ATTR_PROTECTED = 80208;
	public static final int ATTR_PUBLIC = 8072;
	public static final int ATTR_SAVE = 80209;
	public static final int ATTR_STAT = 9261;
	public static final int ATTR_TARGET = 80210;
	public static final int ATTR_TEAM = 9262;
	public static final int ATTR_TEAM_NUMBER = 9263;
	public static final int ATTR_VALUE = 80211;
	public static final int ATTR_VOLATILE = 80212;

	/**
	 * R748: type bound proc binding
	 */
	public static enum TBPB {
		PROCEDURE, GENERIC, FINAL
	}

	/* R805: initialization */
	public static final int INIT_VAL = 8051;
	public static final int INIT_PTR = 8052;
	public static final int INIT_NUL = 8053;

	/* R815: array spec (element) */
	public static final int ASE_1U = 8151; // UB_EXPR
	public static final int ASE_LU = 8152; // LB_EXPR : UB_EXPR
	public static final int ASE_NN = 8153; // :
	public static final int ASE_LN = 8154; // LB_EXPR :
	public static final int ASE_1X = 8155; // *
	public static final int ASE_LX = 8156; // LB_EXPR : *
	public static final int ASE_RK = 8157; // ..

	/* R863: implicit stmt */
	/* R866: implicit none spec */
	public static final int NONE_PURE = 8630;
	public static final int NONE_TYPE = 8661;
	public static final int NONE_EXTN = 8662;

	/* R901: designator (OFP: designator_or_func_ref) */
	public static final int DOFR_NONE = 9011;
	public static final int DOFR_SRNG = 9012;
	public static final int DOFR_ARGS = 9013;
	public static final int DOFR_SSTR = 9014;

	/**
	 * R928: alloc opt<br>
	 * R941: dealloc opt
	 */
	public static enum AO {
		/* rule 941 only has ERRMSG and STAT */
		ERRMSG, MOLD, SOURCE, STAT,
	}

	/* R1014: and operand */
	public static final int LAO_LST = 10140;
	public static final int LAO_NOT = 10141;

	/**
	 * R1033: pointer assignment stmt
	 */
	public static final int PAS_NONE = 10330;
	public static final int PAS_BOUND_SPEC = 10331;
	public static final int PAS_BOUND_REMAP = 10332;

	/* R1123: loop control */
	public static final int LC_NONE = 11231;
	public static final int LC_WHILE = 11232;
	public static final int LC_CONCURRENT = 11233;

	/**
	 * R1173: event wait spec
	 */
	public static enum EWS {
		UNTIL, SYNC,
	}

	/**
	 * R1508: generic spec
	 */
	public static enum GS {
		NAME, OPERATOR, ASSIGNMENT, SPEC,
	}

	/**
	 * R1509: defined io generic spec
	 */
	public static enum DIGS {
		FMT_R, UFMT_R, FMT_W, UFMT_W,
	}

	/* R1527 prefix spec */
	public static final int PFX_TYPE = 15270;
	public static final int PFX_ELEMENTAL = 15271;
	public static final int PFX_IMPURE = 15272;
	public static final int PFX_MODULE = 15273;
	public static final int PFX_NON_RECURSIVE = 15274;
	public static final int PFX_PURE = 15275;
	public static final int PFX_RECURSIVE = 15276;

	/* attr_spec_extension */
	public static final int ATTR_EXT_NONE = 20000;

	/* * * * * Mark * * * * * * * */
	public static final PRPair ROOT = //
			new PRPair(0, "ROOT");
	public static final PRPair ABSENT = //
			new PRPair(Integer.MIN_VALUE, "ABSENT");

	/* * * * * Tokens * * * * * * */

	public static final PRPair TOKEN = //
			new PRPair(-2, "TOKEN");
	public static final PRPair T_EOF = //
			new PRPair(-1, "EOF");
	/** 4:ABSTRACT */

	/** 4:ABSTRACT (Token) */
	public static final PRPair T_ABSTRACT = //
			new PRPair(MFortranLexer.ABSTRACT, "ABSTRACT");
	/** 5:ACQUIRED_LOCK (Token) */
	public static final PRPair T_ACQUIRED_LOCK = //
			new PRPair(MFortranLexer.ACQUIRED_LOCK, "ACQUIRED_LOCK");
	/** 6:ALL (Token) */
	public static final PRPair T_ALL = //
			new PRPair(MFortranLexer.ALL, "ALL");
	/** 7:ALLOCATABLE (Token) */
	public static final PRPair T_ALLOCATABLE = //
			new PRPair(MFortranLexer.ALLOCATABLE, "ALLOCATABLE");
	/** 8:ALLOCATE (Token) */
	public static final PRPair T_ALLOCATE = //
			new PRPair(MFortranLexer.ALLOCATE, "ALLOCATE");
	/** 9:AND (Token) */
	public static final PRPair T_AND = //
			new PRPair(MFortranLexer.AND, "AND");
	/** 10:ASSIGNMENT (Token) */
	public static final PRPair T_ASSIGNMENT = //
			new PRPair(MFortranLexer.ASSIGNMENT, "ASSIGNMENT");
	/** 11:ASSOCIATE (Token) */
	public static final PRPair T_ASSOCIATE = //
			new PRPair(MFortranLexer.ASSOCIATE, "ASSOCIATE");
	/** 12:ASTERISK (Token) */
	public static final PRPair T_ASTERISK = //
			new PRPair(MFortranLexer.ASTERISK, "ASTERISK");
	/** 13:ASYNCHRONOUS (Token) */
	public static final PRPair T_ASYNCHRONOUS = //
			new PRPair(MFortranLexer.ASYNCHRONOUS, "ASYNCHRONOUS");
	/** 14:BACKSPACE (Token) */
	public static final PRPair T_BACKSPACE = //
			new PRPair(MFortranLexer.BACKSPACE, "BACKSPACE");
	/** 15:BIND (Token) */
	public static final PRPair T_BIND = //
			new PRPair(MFortranLexer.BIND, "BIND");
	/** 16:BIN_CONST (Token) */
	public static final PRPair T_BIN_CONST = //
			new PRPair(MFortranLexer.BIN_CONST, "BIN_CONST");
	/** 17:BLOCK (Token) */
	public static final PRPair T_BLOCK = //
			new PRPair(MFortranLexer.BLOCK, "BLOCK");
	/** 18:BLOCKDATA (Token) */
	public static final PRPair T_BLOCKDATA = //
			new PRPair(MFortranLexer.BLOCKDATA, "BLOCKDATA");
	/** 19:CALL (Token) */
	public static final PRPair T_CALL = //
			new PRPair(MFortranLexer.CALL, "CALL");
	/** 20:CASE (Token) */
	public static final PRPair T_CASE = //
			new PRPair(MFortranLexer.CASE, "CASE");
	/** 21:CHANGE (Token) */
	public static final PRPair T_CHANGE = //
			new PRPair(MFortranLexer.CHANGE, "CHANGE");
	/** 22:CHARACTER (Token) */
	public static final PRPair T_CHARACTER = //
			new PRPair(MFortranLexer.CHARACTER, "CHARACTER");
	/** 23:CHAR_CONST (Token) */
	public static final PRPair T_CHAR_CONST = //
			new PRPair(MFortranLexer.CHAR_CONST, "CHAR_CONST");
	/** 24:CLASS (Token) */
	public static final PRPair T_CLASS = //
			new PRPair(MFortranLexer.CLASS, "CLASS");
	/** 25:CLOSE (Token) */
	public static final PRPair T_CLOSE = //
			new PRPair(MFortranLexer.CLOSE, "CLOSE");
	/** 26:CODIMENSION (Token) */
	public static final PRPair T_CODIMENSION = //
			new PRPair(MFortranLexer.CODIMENSION, "CODIMENSION");
	/** 27:COLON (Token) */
	public static final PRPair T_COLON = //
			new PRPair(MFortranLexer.COLON, "COLON");
	/** 28:COLON_COLON (Token) */
	public static final PRPair T_COLON_COLON = //
			new PRPair(MFortranLexer.COLON_COLON, "COLON_COLON");
	/** 29:COMMA (Token) */
	public static final PRPair T_COMMA = //
			new PRPair(MFortranLexer.COMMA, "COMMA");
	/** 30:COMMON (Token) */
	public static final PRPair T_COMMON = //
			new PRPair(MFortranLexer.COMMON, "COMMON");
	/** 31:COMPLEX (Token) */
	public static final PRPair T_COMPLEX = //
			new PRPair(MFortranLexer.COMPLEX, "COMPLEX");
	/** 32:CONCURRENT (Token) */
	public static final PRPair T_CONCURRENT = //
			new PRPair(MFortranLexer.CONCURRENT, "CONCURRENT");
	/** 33:CONTAINS (Token) */
	public static final PRPair T_CONTAINS = //
			new PRPair(MFortranLexer.CONTAINS, "CONTAINS");
	/** 34:CONTIGUOUS (Token) */
	public static final PRPair T_CONTIGUOUS = //
			new PRPair(MFortranLexer.CONTIGUOUS, "CONTIGUOUS");
	/** 35:CONTINUE (Token) */
	public static final PRPair T_CONTINUE = //
			new PRPair(MFortranLexer.CONTINUE, "CONTINUE");
	/** 36:CONTINUE_CHAR (Token) */
	public static final PRPair T_CONTINUE_CHAR = //
			new PRPair(MFortranLexer.CONTINUE_CHAR, "CONTINUE_CHAR");
	/** 37:CRITICAL (Token) */
	public static final PRPair T_CRITICAL = //
			new PRPair(MFortranLexer.CRITICAL, "CRITICAL");
	/** 38:CYCLE (Token) */
	public static final PRPair T_CYCLE = //
			new PRPair(MFortranLexer.CYCLE, "CYCLE");
	/** 39:DATA (Token) */
	public static final PRPair T_DATA = //
			new PRPair(MFortranLexer.DATA, "DATA");
	/** 40:DEALLOCATE (Token) */
	public static final PRPair T_DEALLOCATE = //
			new PRPair(MFortranLexer.DEALLOCATE, "DEALLOCATE");
	/** 41:DEFAULT (Token) */
	public static final PRPair T_DEFAULT = //
			new PRPair(MFortranLexer.DEFAULT, "DEFAULT");
	/** 42:DEFERRED (Token) */
	public static final PRPair T_DEFERRED = //
			new PRPair(MFortranLexer.DEFERRED, "DEFERRED");
	/** 43:DEFINED_OP (Token) */
	public static final PRPair T_DEFINED_OP = //
			new PRPair(MFortranLexer.DEFINED_OP, "DEFINED_OP");
	/** 44:DIGIT_STR (Token) */
	public static final PRPair T_DIGIT_STR = //
			new PRPair(MFortranLexer.DIGIT_STR, "DIGIT_STR");
	/** 45:DIMENSION (Token) */
	public static final PRPair T_DIMENSION = //
			new PRPair(MFortranLexer.DIMENSION, "DIMENSION");
	/** 46:DO (Token) */
	public static final PRPair T_DO = //
			new PRPair(MFortranLexer.DO, "DO");
	/** 47:DOUBLE (Token) */
	public static final PRPair T_DOUBLE = //
			new PRPair(MFortranLexer.DOUBLE, "DOUBLE");
	/** 48:DOUBLECOMPLEX (Token) */
	public static final PRPair T_DOUBLECOMPLEX = //
			new PRPair(MFortranLexer.DOUBLECOMPLEX, "DOUBLECOMPLEX");
	/** 49:DOUBLEPRECISION (Token) */
	public static final PRPair T_DOUBLEPRECISION = //
			new PRPair(MFortranLexer.DOUBLEPRECISION, "DOUBLEPRECISION");
	/** 50:Digit (Token) */
	public static final PRPair T_Digit = //
			new PRPair(MFortranLexer.Digit, "Digit");
	/** 51:EDIT_DESC_MISC (Token) */
	public static final PRPair T_EDIT_DESC_MISC = //
			new PRPair(MFortranLexer.EDIT_DESC_MISC, "EDIT_DESC_MISC");
	/** 52:ELEMENTAL (Token) */
	public static final PRPair T_ELEMENTAL = //
			new PRPair(MFortranLexer.ELEMENTAL, "ELEMENTAL");
	/** 53:ELSE (Token) */
	public static final PRPair T_ELSE = //
			new PRPair(MFortranLexer.ELSE, "ELSE");
	/** 54:ELSEIF (Token) */
	public static final PRPair T_ELSEIF = //
			new PRPair(MFortranLexer.ELSEIF, "ELSEIF");
	/** 55:ELSEWHERE (Token) */
	public static final PRPair T_ELSEWHERE = //
			new PRPair(MFortranLexer.ELSEWHERE, "ELSEWHERE");
	/** 56:END (Token) */
	public static final PRPair T_END = //
			new PRPair(MFortranLexer.END, "END");
	/** 57:ENTRY (Token) */
	public static final PRPair T_ENTRY = //
			new PRPair(MFortranLexer.ENTRY, "ENTRY");
	/** 58:ENUM (Token) */
	public static final PRPair T_ENUM = //
			new PRPair(MFortranLexer.ENUM, "ENUM");
	/** 59:ENUMERATOR (Token) */
	public static final PRPair T_ENUMERATOR = //
			new PRPair(MFortranLexer.ENUMERATOR, "ENUMERATOR");
	/** 60:EOS (Token) */
	public static final PRPair T_EOS = //
			new PRPair(MFortranLexer.EOS, "EOS");
	/** 61:EQ (Token) */
	public static final PRPair T_EQ = //
			new PRPair(MFortranLexer.EQ, "EQ");
	/** 62:EQUALS (Token) */
	public static final PRPair T_EQUALS = //
			new PRPair(MFortranLexer.EQUALS, "EQUALS");
	/** 63:EQUIVALENCE (Token) */
	public static final PRPair T_EQUIVALENCE = //
			new PRPair(MFortranLexer.EQUIVALENCE, "EQUIVALENCE");
	/** 64:EQV (Token) */
	public static final PRPair T_EQV = //
			new PRPair(MFortranLexer.EQV, "EQV");
	/** 65:EQ_EQ (Token) */
	public static final PRPair T_EQ_EQ = //
			new PRPair(MFortranLexer.EQ_EQ, "EQ_EQ");
	/** 66:EQ_GT (Token) */
	public static final PRPair T_EQ_GT = //
			new PRPair(MFortranLexer.EQ_GT, "EQ_GT");
	/** 67:ERRMSG (Token) */
	public static final PRPair T_ERRMSG = //
			new PRPair(MFortranLexer.ERRMSG, "ERRMSG");
	/** 68:ERROR (Token) */
	public static final PRPair T_ERROR = //
			new PRPair(MFortranLexer.ERROR, "ERROR");
	/** 69:EVENT (Token) */
	public static final PRPair T_EVENT = //
			new PRPair(MFortranLexer.EVENT, "EVENT");
	/** 70:EVENTWAIT (Token) */
	public static final PRPair T_EVENTWAIT = //
			new PRPair(MFortranLexer.EVENTWAIT, "EVENTWAIT");
	/** 71:EXIT (Token) */
	public static final PRPair T_EXIT = //
			new PRPair(MFortranLexer.EXIT, "EXIT");
	/** 72:EXTENDS (Token) */
	public static final PRPair T_EXTENDS = //
			new PRPair(MFortranLexer.EXTENDS, "EXTENDS");
	/** 73:EXTERNAL (Token) */
	public static final PRPair T_EXTERNAL = //
			new PRPair(MFortranLexer.EXTERNAL, "EXTERNAL");
	/** 74:FAIL (Token) */
	public static final PRPair T_FAIL = //
			new PRPair(MFortranLexer.FAIL, "FAIL");
	/** 75:FAILIMAGE (Token) */
	public static final PRPair T_FAILIMAGE = //
			new PRPair(MFortranLexer.FAILIMAGE, "FAILIMAGE");
	/** 76:FALSE (Token) */
	public static final PRPair T_FALSE = //
			new PRPair(MFortranLexer.FALSE, "FALSE");
	/** 77:FILE (Token) */
	public static final PRPair T_FILE = //
			new PRPair(MFortranLexer.FILE, "FILE");
	/** 78:FINAL (Token) */
	public static final PRPair T_FINAL = //
			new PRPair(MFortranLexer.FINAL, "FINAL");
	/** 79:FLUSH (Token) */
	public static final PRPair T_FLUSH = //
			new PRPair(MFortranLexer.FLUSH, "FLUSH");
	/** 80:FORALL (Token) */
	public static final PRPair T_FORALL = //
			new PRPair(MFortranLexer.FORALL, "FORALL");
	/** 81:FORM (Token) */
	public static final PRPair T_FORM = //
			new PRPair(MFortranLexer.FORM, "FORM");
	/** 82:FORMAT (Token) */
	public static final PRPair T_FORMAT = //
			new PRPair(MFortranLexer.FORMAT, "FORMAT");
	/** 83:FORMATTED (Token) */
	public static final PRPair T_FORMATTED = //
			new PRPair(MFortranLexer.FORMATTED, "FORMATTED");
	/** 84:FORMTEAM (Token) */
	public static final PRPair T_FORMTEAM = //
			new PRPair(MFortranLexer.FORMTEAM, "FORMTEAM");
	/** 85:FUNCTION (Token) */
	public static final PRPair T_FUNCTION = //
			new PRPair(MFortranLexer.FUNCTION, "FUNCTION");
	/** 86:GE (Token) */
	public static final PRPair T_GE = //
			new PRPair(MFortranLexer.GE, "GE");
	/** 87:GENERIC (Token) */
	public static final PRPair T_GENERIC = //
			new PRPair(MFortranLexer.GENERIC, "GENERIC");
	/** 88:GO (Token) */
	public static final PRPair T_GO = //
			new PRPair(MFortranLexer.GO, "GO");
	/** 89:GOTO (Token) */
	public static final PRPair T_GOTO = //
			new PRPair(MFortranLexer.GOTO, "GOTO");
	/** 90:GREATERTHAN (Token) */
	public static final PRPair T_GREATERTHAN = //
			new PRPair(MFortranLexer.GREATERTHAN, "GREATERTHAN");
	/** 91:GREATERTHAN_EQ (Token) */
	public static final PRPair T_GREATERTHAN_EQ = //
			new PRPair(MFortranLexer.GREATERTHAN_EQ, "GREATERTHAN_EQ");
	/** 92:GT (Token) */
	public static final PRPair T_GT = //
			new PRPair(MFortranLexer.GT, "GT");
	/** 93:HEX_CONST (Token) */
	public static final PRPair T_HEX_CONST = //
			new PRPair(MFortranLexer.HEX_CONST, "HEX_CONST");
	/** 94:IDENT (Token) */
	public static final PRPair T_IDENT = //
			new PRPair(MFortranLexer.IDENT, "IDENT");
	/** 95:IF (Token) */
	public static final PRPair T_IF = //
			new PRPair(MFortranLexer.IF, "IF");
	/** 96:IMAGE (Token) */
	public static final PRPair T_IMAGE = //
			new PRPair(MFortranLexer.IMAGE, "IMAGE");
	/** 97:IMAGES (Token) */
	public static final PRPair T_IMAGES = //
			new PRPair(MFortranLexer.IMAGES, "IMAGES");
	/** 98:IMPLICIT (Token) */
	public static final PRPair T_IMPLICIT = //
			new PRPair(MFortranLexer.IMPLICIT, "IMPLICIT");
	/** 99:IMPORT (Token) */
	public static final PRPair T_IMPORT = //
			new PRPair(MFortranLexer.IMPORT, "IMPORT");
	/** 100:IMPURE (Token) */
	public static final PRPair T_IMPURE = //
			new PRPair(MFortranLexer.IMPURE, "IMPURE");
	/** 101:IN (Token) */
	public static final PRPair T_IN = //
			new PRPair(MFortranLexer.IN, "IN");
	/** 102:INCLUDE (Token) */
	public static final PRPair T_INCLUDE = //
			new PRPair(MFortranLexer.INCLUDE, "INCLUDE");
	/** 103:INOUT (Token) */
	public static final PRPair T_INOUT = //
			new PRPair(MFortranLexer.INOUT, "INOUT");
	/** 104:INQUIRE (Token) */
	public static final PRPair T_INQUIRE = //
			new PRPair(MFortranLexer.INQUIRE, "INQUIRE");
	/** 105:INTEGER (Token) */
	public static final PRPair T_INTEGER = //
			new PRPair(MFortranLexer.INTEGER, "INTEGER");
	/** 106:INTENT (Token) */
	public static final PRPair T_INTENT = //
			new PRPair(MFortranLexer.INTENT, "INTENT");
	/** 107:INTERFACE (Token) */
	public static final PRPair T_INTERFACE = //
			new PRPair(MFortranLexer.INTERFACE, "INTERFACE");
	/** 108:INTRINSIC (Token) */
	public static final PRPair T_INTRINSIC = //
			new PRPair(MFortranLexer.INTRINSIC, "INTRINSIC");
	/** 109:IS (Token) */
	public static final PRPair T_IS = //
			new PRPair(MFortranLexer.IS, "IS");
	/** 110:KIND (Token) */
	public static final PRPair T_KIND = //
			new PRPair(MFortranLexer.KIND, "KIND");
	/** 111:LBRACKET (Token) */
	public static final PRPair T_LBRACKET = //
			new PRPair(MFortranLexer.LBRACKET, "LBRACKET");
	/** 112:LE (Token) */
	public static final PRPair T_LE = //
			new PRPair(MFortranLexer.LE, "LE");
	/** 113:LEN (Token) */
	public static final PRPair T_LEN = //
			new PRPair(MFortranLexer.LEN, "LEN");
	/** 114:LESSTHAN (Token) */
	public static final PRPair T_LESSTHAN = //
			new PRPair(MFortranLexer.LESSTHAN, "LESSTHAN");
	/** 115:LESSTHAN_EQ (Token) */
	public static final PRPair T_LESSTHAN_EQ = //
			new PRPair(MFortranLexer.LESSTHAN_EQ, "LESSTHAN_EQ");
	/** 116:LINE_COMMENT (Token) */
	public static final PRPair T_LINE_COMMENT = //
			new PRPair(MFortranLexer.LINE_COMMENT, "LINE_COMMENT");
	/** 117:LOCAL (Token) */
	public static final PRPair T_LOCAL = //
			new PRPair(MFortranLexer.LOCAL, "LOCAL");
	/** 118:LOCAL_INT (Token) */
	public static final PRPair T_LOCAL_INT = //
			new PRPair(MFortranLexer.LOCAL_INT, "LOCAL_INT");
	/** 119:LOCK (Token) */
	public static final PRPair T_LOCK = //
			new PRPair(MFortranLexer.LOCK, "LOCK");
	/** 120:LOGICAL (Token) */
	public static final PRPair T_LOGICAL = //
			new PRPair(MFortranLexer.LOGICAL, "LOGICAL");
	/** 121:LPAREN (Token) */
	public static final PRPair T_LPAREN = //
			new PRPair(MFortranLexer.LPAREN, "LPAREN");
	/** 122:LT (Token) */
	public static final PRPair T_LT = //
			new PRPair(MFortranLexer.LT, "LT");
	/** 123:Letter (Token) */
	public static final PRPair T_Letter = //
			new PRPair(MFortranLexer.Letter, "Letter");
	/** 124:MEMORY (Token) */
	public static final PRPair T_MEMORY = //
			new PRPair(MFortranLexer.MEMORY, "MEMORY");
	/** 125:MINUS (Token) */
	public static final PRPair T_MINUS = //
			new PRPair(MFortranLexer.MINUS, "MINUS");
	/** 126:MISC_CHAR (Token) */
	public static final PRPair T_MISC_CHAR = //
			new PRPair(MFortranLexer.MISC_CHAR, "MISC_CHAR");
	/** 127:MODULE (Token) */
	public static final PRPair T_MODULE = //
			new PRPair(MFortranLexer.MODULE, "MODULE");
	/** 128:MOLD (Token) */
	public static final PRPair T_MOLD = //
			new PRPair(MFortranLexer.MOLD, "MOLD");
	/** 129:M_ALLOCATE_STMT_1 (Token) */
	public static final PRPair T_M_ALLOCATE_STMT_1 = //
			new PRPair(MFortranLexer.M_ALLOCATE_STMT_1, "M_ALLOCATE_STMT_1");
	/** 130:M_ASSIGNMENT_STMT (Token) */
	public static final PRPair T_M_ASSIGNMENT_STMT = //
			new PRPair(MFortranLexer.M_ASSIGNMENT_STMT, "M_ASSIGNMENT_STMT");
	/** 131:M_CSTR_EDIT_DESC (Token) */
	public static final PRPair T_M_CSTR_EDIT_DESC = //
			new PRPair(MFortranLexer.M_CSTR_EDIT_DESC, "M_CSTR_EDIT_DESC");
	/** 132:M_CTRL_EDIT_DESC (Token) */
	public static final PRPair T_M_CTRL_EDIT_DESC = //
			new PRPair(MFortranLexer.M_CTRL_EDIT_DESC, "M_CTRL_EDIT_DESC");
	/** 133:M_DATA_EDIT_DESC (Token) */
	public static final PRPair T_M_DATA_EDIT_DESC = //
			new PRPair(MFortranLexer.M_DATA_EDIT_DESC, "M_DATA_EDIT_DESC");
	/** 134:M_EOF (Token) */
	public static final PRPair T_M_EOF = //
			new PRPair(MFortranLexer.M_EOF, "M_EOF");
	/** 135:M_FORALL_CONSTRUCT_STMT (Token) */
	public static final PRPair T_M_FORALL_CONSTRUCT_STMT = //
			new PRPair(MFortranLexer.M_FORALL_CONSTRUCT_STMT,
					"M_FORALL_CONSTRUCT_STMT");
	/** 136:M_FORALL_STMT (Token) */
	public static final PRPair T_M_FORALL_STMT = //
			new PRPair(MFortranLexer.M_FORALL_STMT, "M_FORALL_STMT");
	/** 137:M_IF_STMT (Token) */
	public static final PRPair T_M_IF_STMT = //
			new PRPair(MFortranLexer.M_IF_STMT, "M_IF_STMT");
	/** 138:M_INCLUDE_NAME (Token) */
	public static final PRPair T_M_INCLUDE_NAME = //
			new PRPair(MFortranLexer.M_INCLUDE_NAME, "M_INCLUDE_NAME");
	/** 139:M_INQUIRE_STMT_2 (Token) */
	public static final PRPair T_M_INQUIRE_STMT_2 = //
			new PRPair(MFortranLexer.M_INQUIRE_STMT_2, "M_INQUIRE_STMT_2");
	/** 140:M_LBL_DO_TERMINAL (Token) */
	public static final PRPair T_M_LBL_DO_TERMINAL = //
			new PRPair(MFortranLexer.M_LBL_DO_TERMINAL, "M_LBL_DO_TERMINAL");
	/** 141:M_PTR_ASSIGNMENT_STMT (Token) */
	public static final PRPair T_M_PTR_ASSIGNMENT_STMT = //
			new PRPair(MFortranLexer.M_PTR_ASSIGNMENT_STMT,
					"M_PTR_ASSIGNMENT_STMT");
	/** 142:M_REAL_CONST (Token) */
	public static final PRPair T_M_REAL_CONST = //
			new PRPair(MFortranLexer.M_REAL_CONST, "M_REAL_CONST");
	/** 143:M_WHERE_CONSTRUCT_STMT (Token) */
	public static final PRPair T_M_WHERE_CONSTRUCT_STMT = //
			new PRPair(MFortranLexer.M_WHERE_CONSTRUCT_STMT,
					"M_WHERE_CONSTRUCT_STMT");
	/** 144:M_WHERE_STMT (Token) */
	public static final PRPair T_M_WHERE_STMT = //
			new PRPair(MFortranLexer.M_WHERE_STMT, "M_WHERE_STMT");
	/** 145:NAMELIST (Token) */
	public static final PRPair T_NAMELIST = //
			new PRPair(MFortranLexer.NAMELIST, "NAMELIST");
	/** 146:NE (Token) */
	public static final PRPair T_NE = //
			new PRPair(MFortranLexer.NE, "NE");
	/** 147:NEQV (Token) */
	public static final PRPair T_NEQV = //
			new PRPair(MFortranLexer.NEQV, "NEQV");
	/** 148:NONE (Token) */
	public static final PRPair T_NONE = //
			new PRPair(MFortranLexer.NONE, "NONE");
	/** 149:NON_INTRINSIC (Token) */
	public static final PRPair T_NON_INTRINSIC = //
			new PRPair(MFortranLexer.NON_INTRINSIC, "NON_INTRINSIC");
	/** 150:NON_OVERRIDABLE (Token) */
	public static final PRPair T_NON_OVERRIDABLE = //
			new PRPair(MFortranLexer.NON_OVERRIDABLE, "NON_OVERRIDABLE");
	/** 151:NON_RECURSIVE (Token) */
	public static final PRPair T_NON_RECURSIVE = //
			new PRPair(MFortranLexer.NON_RECURSIVE, "NON_RECURSIVE");
	/** 152:NOPASS (Token) */
	public static final PRPair T_NOPASS = //
			new PRPair(MFortranLexer.NOPASS, "NOPASS");
	/** 153:NOT (Token) */
	public static final PRPair T_NOT = //
			new PRPair(MFortranLexer.NOT, "NOT");
	/** 154:NO_LANG_EXT (Token) */
	public static final PRPair T_NO_LANG_EXT = //
			new PRPair(MFortranLexer.NO_LANG_EXT, "NO_LANG_EXT");
	/** 155:NULLIFY (Token) */
	public static final PRPair T_NULLIFY = //
			new PRPair(MFortranLexer.NULLIFY, "NULLIFY");
	/** 156:OCT_CONST (Token) */
	public static final PRPair T_OCT_CONST = //
			new PRPair(MFortranLexer.OCT_CONST, "OCT_CONST");
	/** 157:ONLY (Token) */
	public static final PRPair T_ONLY = //
			new PRPair(MFortranLexer.ONLY, "ONLY");
	/** 158:OPEN (Token) */
	public static final PRPair T_OPEN = //
			new PRPair(MFortranLexer.OPEN, "OPEN");
	/** 159:OPERATOR (Token) */
	public static final PRPair T_OPERATOR = //
			new PRPair(MFortranLexer.OPERATOR, "OPERATOR");
	/** 160:OPTIONAL (Token) */
	public static final PRPair T_OPTIONAL = //
			new PRPair(MFortranLexer.OPTIONAL, "OPTIONAL");
	/** 161:OR (Token) */
	public static final PRPair T_OR = //
			new PRPair(MFortranLexer.OR, "OR");
	/** 162:OUT (Token) */
	public static final PRPair T_OUT = //
			new PRPair(MFortranLexer.OUT, "OUT");
	/** 163:PARAMETER (Token) */
	public static final PRPair T_PARAMETER = //
			new PRPair(MFortranLexer.PARAMETER, "PARAMETER");
	/** 164:PASS (Token) */
	public static final PRPair T_PASS = //
			new PRPair(MFortranLexer.PASS, "PASS");
	/** 165:PAUSE (Token) */
	public static final PRPair T_PAUSE = //
			new PRPair(MFortranLexer.PAUSE, "PAUSE");
	/** 166:PERCENT (Token) */
	public static final PRPair T_PERCENT = //
			new PRPair(MFortranLexer.PERCENT, "PERCENT");
	/** 167:PERIOD (Token) */
	public static final PRPair T_PERIOD = //
			new PRPair(MFortranLexer.PERIOD, "PERIOD");
	/** 168:PERIOD_EXPONENT (Token) */
	public static final PRPair T_PERIOD_EXPONENT = //
			new PRPair(MFortranLexer.PERIOD_EXPONENT, "PERIOD_EXPONENT");
	/** 169:PLUS (Token) */
	public static final PRPair T_PLUS = //
			new PRPair(MFortranLexer.PLUS, "PLUS");
	/** 170:POINTER (Token) */
	public static final PRPair T_POINTER = //
			new PRPair(MFortranLexer.POINTER, "POINTER");
	/** 171:POST (Token) */
	public static final PRPair T_POST = //
			new PRPair(MFortranLexer.POST, "POST");
	/** 172:POWER (Token) */
	public static final PRPair T_POWER = //
			new PRPair(MFortranLexer.POWER, "POWER");
	/** 173:PRAGMA (Token) */
	public static final PRPair T_PRAGMA = //
			new PRPair(MFortranLexer.PRAGMA, "PRAGMA");
	/** 174:PRECISION (Token) */
	public static final PRPair T_PRECISION = //
			new PRPair(MFortranLexer.PRECISION, "PRECISION");
	/** 175:PREPROCESS_LINE (Token) */
	public static final PRPair T_PREPROCESS_LINE = //
			new PRPair(MFortranLexer.PREPROCESS_LINE, "PREPROCESS_LINE");
	/** 176:PRINT (Token) */
	public static final PRPair T_PRINT = //
			new PRPair(MFortranLexer.PRINT, "PRINT");
	/** 177:PRIVATE (Token) */
	public static final PRPair T_PRIVATE = //
			new PRPair(MFortranLexer.PRIVATE, "PRIVATE");
	/** 178:PROCEDURE (Token) */
	public static final PRPair T_PROCEDURE = //
			new PRPair(MFortranLexer.PROCEDURE, "PROCEDURE");
	/** 179:PROGRAM (Token) */
	public static final PRPair T_PROGRAM = //
			new PRPair(MFortranLexer.PROGRAM, "PROGRAM");
	/** 180:PROTECTED (Token) */
	public static final PRPair T_PROTECTED = //
			new PRPair(MFortranLexer.PROTECTED, "PROTECTED");
	/** 181:PUBLIC (Token) */
	public static final PRPair T_PUBLIC = //
			new PRPair(MFortranLexer.PUBLIC, "PUBLIC");
	/** 182:PURE (Token) */
	public static final PRPair T_PURE = //
			new PRPair(MFortranLexer.PURE, "PURE");
	/** 183:QUIET (Token) */
	public static final PRPair T_QUIET = //
			new PRPair(MFortranLexer.QUIET, "QUIET");
	/** 184:RANK (Token) */
	public static final PRPair T_RANK = //
			new PRPair(MFortranLexer.RANK, "RANK");
	/** 185:RBRACKET (Token) */
	public static final PRPair T_RBRACKET = //
			new PRPair(MFortranLexer.RBRACKET, "RBRACKET");
	/** 186:READ (Token) */
	public static final PRPair T_READ = //
			new PRPair(MFortranLexer.READ, "READ");
	/** 187:REAL (Token) */
	public static final PRPair T_REAL = //
			new PRPair(MFortranLexer.REAL, "REAL");
	/** 188:RECURSIVE (Token) */
	public static final PRPair T_RECURSIVE = //
			new PRPair(MFortranLexer.RECURSIVE, "RECURSIVE");
	/** 189:RESULT (Token) */
	public static final PRPair T_RESULT = //
			new PRPair(MFortranLexer.RESULT, "RESULT");
	/** 190:RETURN (Token) */
	public static final PRPair T_RETURN = //
			new PRPair(MFortranLexer.RETURN, "RETURN");
	/** 191:REWIND (Token) */
	public static final PRPair T_REWIND = //
			new PRPair(MFortranLexer.REWIND, "REWIND");
	/** 192:RPAREN (Token) */
	public static final PRPair T_RPAREN = //
			new PRPair(MFortranLexer.RPAREN, "RPAREN");
	/** 193:SAVE (Token) */
	public static final PRPair T_SAVE = //
			new PRPair(MFortranLexer.SAVE, "SAVE");
	/** 194:SELECT (Token) */
	public static final PRPair T_SELECT = //
			new PRPair(MFortranLexer.SELECT, "SELECT");
	/** 195:SELECTCASE (Token) */
	public static final PRPair T_SELECTCASE = //
			new PRPair(MFortranLexer.SELECTCASE, "SELECTCASE");
	/** 196:SELECTTYPE (Token) */
	public static final PRPair T_SELECTTYPE = //
			new PRPair(MFortranLexer.SELECTTYPE, "SELECTTYPE");
	/** 197:SEQUENCE (Token) */
	public static final PRPair T_SEQUENCE = //
			new PRPair(MFortranLexer.SEQUENCE, "SEQUENCE");
	/** 198:SHARED (Token) */
	public static final PRPair T_SHARED = //
			new PRPair(MFortranLexer.SHARED, "SHARED");
	/** 199:SLASH (Token) */
	public static final PRPair T_SLASH = //
			new PRPair(MFortranLexer.SLASH, "SLASH");
	/** 200:SLASH_EQ (Token) */
	public static final PRPair T_SLASH_EQ = //
			new PRPair(MFortranLexer.SLASH_EQ, "SLASH_EQ");
	/** 201:SLASH_SLASH (Token) */
	public static final PRPair T_SLASH_SLASH = //
			new PRPair(MFortranLexer.SLASH_SLASH, "SLASH_SLASH");
	/** 202:SOURCE (Token) */
	public static final PRPair T_SOURCE = //
			new PRPair(MFortranLexer.SOURCE, "SOURCE");
	/** 203:STAT (Token) */
	public static final PRPair T_STAT = //
			new PRPair(MFortranLexer.STAT, "STAT");
	/** 204:STMT_FUNCTION (Token) */
	public static final PRPair T_STMT_FUNCTION = //
			new PRPair(MFortranLexer.STMT_FUNCTION, "STMT_FUNCTION");
	/** 205:STOP (Token) */
	public static final PRPair T_STOP = //
			new PRPair(MFortranLexer.STOP, "STOP");
	/** 206:SUBMODULE (Token) */
	public static final PRPair T_SUBMODULE = //
			new PRPair(MFortranLexer.SUBMODULE, "SUBMODULE");
	/** 207:SUBROUTINE (Token) */
	public static final PRPair T_SUBROUTINE = //
			new PRPair(MFortranLexer.SUBROUTINE, "SUBROUTINE");
	/** 208:SYNC (Token) */
	public static final PRPair T_SYNC = //
			new PRPair(MFortranLexer.SYNC, "SYNC");
	/** 209:SYNCTEAM (Token) */
	public static final PRPair T_SYNCTEAM = //
			new PRPair(MFortranLexer.SYNCTEAM, "SYNCTEAM");
	/** 210:Sp_Char (Token) */
	public static final PRPair T_Sp_Char = //
			new PRPair(MFortranLexer.Sp_Char, "Sp_Char");
	/** 211:TARGET (Token) */
	public static final PRPair T_TARGET = //
			new PRPair(MFortranLexer.TARGET, "TARGET");
	/** 212:TEAM (Token) */
	public static final PRPair T_TEAM = //
			new PRPair(MFortranLexer.TEAM, "TEAM");
	/** 213:THEN (Token) */
	public static final PRPair T_THEN = //
			new PRPair(MFortranLexer.THEN, "THEN");
	/** 214:TO (Token) */
	public static final PRPair T_TO = //
			new PRPair(MFortranLexer.TO, "TO");
	/** 215:TRUE (Token) */
	public static final PRPair T_TRUE = //
			new PRPair(MFortranLexer.TRUE, "TRUE");
	/** 216:TYPE (Token) */
	public static final PRPair T_TYPE = //
			new PRPair(MFortranLexer.TYPE, "TYPE");
	/** 217:UNDERSCORE (Token) */
	public static final PRPair T_UNDERSCORE = //
			new PRPair(MFortranLexer.UNDERSCORE, "UNDERSCORE");
	/** 218:UNFORMATTED (Token) */
	public static final PRPair T_UNFORMATTED = //
			new PRPair(MFortranLexer.UNFORMATTED, "UNFORMATTED");
	/** 219:UNLOCK (Token) */
	public static final PRPair T_UNLOCK = //
			new PRPair(MFortranLexer.UNLOCK, "UNLOCK");
	/** 220:USE (Token) */
	public static final PRPair T_USE = //
			new PRPair(MFortranLexer.USE, "USE");
	/** 221:VALUE (Token) */
	public static final PRPair T_VALUE = //
			new PRPair(MFortranLexer.VALUE, "VALUE");
	/** 222:VOLATILE (Token) */
	public static final PRPair T_VOLATILE = //
			new PRPair(MFortranLexer.VOLATILE, "VOLATILE");
	/** 223:WAIT (Token) */
	public static final PRPair T_WAIT = //
			new PRPair(MFortranLexer.WAIT, "WAIT");
	/** 224:WHERE (Token) */
	public static final PRPair T_WHERE = //
			new PRPair(MFortranLexer.WHERE, "WHERE");
	/** 225:WHILE (Token) */
	public static final PRPair T_WHILE = //
			new PRPair(MFortranLexer.WHILE, "WHILE");
	/** 226:WRITE (Token) */
	public static final PRPair T_WRITE = //
			new PRPair(MFortranLexer.WRITE, "WRITE");
	/** 227:WS (Token) */
	public static final PRPair T_WS = //
			new PRPair(MFortranLexer.WS, "WS");

	/* * * * * Fortran 2018 * * * * */
	public static final PRPair PROGRAM = //
			new PRPair(501, "PROGRAM");
	public static final PRPair PROGRAM_UNIT = //
			new PRPair(502, "PROGRAM_UNIT");
	public static final PRPair EXTERNAL_SUBPROGRAM = //
			new PRPair(503, "EXTERNAL_SUBPROGRAM");
	public static final PRPair SPECIFICATION_PART = //
			new PRPair(504, "SPECIFICATION_PART");
	public static final PRPair IMPLICIT_PART = //
			new PRPair(505, "IMPLICIT_PART");
	public static final PRPair IMPLICIT_PART_STMT = //
			new PRPair(506, "IMPLICIT_PART_STMT");
	public static final PRPair DECLARATION_CONSTRUCT = //
			new PRPair(507, "DECLARATION_CONSTRUCT");
	public static final PRPair SPECIFICATION_CONSTRUCT = //
			new PRPair(508, "SPECIFICATION_CONSTRUCT");
	public static final PRPair EXECUTION_PART = //
			new PRPair(509, "EXECUTION_PART");
	public static final PRPair EXECUTION_PART_CONSTRUCT = //
			new PRPair(510, "EXECUTION_PART_CONSTRUCT");
	public static final PRPair INTERNAL_SUBPROGRAM_PART = //
			new PRPair(511, "INTERNAL_SUBPROGRAM_PART");
	public static final PRPair INTERNAL_SUBPROGRAM = //
			new PRPair(512, "INTERNAL_SUBPROGRAM");
	public static final PRPair OTHER_SPECIFICATION_STMT = //
			new PRPair(513, "OTHER_SPECIFICATION_STMT");
	public static final PRPair EXECUTABLE_CONSTRUCT = //
			new PRPair(514, "EXECUTABLE_CONSTRUCT");
	public static final PRPair ACTION_STMT = //
			new PRPair(515, "ACTION_STMT");
	public static final PRPair KEYWORD = //
			new PRPair(516, "KEYWORD");

	public static final PRPair ALPHANUMERIC_CHARACTER = //
			new PRPair(601, "ALPHANUMERIC_CHARACTER");
	public static final PRPair UNDERSCORE = //
			new PRPair(602, "UNDERSCORE");
	public static final PRPair NAME = //
			new PRPair(603, "NAME");
	public static final PRPair GENERIC_NAME = //
			NAME;
	public static final PRPair CONSTANT = //
			new PRPair(604, "CONSTANT");
	public static final PRPair LITERAL_CONSTANT = //
			new PRPair(605, "LITERAL_CONSTANT");
	public static final PRPair NAMED_CONSTANT = //
			new PRPair(606, "NAMED_CONSTANT");
	public static final PRPair INT_CONSTANT = //
			new PRPair(607, "INT_CONSTANT");
	public static final PRPair INTRINSIC_OPERATOR = //
			new PRPair(608, "INTRINSIC_OPERATOR");
	public static final PRPair DEFINED_OPERATOR = //
			new PRPair(609, "DEFINED_OPERATOR");
	public static final PRPair EXTENDED_INTRINSIC_OP = //
			new PRPair(610, "EXTENDED_INTRINSIC_OP");
	public static final PRPair LABEL = //
			new PRPair(611, "LABEL");

	public static final PRPair TYPE_PARAM_VALUE = //
			new PRPair(701, "TYPE_PARAM_VALUE");
	public static final PRPair TYPE_SPEC = //
			new PRPair(702, "TYPE_SPEC");
	public static final PRPair DECLARATION_TYPE_SPEC = //
			new PRPair(703, "DECLARATION_TYPE_SPEC");
	public static final PRPair INTRINSIC_TYPE_SPEC = //
			new PRPair(704, "INTRINSICTYPE_SPEC");
	public static final PRPair INTEGER_TYPE_SPEC = //
			new PRPair(705, "INTEGER_TYPE_SPEC");
	public static final PRPair KIND_SELECTOR = //
			new PRPair(706, "KIND_SELECTOR");
	public static final PRPair SIGNED_INT_LITERAL_CONSTANT = //
			new PRPair(707, "SIGNED_INT_LITERAL_CONSTANT");
	public static final PRPair INT_LITERAL_CONSTANT = //
			new PRPair(708, "INT_LITERAL_CONSTANT");
	public static final PRPair KIND_PARAM = //
			new PRPair(709, "KIND_PARAM");
	public static final PRPair SIGNED_DIGIT_STRING = //
			new PRPair(710, "SIGNED_DIGIT_STRING");
	public static final PRPair DIGIT_STRING = //
			new PRPair(711, "DIGIT_STRING");
	public static final PRPair SIGN = //
			new PRPair(712, "SIGN");
	public static final PRPair SIGNED_REAL_LITERAL_CONSTANT = //
			new PRPair(713, "SIGNED_REAL_LITERAL_CONSTANT");
	public static final PRPair REAL_LITERAL_CONSTANT = //
			new PRPair(714, "REAL_LITERAL_CONSTANT");
	public static final PRPair SIGNIFICAND = //
			new PRPair(715, "SIGNIFICAND");
	public static final PRPair EXPONENT_LETTER = //
			new PRPair(716, "EXPONENT_LETTER");
	public static final PRPair EXPONENT = //
			new PRPair(717, "EXPONENT");
	public static final PRPair COMPLEX_LITERAL_CONSTANT = //
			new PRPair(718, "COMPLEX_LITERAL_CONSTANT");
	public static final PRPair REAL_PART = //
			new PRPair(719, "REAL_PART");
	public static final PRPair IMAG_PART = //
			new PRPair(720, "IMAG_PART");
	public static final PRPair CHAR_SELECTOR = //
			new PRPair(721, "CHAR_SELECTOR");
	public static final PRPair LENGTH_SELECTOR = //
			new PRPair(722, "LENGTH_SELECTOR");
	public static final PRPair CHAR_LENGTH = //
			new PRPair(723, "CHAR_LENGTH");
	public static final PRPair CHAR_LITERAL_CONSTANT = //
			new PRPair(724, "CHAR_LITERAL_CONSTANT");
	public static final PRPair LOGICAL_LITERAL_CONSTANT = //
			new PRPair(725, "LOGICAL_LITERAL_CONSTANT");
	public static final PRPair DERIVED_TYPE_DEF = //
			new PRPair(726, "DERIVED_TYPE_DEF");
	public static final PRPair DERIVED_TYPE_STMT = //
			new PRPair(727, "DERIVED_TYPE_STMT");
	public static final PRPair TYPE_ATTR_SPEC = //
			new PRPair(728, "TYPE_ATTR_SPEC");
	public static final PRPair PRIVATE_OR_SEQUENCE = //
			new PRPair(729, "PRIVATE_OR_SEQUENCE");
	public static final PRPair END_TYPE_STMT = //
			new PRPair(730, "END_TYPE_STMT");
	public static final PRPair SEQUENCE_STMT = //
			new PRPair(731, "SEQUENCE_STMT");
	public static final PRPair TYPE_PARAM_DEF_STMT = //
			new PRPair(732, "TYPE_PARAM_DEF_STMT");
	public static final PRPair TYPE_PARAM_DECL = //
			new PRPair(733, "TYPE_PARAM_DECL");
	public static final PRPair TYPE_PARAM_ATTR_SPEC = //
			new PRPair(734, "TYPE_PARAM_ATTR_SPEC");
	public static final PRPair COMPONENT_PART = //
			new PRPair(735, "COMPONENT_PART");
	public static final PRPair COMPONENT_DEF_STMT = //
			new PRPair(736, "COMPONENT_DEF_STMT");
	public static final PRPair DATA_COMPONENT_DEF_STMT = //
			new PRPair(737, "DATA_COMPONENT_DEF_STMT");
	public static final PRPair COMPONENT_ATTR_SPEC = //
			new PRPair(738, "COMPONENT_ATTR_SPEC");
	public static final PRPair COMPONENT_DECL = //
			new PRPair(739, "COMPONENT_DECL");
	public static final PRPair COMPONENT_ARRAY_SPEC = //
			new PRPair(740, "COMPONENT_ARRAY_SPEC");
	public static final PRPair PROC_COMPONENT_DEF_STMT = //
			new PRPair(741, "PROC_COMPONENT_DEF_STMT");
	public static final PRPair PROC_COMPONENT_ATTR_SPEC = //
			new PRPair(742, "PROC_COMPONENT_ATTR_SPEC");
	public static final PRPair COMPONENT_INITIALIZATION = //
			new PRPair(743, "COMPONENT_INITIALIZATION");
	public static final PRPair INITIAL_DATA_TARGET = //
			new PRPair(744, "INITIAL_DATA_TARGET");
	public static final PRPair PRIVATE_COMPONENTS_STMT = //
			new PRPair(745, "PRIVATE_COMPONENTS_STMT");
	public static final PRPair TYPE_BOUND_PROCEDURE_PART = //
			new PRPair(746, "TYPE_BOUND_PROCEDURE_PART");
	public static final PRPair BINDING_PRIVATE_STMT = //
			new PRPair(747, "BINDING_PRIVATE_STMT");
	public static final PRPair TYPE_BOUND_PROC_BINDING = //
			new PRPair(748, "TYPE_BOUND_PROC_BINDING");
	public static final PRPair TYPE_BOUND_PROCEDURE_STMT = //
			new PRPair(749, "TYPE_BOUND_PROCEDURE_STMT");
	public static final PRPair TYPE_BOUND_PROC_DECL = //
			new PRPair(750, "TYPE_BOUND_PROC_DECL");
	public static final PRPair TYPE_BOUND_GENERIC_STMT = //
			new PRPair(751, "TYPE_BOUND_GENERIC_STMT");
	public static final PRPair BINDING_ATTR = //
			new PRPair(752, "BINDING_ATTR");
	public static final PRPair FINAL_PROCEDURE_STMT = //
			new PRPair(753, "FINAL_PROCEDURE_STMT");
	public static final PRPair DERIVED_TYPE_SPEC = //
			new PRPair(754, "DERIVED_TYPE_SPEC");
	public static final PRPair TYPE_PARAM_SPEC = //
			new PRPair(755, "TYPE_PARAM_SPEC");
	public static final PRPair STRUCTURE_CONSTRUCTOR = //
			new PRPair(756, "STRUCTURE_CONSTRUCTOR");
	public static final PRPair COMPONENT_SPEC = //
			new PRPair(757, "COMPONENT_SPEC");
	public static final PRPair COMPONENT_DATA_SOURCE = //
			new PRPair(758, "COMPONENT_DATA_SOURCE");
	public static final PRPair ENUM_DEF = //
			new PRPair(759, "ENUM_DEF");
	public static final PRPair ENUM_DEF_STMT = //
			new PRPair(760, "ENUM_DEF_STMT");
	public static final PRPair ENUMERATOR_DEF_STMT = //
			new PRPair(761, "ENUMERATOR_DEF_STMT");
	public static final PRPair ENUMERATOR = //
			new PRPair(762, "ENUMERATOR");
	public static final PRPair END_ENUM_STMT = //
			new PRPair(763, "END_ENUM_STMT");
	public static final PRPair BOZ_LITERAL_CONSTANT = //
			new PRPair(764, "BOZ_LITERAL_CONSTANT");
	public static final PRPair BINARY_CONSTANT = //
			new PRPair(765, "BINARY_CONSTANT");
	public static final PRPair OCTAL_CONSTANT = //
			new PRPair(766, "OCTAL_CONSTANT");
	public static final PRPair HEX_CONSTANT = //
			new PRPair(767, "HEX_CONSTANT");
	public static final PRPair HEX_DIGIT = //
			new PRPair(768, "HEX_DIGIT");
	public static final PRPair ARRAY_CONSTRUCTOR = //
			new PRPair(769, "ARRAY_CONSTRUCTOR");
	public static final PRPair AC_SPEC = //
			new PRPair(770, "AC_SPEC");
	public static final PRPair LBRACKET = //
			new PRPair(771, "LBRACKET");
	public static final PRPair RBRACKET = //
			new PRPair(772, "RBRACKET");
	public static final PRPair AC_VALUE = //
			new PRPair(773, "AC_VALUE");
	public static final PRPair AC_IMPLIED_DO = //
			new PRPair(774, "AC_IMPLIED_DO");
	public static final PRPair AC_IMPLIED_DO_CONTROL = //
			new PRPair(775, "AC_IMPLIED_DO_CONTROL");
	public static final PRPair AC_DO_VARIABLE = //
			new PRPair(776, "AC_DO_VARIABLE");

	public static final PRPair TYPE_DECLARATION_STMT = //
			new PRPair(801, "TYPE_DECLARATION_STMT");
	public static final PRPair ATTR_SPEC = //
			new PRPair(802, "ATTR_SPEC");
	public static final PRPair ENTITY_DECL = //
			new PRPair(803, "ENTITY_DECL");
	public static final PRPair OBJECT_NAME = //
			new PRPair(804, "OBJECT_NAME");
	public static final PRPair INITIALIZATION = //
			new PRPair(805, "INITIALIZATION");
	public static final PRPair NULL_INIT = //
			new PRPair(806, "NULL_INIT");
	public static final PRPair ACCESS_SPEC = //
			new PRPair(807, "ACCESS_SPEC");
	public static final PRPair LANGUAGE_BINDING_SPEC = //
			new PRPair(808, "LANGUAGE_BINDING_SPEC");
	public static final PRPair COARRAY_SPEC = //
			new PRPair(809, "COARRAY_SPEC");
	public static final PRPair DEFERRED_COSHAPE_SPEC = //
			new PRPair(810, "DEFERRED_COSHAPE_SPEC");
	public static final PRPair EXPLICIT_COSHAPE_SPEC = //
			new PRPair(811, "EXPLICIT_COSHAPE_SPEC");
	public static final PRPair LOWER_COBOUND = //
			new PRPair(812, "LOWER_COBOUND");
	public static final PRPair UPPER_COBOUND = //
			new PRPair(813, "UPPER_COBOUND");
	public static final PRPair DIMENSION_SPEC = //
			new PRPair(814, "DIMENSION_SPEC");
	public static final PRPair ARRAY_SPEC = //
			new PRPair(815, "ARRAY_SPEC");
	public static final PRPair EXPLICIT_SHAPE_SPEC = //
			new PRPair(816, "EXPLICIT_SHAPE_SPEC");
	public static final PRPair LOWER_BOUND = //
			new PRPair(817, "LOWER_BOUND");
	public static final PRPair UPPER_BOUND = //
			new PRPair(818, "UPPER_BOUND");
	public static final PRPair ASSUMED_SHAPE_SPEC = //
			new PRPair(819, "ASSUMED_SHAPE_SPEC");
	public static final PRPair DEFERRED_SHAPE_SPEC = //
			new PRPair(820, "DEFERRED_SHAPE_SPEC");
	public static final PRPair ASSUMED_IMPLIED_SPEC = //
			new PRPair(821, "ASSUMED_IMPLIED_SPEC");
	public static final PRPair ASSUMED_SIZE_SPEC = //
			new PRPair(822, "ASSUMED_SIZE_SPEC");
	public static final PRPair IMPLIED_SHAPE_OR_ASSUMED_SIZE_SPEC = //
			new PRPair(823, "IMPLIED_SHAPE_OR_ASSUMED_SIZE_SPEC");
	public static final PRPair IMPLIED_SHAPE_SPEC = //
			new PRPair(824, "IMPLIED_SHAPE_SPEC");
	public static final PRPair ASSUMED_RANK_SPEC = //
			new PRPair(825, "ASSUMED_RANK_SPEC");
	public static final PRPair INTENT_SPEC = //
			new PRPair(826, "INTENT_SPEC");
	public static final PRPair ACCESS_STMT = //
			new PRPair(827, "ACCESS_STMT");
	public static final PRPair ACCESS_ID = //
			new PRPair(828, "ACCESS_ID");
	public static final PRPair ALLOCATABLE_STMT = //
			new PRPair(829, "ALLOCATABLE_STMT");
	public static final PRPair ALLOCATABLE_DECL = //
			new PRPair(830, "ALLOCATABLE_DECL");
	public static final PRPair ASYNCHRONOUS_STMT = //
			new PRPair(831, "ASYNCHRONOUS_STMT");
	public static final PRPair BIND_STMT = //
			new PRPair(832, "BIND_STMT");
	public static final PRPair BIND_ENTITY = //
			new PRPair(833, "BIND_ENTITY");
	public static final PRPair CODIMENSION_STMT = //
			new PRPair(834, "CODIMENSION_STMT");
	public static final PRPair CODIMENSION_DECL = //
			new PRPair(835, "CODIMENSION_DECL");
	public static final PRPair CONTIGUOUS_STMT = //
			new PRPair(836, "CONTIGUOUS_STMT");
	public static final PRPair DATA_STMT = //
			new PRPair(837, "DATA_STMT");
	public static final PRPair DATA_STMT_SET = //
			new PRPair(838, "DATA_STMT_SET");
	public static final PRPair DATA_STMT_OBJECT = //
			new PRPair(839, "DATA_STMT_OBJECT");
	public static final PRPair DATA_IMPLIED_DO = //
			new PRPair(840, "DATA_IMPLIED_DO");
	public static final PRPair DATA_I_DO_OBJECT = //
			new PRPair(841, "DATA_I_DO_OBJECT");
	public static final PRPair DATA_I_DO_VARIABLE = //
			new PRPair(842, "DATA_I_DO_VARIABLE");
	public static final PRPair DATA_STMT_VALUE = //
			new PRPair(843, "DATA_STMT_VALUE");
	public static final PRPair DATA_STMT_REPEAT = //
			new PRPair(844, "DATA_STMT_REPEAT");
	public static final PRPair DATA_STMT_CONSTANT = //
			new PRPair(845, "DATA_STMT_CONSTANT");
	public static final PRPair INT_CONSTANT_SUBOBJECT = //
			new PRPair(846, "INT_CONSTANT_SUBOBJECT");
	public static final PRPair CONSTANT_SUBOBJECT = //
			new PRPair(847, "CONSTANT_SUBOBJECT");
	public static final PRPair DIMENSION_STMT = //
			new PRPair(848, "DIMENSION_STMT");
	public static final PRPair INTENT_STMT = //
			new PRPair(849, "INTENT_STMT");
	public static final PRPair OPTIONAL_STMT = //
			new PRPair(850, "OPTIONAL_STMT");
	public static final PRPair PARAMETER_STMT = //
			new PRPair(851, "PARAMETER_STMT");
	public static final PRPair NAMED_CONSTANT_DEF = //
			new PRPair(852, "NAMED_CONSTANT_DEF");
	public static final PRPair POINTER_STMT = //
			new PRPair(853, "POINTER_STMT");
	public static final PRPair POINTER_DECL = //
			new PRPair(854, "POINTER_DECL");
	public static final PRPair PROTECTED_STMT = //
			new PRPair(855, "PROTECTED_STMT");
	public static final PRPair SAVE_STMT = //
			new PRPair(856, "SAVE_STMT");
	public static final PRPair SAVED_ENTITY = //
			new PRPair(857, "SAVED_ENTITY");
	public static final PRPair PROC_POINTER_NAME = //
			new PRPair(858, "PROC_POINTER_NAME");
	public static final PRPair TARGET_STMT = //
			new PRPair(859, "TARGET_STMT");
	public static final PRPair TARGET_DECL = //
			new PRPair(860, "TARGET_DECL");
	public static final PRPair VALUE_STMT = //
			new PRPair(861, "VALUE_STMT");
	public static final PRPair VOLATILE_STMT = //
			new PRPair(862, "VOLATILE_STMT");
	public static final PRPair IMPLICIT_STMT = //
			new PRPair(863, "IMPLICIT_STMT");
	public static final PRPair IMPLICIT_SPEC = //
			new PRPair(864, "IMPLICIT_SPEC");
	public static final PRPair LETTER_SPEC = //
			new PRPair(865, "LETTER_SPEC");
	public static final PRPair IMPLICIT_NONE_SPEC = //
			new PRPair(866, "IMPLICIT_NONE_SPEC");
	public static final PRPair IMPORT_STMT = //
			new PRPair(867, "IMPORT_STMT");
	public static final PRPair NAMELIST_STMT = //
			new PRPair(868, "NAMELIST_STMT");
	public static final PRPair NAMELIST_GROUP_OBJECT = //
			new PRPair(869, "NAMELIST_GROUP_OBJECT");
	public static final PRPair EQUIVALENCE_STMT = //
			new PRPair(870, "EQUIVALENCE_STMT");
	public static final PRPair EUIVALENCE_SET = //
			new PRPair(871, "EUIVALENCE_SET");
	public static final PRPair EQUIVALENCE_OBJECT = //
			new PRPair(872, "EQUIVALENCE_OBJECT");
	public static final PRPair COMMON_STMT = //
			new PRPair(873, "COMMON_STMT");
	public static final PRPair COMMON_BLOCK_OBJECT = //
			new PRPair(874, "COMMON_BLOCK_OBJECT");

	public static final PRPair DESIGNATOR = //
			new PRPair(901, "DESIGNATOR");
	public static final PRPair VARIABLE = //
			new PRPair(902, "VARIABLE");
	public static final PRPair VARIABLE_NAME = //
			new PRPair(903, "VARIABLE_NAME");
	public static final PRPair LOGICAL_VARIABLE = //
			new PRPair(904, "LOGICAL_VARIABLE");
	public static final PRPair CHAR_VARIABLE = //
			new PRPair(905, "CHAR_VARIABLE");
	public static final PRPair DEFAULT_CHAR_VARIABLE = //
			new PRPair(906, "DEFAULT_CHAR_VARIABLE");
	public static final PRPair INT_VARIABLE = //
			new PRPair(907, "INT_VARIABLE");
	public static final PRPair SUBSTRING = //
			new PRPair(908, "SUBSTRING");
	public static final PRPair PARENT_STRING = //
			new PRPair(909, "PARENT_STRING");
	public static final PRPair SUBSTRING_RANGE = //
			new PRPair(910, "SUBSTRING_RANGE");
	public static final PRPair DATA_REF = //
			new PRPair(911, "DATA_REF");
	public static final PRPair PART_REF = //
			new PRPair(912, "PART_REF");
	public static final PRPair STRUCTURE_COMPONENT = //
			new PRPair(913, "STRUCTURE_COMPONENT");
	public static final PRPair COINDEXED_NAMED_OBJECT = //
			new PRPair(914, "COINDEXED_NAMED_OBJECT");
	public static final PRPair COMPLEX_PART_DESIGNATOR = //
			new PRPair(915, "COMPLEX_PART_DESIGNATOR");
	public static final PRPair TYPE_PARAM_INQUIRY = //
			new PRPair(916, "TYPE_PARAM_INQUIRY");
	public static final PRPair ARRAY_ELEMENT = //
			new PRPair(917, "ARRAY_ELEMENT");
	public static final PRPair ARRAY_SECTION = //
			new PRPair(918, "ARRAY_SECTION");
	public static final PRPair SUBSCRIPT = //
			new PRPair(919, "SUBSCRIPT");
	public static final PRPair SECTION_SUBSCRIPT = //
			new PRPair(920, "SECTION_SUBSCRIPT");
	public static final PRPair SUBSCRIPT_TRIPLET = //
			new PRPair(921, "SUBSCRIPT_TRIPLET");
	public static final PRPair STRIDE = //
			new PRPair(922, "STRIDE");
	public static final PRPair VECTOR_SUBSCRIPT = //
			new PRPair(923, "VECTOR_SUBSCRIPT");
	public static final PRPair IMAGE_SELECTOR = //
			new PRPair(924, "IMAGE_SELECTOR");
	public static final PRPair COSUBSCRIPT = //
			new PRPair(925, "COSUBSCRIPT");
	public static final PRPair IMAGE_SELECTOR_SPEC = //
			new PRPair(926, "IMAGE_SELECTOR_SPEC");
	public static final PRPair ALLOCATE_STMT = //
			new PRPair(927, "ALLOCATE_STMT");
	public static final PRPair ALLOC_OPT = //
			new PRPair(928, "ALLOC_OPT");
	public static final PRPair ERRMSG_VARIABLE = //
			new PRPair(929, "ERRMSG_VARIABLE");
	public static final PRPair SOURCE_EXPR = //
			new PRPair(930, "SOURCE_EXPR");
	public static final PRPair ALLOCATION = //
			new PRPair(931, "ALLOCATION");
	public static final PRPair ALLOCATE_OBJECT = //
			new PRPair(932, "ALLOCATE_OBJECT");
	public static final PRPair ALLOCATE_SHAPE_SPEC = //
			new PRPair(933, "ALLOCATE_SHAPE_SPEC");
	public static final PRPair LOWER_BOUND_EXPR = //
			new PRPair(934, "LOWER_BOUND_EXPR");
	public static final PRPair UPPER_BOUND_EXPR = //
			new PRPair(935, "UPPER_BOUND_EXPR");
	public static final PRPair ALLOCATE_COARRAY_SPEC = //
			new PRPair(936, "ALLOCATE_COARRAY_SPEC");
	public static final PRPair ALLOCATE_COSHAPE_SPEC = //
			new PRPair(937, "ALLOCATE_COSHAPE_SPEC");
	public static final PRPair NULLIFY_STMT = //
			new PRPair(938, "NULLIFY_STMT");
	public static final PRPair POINTER_OBJECT = //
			new PRPair(939, "POINTER_OBJECT");
	public static final PRPair DEALLOCATE_STMT = //
			new PRPair(940, "DEALLOCATE_STMT");
	public static final PRPair DEALLOC_OPT = //
			new PRPair(941, "DEALLOC_OPT");
	public static final PRPair STAT_VARIABLE = //
			new PRPair(942, "STAT_VARIABLE");
	public static final PRPair DESIGNATOR_OR_FUNC_REF = // DUMMY
			new PRPair(996, "DESIGNATOR_OR_FUNC_REF");

	public static final PRPair PRIMARY = //
			new PRPair(1001, "PRIMARY");
	public static final PRPair LEVEL_1_EXPR = //
			new PRPair(1002, "LEVEL_1_EXPR");
	public static final PRPair DEFINED_UNARY_OP_ = //
			new PRPair(1003, "DEFINED_UNARY_OP_");
	public static final PRPair MULT_OPERAND = //
			new PRPair(1004, "MULT_OPERAND");
	public static final PRPair ADD_OPERAND = //
			new PRPair(1005, "ADD_OPERAND");
	public static final PRPair LEVEL_2_EXPR = //
			new PRPair(1006, "LEVEL_2_EXPR");
	public static final PRPair POWER_OP = //
			new PRPair(1007, "POWER_OP");
	public static final PRPair MULT_OP = //
			new PRPair(1008, "MULT_OP");
	public static final PRPair ADD_OP = //
			new PRPair(1009, "ADD_OP");
	public static final PRPair LEVEL_3_EXPR = //
			new PRPair(1010, "LEVEL_3_EXPR");
	public static final PRPair CONCAT_OP = //
			new PRPair(1011, "CONCAT_OP");
	public static final PRPair LEVEL_4_EXPR = //
			new PRPair(1012, "LEVEL_4_EXPR");
	public static final PRPair REL_OP__ = //
			new PRPair(1013, "REL_OP__");
	public static final PRPair AND_OPERAND = //
			new PRPair(1014, "AND_OPERAND");
	public static final PRPair OR_OPERAND = //
			new PRPair(1015, "OR_OPERAND");
	public static final PRPair EQUIV_OPERAND = //
			new PRPair(1016, "EQUIV_OPERAND");
	public static final PRPair LEVEL_5_EXPR = //
			new PRPair(1017, "LEVEL_5_EXPR");
	public static final PRPair NOT_OP = //
			new PRPair(1018, "NOT_OP");
	public static final PRPair AND_OP = //
			new PRPair(1019, "AND_OP");
	public static final PRPair OR_OP = //
			new PRPair(1020, "OR_OP");
	public static final PRPair EQUIV_OP = //
			new PRPair(1021, "EQUIV_OP");
	public static final PRPair EXPR = //
			new PRPair(1022, "EXPR");
	public static final PRPair DEFINED_BINARY_OP = //
			new PRPair(1023, "DEFINED_BINARY_OP");
	public static final PRPair LOGICAL_EXPR = //
			new PRPair(1024, "LOGICAL_EXPR");
	public static final PRPair DEFAULT_CHAR_EXPR = //
			new PRPair(1025, "DEFAULT_CHAR_EXPR");
	public static final PRPair INT_EXPR = //
			new PRPair(1026, "INT_EXPR");
	public static final PRPair NUMERIC_EXPR = //
			new PRPair(1027, "NUMERIC_EXPR");
	public static final PRPair SPECIFICATION_EXPR = //
			new PRPair(1028, "SPECIFICATION_EXPR");
	public static final PRPair CONSTANT_EXPR = //
			new PRPair(1029, "CONSTANT_EXPR");
	public static final PRPair DEFAULT_CHAR_CONSTANT_EXPR = //
			new PRPair(1030, "DEFAULT_CHAR_CONSTANT_EXPR");
	public static final PRPair INT_CONSTANT_EXPR = //
			new PRPair(1031, "INT_CONSTANT_EXPR");
	public static final PRPair ASSIGNMENT_STMT = //
			new PRPair(1032, "ASSIGNMENT_STMT");
	public static final PRPair POINTER_ASSIGNMENT_STMT = //
			new PRPair(1033, "POINTER_ASSIGNMENT_STMT");
	public static final PRPair DATA_POINTER_OBJECT = //
			new PRPair(1034, "DATA_POINTER_OBJECT");
	public static final PRPair BOUNDS_SPEC = //
			new PRPair(1035, "BOUNDS_SPEC");
	public static final PRPair BOUNDS_REMAPPING = //
			new PRPair(1036, "BOUNDS_REMAPPING");
	public static final PRPair DATA_TARGET = //
			new PRPair(1037, "DATA_TARGET");
	public static final PRPair PROC_POINTER_OBJECT = //
			new PRPair(1038, "PROC_POINTER_OBJECT");
	public static final PRPair PROC_COMPONENT_REF = //
			new PRPair(1039, "PROC_COMPONENT_REF");
	public static final PRPair PROC_TARGET = //
			new PRPair(1040, "PROC_TARGET");
	public static final PRPair WHERE_STMT = //
			new PRPair(1041, "WHERE_STMT");
	public static final PRPair WHERE_CONSTRUCT = //
			new PRPair(1042, "WHERE_CONSTRUCT");
	public static final PRPair WHERE_CONSTRUCT_STMT = //
			new PRPair(1043, "WHERE_CONSTRUCT_STMT");
	public static final PRPair WHERE_BODY_CONSTRUCT = //
			new PRPair(1044, "WHERE_BODY_CONSTRUCT");
	public static final PRPair WHERE_ASSIGNMENT_STMT = //
			new PRPair(1045, "WHERE_ASSIGNMENT_STMT");
	public static final PRPair MASK_EXPR = //
			new PRPair(1046, "MASK_EXPR");
	public static final PRPair MASKED_ELSEWHERE_STMT = //
			new PRPair(1047, "MASKED_ELSEWHERE_STMT");
	public static final PRPair ELSEWHERE_STMT = //
			new PRPair(1048, "ELSEWHERE_STMT");
	public static final PRPair END_WHERE_STMT = //
			new PRPair(1049, "END_WHERE_STMT");
	public static final PRPair FORALL_CONSTRUCT = //
			new PRPair(1050, "FORALL_CONSTRUCT");
	public static final PRPair FORALL_CONSTRUCT_STMT = //
			new PRPair(1051, "FORALL_CONSTRUCT_STMT");
	public static final PRPair FORALL_BODY_CONSTRUCT = //
			new PRPair(1052, "FORALL_BODY_CONSTRUCT");
	public static final PRPair FORALL_ASSIGNMENT_STMT = //
			new PRPair(1053, "FORALL_ASSIGNMENT_STMT");
	public static final PRPair END_FORALL_STMT = //
			new PRPair(1054, "END_FORALL_STMT");
	public static final PRPair FORALL_STMT = //
			new PRPair(1055, "FORALL_STMT");

	public static final PRPair BLOCK = //
			new PRPair(1101, "BLOCK");
	public static final PRPair ASSOCIATE_CONSTRUCT = //
			new PRPair(1102, "ASSOCIATE_CONSTRUCT");
	public static final PRPair ASSOCIATE_STMT = //
			new PRPair(1103, "ASSOCIATE_STMT");
	public static final PRPair ASSOCIATION = //
			new PRPair(1104, "ASSOCIATION");
	public static final PRPair SELECTOR = //
			new PRPair(1105, "SELECTOR");
	public static final PRPair END_ASSOCIATE_STMT = //
			new PRPair(1106, "END_ASSOCIATE_STMT");
	public static final PRPair BLOCK_CONSTRUCT = //
			new PRPair(1107, "BLOCK_CONSTRUCT");
	public static final PRPair BLOCK_STMT = //
			new PRPair(1108, "BLOCK_STMT");
	public static final PRPair BLOCK_SPECIFICATION_PART = //
			new PRPair(1109, "BLOCK_SPECIFICATION_PART");
	public static final PRPair END_BLOCK_STMT = //
			new PRPair(1110, "END_BLOCK_STMT");
	public static final PRPair CHANGE_TEAM_CONSTRUCT = //
			new PRPair(1111, "CHANGE_TEAM_CONSTRUCT");
	public static final PRPair CHANGE_TEAM_STMT = //
			new PRPair(1112, "CHANGE_TEAM_STMT");
	public static final PRPair COARRAY_ASSOCIATION = //
			new PRPair(1113, "COARRAY_ASSOCIATION");
	public static final PRPair END_CHANGE_TEAM_STMT = //
			new PRPair(1114, "END_CHANGE_TEAM_STMT");
	public static final PRPair TEAM_VALUE = //
			new PRPair(1115, "TEAM_VALUE");
	public static final PRPair CRITICAL_CONSTRUCT = //
			new PRPair(1116, "CRITICAL_CONSTRUCT");
	public static final PRPair CRITICAL_STMT = //
			new PRPair(1117, "CRITICAL_STMT");
	public static final PRPair END_CRITICAL_STMT = //
			new PRPair(1118, "END_CRITICAL_STMT");
	public static final PRPair DO_CONSTRUCT = //
			new PRPair(1119, "DO_CONSTRUCT");
	public static final PRPair DO_STMT = //
			new PRPair(1120, "DO_STMT");
	public static final PRPair LABEL_DO_STMT = //
			new PRPair(1121, "LABEL_DO_STMT");
	public static final PRPair NONLABEL_DO_STMT = //
			new PRPair(1122, "NONLABEL_DO_STMT");
	public static final PRPair LOOP_CONTROL = //
			new PRPair(1123, "LOOP_CONTROL");
	public static final PRPair DO_VARIABLE = //
			new PRPair(1124, "DO_VARIABLE");
	public static final PRPair CONCURRENT_HEADER = //
			new PRPair(1125, "CONCURRENT_HEADER");
	public static final PRPair CONCURRENT_CONTROL = //
			new PRPair(1126, "CONCURRENT_CONTROL");
	public static final PRPair CONCURRENT_LIMIT = //
			new PRPair(1127, "CONCURRENT_LIMIT");
	public static final PRPair CONCURRENT_STEP = //
			new PRPair(1128, "CONCURRENT_STEP");
	public static final PRPair CONCURRENT_LOCALITY = //
			new PRPair(1129, "CONCURRENT_LOCALITY");
	public static final PRPair LOCALITY_SPEC = //
			new PRPair(1130, "LOCALITY_SPEC");
	public static final PRPair END_DO = //
			new PRPair(1131, "END_DO");
	public static final PRPair END_DO_STMT = //
			new PRPair(1132, "END_DO_STMT");
	public static final PRPair CYCLE_STMT = //
			new PRPair(1133, "CYCLE_STMT");
	public static final PRPair IF_CONSTRUCT = //
			new PRPair(1134, "IF_CONSTRUCT");
	public static final PRPair IF_THEN_STMT = //
			new PRPair(1135, "IF_THEN_STMT");
	public static final PRPair ELSE_IF_STMT = //
			new PRPair(1136, "ELSE_IF_STMT");
	public static final PRPair ELSE_STMT = //
			new PRPair(1137, "ELSE_STMT");
	public static final PRPair END_IF_STMT = //
			new PRPair(1138, "END_IF_STMT");
	public static final PRPair IF_STMT = //
			new PRPair(1139, "IF_STMT");
	public static final PRPair CASE_CONSTRUCT = //
			new PRPair(1140, "CASE_CONSTRUCT");
	public static final PRPair SELECT_CASE_STMT = //
			new PRPair(1141, "SELECT_CASE_STMT");
	public static final PRPair CASE_STMT = //
			new PRPair(1142, "CASE_STMT");
	public static final PRPair END_SELECT_STMT = //
			new PRPair(1143, "END_SELECT_STMT");
	public static final PRPair CASE_EXPR = //
			new PRPair(1144, "CASE_EXPR");
	public static final PRPair CASE_SELECTOR = //
			new PRPair(1145, "CASE_SELECTOR");
	public static final PRPair CASE_VALUE_RANGE = //
			new PRPair(1146, "CASE_VALUE_RANGE");
	public static final PRPair CASE_VALUE = //
			new PRPair(1147, "CASE_VALUE");
	public static final PRPair SELECT_RANK_CONSTRUCT = //
			new PRPair(1148, "SELECT_RANK_CONSTRUCT");
	public static final PRPair SELECT_RANK_STMT = //
			new PRPair(1149, "SELECT_RANK_STMT");
	public static final PRPair SELECT_RANK_CASE_STMT = //
			new PRPair(1150, "SELECT_RANK_CASE_STMT");
	public static final PRPair END_SELECT_RANK_STMT = //
			new PRPair(1151, "END_SELECT_RANK_STMT");
	public static final PRPair SELECT_TYPE_CONSTRUCT = //
			new PRPair(1152, "SELECT_TYPE_CONSTRUCT");
	public static final PRPair SELECT_TYPE_STMT = //
			new PRPair(1153, "SELECT_TYPE_STMT");
	public static final PRPair TYPE_GUARD_STMT = //
			new PRPair(1154, "TYPE_GUARD_STMT");
	public static final PRPair END_SELECT_TYPE_STMT = //
			new PRPair(1155, "END_SELECT_TYPE_STMT");
	public static final PRPair EXIT_STMT = //
			new PRPair(1156, "EXIT_STMT");
	public static final PRPair GOTO_STMT = //
			new PRPair(1157, "GOTO_STMT");
	public static final PRPair COMPUTED_GOTO_STMT = //
			new PRPair(1158, "COMPUTED_GOTO_STMT");
	public static final PRPair CONTINUE_STMT = //
			new PRPair(1159, "CONTINUE_STMT");
	public static final PRPair STOP_STMT = //
			new PRPair(1160, "STOP_STMT");
	public static final PRPair ERROR_STOP_STMT = //
			new PRPair(1161, "ERROR_STOP_STMT");
	public static final PRPair STOP_CODE = //
			new PRPair(1162, "STOP_CODE");
	public static final PRPair FAIL_IMAGE_STMT = //
			new PRPair(1163, "FAIL_IMAGE_STMT");
	public static final PRPair SYNC_ALL_STMT = //
			new PRPair(1164, "SYNC_ALL_STMT");
	public static final PRPair SYNC_STAT = //
			new PRPair(1165, "SYNC_STAT");
	public static final PRPair SYNC_IMAGES_STMT = //
			new PRPair(1166, "SYNC_IMAGES_STMT");
	public static final PRPair IMAGE_SET = //
			new PRPair(1167, "IMAGE_SET");
	public static final PRPair SYNC_MEMORY_STMT = //
			new PRPair(1168, "SYNC_MEMORY_STMT");
	public static final PRPair SYNC_TEAM_STMT = //
			new PRPair(1169, "SYNC_TEAM_STMT");
	public static final PRPair EVENT_POST_STMT = //
			new PRPair(1170, "EVENT_POST_STMT");
	public static final PRPair EVENT_VARIABLE = //
			new PRPair(1171, "EVENT_VARIABLE");
	public static final PRPair EVENT_WAIT_STMT = //
			new PRPair(1172, "EVENT_WAIT_STMT");
	public static final PRPair EVENT_WAIT_SPEC = //
			new PRPair(1173, "EVENT_WAIT_SPEC");
	public static final PRPair UNTIL_SPEC = //
			new PRPair(1174, "UNTIL_SPEC");
	public static final PRPair FORM_TEAM_STMT = //
			new PRPair(1175, "FORM_TEAM_STMT");
	public static final PRPair TEAM_NUMBER = //
			new PRPair(1176, "TEAM_NUMBER");
	public static final PRPair TEAM_VARIABLE = //
			new PRPair(1177, "TEAM_VARIABLE");
	public static final PRPair FORM_TEAM_SPEC = //
			new PRPair(1178, "FORM_TEAM_SPEC");
	public static final PRPair LOCK_STMT = //
			new PRPair(1179, "LOCK_STMT");
	public static final PRPair LOCK_STAT = //
			new PRPair(1180, "LOCK_STAT");
	public static final PRPair UNLOCK_STMT = //
			new PRPair(1181, "UNLCOK_STMT");
	public static final PRPair LOCK_VARIABLE = //
			new PRPair(1182, "LOCK_VARIABLE");

	public static final PRPair IO_UNIT = //
			new PRPair(1201, "IO_UNIT");
	public static final PRPair FILE_UNTIL_NUMBER = //
			new PRPair(1202, "FILE_UNTIL_NUMBER");
	public static final PRPair INTERNAL_FILE_VARIABLE = //
			new PRPair(1203, "INTERNAL_FILE_VARIABLE");
	public static final PRPair OPEN_STMT = //
			new PRPair(1204, "OPEN_STMT");
	public static final PRPair CONNECT_SPEC = //
			new PRPair(1205, "CONNECT_SPEC");
	public static final PRPair FILE_NAME_EXPR = //
			new PRPair(1206, "FILE_NAME_EXPR");
	public static final PRPair IOMSG_VARIABLE = //
			new PRPair(1207, "IOMSG_VARIABLE");
	public static final PRPair CLOSE_STMT = //
			new PRPair(1208, "CLOSE_STMT");
	public static final PRPair CLOSE_SPEC = //
			new PRPair(1209, "CLOSE_SPEC");
	public static final PRPair READ_STMT = //
			new PRPair(1210, "READ_STMT");
	public static final PRPair WRITE_STMT = //
			new PRPair(1211, "WRITE_STMT");
	public static final PRPair PRINT_STMT = //
			new PRPair(1212, "PRINT_STMT");
	public static final PRPair IO_CONTROL_SPEC = //
			new PRPair(1213, "IO_CONTROL_SPEC");
	public static final PRPair ID_VARIABLE = //
			new PRPair(1214, "ID_VARIABLE");
	public static final PRPair FORMAT = //
			new PRPair(1215, "FORMAT");
	public static final PRPair INPUT_ITEM = //
			new PRPair(1216, "INPUT_ITEM");
	public static final PRPair OUTPUT_ITEM = //
			new PRPair(1217, "OUTPUT_ITEM");
	public static final PRPair IO_IMPLIED_DO = //
			new PRPair(1218, "IO_IMPLIED_DO");
	public static final PRPair IO_IMPLIED_DO_OBJECT = //
			new PRPair(1219, "IO_IMPLIED_DO_OBJECT");
	public static final PRPair IO_IMPLIED_DO_CONTROL = //
			new PRPair(1220, "IO_IMPLIED_DO_CONTROL");
	public static final PRPair DTV_TYPE_SPEC = //
			new PRPair(1221, "DTV_TYPE_SPEC");
	public static final PRPair WAIT_STMT = //
			new PRPair(1222, "WAIT_STMT");
	public static final PRPair WAIT_SPEC = //
			new PRPair(1223, "WAIT_SPEC");
	public static final PRPair BACKSPACE_STMT = //
			new PRPair(1224, "BACKSPACE_STMT");
	public static final PRPair ENDFILE_STMT = //
			new PRPair(1225, "ENDFILE_STMT");
	public static final PRPair REWIND_STMT = //
			new PRPair(1226, "REWIND_STMT");
	public static final PRPair POSITION_SPEC = //
			new PRPair(1227, "POSITION_SPEC");
	public static final PRPair FLUSH_STMT = //
			new PRPair(1228, "FLUSH_STMT");
	public static final PRPair FLUSH_SPEC = //
			new PRPair(1229, "FLUSH_SPEC");
	public static final PRPair INQUIRE_STMT = //
			new PRPair(1230, "INQUIRE_STMT");
	public static final PRPair INQUIRE_SPEC = //
			new PRPair(1231, "INQUIRE_SPEC");

	public static final PRPair FORMAT_STMT = //
			new PRPair(1301, "FORMAT_STMT");
	public static final PRPair FORMAT_SPECIFICATION = //
			new PRPair(1302, "FORMAT_SPECIFICATION");
	public static final PRPair FORMAT_ITEMS = //
			new PRPair(1303, "FORMAT_ITEMS");
	public static final PRPair FORMAT_ITEM = //
			new PRPair(1304, "FORMAT_ITEM");
	public static final PRPair UNLIMITED_FORMAT_ITEM = //
			new PRPair(1305, "UNLIMITED_FORMAT_ITEM");
	public static final PRPair R = //
			new PRPair(1306, "R");
	public static final PRPair DATA_EDIT_SPEC = //
			new PRPair(1307, "DATA_EDIT_SPEC");
	public static final PRPair W = //
			new PRPair(1308, "W");
	public static final PRPair M = //
			new PRPair(1309, "M");
	public static final PRPair D = //
			new PRPair(1310, "D");
	public static final PRPair E = //
			new PRPair(1311, "E");
	public static final PRPair V = //
			new PRPair(1312, "V");
	public static final PRPair CONTROL_EDIT_SPEC = //
			new PRPair(1313, "CONTROL_EDIT_SPEC");
	public static final PRPair K = //
			new PRPair(1314, "K");
	public static final PRPair POSITION_EDIT_SPEC = //
			new PRPair(1315, "POSITION_EDIT_SPEC");
	public static final PRPair N = //
			new PRPair(1316, "N");
	public static final PRPair SIGN_EDIT_DESC = //
			new PRPair(1317, "SIGN_EDIT_DESC");
	public static final PRPair BLANK_INTERP_EDIT_DESC = //
			new PRPair(1318, "BLANK_INTERP_EDIT_DESC");
	public static final PRPair ROUND_EDIT_DESC = //
			new PRPair(1319, "ROUND_EDIT_DESC");
	public static final PRPair DECIMAL_EDIT_DESC = //
			new PRPair(1320, "DECIMAL_EDIT_DESC");
	public static final PRPair CHAR_STRING_EDIT_SPEC = //
			new PRPair(1321, "CHAR_STRING_EDIT_SPEC");

	public static final PRPair MAIN_PROGRAM = //
			new PRPair(1401, "MAIN_PROGRAM");
	public static final PRPair PROGRAM_STMT = //
			new PRPair(1402, "PROGRAM_STMT");
	public static final PRPair END_PROGRAM_STMT = //
			new PRPair(1403, "END_PROGRAM_STMT");
	public static final PRPair MODULE = //
			new PRPair(1404, "MODULE");
	public static final PRPair MODULE_STMT = //
			new PRPair(1405, "MODULE_STMT");
	public static final PRPair END_MODULE_STMT = //
			new PRPair(1406, "END_MODULE_STMT");
	public static final PRPair MODULE_SUBPROGRAM_PART = //
			new PRPair(1407, "MODULE_SUBPROGRAM_PART");
	public static final PRPair MODULE_SUBPROGRAM = //
			new PRPair(1408, "MODULE_SUBPROGRAM");
	public static final PRPair USE_STMT = //
			new PRPair(1409, "USE_STMT");
	public static final PRPair MODULE_NATURE = //
			new PRPair(1410, "MODULE_NATURE");
	public static final PRPair RENAME = //
			new PRPair(1411, "RENAME");
	public static final PRPair ONLY = //
			new PRPair(1412, "ONLY");
	public static final PRPair ONLY_USE_STMT = //
			new PRPair(1413, "ONLY_USE_STMT");
	public static final PRPair LOCAL_DEFINED_OPERATOR = //
			new PRPair(1414, "LOCAL_DEFINED_OPERATOR");
	public static final PRPair USE_DEFINED_OPERATOR = //
			new PRPair(1415, "USE_DEFINED_OPERATOR");
	public static final PRPair SUBMODULE = //
			new PRPair(1416, "SUBMODULE");
	public static final PRPair SUBMODULE_STMT = //
			new PRPair(1417, "SUBMODULE_STMT");
	public static final PRPair PARENT_IDENTIFIER = //
			new PRPair(1418, "PARENT_IDENTIFIER");
	public static final PRPair END_SUBMODULE_STMT = //
			new PRPair(1419, "END_SUBMODULE_STMT");
	public static final PRPair BLOCK_DATA = //
			new PRPair(1420, "BLOCK_DATA");
	public static final PRPair BLOCK_DATA_STMT = //
			new PRPair(1421, "BLOCK_DATA_STMT");
	public static final PRPair END_BLOCK_DATA_STMT = //
			new PRPair(1422, "END_BLOCK_DATA_STMT");

	public static final PRPair INTERFACE_BLOCK = //
			new PRPair(1501, "INTERFACE_BLOCK");
	public static final PRPair INTERFACE_SPECIFICATION = //
			new PRPair(1502, "INTERFACE_SPECIFICATION");
	public static final PRPair INTERFACE_STMT = //
			new PRPair(1503, "INTERFACE_STMT");
	public static final PRPair END_INTERFACE_STMT = //
			new PRPair(1504, "END_INTERFACE_STMT");
	public static final PRPair INTERFACE_BODY = //
			new PRPair(1505, "INTERFACE_BODY");
	public static final PRPair PROCEDURE_STMT = //
			new PRPair(1506, "PROCEDURE_STMT");
	public static final PRPair SPECIFIC_PROCEDURE = //
			new PRPair(1507, "SPECIFIC_PROCEDURE");
	public static final PRPair GENERIC_SPEC = //
			new PRPair(1508, "GENERIC_SPEC");
	public static final PRPair DEFINED_IO_GENERIC_SPEC = //
			new PRPair(1509, "DEFINED_IO_GENERIC_SPEC");
	public static final PRPair GENERIC_STMT = //
			new PRPair(1510, "GENERIC_STMT");
	public static final PRPair EXTERNAL_STMT = //
			new PRPair(1511, "EXTERNAL_STMT");
	public static final PRPair PROCEDURE_DECLARATION_STMT = //
			new PRPair(1512, "PROCEDURE_DECLARATION_STMT");
	public static final PRPair PROC_INTERFACE = //
			new PRPair(1513, "PROC_INTERFACE");
	public static final PRPair PROC_ATTR_SPEC = //
			new PRPair(1514, "PROC_ATTR_SPEC");
	public static final PRPair PROC_DECL = //
			new PRPair(1515, "PROC_DECL");
	public static final PRPair INTERFACE_NAME = //
			new PRPair(1516, "INTERFACE_NAME");
	public static final PRPair PROC_POINTER_INIT = //
			new PRPair(1517, "PROC_POINTER_INIT");
	public static final PRPair INITIAL_PROC_TARGET = //
			new PRPair(1518, "INITIAL_PROC_TARGET");
	public static final PRPair INTRINSIC_STMT = //
			new PRPair(1519, "INTRINSIC_STMT");
	public static final PRPair FUNCTION_REFERENCE = //
			new PRPair(1520, "FUNCTION_REFERENCE");
	public static final PRPair CALL_STMT = //
			new PRPair(1521, "CALL_STMT");
	public static final PRPair PROCEDURE_DESIGNATOR = //
			new PRPair(1522, "PROCEDURE_DESIGNATOR");
	public static final PRPair ACTUAL_ARG_SPEC = //
			new PRPair(1523, "ACTUAL_ARG_SPEC");
	public static final PRPair ACTUAL_ARG = //
			new PRPair(1524, "ACTUAL_ARG");
	public static final PRPair ALT_RETURN_SPEC = //
			new PRPair(1525, "ALT_RETURN_SPEC");
	public static final PRPair PREFIX = //
			new PRPair(1526, "PREFIX");
	public static final PRPair PREFIX_SPEC = //
			new PRPair(1527, "PREFIX_SPEC");
	public static final PRPair PROC_LANGUAGE_BINDING_SPEC = //
			new PRPair(1528, "PROC_LANGUAGE_BINDING_SPEC");
	public static final PRPair FUNCTION_SUBPROGRAM = //
			new PRPair(1529, "FUNCTION_SUBPROGRAM");
	public static final PRPair FUNCTION_STMT = //
			new PRPair(1530, "FUNCTION_STMT");
	public static final PRPair DUMMY_ARG_STMT = //
			new PRPair(1531, "DUMMY_ARG_STMT");
	public static final PRPair SUFFIX = //
			new PRPair(1532, "SUFFIX");
	public static final PRPair END_FUNCTION_STMT = //
			new PRPair(1533, "END_FUNCTION_STMT");
	public static final PRPair SUBROUTINE_SUBPROGRAM = //
			new PRPair(1534, "SUBROUTINE_SUBPROGRAM");
	public static final PRPair SUBROUTINE_STMT = //
			new PRPair(1535, "SUBROUTINE_STMT");
	public static final PRPair DUMMY_ARG = //
			new PRPair(1536, "DUMMY_ARG");
	public static final PRPair END_SUBROUTINE_STMT = //
			new PRPair(1537, "END_SUBROUTINE_STMT");
	public static final PRPair SEPARATE_MODULE_SUBPROGRAM = //
			new PRPair(1538, "SEPARATE_MODULE_SUBPROGRAM");
	public static final PRPair MP_SUBPROGRAM_STMT = //
			new PRPair(1539, "MP_SUBPROGRAM_STMT");
	public static final PRPair END_MP_SUBPROGRAM_STMT = //
			new PRPair(1540, "END_MP_SUBPROGRAM_STMT");
	public static final PRPair ENTRY_STMT = //
			new PRPair(1541, "ENTRY_STMT");
	public static final PRPair RETURN_STMT = //
			new PRPair(1542, "RETURN_STMT");
	public static final PRPair CONTAINS_STMT = //
			new PRPair(1543, "CONTAINS_STMT");
	public static final PRPair STMT_FUCNTION_STMT = //
			new PRPair(1544, "STMT_FUCNTION_STMT");

	/* * * * * CIVL Extension Rules * * * * */

	public static final PRPair PRAGMA_STMT = //
			new PRPair(1600, "STMT_PRAGMA");
	public static final PRPair PRAGMA_TOKEN = //
			new PRPair(1601, "PRAGMA_TOKEN");
	public static final PRPair CIVL_PRIMITIVE = //
			new PRPair(1610, "CIVL_PRIMITIVE");
	public static final PRPair PRAGMA_TYPE_QUALIFIER_STMT = //
			new PRPair(1611, "PRAGMA_TYPE_QUALIFIER_STMT");
	public static final PRPair PRAGMA_TYPE_QUALIFIER = //
			new PRPair(1612, "PRAGMA_TYPE_QUALIFIER");
	public static final PRPair CIVL_STMT = //
			new PRPair(1613, "CIVL_STMT");
	public static final PRPair QUANTIFIED_EXPR = //
			new PRPair(1614, "QUANTIFIED_EXPR");

	/* * * * * Other Extension Rules * * * * */
	public static final PRPair ATTR_SPEC_EXT = //
			new PRPair(2000, "ATTR_SPEC_EXT");
	public static final PRPair DIMENSION_DECL = //
			new PRPair(8481, "DIMENSION_DECL");

	public static class PRPair {
		private int rId;
		private String rName;

		PRPair(int ruleId, String ruleName) {
			rId = ruleId;
			rName = ruleName;
		}

		public int getRule() {
			return rId;
		}

		String getName() {
			return rName;
		}

		@Override
		public String toString() {
			return Integer.toString(rId) + ":" + rName;
		}
	}
}
