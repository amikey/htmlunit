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

import static jdk2.nashorn.internal.lookup.Lookup.MH;
import static jdk2.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk2.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import jdk2.internal.dynalink.beans.BeansLinker;
import jdk2.internal.dynalink.beans.StaticClass;
import jdk2.internal.dynalink.linker.GuardedInvocation;
import jdk2.internal.dynalink.linker.GuardingDynamicLinker;
import jdk2.internal.dynalink.linker.LinkRequest;
import jdk2.internal.dynalink.support.CallSiteDescriptorFactory;
import jdk2.internal.dynalink.support.LinkRequestImpl;
import jdk2.nashorn.api.scripting.ScriptObjectMirror;
import jdk2.nashorn.internal.lookup.Lookup;
import jdk2.nashorn.internal.runtime.AccessorProperty;
import jdk2.nashorn.internal.runtime.ECMAException;
import jdk2.nashorn.internal.runtime.JSType;
import jdk2.nashorn.internal.runtime.Property;
import jdk2.nashorn.internal.runtime.PropertyMap;
import jdk2.nashorn.internal.runtime.ScriptFunction;
import jdk2.nashorn.internal.runtime.ScriptObject;
import jdk2.nashorn.internal.runtime.ScriptRuntime;
import jdk2.nashorn.internal.runtime.Specialization;
import jdk2.nashorn.internal.runtime.arrays.ArrayData;
import jdk2.nashorn.internal.runtime.linker.Bootstrap;
import jdk2.nashorn.internal.runtime.linker.InvokeByName;
import jdk2.nashorn.internal.runtime.linker.NashornBeansLinker;

/**
 * ECMA 15.2 Object objects
 *
 * JavaScript Object constructor/prototype. Note: instances of this class are
 * never created. This class is not even a subclass of ScriptObject. But, we use
 * this class to generate prototype and constructor for "Object".
 *
 */
public final class NativeObject {
    /** Methodhandle to proto getter */
    public static final MethodHandle GET__PROTO__ = findOwnMH("get__proto__", ScriptObject.class, Object.class);

    /** Methodhandle to proto setter */
    public static final MethodHandle SET__PROTO__ = findOwnMH("set__proto__", Object.class, Object.class, Object.class);

    private static final Object TO_STRING = new Object();

    private static final MethodType MIRROR_GETTER_TYPE = MethodType.methodType(Object.class, ScriptObjectMirror.class);
    private static final MethodType MIRROR_SETTER_TYPE = MethodType.methodType(Object.class, ScriptObjectMirror.class, Object.class);

    private static InvokeByName getTO_STRING() {
        return Global.instance().getInvokeByName(TO_STRING,
                new Callable<InvokeByName>() {
                    @Override
                    public InvokeByName call() {
                        return new InvokeByName("toString", ScriptObject.class);
                    }
                });
    }

    @SuppressWarnings("unused")
    private static ScriptObject get__proto__(final Object self) {
        // See ES6 draft spec: B.2.2.1.1 get Object.prototype.__proto__
        // Step 1 Let O be the result of calling ToObject passing the this.
        final ScriptObject sobj = Global.checkObject(Global.toObject(self));
        return sobj.getProto();
    }

    @SuppressWarnings("unused")
    private static Object set__proto__(final Object self, final Object proto) {
        // See ES6 draft spec: B.2.2.1.2 set Object.prototype.__proto__
        // Step 1
        Global.checkObjectCoercible(self);
        // Step 4
        if (! (self instanceof ScriptObject)) {
            return UNDEFINED;
        }

        final ScriptObject sobj = (ScriptObject)self;
        // __proto__ assignment ignores non-nulls and non-objects
        // step 3: If Type(proto) is neither Object nor Null, then return undefined.
        if (proto == null || proto instanceof ScriptObject) {
            sobj.setPrototypeOf(proto);
        }
        return UNDEFINED;
    }


    private NativeObject() {
        // don't create me!
        throw new UnsupportedOperationException();
    }

    private static ECMAException notAnObject(final Object obj) {
        return typeError("not.an.object", ScriptRuntime.safeToString(obj));
    }

    /**
     * Nashorn extension: setIndexedPropertiesToExternalArrayData
     *
     * @param self self reference
     * @param obj object whose index properties are backed by buffer
     * @param buf external buffer - should be a nio ByteBuffer
     * @return the 'obj' object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject setIndexedPropertiesToExternalArrayData(final Object self, final Object obj, final Object buf) {
        Global.checkObject(obj);
        final ScriptObject sobj = (ScriptObject)obj;
        if (buf instanceof ByteBuffer) {
            sobj.setArray(ArrayData.allocate((ByteBuffer)buf));
        } else {
            throw typeError("not.a.bytebuffer", "setIndexedPropertiesToExternalArrayData's buf argument");
        }
        return sobj;
    }


    /**
     * ECMA 15.2.3.2 Object.getPrototypeOf ( O )
     *
     * @param  self self reference
     * @param  obj object to get prototype from
     * @return the prototype of an object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object getPrototypeOf(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).getProto();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).getProto();
        } else {
            final JSType type = JSType.of(obj);
            if (type == JSType.OBJECT) {
                // host (Java) objects have null __proto__
                return null;
            }

            // must be some JS primitive
            throw notAnObject(obj);
        }
    }

    /**
     * Nashorn extension: Object.setPrototypeOf ( O, proto )
     * Also found in ES6 draft specification.
     *
     * @param  self self reference
     * @param  obj object to set prototype for
     * @param  proto prototype object to be used
     * @return object whose prototype is set
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object setPrototypeOf(final Object self, final Object obj, final Object proto) {
        if (obj instanceof ScriptObject) {
            ((ScriptObject)obj).setPrototypeOf(proto);
            return obj;
        } else if (obj instanceof ScriptObjectMirror) {
            ((ScriptObjectMirror)obj).setProto(proto);
            return obj;
        }

        throw notAnObject(obj);
    }

    /**
     * ECMA 15.2.3.3 Object.getOwnPropertyDescriptor ( O, P )
     *
     * @param self  self reference
     * @param obj   object from which to get property descriptor for {@code ToString(prop)}
     * @param prop  property descriptor
     * @return property descriptor
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object getOwnPropertyDescriptor(final Object self, final Object obj, final Object prop) {
        if (obj instanceof ScriptObject) {
            final String       key  = JSType.toString(prop);
            final ScriptObject sobj = (ScriptObject)obj;

            return sobj.getOwnPropertyDescriptor(key);
        } else if (obj instanceof ScriptObjectMirror) {
            final String       key  = JSType.toString(prop);
            final ScriptObjectMirror sobjMirror = (ScriptObjectMirror)obj;

            return sobjMirror.getOwnPropertyDescriptor(key);
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.4 Object.getOwnPropertyNames ( O )
     *
     * @param self self reference
     * @param obj  object to query for property names
     * @return array of property names
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject getOwnPropertyNames(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return new NativeArray(((ScriptObject)obj).getOwnKeys(true));
        } else if (obj instanceof ScriptObjectMirror) {
            return new NativeArray(((ScriptObjectMirror)obj).getOwnKeys(true));
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.5 Object.create ( O [, Properties] )
     *
     * @param self  self reference
     * @param proto prototype object
     * @param props properties to define
     * @return object created
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject create(final Object self, final Object proto, final Object props) {
        if (proto != null) {
            Global.checkObject(proto);
        }

        // FIXME: should we create a proper object with correct number of
        // properties?
        final ScriptObject newObj = Global.newEmptyInstance();
        newObj.setProto((ScriptObject)proto);
        if (props != UNDEFINED) {
            NativeObject.defineProperties(self, newObj, props);
        }

        return newObj;
    }

    /**
     * ECMA 15.2.3.6 Object.defineProperty ( O, P, Attributes )
     *
     * @param self self reference
     * @param obj  object in which to define a property
     * @param prop property to define
     * @param attr attributes for property descriptor
     * @return object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject defineProperty(final Object self, final Object obj, final Object prop, final Object attr) {
        final ScriptObject sobj = Global.checkObject(obj);
        sobj.defineOwnProperty(JSType.toString(prop), attr, true);
        return sobj;
    }

    /**
     * ECMA 5.2.3.7 Object.defineProperties ( O, Properties )
     *
     * @param self  self reference
     * @param obj   object in which to define properties
     * @param props properties
     * @return object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject defineProperties(final Object self, final Object obj, final Object props) {
        final ScriptObject sobj     = Global.checkObject(obj);
        final Object       propsObj = Global.toObject(props);

        if (propsObj instanceof ScriptObject) {
            final Object[] keys = ((ScriptObject)propsObj).getOwnKeys(false);
            for (final Object key : keys) {
                final String prop = JSType.toString(key);
                sobj.defineOwnProperty(prop, ((ScriptObject)propsObj).get(prop), true);
            }
        }
        return sobj;
    }

    /**
     * ECMA 15.2.3.8 Object.seal ( O )
     *
     * @param self self reference
     * @param obj  object to seal
     * @return sealed object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object seal(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).seal();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).seal();
        } else {
            throw notAnObject(obj);
        }
    }


    /**
     * ECMA 15.2.3.9 Object.freeze ( O )
     *
     * @param self self reference
     * @param obj object to freeze
     * @return frozen object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object freeze(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).freeze();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).freeze();
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.10 Object.preventExtensions ( O )
     *
     * @param self self reference
     * @param obj  object, for which to set the internal extensible property to false
     * @return object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object preventExtensions(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).preventExtensions();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).preventExtensions();
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.11 Object.isSealed ( O )
     *
     * @param self self reference
     * @param obj check whether an object is sealed
     * @return true if sealed, false otherwise
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static boolean isSealed(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).isSealed();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).isSealed();
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.12 Object.isFrozen ( O )
     *
     * @param self self reference
     * @param obj check whether an object
     * @return true if object is frozen, false otherwise
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static boolean isFrozen(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).isFrozen();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).isFrozen();
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.13 Object.isExtensible ( O )
     *
     * @param self self reference
     * @param obj check whether an object is extensible
     * @return true if object is extensible, false otherwise
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static boolean isExtensible(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            return ((ScriptObject)obj).isExtensible();
        } else if (obj instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror)obj).isExtensible();
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.3.14 Object.keys ( O )
     *
     * @param self self reference
     * @param obj  object from which to extract keys
     * @return array of keys in object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static ScriptObject keys(final Object self, final Object obj) {
        if (obj instanceof ScriptObject) {
            final ScriptObject sobj = (ScriptObject)obj;
            return new NativeArray(sobj.getOwnKeys(false));
        } else if (obj instanceof ScriptObjectMirror) {
            final ScriptObjectMirror sobjMirror = (ScriptObjectMirror)obj;
            return new NativeArray(sobjMirror.getOwnKeys(false));
        } else {
            throw notAnObject(obj);
        }
    }

    /**
     * ECMA 15.2.2.1 , 15.2.1.1 new Object([value]) and Object([value])
     *
     * Constructor
     *
     * @param newObj is the new object instantiated with the new operator
     * @param self   self reference
     * @param value  value of object to be instantiated
     * @return the new NativeObject
     */
    //@Constructor
    public static Object construct(final boolean newObj, final Object self, final Object value) {
        final JSType type = JSType.ofNoFunction(value);

        // Object(null), Object(undefined), Object() are same as "new Object()"

        if (newObj || type == JSType.NULL || type == JSType.UNDEFINED) {
            switch (type) {
            case BOOLEAN:
            case NUMBER:
            case STRING:
                return Global.toObject(value);
            case OBJECT:
                return value;
            case NULL:
            case UNDEFINED:
                // fall through..
            default:
                break;
            }

            return Global.newEmptyInstance();
        }

        return Global.toObject(value);
    }

    /**
     * ECMA 15.2.4.2 Object.prototype.toString ( )
     *
     * @param self self reference
     * @return ToString of object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static String toString(final Object self) {
        return ScriptRuntime.builtinObjectToString(self);
    }

    /**
     * ECMA 15.2.4.3 Object.prototype.toLocaleString ( )
     *
     * @param self self reference
     * @return localized ToString
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static Object toLocaleString(final Object self) {
        final Object obj = JSType.toScriptObject(self);
        if (obj instanceof ScriptObject) {
            final InvokeByName toStringInvoker = getTO_STRING();
            final ScriptObject sobj = (ScriptObject)self;
            try {
                final Object toString = toStringInvoker.getGetter().invokeExact(sobj);

                if (Bootstrap.isCallable(toString)) {
                    return toStringInvoker.getInvoker().invokeExact(toString, sobj);
                }
            } catch (final RuntimeException | Error e) {
                throw e;
            } catch (final Throwable t) {
                throw new RuntimeException(t);
            }

            throw typeError("not.a.function", "toString");
        }

        return ScriptRuntime.builtinObjectToString(self);
    }

    /**
     * ECMA 15.2.4.4 Object.prototype.valueOf ( )
     *
     * @param self self reference
     * @return value of object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static Object valueOf(final Object self) {
        return Global.toObject(self);
    }

    /**
     * ECMA 15.2.4.5 Object.prototype.hasOwnProperty (V)
     *
     * @param self self reference
     * @param v property to check for
     * @return true if property exists in object
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static boolean hasOwnProperty(final Object self, final Object v) {
        // Convert ScriptObjects to primitive with String.class hint
        // but no need to convert other primitives to string.
        final Object key = JSType.toPrimitive(v, String.class);
        final Object obj = Global.toObject(self);

        return obj instanceof ScriptObject && ((ScriptObject)obj).hasOwnProperty(key);
    }

    /**
     * ECMA 15.2.4.6 Object.prototype.isPrototypeOf (V)
     *
     * @param self self reference
     * @param v v prototype object to check against
     * @return true if object is prototype of v
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static boolean isPrototypeOf(final Object self, final Object v) {
        if (!(v instanceof ScriptObject)) {
            return false;
        }

        final Object obj   = Global.toObject(self);
        ScriptObject proto = (ScriptObject)v;

        do {
            proto = proto.getProto();
            if (proto == obj) {
                return true;
            }
        } while (proto != null);

        return false;
    }

    /**
     * ECMA 15.2.4.7 Object.prototype.propertyIsEnumerable (V)
     *
     * @param self self reference
     * @param v property to check if enumerable
     * @return true if property is enumerable
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE)
    public static boolean propertyIsEnumerable(final Object self, final Object v) {
        final String str = JSType.toString(v);
        final Object obj = Global.toObject(self);

        if (obj instanceof ScriptObject) {
            final jdk2.nashorn.internal.runtime.Property property = ((ScriptObject)obj).getMap().findProperty(str);
            return property != null && property.isEnumerable();
        }

        return false;
    }

    /**
     * Nashorn extension: Object.bindProperties
     *
     * Binds the source object's properties to the target object. Binding
     * properties allows two-way read/write for the properties of the source object.
     *
     * Example:
     * <pre>
     * var obj = { x: 34, y: 100 };
     * var foo = {}
     *
     * // bind properties of "obj" to "foo" object
     * Object.bindProperties(foo, obj);
     *
     * // now, we can access/write on 'foo' properties
     * print(foo.x); // prints obj.x which is 34
     *
     * // update obj.x via foo.x
     * foo.x = "hello";
     * print(obj.x); // prints "hello" now
     *
     * obj.x = 42;   // foo.x also becomes 42
     * print(foo.x); // prints 42
     * </pre>
     * <p>
     * The source object bound can be a ScriptObject or a ScriptOjectMirror.
     * null or undefined source object results in TypeError being thrown.
     * </p>
     * Example:
     * <pre>
     * var obj = loadWithNewGlobal({
     *    name: "test",
     *    script: "obj = { x: 33, y: 'hello' }"
     * });
     *
     * // bind 'obj's properties to global scope 'this'
     * Object.bindProperties(this, obj);
     * print(x);         // prints 33
     * print(y);         // prints "hello"
     * x = Math.PI;      // changes obj.x to Math.PI
     * print(obj.x);     // prints Math.PI
     * </pre>
     *
     * Limitations of property binding:
     * <ul>
     * <li> Only enumerable, immediate (not proto inherited) properties of the source object are bound.
     * <li> If the target object already contains a property called "foo", the source's "foo" is skipped (not bound).
     * <li> Properties added to the source object after binding to the target are not bound.
     * <li> Property configuration changes on the source object (or on the target) is not propagated.
     * <li> Delete of property on the target (or the source) is not propagated -
     * only the property value is set to 'undefined' if the property happens to be a data property.
     * </ul>
     * <p>
     * It is recommended that the bound properties be treated as non-configurable
     * properties to avoid surprises.
     * </p>
     *
     * @param self self reference
     * @param target the target object to which the source object's properties are bound
     * @param source the source object whose properties are bound to the target
     * @return the target object after property binding
     */
    //@Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object bindProperties(final Object self, final Object target, final Object source) {
        // target object has to be a ScriptObject
        final ScriptObject targetObj = Global.checkObject(target);
        // check null or undefined source object
        Global.checkObjectCoercible(source);

        if (source instanceof ScriptObject) {
            final ScriptObject sourceObj  = (ScriptObject)source;

            final PropertyMap  sourceMap  = sourceObj.getMap();
            final Property[]   properties = sourceMap.getProperties();
            //replace the map and blow up everything to objects to work with dual fields :-(

            // filter non-enumerable properties
            final ArrayList<Property> propList = new ArrayList<>();
            for (final Property prop : properties) {
                if (prop.isEnumerable()) {
                    final Object value = sourceObj.get(prop.getKey());
                    prop.setType(Object.class);
                    prop.setValue(sourceObj, sourceObj, value, false);
                    propList.add(prop);
                }
            }

            if (!propList.isEmpty()) {
                targetObj.addBoundProperties(sourceObj, propList.toArray(new Property[propList.size()]));
            }
        } else if (source instanceof ScriptObjectMirror) {
            // get enumerable, immediate properties of mirror
            final ScriptObjectMirror mirror = (ScriptObjectMirror)source;
            final String[] keys = mirror.getOwnKeys(false);
            if (keys.length == 0) {
                // nothing to bind
                return target;
            }

            // make accessor properties using dynamic invoker getters and setters
            final AccessorProperty[] props = new AccessorProperty[keys.length];
            for (int idx = 0; idx < keys.length; idx++) {
                final String name = keys[idx];
                final MethodHandle getter = Bootstrap.createDynamicInvoker("dyn:getMethod|getProp|getElem:" + name, MIRROR_GETTER_TYPE);
                final MethodHandle setter = Bootstrap.createDynamicInvoker("dyn:setProp|setElem:" + name, MIRROR_SETTER_TYPE);
                props[idx] = AccessorProperty.create(name, 0, getter, setter);
            }

            targetObj.addBoundProperties(source, props);
        } else if (source instanceof StaticClass) {
            final Class<?> clazz = ((StaticClass)source).getRepresentedClass();
            Bootstrap.checkReflectionAccess(clazz, true);
            bindBeanProperties(targetObj, source, BeansLinker.getReadableStaticPropertyNames(clazz),
                    BeansLinker.getWritableStaticPropertyNames(clazz), BeansLinker.getStaticMethodNames(clazz));
        } else {
            final Class<?> clazz = source.getClass();
            Bootstrap.checkReflectionAccess(clazz, false);
            bindBeanProperties(targetObj, source, BeansLinker.getReadableInstancePropertyNames(clazz),
                    BeansLinker.getWritableInstancePropertyNames(clazz), BeansLinker.getInstanceMethodNames(clazz));
        }

        return target;
    }

    /**
     * Binds the source mirror object's properties to the target object. Binding
     * properties allows two-way read/write for the properties of the source object.
     * All inherited, enumerable properties are also bound. This method is used to
     * to make 'with' statement work with ScriptObjectMirror as scope object.
     *
     * @param target the target object to which the source object's properties are bound
     * @param source the source object whose properties are bound to the target
     * @return the target object after property binding
     */
    public static Object bindAllProperties(final ScriptObject target, final ScriptObjectMirror source) {
        final Set<String> keys = source.keySet();
        // make accessor properties using dynamic invoker getters and setters
        final AccessorProperty[] props = new AccessorProperty[keys.size()];
        int idx = 0;
        for (final String name : keys) {
            final MethodHandle getter = Bootstrap.createDynamicInvoker("dyn:getMethod|getProp|getElem:" + name, MIRROR_GETTER_TYPE);
            final MethodHandle setter = Bootstrap.createDynamicInvoker("dyn:setProp|setElem:" + name, MIRROR_SETTER_TYPE);
            props[idx] = AccessorProperty.create(name, 0, getter, setter);
            idx++;
        }

        target.addBoundProperties(source, props);
        return target;
    }

    private static void bindBeanProperties(final ScriptObject targetObj, final Object source,
            final Collection<String> readablePropertyNames, final Collection<String> writablePropertyNames,
            final Collection<String> methodNames) {
        final Set<String> propertyNames = new HashSet<>(readablePropertyNames);
        propertyNames.addAll(writablePropertyNames);

        final Class<?> clazz = source.getClass();

        final MethodType getterType = MethodType.methodType(Object.class, clazz);
        final MethodType setterType = MethodType.methodType(Object.class, clazz, Object.class);

        final GuardingDynamicLinker linker = BeansLinker.getLinkerForClass(clazz);

        final List<AccessorProperty> properties = new ArrayList<>(propertyNames.size() + methodNames.size());
        for(final String methodName: methodNames) {
            final MethodHandle method;
            try {
                method = getBeanOperation(linker, "dyn:getMethod:" + methodName, getterType, source);
            } catch(final IllegalAccessError e) {
                // Presumably, this was a caller sensitive method. Ignore it and carry on.
                continue;
            }
            properties.add(AccessorProperty.create(methodName, Property.NOT_WRITABLE, getBoundBeanMethodGetter(source,
                    method), null));
        }
        for(final String propertyName: propertyNames) {
            MethodHandle getter;
            if(readablePropertyNames.contains(propertyName)) {
                try {
                    getter = getBeanOperation(linker, "dyn:getProp:" + propertyName, getterType, source);
                } catch(final IllegalAccessError e) {
                    // Presumably, this was a caller sensitive method. Ignore it and carry on.
                    getter = Lookup.EMPTY_GETTER;
                }
            } else {
                getter = Lookup.EMPTY_GETTER;
            }
            final boolean isWritable = writablePropertyNames.contains(propertyName);
            MethodHandle setter;
            if(isWritable) {
                try {
                    setter = getBeanOperation(linker, "dyn:setProp:" + propertyName, setterType, source);
                } catch(final IllegalAccessError e) {
                    // Presumably, this was a caller sensitive method. Ignore it and carry on.
                    setter = Lookup.EMPTY_SETTER;
                }
            } else {
                setter = Lookup.EMPTY_SETTER;
            }
            if(getter != Lookup.EMPTY_GETTER || setter != Lookup.EMPTY_SETTER) {
                properties.add(AccessorProperty.create(propertyName, isWritable ? 0 : Property.NOT_WRITABLE, getter, setter));
            }
        }

        targetObj.addBoundProperties(source, properties.toArray(new AccessorProperty[properties.size()]));
    }

    private static MethodHandle getBoundBeanMethodGetter(final Object source, final MethodHandle methodGetter) {
        try {
            // NOTE: we're relying on the fact that "dyn:getMethod:..." return value is constant for any given method
            // name and object linked with BeansLinker. (Actually, an even stronger assumption is true: return value is
            // constant for any given method name and object's class.)
            return MethodHandles.dropArguments(MethodHandles.constant(Object.class,
                    Bootstrap.bindCallable(methodGetter.invoke(source), source, null)), 0, Object.class);
        } catch(RuntimeException|Error e) {
            throw e;
        } catch(final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static MethodHandle getBeanOperation(final GuardingDynamicLinker linker, final String operation,
            final MethodType methodType, final Object source) {
        final GuardedInvocation inv;
        try {
            inv = NashornBeansLinker.getGuardedInvocation(linker, createLinkRequest(operation, methodType, source), Bootstrap.getLinkerServices());
            assert passesGuard(source, inv.getGuard());
        } catch(RuntimeException|Error e) {
            throw e;
        } catch(final Throwable t) {
            throw new RuntimeException(t);
        }
        assert inv.getSwitchPoints() == null; // Linkers in Dynalink's beans package don't use switchpoints.
        // We discard the guard, as all method handles will be bound to a specific object.
        return inv.getInvocation();
    }

    private static boolean passesGuard(final Object obj, final MethodHandle guard) throws Throwable {
        return guard == null || (boolean)guard.invoke(obj);
    }

    private static LinkRequest createLinkRequest(final String operation, final MethodType methodType, final Object source) {
        return new LinkRequestImpl(CallSiteDescriptorFactory.create(MethodHandles.publicLookup(), operation,
                methodType), null, 0, false, source);
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(MethodHandles.lookup(), NativeObject.class, name, MH.type(rtype, types));
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(NativeObject.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction toString = ScriptFunctionImpl.makeFunction("toString", 
                staticHandle("toString", String.class, Object.class));
        private ScriptFunction toLocaleString = ScriptFunctionImpl.makeFunction("toLocaleString", 
                staticHandle("toLocaleString", Object.class, Object.class));
        private ScriptFunction valueOf = ScriptFunctionImpl.makeFunction("valueOf", 
                staticHandle("valueOf", Object.class, Object.class));
        private ScriptFunction hasOwnProperty = ScriptFunctionImpl.makeFunction("hasOwnProperty", 
                staticHandle("hasOwnProperty", boolean.class, Object.class, Object.class));
        private ScriptFunction isPrototypeOf = ScriptFunctionImpl.makeFunction("isPrototypeOf", 
                staticHandle("isPrototypeOf", boolean.class, Object.class, Object.class));
        private ScriptFunction propertyIsEnumerable = ScriptFunctionImpl.makeFunction("propertyIsEnumerable", 
                staticHandle("propertyIsEnumerable", boolean.class, Object.class, Object.class));

        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$toString() {
           return this.toString;
        }

        public void S$toString(final ScriptFunction var1) {
           this.toString = var1;
        }

        public ScriptFunction G$toLocaleString() {
           return this.toLocaleString;
        }

        public void S$toLocaleString(final ScriptFunction var1) {
           this.toLocaleString = var1;
        }

        public ScriptFunction G$valueOf() {
           return this.valueOf;
        }

        public void S$valueOf(final ScriptFunction var1) {
           this.valueOf = var1;
        }

        public ScriptFunction G$hasOwnProperty() {
           return this.hasOwnProperty;
        }

        public void S$hasOwnProperty(final ScriptFunction var1) {
           this.hasOwnProperty = var1;
        }

        public ScriptFunction G$isPrototypeOf() {
           return this.isPrototypeOf;
        }

        public void S$isPrototypeOf(final ScriptFunction var1) {
           this.isPrototypeOf = var1;
        }

        public ScriptFunction G$propertyIsEnumerable() {
           return this.propertyIsEnumerable;
        }

        public void S$propertyIsEnumerable(final ScriptFunction var1) {
           this.propertyIsEnumerable = var1;
        }

        static {
           ArrayList<Property> list = new ArrayList<>(7);
           list.add(AccessorProperty.create("toString", Property.NOT_ENUMERABLE,
                   virtualHandle("G$toString", ScriptFunction.class),
                   virtualHandle("S$toString", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("toLocaleString", Property.NOT_ENUMERABLE,
                   virtualHandle("G$toLocaleString", ScriptFunction.class),
                   virtualHandle("S$toLocaleString", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("valueOf", Property.NOT_ENUMERABLE,
                   virtualHandle("G$valueOf", ScriptFunction.class),
                   virtualHandle("S$valueOf", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("hasOwnProperty", Property.NOT_ENUMERABLE,
                   virtualHandle("G$hasOwnProperty", ScriptFunction.class),
                   virtualHandle("S$hasOwnProperty", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("isPrototypeOf", Property.NOT_ENUMERABLE,
                   virtualHandle("G$isPrototypeOf", ScriptFunction.class),
                   virtualHandle("S$isPrototypeOf", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("propertyIsEnumerable", Property.NOT_ENUMERABLE,
                   virtualHandle("G$propertyIsEnumerable", ScriptFunction.class),
                   virtualHandle("S$propertyIsEnumerable", void.class, ScriptFunction.class)));
           $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
           super($nasgenmap$);
        }

        public String getClassName() {
           return "Object";
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

    static final class Constructor extends ScriptFunctionImpl {
        private ScriptFunction setIndexedPropertiesToExternalArrayData = ScriptFunctionImpl.makeFunction("setIndexedPropertiesToExternalArrayData", 
                staticHandle("setIndexedPropertiesToExternalArrayData", ScriptObject.class, Object.class, Object.class, Object.class));
        private ScriptFunction getPrototypeOf = ScriptFunctionImpl.makeFunction("getPrototypeOf", 
                staticHandle("getPrototypeOf", Object.class, Object.class, Object.class));
        private ScriptFunction setPrototypeOf = ScriptFunctionImpl.makeFunction("setPrototypeOf", 
                staticHandle("setPrototypeOf", Object.class, Object.class, Object.class, Object.class));
        private ScriptFunction getOwnPropertyDescriptor = ScriptFunctionImpl.makeFunction("getOwnPropertyDescriptor", 
                staticHandle("getOwnPropertyDescriptor", Object.class, Object.class, Object.class, Object.class));
        private ScriptFunction getOwnPropertyNames = ScriptFunctionImpl.makeFunction("getOwnPropertyNames", 
                staticHandle("getOwnPropertyNames", ScriptObject.class, Object.class, Object.class));
        private ScriptFunction create = ScriptFunctionImpl.makeFunction("create", 
                staticHandle("create", ScriptObject.class, Object.class, Object.class, Object.class));
        private ScriptFunction defineProperty = ScriptFunctionImpl.makeFunction("defineProperty", 
                staticHandle("defineProperty", ScriptObject.class, Object.class, Object.class, Object.class, Object.class));
        private ScriptFunction defineProperties = ScriptFunctionImpl.makeFunction("defineProperties", 
                staticHandle("defineProperties", ScriptObject.class, Object.class, Object.class, Object.class));
        private ScriptFunction seal = ScriptFunctionImpl.makeFunction("seal", 
                staticHandle("seal", Object.class, Object.class, Object.class));
        private ScriptFunction freeze = ScriptFunctionImpl.makeFunction("freeze", 
                staticHandle("freeze", Object.class, Object.class, Object.class));
        private ScriptFunction preventExtensions = ScriptFunctionImpl.makeFunction("preventExtensions", 
                staticHandle("preventExtensions", Object.class, Object.class, Object.class));
        private ScriptFunction isSealed = ScriptFunctionImpl.makeFunction("isSealed", 
                staticHandle("isSealed", boolean.class, Object.class, Object.class));
        private ScriptFunction isFrozen = ScriptFunctionImpl.makeFunction("isFrozen", 
                staticHandle("isFrozen", boolean.class, Object.class, Object.class));
        private ScriptFunction isExtensible = ScriptFunctionImpl.makeFunction("isExtensible", 
                staticHandle("isExtensible", boolean.class, Object.class, Object.class));
        private ScriptFunction keys = ScriptFunctionImpl.makeFunction("keys", 
                staticHandle("keys", ScriptObject.class, Object.class, Object.class));
        private ScriptFunction bindProperties = ScriptFunctionImpl.makeFunction("bindProperties", 
                staticHandle("bindProperties", Object.class, Object.class, Object.class, Object.class));
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$setIndexedPropertiesToExternalArrayData() {
           return this.setIndexedPropertiesToExternalArrayData;
        }

        public void S$setIndexedPropertiesToExternalArrayData(final ScriptFunction var1) {
           this.setIndexedPropertiesToExternalArrayData = var1;
        }

        public ScriptFunction G$getPrototypeOf() {
           return this.getPrototypeOf;
        }

        public void S$getPrototypeOf(final ScriptFunction var1) {
           this.getPrototypeOf = var1;
        }

        public ScriptFunction G$setPrototypeOf() {
           return this.setPrototypeOf;
        }

        public void S$setPrototypeOf(final ScriptFunction var1) {
           this.setPrototypeOf = var1;
        }

        public ScriptFunction G$getOwnPropertyDescriptor() {
           return this.getOwnPropertyDescriptor;
        }

        public void S$getOwnPropertyDescriptor(final ScriptFunction var1) {
           this.getOwnPropertyDescriptor = var1;
        }

        public ScriptFunction G$getOwnPropertyNames() {
           return this.getOwnPropertyNames;
        }

        public void S$getOwnPropertyNames(final ScriptFunction var1) {
           this.getOwnPropertyNames = var1;
        }

        public ScriptFunction G$create() {
           return this.create;
        }

        public void S$create(final ScriptFunction var1) {
           this.create = var1;
        }

        public ScriptFunction G$defineProperty() {
           return this.defineProperty;
        }

        public void S$defineProperty(final ScriptFunction var1) {
           this.defineProperty = var1;
        }

        public ScriptFunction G$defineProperties() {
           return this.defineProperties;
        }

        public void S$defineProperties(final ScriptFunction var1) {
           this.defineProperties = var1;
        }

        public ScriptFunction G$seal() {
           return this.seal;
        }

        public void S$seal(final ScriptFunction var1) {
           this.seal = var1;
        }

        public ScriptFunction G$freeze() {
           return this.freeze;
        }

        public void S$freeze(final ScriptFunction var1) {
           this.freeze = var1;
        }

        public ScriptFunction G$preventExtensions() {
           return this.preventExtensions;
        }

        public void S$preventExtensions(final ScriptFunction var1) {
           this.preventExtensions = var1;
        }

        public ScriptFunction G$isSealed() {
           return this.isSealed;
        }

        public void S$isSealed(final ScriptFunction var1) {
           this.isSealed = var1;
        }

        public ScriptFunction G$isFrozen() {
           return this.isFrozen;
        }

        public void S$isFrozen(final ScriptFunction var1) {
           this.isFrozen = var1;
        }

        public ScriptFunction G$isExtensible() {
           return this.isExtensible;
        }

        public void S$isExtensible(final ScriptFunction var1) {
           this.isExtensible = var1;
        }

        public ScriptFunction G$keys() {
           return this.keys;
        }

        public void S$keys(final ScriptFunction var1) {
           this.keys = var1;
        }

        public ScriptFunction G$bindProperties() {
           return this.bindProperties;
        }

        public void S$bindProperties(final ScriptFunction var1) {
           this.bindProperties = var1;
        }

        static {
           ArrayList<Property> list = new ArrayList<>(17);
           list.add(AccessorProperty.create("setIndexedPropertiesToExternalArrayData", Property.NOT_ENUMERABLE,
                   virtualHandle("G$setIndexedPropertiesToExternalArrayData", ScriptFunction.class),
                   virtualHandle("S$setIndexedPropertiesToExternalArrayData", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("getPrototypeOf", Property.NOT_ENUMERABLE,
                   virtualHandle("G$getPrototypeOf", ScriptFunction.class),
                   virtualHandle("S$getPrototypeOf", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("setPrototypeOf", Property.NOT_ENUMERABLE,
                   virtualHandle("G$setPrototypeOf", ScriptFunction.class),
                   virtualHandle("S$setPrototypeOf", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("getOwnPropertyDescriptor", Property.NOT_ENUMERABLE,
                   virtualHandle("G$getOwnPropertyDescriptor", ScriptFunction.class),
                   virtualHandle("S$getOwnPropertyDescriptor", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("getOwnPropertyNames", Property.NOT_ENUMERABLE,
                   virtualHandle("G$getOwnPropertyNames", ScriptFunction.class),
                   virtualHandle("S$getOwnPropertyNames", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("create", Property.NOT_ENUMERABLE,
                   virtualHandle("G$create", ScriptFunction.class),
                   virtualHandle("S$create", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("defineProperty", Property.NOT_ENUMERABLE,
                   virtualHandle("G$defineProperty", ScriptFunction.class),
                   virtualHandle("S$defineProperty", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("defineProperties", Property.NOT_ENUMERABLE,
                   virtualHandle("G$defineProperties", ScriptFunction.class),
                   virtualHandle("S$defineProperties", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("seal", Property.NOT_ENUMERABLE,
                   virtualHandle("G$seal", ScriptFunction.class),
                   virtualHandle("S$seal", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("freeze", Property.NOT_ENUMERABLE,
                   virtualHandle("G$freeze", ScriptFunction.class),
                   virtualHandle("S$freeze", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("preventExtensions", Property.NOT_ENUMERABLE,
                   virtualHandle("G$preventExtensions", ScriptFunction.class),
                   virtualHandle("S$preventExtensions", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("isSealed", Property.NOT_ENUMERABLE,
                   virtualHandle("G$isSealed", ScriptFunction.class),
                   virtualHandle("S$isSealed", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("isFrozen", Property.NOT_ENUMERABLE,
                   virtualHandle("G$isFrozen", ScriptFunction.class),
                   virtualHandle("S$isFrozen", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("isExtensible", Property.NOT_ENUMERABLE,
                   virtualHandle("G$isExtensible", ScriptFunction.class),
                   virtualHandle("S$isExtensible", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("keys", Property.NOT_ENUMERABLE,
                   virtualHandle("G$keys", ScriptFunction.class),
                   virtualHandle("S$keys", void.class, ScriptFunction.class)));
           list.add(AccessorProperty.create("bindProperties", Property.NOT_ENUMERABLE,
                   virtualHandle("G$bindProperties", ScriptFunction.class),
                   virtualHandle("S$bindProperties", void.class, ScriptFunction.class)));
           $nasgenmap$ = PropertyMap.newMap(list);
        }

        Constructor() {
           super("Object",
                   staticHandle("construct", Object.class, boolean.class, Object.class, Object.class),
                   $nasgenmap$, null);
           Prototype var10001 = new Prototype();
           PrototypeObject.setConstructor(var10001, this);
           setPrototype(var10001);
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
}
