/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk2.nashorn.internal.objects;

import static jdk2.nashorn.internal.codegen.CompilerConstants.specialCall;
import static jdk2.nashorn.internal.codegen.CompilerConstants.staticCall;
import static jdk2.nashorn.internal.lookup.Lookup.MH;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jdk2.nashorn.internal.objects.annotations.Attribute;
import jdk2.nashorn.internal.objects.annotations.Function;
import jdk2.nashorn.internal.objects.annotations.Property;
import jdk2.nashorn.internal.objects.annotations.ScriptClass;
import jdk2.nashorn.internal.objects.annotations.Where;
import jdk2.nashorn.internal.runtime.AccessorProperty;
import jdk2.nashorn.internal.runtime.JSType;
import jdk2.nashorn.internal.runtime.PropertyMap;
import jdk2.nashorn.internal.runtime.ScriptFunction;
import jdk2.nashorn.internal.runtime.ScriptObject;
import jdk2.nashorn.internal.runtime.arrays.ArrayData;
import jdk2.nashorn.internal.runtime.arrays.TypedArrayData;

/**
 * Uint8 clamped array for TypedArray extension
 */
@ScriptClass("Uint8ClampedArray")
public final class NativeUint8ClampedArray extends ArrayBufferView {
    /**
     * The size in bytes of each element in the array.
     */
    @Property(attributes = Attribute.NOT_ENUMERABLE | Attribute.NOT_WRITABLE | Attribute.NOT_CONFIGURABLE, where = Where.CONSTRUCTOR)
    public static final int BYTES_PER_ELEMENT = 1;

    // initialized by nasgen
    @SuppressWarnings("unused")
    private static PropertyMap $nasgenmap$;

    private static final Factory FACTORY = new Factory(BYTES_PER_ELEMENT) {
        @Override
        public ArrayBufferView construct(final NativeArrayBuffer buffer, final int byteOffset, final int length) {
            return new NativeUint8ClampedArray(buffer, byteOffset, length);
        }

        @Override
        public Uint8ClampedArrayData createArrayData(final ByteBuffer nb, final int start, final int end) {
            return new Uint8ClampedArrayData(nb, start, end);
        }

        @Override
        public String getClassName() {
            return "Uint8ClampedArray";
        }
    };

    private static final class Uint8ClampedArrayData extends TypedArrayData<ByteBuffer> {

        private static final MethodHandle GET_ELEM = specialCall(MethodHandles.lookup(), Uint8ClampedArrayData.class, "getElem", int.class, int.class).methodHandle();
        private static final MethodHandle SET_ELEM = specialCall(MethodHandles.lookup(), Uint8ClampedArrayData.class, "setElem", void.class, int.class, int.class).methodHandle();
        private static final MethodHandle RINT_D   = staticCall(MethodHandles.lookup(), Uint8ClampedArrayData.class, "rint", double.class, double.class).methodHandle();
        private static final MethodHandle RINT_O   = staticCall(MethodHandles.lookup(), Uint8ClampedArrayData.class, "rint", Object.class, Object.class).methodHandle();
        private static final MethodHandle CLAMP_LONG = staticCall(MethodHandles.lookup(), Uint8ClampedArrayData.class, "clampLong", long.class, long.class).methodHandle();

        private Uint8ClampedArrayData(final ByteBuffer nb, final int start, final int end) {
            super(((ByteBuffer)nb.position(start).limit(end)).slice(), end - start);
        }

        @Override
        protected MethodHandle getGetElem() {
            return GET_ELEM;
        }

        @Override
        protected MethodHandle getSetElem() {
            return SET_ELEM;
        }

        @Override
        public Class<?> getElementType() {
            return int.class;
        }

        @Override
        public Class<?> getBoxedElementType() {
            return int.class;
        }

        private int getElem(final int index) {
            try {
                return nb.get(index) & 0xff;
            } catch (final IndexOutOfBoundsException e) {
                throw new ClassCastException(); //force relink - this works for unoptimistic too
            }
        }

        @Override
        public MethodHandle getElementSetter(final Class<?> elementType) {
            final MethodHandle setter = super.getElementSetter(elementType); //getContinuousElementSetter(getClass(), setElem(), elementType);
            if (setter != null) {
                if (elementType == Object.class) {
                    return MH.filterArguments(setter, 2, RINT_O);
                } else if (elementType == double.class) {
                    return MH.filterArguments(setter, 2, RINT_D);
                } else if (elementType == long.class) {
                    return MH.filterArguments(setter, 2, CLAMP_LONG);
                }
            }
            return setter;
        }

        private void setElem(final int index, final int elem) {
            try {
                final byte clamped;
                if ((elem & 0xffff_ff00) == 0) {
                    clamped = (byte)elem;
                } else {
                    clamped = elem < 0 ? 0 : (byte)0xff;
                }
                nb.put(index, clamped);
            } catch (final IndexOutOfBoundsException e) {
                //swallow valid array indexes. it's ok.
                if (index < 0) {
                    throw new ClassCastException();
                }
            }
        }

        @Override
        public boolean isClamped() {
            return true;
        }

        @Override
        public boolean isUnsigned() {
            return true;
        }

        @Override
        public int getInt(final int index) {
            return getElem(index);
        }

        @Override
        public int getIntOptimistic(final int index, final int programPoint) {
            return getElem(index);
        }

        @Override
        public long getLong(final int index) {
            return getInt(index);
        }

        @Override
        public long getLongOptimistic(final int index, final int programPoint) {
            return getElem(index);
        }

        @Override
        public double getDouble(final int index) {
            return getInt(index);
        }

        @Override
        public double getDoubleOptimistic(final int index, final int programPoint) {
            return getElem(index);
        }

        @Override
        public Object getObject(final int index) {
            return getInt(index);
        }

        @Override
        public ArrayData set(final int index, final Object value, final boolean strict) {
            return set(index, JSType.toNumber(value), strict);
        }

        @Override
        public ArrayData set(final int index, final int value, final boolean strict) {
            setElem(index, value);
            return this;
        }

        @Override
        public ArrayData set(final int index, final long value, final boolean strict) {
            return set(index, (int)value, strict);
        }

        @Override
        public ArrayData set(final int index, final double value, final boolean strict) {
            return set(index, rint(value), strict);
        }

        private static double rint(final double rint) {
            return (int)Math.rint(rint);
        }

        @SuppressWarnings("unused")
        private static Object rint(final Object rint) {
            return rint(JSType.toNumber(rint));
        }

        @SuppressWarnings("unused")
        private static long clampLong(final long l) {
            if(l < 0L) {
                return 0L;
            } else if(l > 0xffL) {
                return 0xffL;
            }
            return l;
        }
    }

    /**
     * Constructor
     *
     * @param newObj is this typed array instantiated with the new operator
     * @param self   self reference
     * @param args   args
     *
     * @return new typed array
     */
    @jdk2.nashorn.internal.objects.annotations.Constructor(arity = 1)
    public static NativeUint8ClampedArray constructor(final boolean newObj, final Object self, final Object... args) {
        return (NativeUint8ClampedArray)constructorImpl(newObj, args, FACTORY);
    }

    NativeUint8ClampedArray(final NativeArrayBuffer buffer, final int byteOffset, final int length) {
        super(buffer, byteOffset, length);
    }

    @Override
    protected Factory factory() {
        return FACTORY;
    }

    /**
     * Set values
     * @param self   self reference
     * @param array  multiple values of array's type to set
     * @param offset optional start index, interpreted  0 if undefined
     * @return undefined
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    protected static Object set(final Object self, final Object array, final Object offset) {
        return ArrayBufferView.setImpl(self, array, offset);
    }

    /**
     * Returns a new TypedArray view of the ArrayBuffer store for this TypedArray,
     * referencing the elements at begin, inclusive, up to end, exclusive. If either
     * begin or end is negative, it refers to an index from the end of the array,
     * as opposed to from the beginning.
     * <p>
     * If end is unspecified, the subarray contains all elements from begin to the end
     * of the TypedArray. The range specified by the begin and end values is clamped to
     * the valid index range for the current array. If the computed length of the new
     * TypedArray would be negative, it is clamped to zero.
     * <p>
     * The returned TypedArray will be of the same type as the array on which this
     * method is invoked.
     *
     * @param self self reference
     * @param begin begin position
     * @param end end position
     *
     * @return sub array
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    protected static NativeUint8ClampedArray subarray(final Object self, final Object begin, final Object end) {
        return (NativeUint8ClampedArray)ArrayBufferView.subarrayImpl(self, begin, end);
    }

    @Override
    protected ScriptObject getPrototype(final Global global) {
        return global.getUint8ClampedArrayPrototype();
    }

    static {
            final List<jdk2.nashorn.internal.runtime.Property> list = Collections.emptyList();
            $nasgenmap$ = PropertyMap.newMap(list);
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(NativeUint8ClampedArray.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
    static final class Constructor extends ScriptFunctionImpl {
        private static final PropertyMap $nasgenmap$;

        static {
            final List<jdk2.nashorn.internal.runtime.Property> list = new ArrayList<>(2);
            list.add(AccessorProperty.create("BYTES_PER_ELEMENT", jdk2.nashorn.internal.runtime.Property.NOT_CONFIGURABLE, 
                    virtualHandle("G$BYTES_PER_ELEMENT", int.class),
null));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        public int G$BYTES_PER_ELEMENT() {
            return BYTES_PER_ELEMENT;
         }

        Constructor() {
            super("Uint8ClampedArray", 
                    staticHandle("constructor", NativeUint8ClampedArray.class, boolean.class, Object.class, Object[].class),
                    $nasgenmap$, null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Constructor.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    static final class Prototype extends PrototypeObject {
        private ScriptFunction set;
        private ScriptFunction subarray;
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$set() {
            return this.set;
        }

        public void S$set(final ScriptFunction function) {
            this.set = function;
        }

        public ScriptFunction G$subarray() {
            return this.subarray;
        }

        public void S$subarray(final ScriptFunction function) {
            this.subarray = function;
        }

        static {
            final List<jdk2.nashorn.internal.runtime.Property> list = new ArrayList<>(3);
            list.add(AccessorProperty.create("set", jdk2.nashorn.internal.runtime.Property.NOT_ENUMERABLE, 
                    virtualHandle("G$set", ScriptFunction.class),
                    virtualHandle("S$set", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("subarray", jdk2.nashorn.internal.runtime.Property.NOT_ENUMERABLE, 
                    virtualHandle("G$subarray", ScriptFunction.class),
                    virtualHandle("S$subarray", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            set = ScriptFunctionImpl.makeFunction("set",
                    staticHandle("set", Object.class, Object.class, Object.class, Object.class));
            subarray = ScriptFunctionImpl.makeFunction("subarray",
                    staticHandle("subarray", NativeUint8ClampedArray.class, Object.class, Object.class, Object.class));
        }

       public String getClassName() {
           return "Uint8ClampedArray";
       }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Prototype.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
