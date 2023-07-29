package edu.udel.cis.vsl.civl.dynamic.common;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.NTReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.expr.TupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.UnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * This class implements operations on a heap. A heap consists of a fixed number
 * (however, the number may vary from models to models) of heap fields, while a
 * heap field is composed of a dynamic number of heap objects of type array of
 * type T.
 * <ul>
 * <li>each dyscope has a heap by default, as the 0 variable of that
 * dyscope;</li>
 * <li>the initial value of a heap is a SARL NULL expression (not JAVA's
 * NULL);</li>
 * <li>a heap has the type &lt;array-of-array-of-T1, array-of-array-of-T2, ...>,
 * corresponding to malloc statements and handle objects used in the model;</li>
 * <li>a heap field has the type array-of-array-of-T;</li>
 * <li>a heap object has the type array of type T, which is
 * allocated/deallocated by malloc(handle create)/deallocate(handle destroy)
 * methods;</li>
 * <li>a heap atomic object has the type T, and is an element of a certain heap
 * object;</li>
 * <li>a heap memory unit is a pointer to a heap object, which has type
 * pointer-to-array-of-T, and has the form <code>&di$heap&lt;j,k></code>, where
 * <code>i</code> is the dyscope ID of the heap, <code>j</code> is the index of
 * the heap field that the heap object belongs to, and <code>k</code> is the
 * index of the heap object in the heap field.</li>
 * <li>a heap pointer is any pointer pointing to some part of the heap.</li>
 * </ul>
 * 
 * TODO: malloc pointer This class is part of the symbolic utility, the purpose
 * of which is to factor out the code related to heap.
 * 
 * @author Manchun Zheng
 * 
 */
public class HeapAnalyzer {

	/**
	 * <p>
	 * This is a representation of a CIVL memory block. A CIVL memory block is
	 * space in heap which can store a sequence of heap objects allocated by
	 * once execution of a <code>$malloc</code> statement. A CIVL memory block
	 * is identified by a pair: <code>{mallocID : executionRecord}</code>, where
	 * the mallocID is a unique integer associates to a lexical $malloc
	 * statement in CIVL model and a executionRecord is a unique integer in a
	 * state for the times of a $malloc statement being executed.
	 * </p>
	 * 
	 * @author xxxx
	 *
	 */
	class CIVLMemoryBlock {
		/**
		 * A part of a memory block identifier: the lexical 'malloc' statement
		 * ID.
		 */
		private IntObject mallocID;
		/**
		 * A part of a memory block identifier: the execution record.
		 */
		private NumericExpression execRecord;

		private CIVLMemoryBlock(IntObject mallocRecord,
				NumericExpression execRecord) {
			this.mallocID = mallocRecord;
			this.execRecord = execRecord;
		}

		/**
		 * Compares if this memory block and the 'other' are the same memory
		 * block.
		 * 
		 * @param other
		 *            Another {@link CIVLMemoryBlock}
		 * @return True iff this this memory block and the 'other' are the same
		 *         memory block
		 */
		public boolean compare(CIVLMemoryBlock other) {
			return mallocID.equals(other.mallocID)
					&& execRecord.equals(other.execRecord);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			else if (other instanceof CIVLMemoryBlock)
				return compare((CIVLMemoryBlock) other);
			return false;
		}

		@Override
		public String toString() {
			return mallocID + ":" + execRecord;
		}
	}

	/* *************************** Instance Fields ************************* */

	/**
	 * The symbolic utility to be used.
	 */
	private CommonSymbolicUtility symbolicUtil;

	/**
	 * The symbolic universe for operations on symbolic expressions.
	 */
	private SymbolicUniverse universe;

	/**
	 * Integer object 2.
	 */
	private IntObject twoObj;

	/* ***************************** Constructor *************************** */

	/**
	 * Creates a new instance of a heap analyzer.
	 * 
	 * @param util
	 *            The symbolic utility to be used.
	 */
	HeapAnalyzer(SymbolicUniverse universe, CommonSymbolicUtility util) {
		this.symbolicUtil = util;
		this.universe = universe;
		this.twoObj = universe.intObject(2);
	}

	/* *********************** Package-Private Methods ********************* */

	/**
	 * <p>
	 * Returns the memory unit of a heap object which is involved by the given
	 * pointer. For example,
	 * <code>p = (int*) $malloc($root, sizeof(int)*5)</code> allocates a heap
	 * object that has the type array of int with length 5, and the value of
	 * <code>p</code> is a pointer pointing to the first element of the heap
	 * object.
	 * </p>
	 * 
	 * @param pointer
	 *            A valid pointer that points to some part of a heap.
	 * @return A pointer to a heap object that is involved by the given pointer.
	 */
	SymbolicExpression heapMemUnit(SymbolicExpression pointer) {
		ReferenceExpression ref = symbolicUtil.getSymRef(pointer);
		List<ReferenceExpression> ancestors = symbolicUtil.ancestorsOfRef(ref);

		return symbolicUtil.setSymRef(pointer, ancestors.get(1));
	}

	/**
	 * Extracts the reference expression w.r.t a heap atomic object or a heap
	 * object.
	 * 
	 * @param ref
	 *            The original reference expression.
	 * @param heapAtomicObject
	 *            True iff the context is for a heap atomic object.
	 * @return The reference expression w.r.t. to either a heap atomic object or
	 *         a heap object.
	 */
	Pair<ReferenceExpression, Integer> heapReference(ReferenceExpression ref,
			boolean heapAtomicObject) {
		if (ref.isIdentityReference())
			return new Pair<>(ref, 0);
		else {
			ReferenceExpression parentRef = ((NTReferenceExpression) ref)
					.getParent();
			Pair<ReferenceExpression, Integer> parentResult;

			parentResult = heapReference(parentRef, heapAtomicObject);
			if (!heapAtomicObject) {
				// the case of heap memory unit
				if (parentResult.right < 2)
					return new Pair<>(ref, parentResult.right + 1);
				else {
					ReferenceExpression newRef;

					if (parentResult.right == 2)
						parentRef = universe.identityReference();
					else
						parentRef = parentResult.left;
					if (ref.isArrayElementReference()) {
						newRef = universe.arrayElementReference(parentRef,
								((ArrayElementReference) ref).getIndex());
					} else if (ref.isTupleComponentReference())
						newRef = universe.tupleComponentReference(parentRef,
								((TupleComponentReference) ref).getIndex());
					else
						newRef = universe.unionMemberReference(parentRef,
								((UnionMemberReference) ref).getIndex());
					return new Pair<>(newRef, 3);
				}
			} else {
				// the case of heap atomic object
				if (parentResult.right < 3)
					return new Pair<>(ref, parentResult.right + 1);
				else {
					ReferenceExpression newRef;

					if (parentResult.right == 3)
						parentRef = universe.identityReference();
					else
						parentRef = parentResult.left;
					if (ref.isArrayElementReference()) {
						newRef = universe.arrayElementReference(parentRef,
								((ArrayElementReference) ref).getIndex());
					} else if (ref.isTupleComponentReference())
						newRef = universe.tupleComponentReference(parentRef,
								((TupleComponentReference) ref).getIndex());
					else
						newRef = universe.unionMemberReference(parentRef,
								((UnionMemberReference) ref).getIndex());
					return new Pair<>(newRef, 4);
				}
			}
		}
	}

	/**
	 * Constructs an invalid heap object of a certain type. A heap object
	 * becomes invalid when it gets deallocated. An invalid heap object is a
	 * symbolic constant with the name "INVALID".
	 * 
	 * @param heapObjectType
	 *            The type of the heap object.
	 * @return The invalid heap object of the given type.
	 */
	SymbolicConstant invalidHeapObject(SymbolicType heapObjectType) {
		StringObject name = universe.stringObject(ModelConfiguration.INVALID);

		return universe.symbolicConstant(name, heapObjectType);
	}

	/**
	 * Is this heap empty?
	 * 
	 * @param heapValue
	 *            The heap to be tested.
	 * @return True iff the given heap is empty.
	 */
	boolean isEmptyHeap(SymbolicExpression heapValue) {
		if (heapValue.isNull())
			return true;
		else {
			int count = heapValue.numArguments();

			for (int i = 0; i < count; i++) {
				SymbolicExpression heapField = (SymbolicExpression) heapValue
						.argument(i);
				int numObjects = heapField.numArguments();

				for (int j = 0; j < numObjects; j++) {
					SymbolicExpression heapObj = (SymbolicExpression) heapField
							.argument(j);

					if (!this.isInvalidHeapObject(heapObj))
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Is the given pointer pointing to the first element of a heap object,
	 * i.e., a heap atomic object? A pointer to a heap atomic object shall have
	 * the form of: <code>&<dn,i,j>[0]</code>
	 * 
	 * @param source
	 *            The source code information for error report.
	 * @param pointer
	 *            The pointer to be checked.
	 * @return True iff the given pointer is pointing to the first element of a
	 *         heap object.
	 */
	boolean isHeapAtomicObjectPointer(CIVLSource source,
			SymbolicExpression pointer) {
		ReferenceExpression ref = symbolicUtil.getSymRef(pointer);
		ArrayElementReference arrayEleRef;

		if (!ref.isArrayElementReference())
			return false;
		arrayEleRef = (ArrayElementReference) ref;
		if (!arrayEleRef.getIndex().isZero())
			return false;
		ref = arrayEleRef.getParent();
		if (!ref.isArrayElementReference())
			return false;
		ref = ((ArrayElementReference) ref).getParent();
		if (!ref.isTupleComponentReference())
			return false;
		ref = ((TupleComponentReference) ref).getParent();
		if (ref.isIdentityReference())
			return true;
		return false;
	}

	/**
	 * <p>
	 * Returns true iff the given pointer is a <strong>pointer to a memory
	 * block</strong>, where a memory block is defined as a space in heap which
	 * is allocated by once execution of <code>$malloc</code>
	 * </p>
	 * 
	 * <p>
	 * Pointer arithmetic with offset greater than 1 on pointer to a memory
	 * block is invalid.
	 * </p>
	 * 
	 * @param pointer
	 *            A {@link SymbolicExpression} which is a concrete pointer.
	 * @return true iff the given pointer is a <strong>pointer to a memory
	 *         block</strong>
	 * @author xxxx
	 */
	boolean isPointer2MemoryBlock(SymbolicExpression pointer) {
		if (pointer.operator() != SymbolicOperator.TUPLE)
			return false;
		if (!isPointerToHeap(pointer))
			return false;
		ReferenceExpression ref = symbolicUtil.getSymRef(pointer);

		if (!ref.isArrayElementReference())
			return false;
		ref = ((ArrayElementReference) ref).getParent();
		if (!ref.isTupleComponentReference())
			return false;
		ref = ((TupleComponentReference) ref).getParent();
		if (!ref.isIdentityReference())
			return false;
		return true;
	}

	/**
	 * <p>
	 * <strong>pre-condition:</strong> the given pointer is a pointer to
	 * somewhere in heap.
	 * </p>
	 * <p>
	 * Return the memory block ({@link CIVLMemoryBlock}) pointed by the given
	 * pointer.
	 * </p>
	 * 
	 * @param pointerToHeap
	 * @return
	 */
	CIVLMemoryBlock memoryBlock(SymbolicExpression pointerToHeap) {
		ReferenceExpression refQueue[] = new ReferenceExpression[2];
		ReferenceExpression ref = symbolicUtil.getSymRef(pointerToHeap);
		int head = 0;

		// The last 2 NTReferenceExpression in the given pointer contain the
		// identification info of a memory block, thus maintain a cyclic queue
		// with length 2 whose invariant is "contains the last 2 explored
		// non-trivial references in the pointer":
		assert !ref.isNull() && !ref.isIdentityReference();
		do {
			NTReferenceExpression ntRef;

			refQueue[head] = ref;
			head = 1 - head; // switch head
			ntRef = (NTReferenceExpression) ref;
			ref = ntRef.getParent();
		} while (!ref.isNull() && !ref.isIdentityReference());
		assert refQueue[1 - head].isTupleComponentReference()
				&& refQueue[head].isArrayElementReference();
		return new CIVLMemoryBlock(
				((TupleComponentReference) refQueue[1 - head]).getIndex(),
				((ArrayElementReference) refQueue[head]).getIndex());
	}

	/**
	 * Is this heap object invalid? An invalid heap object is a symbolic
	 * constant with the name "INVALID".
	 * 
	 * @param heapObject
	 *            The heap object.
	 * @return True iff the given heap object is invalid.
	 */
	boolean isInvalidHeapObject(SymbolicExpression heapObject) {
		if (heapObject instanceof SymbolicConstant) {
			SymbolicConstant constant = (SymbolicConstant) heapObject;
			StringObject name = constant.name();

			if (name.getString().equals(ModelConfiguration.INVALID))
				return true;
		}
		return false;
	}

	/**
	 * Is the given pointer pointing to a memory space that is part of a heap?
	 * For any dyscope, variable 0 is always the heap.
	 * 
	 * @param pointer
	 *            The pointer to be tested.
	 * 
	 * @return True iff the pointer points to a certain part of some heap.
	 */
	boolean isPointerToHeap(SymbolicExpression pointer) {
		if (pointer.operator() != SymbolicOperator.TUPLE)
			return false;

		int vid = symbolicUtil.getVariableId(null, pointer);

		return vid == 0;
	}

	/**
	 * Computes the reference expression of a given heap pointer w.r.t the
	 * corresponding heap memory unit.
	 * 
	 * @param heapPointer
	 *            The heap pointer.
	 * @return The reference expression of a given pointer w.r.t the
	 *         corresponding heap memory unit.
	 */
	ReferenceExpression referenceToHeapMemUnit(SymbolicExpression pointer) {
		ReferenceExpression ref = (ReferenceExpression) universe
				.tupleRead(pointer, twoObj);
		Pair<ReferenceExpression, Integer> refResult;

		assert this.isPointerToHeap(pointer);
		refResult = this.heapReference(ref, false);
		if (refResult.right == 2)
			return universe.identityReference();
		else
			return refResult.left;
	}
}
