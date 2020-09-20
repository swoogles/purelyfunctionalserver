(function(){
'use strict';
const $linkingInfo = Object.freeze({
  "assumingES6": true,
  "productionMode": false,
  "linkerVersion": "1.2.0",
  "fileLevelThis": this
});
const $imul = Math.imul;
const $fround = Math.fround;
const $clz32 = Math.clz32;
let $L0;
function $propertyName(obj) {
  for (const prop in obj) {
    return prop
  }
}
class $Char {
  constructor(c) {
    this.c = c
  };
  toString() {
    return String.fromCharCode(this.c)
  };
}
function $throwClassCastException(instance, classFullName) {
  throw new $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError(new $c_jl_ClassCastException(((instance + " is not an instance of ") + classFullName)))
}
function $throwArrayCastException(instance, classArrayEncodedName, depth) {
  while ((--depth)) {
    classArrayEncodedName = ("[" + classArrayEncodedName)
  };
  $throwClassCastException(instance, classArrayEncodedName)
}
function $throwArrayIndexOutOfBoundsException(i) {
  throw new $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError(new $c_jl_ArrayIndexOutOfBoundsException(((i === null) ? null : ("" + i))))
}
function $noIsInstance(instance) {
  throw new TypeError("Cannot call isInstance() on a Class representing a JS trait/object")
}
function $makeNativeArrayWrapper(arrayClassData, nativeArray) {
  return new arrayClassData.constr(nativeArray)
}
function $newArrayObject(arrayClassData, lengths) {
  return $newArrayObjectInternal(arrayClassData, lengths, 0)
}
function $newArrayObjectInternal(arrayClassData, lengths, lengthIndex) {
  const result = new arrayClassData.constr(lengths[lengthIndex]);
  if ((lengthIndex < (lengths.length - 1))) {
    const subArrayClassData = arrayClassData.componentData;
    const subLengthIndex = (lengthIndex + 1);
    const underlying = result.u;
    for (let i = 0; (i < underlying.length); (i++)) {
      underlying[i] = $newArrayObjectInternal(subArrayClassData, lengths, subLengthIndex)
    }
  };
  return result
}
function $objectClassName(instance) {
  switch ((typeof instance)) {
    case "string": {
      return "java.lang.String"
    }
    case "number": {
      if ($isInt(instance)) {
        if ((((instance << 24) >> 24) === instance)) {
          return "java.lang.Byte"
        } else if ((((instance << 16) >> 16) === instance)) {
          return "java.lang.Short"
        } else {
          return "java.lang.Integer"
        }
      } else {
        return "java.lang.Float"
      }
    }
    case "boolean": {
      return "java.lang.Boolean"
    }
    case "undefined": {
      return "java.lang.Void"
    }
    default: {
      if ((instance === null)) {
        return instance.getClass__jl_Class()
      } else if ((instance instanceof $c_RTLong)) {
        return "java.lang.Long"
      } else if ((instance instanceof $Char)) {
        return "java.lang.Character"
      } else if ((!(!(instance && instance.$classData)))) {
        return instance.$classData.name
      } else {
        return null.getName__T()
      }
    }
  }
}
function $dp_toString__T(instance) {
  return ((instance === (void 0)) ? "undefined" : instance.toString())
}
function $dp_getClass__jl_Class(instance) {
  return instance.getClass__jl_Class()
}
function $dp_clone__O(instance) {
  return instance.clone__O()
}
function $dp_notify__V(instance) {
  return instance.notify__V()
}
function $dp_notifyAll__V(instance) {
  return instance.notifyAll__V()
}
function $dp_finalize__V(instance) {
  return instance.finalize__V()
}
function $dp_equals__O__Z(instance, x0) {
  return instance.equals__O__Z(x0)
}
function $dp_hashCode__I(instance) {
  switch ((typeof instance)) {
    case "string": {
      return $f_T__hashCode__I(instance)
    }
    case "number": {
      return $f_jl_Double__hashCode__I(instance)
    }
    case "boolean": {
      return $f_jl_Boolean__hashCode__I(instance)
    }
    case "undefined": {
      return $f_jl_Void__hashCode__I(instance)
    }
    default: {
      if (((!(!(instance && instance.$classData))) || (instance === null))) {
        return instance.hashCode__I()
      } else if ((instance instanceof $Char)) {
        return $f_jl_Character__hashCode__I(instance)
      } else {
        return $c_O.prototype.hashCode__I.call(instance)
      }
    }
  }
}
function $dp_compareTo__O__I(instance, x0) {
  return instance.compareTo__O__I(x0)
}
function $dp_length__I(instance) {
  return instance.length__I()
}
function $dp_charAt__I__C(instance, x0) {
  return instance.charAt__I__C(x0)
}
function $dp_subSequence__I__I__jl_CharSequence(instance, x0, x1) {
  return instance.subSequence__I__I__jl_CharSequence(x0, x1)
}
function $dp_byteValue__B(instance) {
  return instance.byteValue__B()
}
function $dp_shortValue__S(instance) {
  return instance.shortValue__S()
}
function $dp_intValue__I(instance) {
  return instance.intValue__I()
}
function $dp_longValue__J(instance) {
  return instance.longValue__J()
}
function $dp_floatValue__F(instance) {
  return instance.floatValue__F()
}
function $dp_doubleValue__D(instance) {
  return instance.doubleValue__D()
}
function $intDiv(x, y) {
  if ((y === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  } else {
    return ((x / y) | 0)
  }
}
function $intMod(x, y) {
  if ((y === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  } else {
    return ((x % y) | 0)
  }
}
function $doubleToInt(x) {
  return ((x > 2147483647) ? 2147483647 : ((x < (-2147483648)) ? (-2147483648) : (x | 0)))
}
function $newJSObjectWithVarargs(ctor, args) {
  const instance = Object.create(ctor.prototype);
  const result = ctor.apply(instance, args);
  switch ((typeof result)) {
    case "string":
    case "number":
    case "boolean":
    case "undefined":
    case "symbol": {
      return instance
    }
    default: {
      return ((result === null) ? instance : result)
    }
  }
}
function $resolveSuperRef(superClass, propName) {
  const getPrototypeOf = Object.getPrototyeOf;
  const getOwnPropertyDescriptor = Object.getOwnPropertyDescriptor;
  let superProto = superClass.prototype;
  while ((superProto !== null)) {
    const desc = getOwnPropertyDescriptor(superProto, propName);
    if ((desc !== (void 0))) {
      return desc
    };
    superProto = getPrototypeOf(superProto)
  }
}
function $superGet(superClass, self, propName) {
  const desc = $resolveSuperRef(superClass, propName);
  if ((desc !== (void 0))) {
    const getter = desc.get;
    return ((getter !== (void 0)) ? getter.call(self) : getter.value)
  }
}
function $superSet(superClass, self, propName, value) {
  const desc = $resolveSuperRef(superClass, propName);
  if ((desc !== (void 0))) {
    const setter = desc.set;
    if ((setter !== (void 0))) {
      setter.call(self, value);
      return (void 0)
    }
  };
  throw new TypeError((("super has no setter '" + propName) + "'."))
}
function $systemArraycopy(src, srcPos, dest, destPos, length) {
  const srcu = src.u;
  const destu = dest.u;
  if ((((((srcPos < 0) || (destPos < 0)) || (length < 0)) || (srcPos > ((srcu.length - length) | 0))) || (destPos > ((destu.length - length) | 0)))) {
    $throwArrayIndexOutOfBoundsException(null)
  };
  if ((((srcu !== destu) || (destPos < srcPos)) || (((srcPos + length) | 0) < destPos))) {
    for (let i = 0; (i < length); i = ((i + 1) | 0)) {
      destu[((destPos + i) | 0)] = srcu[((srcPos + i) | 0)]
    }
  } else {
    for (let i = ((length - 1) | 0); (i >= 0); i = ((i - 1) | 0)) {
      destu[((destPos + i) | 0)] = srcu[((srcPos + i) | 0)]
    }
  }
}
let $lastIDHash = 0;
const $idHashCodeMap = new WeakMap();
function $systemIdentityHashCode(obj) {
  switch ((typeof obj)) {
    case "string":
    case "number":
    case "bigint":
    case "boolean":
    case "undefined": {
      return $dp_hashCode__I(obj)
    }
    default: {
      if ((obj === null)) {
        return 0
      } else {
        let hash = $idHashCodeMap.get(obj);
        if ((hash === (void 0))) {
          hash = (($lastIDHash + 1) | 0);
          $lastIDHash = hash;
          $idHashCodeMap.set(obj, hash)
        };
        return hash
      }
    }
  }
}
function $isByte(v) {
  return ((((typeof v) === "number") && (((v << 24) >> 24) === v)) && ((1 / v) !== (1 / (-0))))
}
function $isShort(v) {
  return ((((typeof v) === "number") && (((v << 16) >> 16) === v)) && ((1 / v) !== (1 / (-0))))
}
function $isInt(v) {
  return ((((typeof v) === "number") && ((v | 0) === v)) && ((1 / v) !== (1 / (-0))))
}
function $bC(c) {
  return new $Char(c)
}
const $bC0 = $bC(0);
function $uV(v) {
  return (((v === (void 0)) || (v === null)) ? (void 0) : $throwClassCastException(v, "java.lang.Void"))
}
function $uZ(v) {
  return ((((typeof v) === "boolean") || (v === null)) ? (!(!v)) : $throwClassCastException(v, "java.lang.Boolean"))
}
function $uC(v) {
  return (((v instanceof $Char) || (v === null)) ? ((v === null) ? 0 : v.c) : $throwClassCastException(v, "java.lang.Character"))
}
function $uB(v) {
  return (($isByte(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Byte"))
}
function $uS(v) {
  return (($isShort(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Short"))
}
function $uI(v) {
  return (($isInt(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Integer"))
}
function $uJ(v) {
  return (((v instanceof $c_RTLong) || (v === null)) ? ((v === null) ? $L0 : v) : $throwClassCastException(v, "java.lang.Long"))
}
function $uF(v) {
  return ((((typeof v) === "number") || (v === null)) ? (+v) : $throwClassCastException(v, "java.lang.Float"))
}
function $uD(v) {
  return ((((typeof v) === "number") || (v === null)) ? (+v) : $throwClassCastException(v, "java.lang.Double"))
}
function $uT(v) {
  return ((((typeof v) === "string") || (v === null)) ? ((v === null) ? "" : v) : $throwClassCastException(v, "java.lang.String"))
}
function $byteArray2TypedArray(value) {
  return new Int8Array(value.u)
}
function $typedArray2ByteArray(value) {
  return new ($d_B.getArrayOf().constr)(new Int8Array(value))
}
function $shortArray2TypedArray(value) {
  return new Int16Array(value.u)
}
function $typedArray2ShortArray(value) {
  return new ($d_S.getArrayOf().constr)(new Int16Array(value))
}
function $charArray2TypedArray(value) {
  return new Uint16Array(value.u)
}
function $typedArray2CharArray(value) {
  return new ($d_C.getArrayOf().constr)(new Uint16Array(value))
}
function $intArray2TypedArray(value) {
  return new Int32Array(value.u)
}
function $typedArray2IntArray(value) {
  return new ($d_I.getArrayOf().constr)(new Int32Array(value))
}
function $floatArray2TypedArray(value) {
  return new Float32Array(value.u)
}
function $typedArray2FloatArray(value) {
  return new ($d_F.getArrayOf().constr)(new Float32Array(value))
}
function $doubleArray2TypedArray(value) {
  return new Float64Array(value.u)
}
function $typedArray2DoubleArray(value) {
  return new ($d_D.getArrayOf().constr)(new Float64Array(value))
}
class $TypeData {
  constructor() {
    this.constr = (void 0);
    this.ancestors = null;
    this.componentData = null;
    this.arrayBase = null;
    this.arrayDepth = 0;
    this.zero = null;
    this.arrayEncodedName = "";
    this._classOf = (void 0);
    this._arrayOf = (void 0);
    this.isArrayOf = (void 0);
    this.name = "";
    this.isPrimitive = false;
    this.isInterface = false;
    this.isArrayClass = false;
    this.isJSClass = false;
    this.isInstance = (void 0)
  };
  initPrim(zero, arrayEncodedName, displayName, isArrayOf) {
    this.ancestors = {};
    this.zero = zero;
    this.arrayEncodedName = arrayEncodedName;
    this.isArrayOf = isArrayOf;
    this.name = displayName;
    this.isPrimitive = true;
    this.isInstance = (function(obj) {
      return false
    });
    return this
  };
  initClass(internalNameObj, isInterface, fullName, ancestors, isJSType, parentData, isInstance, isArrayOf) {
    const internalName = $propertyName(internalNameObj);
    this.ancestors = ancestors;
    this.arrayEncodedName = (("L" + fullName) + ";");
    this.isArrayOf = (isArrayOf || (function(obj, depth) {
      return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors[internalName])))
    }));
    this.isJSType = (!(!isJSType));
    this.name = fullName;
    this.isInterface = isInterface;
    this.isInstance = (isInstance || (function(obj) {
      return (!(!((obj && obj.$classData) && obj.$classData.ancestors[internalName])))
    }));
    return this
  };
  initArray(componentData) {
    const componentZero = ((componentData.zero === "longZero") ? $L0 : componentData.zero);
    class ArrayClass extends $c_O {
      constructor(arg) {
        super();
        if (((typeof arg) === "number")) {
          this.u = new Array(arg);
          for (let i = 0; (i < arg); (i++)) {
            this.u[i] = componentZero
          }
        } else {
          this.u = arg
        }
      };
      get(i) {
        if (((i < 0) || (i >= this.u.length))) {
          $throwArrayIndexOutOfBoundsException(i)
        };
        return this.u[i]
      };
      set(i, v) {
        if (((i < 0) || (i >= this.u.length))) {
          $throwArrayIndexOutOfBoundsException(i)
        };
        this.u[i] = v
      };
      clone__O() {
        return new ArrayClass(((this.u instanceof Array) ? this.u.slice(0) : new this.u.constructor(this.u)))
      };
    }
    ArrayClass.prototype.$classData = this;
    const encodedName = ("[" + componentData.arrayEncodedName);
    const componentBase = (componentData.arrayBase || componentData);
    const arrayDepth = (componentData.arrayDepth + 1);
    this.constr = ArrayClass;
    this.ancestors = {
      O: 1,
      jl_Cloneable: 1,
      Ljava_io_Serializable: 1
    };
    this.componentData = componentData;
    this.arrayBase = componentBase;
    this.arrayDepth = arrayDepth;
    this.arrayEncodedName = encodedName;
    this.name = encodedName;
    this.isArrayClass = true;
    this.isInstance = (function(obj) {
      return componentBase.isArrayOf(obj, arrayDepth)
    });
    return this
  };
  getArrayOf() {
    if ((!this._arrayOf)) {
      this._arrayOf = new $TypeData().initArray(this)
    };
    return this._arrayOf
  };
}
function $isArrayOf_V(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_V))))
}
function $isArrayOf_Z(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_Z))))
}
function $isArrayOf_C(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_C))))
}
function $isArrayOf_B(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_B))))
}
function $isArrayOf_S(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_S))))
}
function $isArrayOf_I(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_I))))
}
function $isArrayOf_J(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_J))))
}
function $isArrayOf_F(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_F))))
}
function $isArrayOf_D(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_D))))
}
function $asArrayOf_V(obj, depth) {
  if (($isArrayOf_V(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "V", depth)
  }
}
function $asArrayOf_Z(obj, depth) {
  if (($isArrayOf_Z(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "Z", depth)
  }
}
function $asArrayOf_C(obj, depth) {
  if (($isArrayOf_C(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "C", depth)
  }
}
function $asArrayOf_B(obj, depth) {
  if (($isArrayOf_B(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "B", depth)
  }
}
function $asArrayOf_S(obj, depth) {
  if (($isArrayOf_S(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "S", depth)
  }
}
function $asArrayOf_I(obj, depth) {
  if (($isArrayOf_I(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "I", depth)
  }
}
function $asArrayOf_J(obj, depth) {
  if (($isArrayOf_J(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "J", depth)
  }
}
function $asArrayOf_F(obj, depth) {
  if (($isArrayOf_F(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "F", depth)
  }
}
function $asArrayOf_D(obj, depth) {
  if (($isArrayOf_D(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "D", depth)
  }
}
const $d_V = new $TypeData().initPrim((void 0), "V", "void", $isArrayOf_V);
const $d_Z = new $TypeData().initPrim(false, "Z", "boolean", $isArrayOf_Z);
const $d_C = new $TypeData().initPrim(0, "C", "char", $isArrayOf_C);
const $d_B = new $TypeData().initPrim(0, "B", "byte", $isArrayOf_B);
const $d_S = new $TypeData().initPrim(0, "S", "short", $isArrayOf_S);
const $d_I = new $TypeData().initPrim(0, "I", "int", $isArrayOf_I);
const $d_J = new $TypeData().initPrim("longZero", "J", "long", $isArrayOf_J);
const $d_F = new $TypeData().initPrim(0.0, "F", "float", $isArrayOf_F);
const $d_D = new $TypeData().initPrim(0.0, "D", "double", $isArrayOf_D);
class $c_O {
  hashCode__I() {
    return $systemIdentityHashCode(this)
  };
  toString__T() {
    const $$x1 = $objectClassName(this);
    const i = this.hashCode__I();
    return (($$x1 + "@") + $as_T($uD((i >>> 0)).toString(16)))
  };
  "toString"() {
    return this.toString__T()
  };
}
function $is_O(obj) {
  return (obj !== null)
}
function $as_O(obj) {
  return obj
}
function $isArrayOf_O(obj, depth) {
  const data = (obj && obj.$classData);
  if ((!data)) {
    return false
  } else {
    const arrayDepth = (data.arrayDepth || 0);
    return ((!(arrayDepth < depth)) && ((arrayDepth > depth) || (!data.arrayBase.isPrimitive)))
  }
}
function $asArrayOf_O(obj, depth) {
  return (($isArrayOf_O(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Object;", depth))
}
const $d_O = new $TypeData().initClass({
  O: 0
}, false, "java.lang.Object", {
  O: 1
}, (void 0), (void 0), $is_O, $isArrayOf_O);
$c_O.prototype.$classData = $d_O;
class $c_jl_FloatingPointBits$ extends $c_O {
  constructor() {
    super();
    this.jl_FloatingPointBits$__f_java$lang$FloatingPointBits$$_areTypedArraysSupported = false;
    this.jl_FloatingPointBits$__f_arrayBuffer = null;
    this.jl_FloatingPointBits$__f_int32Array = null;
    this.jl_FloatingPointBits$__f_float32Array = null;
    this.jl_FloatingPointBits$__f_float64Array = null;
    this.jl_FloatingPointBits$__f_areTypedArraysBigEndian = false;
    this.jl_FloatingPointBits$__f_highOffset = 0;
    this.jl_FloatingPointBits$__f_lowOffset = 0;
    $n_jl_FloatingPointBits$ = this;
    this.jl_FloatingPointBits$__f_java$lang$FloatingPointBits$$_areTypedArraysSupported = true;
    this.jl_FloatingPointBits$__f_arrayBuffer = new ArrayBuffer(8);
    this.jl_FloatingPointBits$__f_int32Array = new Int32Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 2);
    this.jl_FloatingPointBits$__f_float32Array = new Float32Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 2);
    this.jl_FloatingPointBits$__f_float64Array = new Float64Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 1);
    this.jl_FloatingPointBits$__f_int32Array[0] = 16909060;
    this.jl_FloatingPointBits$__f_areTypedArraysBigEndian = ($uB(new Int8Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 8)[0]) === 1);
    this.jl_FloatingPointBits$__f_highOffset = (this.jl_FloatingPointBits$__f_areTypedArraysBigEndian ? 0 : 1);
    this.jl_FloatingPointBits$__f_lowOffset = (this.jl_FloatingPointBits$__f_areTypedArraysBigEndian ? 1 : 0)
  };
  numberHashCode__D__I(value) {
    const iv = $uI((value | 0));
    if (((iv === value) && ((1.0 / value) !== (-Infinity)))) {
      return iv
    } else {
      const t = this.doubleToLongBits__D__J(value);
      const lo = t.RTLong__f_lo;
      const hi = t.RTLong__f_hi;
      return (lo ^ hi)
    }
  };
  doubleToLongBits__D__J(value) {
    this.jl_FloatingPointBits$__f_float64Array[0] = value;
    const value$1 = $uI(this.jl_FloatingPointBits$__f_int32Array[this.jl_FloatingPointBits$__f_highOffset]);
    const value$2 = $uI(this.jl_FloatingPointBits$__f_int32Array[this.jl_FloatingPointBits$__f_lowOffset]);
    return new $c_RTLong(value$2, value$1)
  };
}
const $d_jl_FloatingPointBits$ = new $TypeData().initClass({
  jl_FloatingPointBits$: 0
}, false, "java.lang.FloatingPointBits$", {
  jl_FloatingPointBits$: 1,
  O: 1
});
$c_jl_FloatingPointBits$.prototype.$classData = $d_jl_FloatingPointBits$;
let $n_jl_FloatingPointBits$;
function $m_jl_FloatingPointBits$() {
  if ((!$n_jl_FloatingPointBits$)) {
    $n_jl_FloatingPointBits$ = new $c_jl_FloatingPointBits$()
  };
  return $n_jl_FloatingPointBits$
}
function $f_jl_Void__hashCode__I($thiz) {
  return 0
}
function $f_jl_Void__toString__T($thiz) {
  return "undefined"
}
const $d_jl_Void = new $TypeData().initClass({
  jl_Void: 0
}, false, "java.lang.Void", {
  jl_Void: 1,
  O: 1
}, (void 0), (void 0), ((x) => (x === (void 0))));
class $c_jl_Number extends $c_O {
}
const $ct_jl_Throwable__T__jl_Throwable__Z__Z__ = (function($thiz, s, e, enableSuppression, writableStackTrace) {
  $thiz.jl_Throwable__f_s = s;
  $thiz.jl_Throwable__f_e = e;
  $thiz.jl_Throwable__f_enableSuppression = enableSuppression;
  $thiz.jl_Throwable__f_writableStackTrace = writableStackTrace;
  if (writableStackTrace) {
    $thiz.fillInStackTrace__jl_Throwable()
  };
  return $thiz
});
class $c_jl_Throwable extends Error {
  constructor() {
    super();
    this.jl_Throwable__f_s = null;
    this.jl_Throwable__f_e = null;
    this.jl_Throwable__f_enableSuppression = false;
    this.jl_Throwable__f_writableStackTrace = false;
    this.jl_Throwable__f_stackTraceStateInternal = null;
    this.jl_Throwable__f_stackTrace = null;
    this.jl_Throwable__f_suppressed = null
  };
  fillInStackTrace__jl_Throwable() {
    const identifyingString = Object.prototype.toString.call(this);
    if ((identifyingString === "[object Error]")) {
      this.jl_Throwable__f_stackTraceStateInternal = this
    } else if ((Error.captureStackTrace === (void 0))) {
      const e = new Error();
      this.jl_Throwable__f_stackTraceStateInternal = e
    } else {
      Error.captureStackTrace(this);
      this.jl_Throwable__f_stackTraceStateInternal = this
    };
    return this
  };
  toString__T() {
    const className = $objectClassName(this);
    const message = this.jl_Throwable__f_s;
    return ((message === null) ? className : ((className + ": ") + message))
  };
  $js$exported$meth$toString__O() {
    return this.toString__T()
  };
  $js$exported$prop$name__O() {
    return $objectClassName(this)
  };
  $js$exported$prop$message__O() {
    const m = this.jl_Throwable__f_s;
    return ((m === null) ? "" : m)
  };
  hashCode__I() {
    return $c_O.prototype.hashCode__I.call(this)
  };
  get "message"() {
    return this.$js$exported$prop$message__O()
  };
  get "name"() {
    return this.$js$exported$prop$name__O()
  };
  "toString"() {
    return this.$js$exported$meth$toString__O()
  };
}
const $p_RTLong$__toUnsignedString__I__I__T = (function($thiz, lo, hi) {
  if ((((-2097152) & hi) === 0)) {
    const this$1 = ((4.294967296E9 * hi) + $uD((lo >>> 0)));
    return ("" + this$1)
  } else {
    return $as_T($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, lo, hi, 1000000000, 0, 2))
  }
});
const $p_RTLong$__unsigned_$div__I__I__I__I__I = (function($thiz, alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      const aDouble = ((4.294967296E9 * ahi) + $uD((alo >>> 0)));
      const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
      const rDouble = (aDouble / bDouble);
      const x = (rDouble / 4.294967296E9);
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = $uI((x | 0));
      return $uI((rDouble | 0))
    } else {
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
      return 0
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    const pow = ((31 - $clz32(blo)) | 0);
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((ahi >>> pow) | 0);
    return (((alo >>> pow) | 0) | ((ahi << 1) << ((31 - pow) | 0)))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    const pow$2 = ((31 - $clz32(bhi)) | 0);
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
    return ((ahi >>> pow$2) | 0)
  } else {
    return $uI($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, alo, ahi, blo, bhi, 0))
  }
});
const $p_RTLong$__unsigned_$percent__I__I__I__I__I = (function($thiz, alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      const aDouble = ((4.294967296E9 * ahi) + $uD((alo >>> 0)));
      const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
      const rDouble = (aDouble % bDouble);
      const x = (rDouble / 4.294967296E9);
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = $uI((x | 0));
      return $uI((rDouble | 0))
    } else {
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ahi;
      return alo
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
    return (alo & (((-1) + blo) | 0))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (ahi & (((-1) + bhi) | 0));
    return alo
  } else {
    return $uI($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, alo, ahi, blo, bhi, 1))
  }
});
const $p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O = (function($thiz, alo, ahi, blo, bhi, ask) {
  let shift = ((((bhi !== 0) ? $clz32(bhi) : ((32 + $clz32(blo)) | 0)) - ((ahi !== 0) ? $clz32(ahi) : ((32 + $clz32(alo)) | 0))) | 0);
  const n = shift;
  const lo = (((32 & n) === 0) ? (blo << n) : 0);
  const hi = (((32 & n) === 0) ? (((((blo >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (bhi << n)) : (blo << n));
  let bShiftLo = lo;
  let bShiftHi = hi;
  let remLo = alo;
  let remHi = ahi;
  let quotLo = 0;
  let quotHi = 0;
  while (((shift >= 0) && (((-2097152) & remHi) !== 0))) {
    const alo$1 = remLo;
    const ahi$1 = remHi;
    const blo$1 = bShiftLo;
    const bhi$1 = bShiftHi;
    if (((ahi$1 === bhi$1) ? (((-2147483648) ^ alo$1) >= ((-2147483648) ^ blo$1)) : (((-2147483648) ^ ahi$1) >= ((-2147483648) ^ bhi$1)))) {
      const lo$1 = remLo;
      const hi$1 = remHi;
      const lo$2 = bShiftLo;
      const hi$2 = bShiftHi;
      const lo$3 = ((lo$1 - lo$2) | 0);
      const hi$3 = ((((-2147483648) ^ lo$3) > ((-2147483648) ^ lo$1)) ? (((-1) + ((hi$1 - hi$2) | 0)) | 0) : ((hi$1 - hi$2) | 0));
      remLo = lo$3;
      remHi = hi$3;
      if ((shift < 32)) {
        quotLo = (quotLo | (1 << shift))
      } else {
        quotHi = (quotHi | (1 << shift))
      }
    };
    shift = (((-1) + shift) | 0);
    const lo$4 = bShiftLo;
    const hi$4 = bShiftHi;
    const lo$5 = (((lo$4 >>> 1) | 0) | (hi$4 << 31));
    const hi$5 = ((hi$4 >>> 1) | 0);
    bShiftLo = lo$5;
    bShiftHi = hi$5
  };
  const alo$2 = remLo;
  const ahi$2 = remHi;
  if (((ahi$2 === bhi) ? (((-2147483648) ^ alo$2) >= ((-2147483648) ^ blo)) : (((-2147483648) ^ ahi$2) >= ((-2147483648) ^ bhi)))) {
    const lo$6 = remLo;
    const hi$6 = remHi;
    const remDouble = ((4.294967296E9 * hi$6) + $uD((lo$6 >>> 0)));
    const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
    if ((ask !== 1)) {
      const x = (remDouble / bDouble);
      const lo$7 = $uI((x | 0));
      const x$1 = (x / 4.294967296E9);
      const hi$7 = $uI((x$1 | 0));
      const lo$8 = quotLo;
      const hi$8 = quotHi;
      const lo$9 = ((lo$8 + lo$7) | 0);
      const hi$9 = ((((-2147483648) ^ lo$9) < ((-2147483648) ^ lo$8)) ? ((1 + ((hi$8 + hi$7) | 0)) | 0) : ((hi$8 + hi$7) | 0));
      quotLo = lo$9;
      quotHi = hi$9
    };
    if ((ask !== 0)) {
      const rem_mod_bDouble = (remDouble % bDouble);
      remLo = $uI((rem_mod_bDouble | 0));
      const x$2 = (rem_mod_bDouble / 4.294967296E9);
      remHi = $uI((x$2 | 0))
    }
  };
  if ((ask === 0)) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = quotHi;
    return quotLo
  } else if ((ask === 1)) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = remHi;
    return remLo
  } else {
    const lo$10 = quotLo;
    const hi$10 = quotHi;
    const quot = ((4.294967296E9 * hi$10) + $uD((lo$10 >>> 0)));
    const this$3 = remLo;
    const remStr = ("" + this$3);
    const start = $uI(remStr.length);
    return ((("" + quot) + $as_T("000000000".substring(start))) + remStr)
  }
});
class $c_RTLong$ extends $c_O {
  constructor() {
    super();
    this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0
  };
  org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(lo, hi) {
    return ((hi === (lo >> 31)) ? ("" + lo) : ((hi < 0) ? ("-" + $p_RTLong$__toUnsignedString__I__I__T(this, ((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))) : $p_RTLong$__toUnsignedString__I__I__T(this, lo, hi)))
  };
  org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi) {
    if ((hi < 0)) {
      const x = ((lo !== 0) ? (~hi) : ((-hi) | 0));
      const $$x1 = $uD((x >>> 0));
      const x$1 = ((-lo) | 0);
      return (-((4.294967296E9 * $$x1) + $uD((x$1 >>> 0))))
    } else {
      return ((4.294967296E9 * hi) + $uD((lo >>> 0)))
    }
  };
  fromInt__I__RTLong(value) {
    return new $c_RTLong(value, (value >> 31))
  };
  fromDouble__D__RTLong(value) {
    const lo = this.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value);
    return new $c_RTLong(lo, this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value) {
    if ((value < (-9.223372036854776E18))) {
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (-2147483648);
      return 0
    } else if ((value >= 9.223372036854776E18)) {
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 2147483647;
      return (-1)
    } else {
      const rawLo = $uI((value | 0));
      const x = (value / 4.294967296E9);
      const rawHi = $uI((x | 0));
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (((value < 0.0) && (rawLo !== 0)) ? (((-1) + rawHi) | 0) : rawHi);
      return rawLo
    }
  };
  org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(alo, ahi, blo, bhi) {
    return ((ahi === bhi) ? ((alo === blo) ? 0 : ((((-2147483648) ^ alo) < ((-2147483648) ^ blo)) ? (-1) : 1)) : ((ahi < bhi) ? (-1) : 1))
  };
  divideImpl__I__I__I__I__I(alo, ahi, blo, bhi) {
    if (((blo | bhi) === 0)) {
      throw new $c_jl_ArithmeticException("/ by zero")
    };
    if ((ahi === (alo >> 31))) {
      if ((bhi === (blo >> 31))) {
        if (((alo === (-2147483648)) && (blo === (-1)))) {
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
          return (-2147483648)
        } else {
          const lo = $intDiv(alo, blo);
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (lo >> 31);
          return lo
        }
      } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (-1);
        return (-1)
      } else {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
        return 0
      }
    } else {
      let aAbs__lo;
      let aAbs__hi;
      if ((ahi < 0)) {
        const lo$1 = ((-alo) | 0);
        const hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
        const $$x1__lo = lo$1;
        const $$x1__hi = hi;
        aAbs__lo = $$x1__lo;
        aAbs__hi = $$x1__hi
      } else {
        const $$x2__lo = alo;
        const $$x2__hi = ahi;
        aAbs__lo = $$x2__lo;
        aAbs__hi = $$x2__hi
      };
      let bAbs__lo;
      let bAbs__hi;
      if ((bhi < 0)) {
        const lo$2 = ((-blo) | 0);
        const hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
        const $$x3__lo = lo$2;
        const $$x3__hi = hi$1;
        bAbs__lo = $$x3__lo;
        bAbs__hi = $$x3__hi
      } else {
        const $$x4__lo = blo;
        const $$x4__hi = bhi;
        bAbs__lo = $$x4__lo;
        bAbs__hi = $$x4__hi
      };
      const absRLo = $p_RTLong$__unsigned_$div__I__I__I__I__I(this, aAbs__lo, aAbs__hi, bAbs__lo, bAbs__hi);
      if (((ahi ^ bhi) >= 0)) {
        return absRLo
      } else {
        const hi$2 = this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
        return ((-absRLo) | 0)
      }
    }
  };
  remainderImpl__I__I__I__I__I(alo, ahi, blo, bhi) {
    if (((blo | bhi) === 0)) {
      throw new $c_jl_ArithmeticException("/ by zero")
    };
    if ((ahi === (alo >> 31))) {
      if ((bhi === (blo >> 31))) {
        if ((blo !== (-1))) {
          const lo = $intMod(alo, blo);
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (lo >> 31);
          return lo
        } else {
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
          return 0
        }
      } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
        return 0
      } else {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ahi;
        return alo
      }
    } else {
      let aAbs__lo;
      let aAbs__hi;
      if ((ahi < 0)) {
        const lo$1 = ((-alo) | 0);
        const hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
        const $$x1__lo = lo$1;
        const $$x1__hi = hi;
        aAbs__lo = $$x1__lo;
        aAbs__hi = $$x1__hi
      } else {
        const $$x2__lo = alo;
        const $$x2__hi = ahi;
        aAbs__lo = $$x2__lo;
        aAbs__hi = $$x2__hi
      };
      let bAbs__lo;
      let bAbs__hi;
      if ((bhi < 0)) {
        const lo$2 = ((-blo) | 0);
        const hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
        const $$x3__lo = lo$2;
        const $$x3__hi = hi$1;
        bAbs__lo = $$x3__lo;
        bAbs__hi = $$x3__hi
      } else {
        const $$x4__lo = blo;
        const $$x4__hi = bhi;
        bAbs__lo = $$x4__lo;
        bAbs__hi = $$x4__hi
      };
      const absRLo = $p_RTLong$__unsigned_$percent__I__I__I__I__I(this, aAbs__lo, aAbs__hi, bAbs__lo, bAbs__hi);
      if ((ahi < 0)) {
        const hi$2 = this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
        return ((-absRLo) | 0)
      } else {
        return absRLo
      }
    }
  };
}
const $d_RTLong$ = new $TypeData().initClass({
  RTLong$: 0
}, false, "org.scalajs.linker.runtime.RuntimeLong$", {
  RTLong$: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_RTLong$.prototype.$classData = $d_RTLong$;
let $n_RTLong$;
function $m_RTLong$() {
  if ((!$n_RTLong$)) {
    $n_RTLong$ = new $c_RTLong$()
  };
  return $n_RTLong$
}
function $f_jl_Boolean__hashCode__I($thiz) {
  return ($uZ($thiz) ? 1231 : 1237)
}
function $f_jl_Boolean__toString__T($thiz) {
  const b = $uZ($thiz);
  return ("" + b)
}
const $d_jl_Boolean = new $TypeData().initClass({
  jl_Boolean: 0
}, false, "java.lang.Boolean", {
  jl_Boolean: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "boolean")));
function $f_jl_Character__hashCode__I($thiz) {
  return $uC($thiz)
}
function $f_jl_Character__toString__T($thiz) {
  const c = $uC($thiz);
  return $as_T(String.fromCharCode(c))
}
const $d_jl_Character = new $TypeData().initClass({
  jl_Character: 0
}, false, "java.lang.Character", {
  jl_Character: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => (x instanceof $Char)));
class $c_jl_Error extends $c_jl_Throwable {
}
class $c_jl_Exception extends $c_jl_Throwable {
}
function $f_jl_Byte__hashCode__I($thiz) {
  return $uB($thiz)
}
function $f_jl_Byte__toString__T($thiz) {
  const b = $uB($thiz);
  return ("" + b)
}
const $d_jl_Byte = new $TypeData().initClass({
  jl_Byte: 0
}, false, "java.lang.Byte", {
  jl_Byte: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isByte(x)));
function $f_jl_Double__hashCode__I($thiz) {
  const value = $uD($thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
}
function $f_jl_Double__toString__T($thiz) {
  const d = $uD($thiz);
  return ("" + d)
}
const $d_jl_Double = new $TypeData().initClass({
  jl_Double: 0
}, false, "java.lang.Double", {
  jl_Double: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "number")));
function $f_jl_Float__hashCode__I($thiz) {
  const value = $uF($thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
}
function $f_jl_Float__toString__T($thiz) {
  const f = $uF($thiz);
  return ("" + f)
}
const $d_jl_Float = new $TypeData().initClass({
  jl_Float: 0
}, false, "java.lang.Float", {
  jl_Float: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "number")));
function $f_jl_Integer__hashCode__I($thiz) {
  return $uI($thiz)
}
function $f_jl_Integer__toString__T($thiz) {
  const i = $uI($thiz);
  return ("" + i)
}
const $d_jl_Integer = new $TypeData().initClass({
  jl_Integer: 0
}, false, "java.lang.Integer", {
  jl_Integer: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isInt(x)));
function $f_jl_Long__hashCode__I($thiz) {
  const t = $uJ($thiz);
  const lo = t.RTLong__f_lo;
  const hi = t.RTLong__f_hi;
  return (lo ^ hi)
}
function $f_jl_Long__toString__T($thiz) {
  const t = $uJ($thiz);
  const lo = t.RTLong__f_lo;
  const hi = t.RTLong__f_hi;
  return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(lo, hi)
}
function $as_jl_Long(obj) {
  return (((obj instanceof $c_RTLong) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Long"))
}
function $isArrayOf_jl_Long(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Long)))
}
function $asArrayOf_jl_Long(obj, depth) {
  return (($isArrayOf_jl_Long(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Long;", depth))
}
const $d_jl_Long = new $TypeData().initClass({
  jl_Long: 0
}, false, "java.lang.Long", {
  jl_Long: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => (x instanceof $c_RTLong)));
class $c_jl_RuntimeException extends $c_jl_Exception {
}
function $f_jl_Short__hashCode__I($thiz) {
  return $uS($thiz)
}
function $f_jl_Short__toString__T($thiz) {
  const s = $uS($thiz);
  return ("" + s)
}
const $d_jl_Short = new $TypeData().initClass({
  jl_Short: 0
}, false, "java.lang.Short", {
  jl_Short: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isShort(x)));
function $f_T__hashCode__I($thiz) {
  let res = 0;
  let mul = 1;
  let i = (((-1) + $uI($thiz.length)) | 0);
  while ((i >= 0)) {
    const $$x1 = res;
    const index = i;
    res = (($$x1 + $imul((65535 & $uI($thiz.charCodeAt(index))), mul)) | 0);
    mul = $imul(31, mul);
    i = (((-1) + i) | 0)
  };
  return res
}
function $f_T__toString__T($thiz) {
  return $thiz
}
function $as_T(obj) {
  return ((((typeof obj) === "string") || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.String"))
}
function $isArrayOf_T(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.T)))
}
function $asArrayOf_T(obj, depth) {
  return (($isArrayOf_T(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.String;", depth))
}
const $d_T = new $TypeData().initClass({
  T: 0
}, false, "java.lang.String", {
  T: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1,
  jl_CharSequence: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "string")));
class $c_jl_VirtualMachineError extends $c_jl_Error {
}
class $c_RTLong extends $c_jl_Number {
  constructor(lo, hi) {
    super();
    this.RTLong__f_lo = 0;
    this.RTLong__f_hi = 0;
    this.RTLong__f_lo = lo;
    this.RTLong__f_hi = hi
  };
  equals__O__Z(that) {
    if ((that instanceof $c_RTLong)) {
      const x2 = $as_RTLong(that);
      return ((this.RTLong__f_lo === x2.RTLong__f_lo) && (this.RTLong__f_hi === x2.RTLong__f_hi))
    } else {
      return false
    }
  };
  hashCode__I() {
    return (this.RTLong__f_lo ^ this.RTLong__f_hi)
  };
  toString__T() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  toInt__I() {
    return this.RTLong__f_lo
  };
  toDouble__D() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  byteValue__B() {
    return ((this.RTLong__f_lo << 24) >> 24)
  };
  shortValue__S() {
    return ((this.RTLong__f_lo << 16) >> 16)
  };
  intValue__I() {
    return this.RTLong__f_lo
  };
  longValue__J() {
    return $uJ(this)
  };
  floatValue__F() {
    return $fround($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi))
  };
  doubleValue__D() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  compareTo__jl_Long__I(that) {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, that.RTLong__f_lo, that.RTLong__f_hi)
  };
  equals__RTLong__Z(b) {
    return ((this.RTLong__f_lo === b.RTLong__f_lo) && (this.RTLong__f_hi === b.RTLong__f_hi))
  };
  notEquals__RTLong__Z(b) {
    return (!((this.RTLong__f_lo === b.RTLong__f_lo) && (this.RTLong__f_hi === b.RTLong__f_hi)))
  };
  $less__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) < ((-2147483648) ^ b.RTLong__f_lo)) : (ahi < bhi))
  };
  $less$eq__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) <= ((-2147483648) ^ b.RTLong__f_lo)) : (ahi < bhi))
  };
  $greater__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) > ((-2147483648) ^ b.RTLong__f_lo)) : (ahi > bhi))
  };
  $greater$eq__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) >= ((-2147483648) ^ b.RTLong__f_lo)) : (ahi > bhi))
  };
  unary_$tilde__RTLong() {
    return new $c_RTLong((~this.RTLong__f_lo), (~this.RTLong__f_hi))
  };
  $bar__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo | b.RTLong__f_lo), (this.RTLong__f_hi | b.RTLong__f_hi))
  };
  $amp__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo & b.RTLong__f_lo), (this.RTLong__f_hi & b.RTLong__f_hi))
  };
  $up__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo ^ b.RTLong__f_lo), (this.RTLong__f_hi ^ b.RTLong__f_hi))
  };
  $less$less__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (this.RTLong__f_lo << n) : 0), (((32 & n) === 0) ? (((((this.RTLong__f_lo >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (this.RTLong__f_hi << n)) : (this.RTLong__f_lo << n)))
  };
  $greater$greater$greater__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (((this.RTLong__f_lo >>> n) | 0) | ((this.RTLong__f_hi << 1) << ((31 - n) | 0))) : ((this.RTLong__f_hi >>> n) | 0)), (((32 & n) === 0) ? ((this.RTLong__f_hi >>> n) | 0) : 0))
  };
  $greater$greater__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (((this.RTLong__f_lo >>> n) | 0) | ((this.RTLong__f_hi << 1) << ((31 - n) | 0))) : (this.RTLong__f_hi >> n)), (((32 & n) === 0) ? (this.RTLong__f_hi >> n) : (this.RTLong__f_hi >> 31)))
  };
  unary_$minus__RTLong() {
    const lo = this.RTLong__f_lo;
    const hi = this.RTLong__f_hi;
    return new $c_RTLong(((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))
  };
  $plus__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    const lo = ((alo + b.RTLong__f_lo) | 0);
    return new $c_RTLong(lo, ((((-2147483648) ^ lo) < ((-2147483648) ^ alo)) ? ((1 + ((ahi + bhi) | 0)) | 0) : ((ahi + bhi) | 0)))
  };
  $minus__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    const lo = ((alo - b.RTLong__f_lo) | 0);
    return new $c_RTLong(lo, ((((-2147483648) ^ lo) > ((-2147483648) ^ alo)) ? (((-1) + ((ahi - bhi) | 0)) | 0) : ((ahi - bhi) | 0)))
  };
  $times__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const blo = b.RTLong__f_lo;
    const a0 = (65535 & alo);
    const a1 = ((alo >>> 16) | 0);
    const b0 = (65535 & blo);
    const b1 = ((blo >>> 16) | 0);
    const a0b0 = $imul(a0, b0);
    const a1b0 = $imul(a1, b0);
    const a0b1 = $imul(a0, b1);
    const lo = ((a0b0 + (((a1b0 + a0b1) | 0) << 16)) | 0);
    const c1part = ((((a0b0 >>> 16) | 0) + a0b1) | 0);
    const hi = (((((((($imul(alo, b.RTLong__f_hi) + $imul(this.RTLong__f_hi, blo)) | 0) + $imul(a1, b1)) | 0) + ((c1part >>> 16) | 0)) | 0) + (((((65535 & c1part) + a1b0) | 0) >>> 16) | 0)) | 0);
    return new $c_RTLong(lo, hi)
  };
  $div__RTLong__RTLong(b) {
    const this$1 = $m_RTLong$();
    const lo = this$1.divideImpl__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, b.RTLong__f_lo, b.RTLong__f_hi);
    return new $c_RTLong(lo, this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  $percent__RTLong__RTLong(b) {
    const this$1 = $m_RTLong$();
    const lo = this$1.remainderImpl__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, b.RTLong__f_lo, b.RTLong__f_hi);
    return new $c_RTLong(lo, this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  compareTo__O__I(x$1) {
    const that = $as_jl_Long(x$1);
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, that.RTLong__f_lo, that.RTLong__f_hi)
  };
}
function $as_RTLong(obj) {
  return (((obj instanceof $c_RTLong) || (obj === null)) ? obj : $throwClassCastException(obj, "org.scalajs.linker.runtime.RuntimeLong"))
}
function $isArrayOf_RTLong(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.RTLong)))
}
function $asArrayOf_RTLong(obj, depth) {
  return (($isArrayOf_RTLong(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lorg.scalajs.linker.runtime.RuntimeLong;", depth))
}
const $d_RTLong = new $TypeData().initClass({
  RTLong: 0
}, false, "org.scalajs.linker.runtime.RuntimeLong", {
  RTLong: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
});
$c_RTLong.prototype.$classData = $d_RTLong;
class $c_jl_ArithmeticException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ArithmeticException = new $TypeData().initClass({
  jl_ArithmeticException: 0
}, false, "java.lang.ArithmeticException", {
  jl_ArithmeticException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ArithmeticException.prototype.$classData = $d_jl_ArithmeticException;
class $c_jl_ClassCastException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ClassCastException = new $TypeData().initClass({
  jl_ClassCastException: 0
}, false, "java.lang.ClassCastException", {
  jl_ClassCastException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ClassCastException.prototype.$classData = $d_jl_ClassCastException;
class $c_jl_IndexOutOfBoundsException extends $c_jl_RuntimeException {
}
class $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError extends $c_jl_VirtualMachineError {
  constructor(cause) {
    super();
    const message = ((cause === null) ? null : cause.toString__T());
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, message, cause, true, true)
  };
}
const $d_Lorg_scalajs_linker_runtime_UndefinedBehaviorError = new $TypeData().initClass({
  Lorg_scalajs_linker_runtime_UndefinedBehaviorError: 0
}, false, "org.scalajs.linker.runtime.UndefinedBehaviorError", {
  Lorg_scalajs_linker_runtime_UndefinedBehaviorError: 1,
  jl_VirtualMachineError: 1,
  jl_Error: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError.prototype.$classData = $d_Lorg_scalajs_linker_runtime_UndefinedBehaviorError;
class $c_jl_ArrayIndexOutOfBoundsException extends $c_jl_IndexOutOfBoundsException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ArrayIndexOutOfBoundsException = new $TypeData().initClass({
  jl_ArrayIndexOutOfBoundsException: 0
}, false, "java.lang.ArrayIndexOutOfBoundsException", {
  jl_ArrayIndexOutOfBoundsException: 1,
  jl_IndexOutOfBoundsException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ArrayIndexOutOfBoundsException.prototype.$classData = $d_jl_ArrayIndexOutOfBoundsException;
$L0 = new $c_RTLong(0, 0);
}).call(this);
//# sourceMappingURL=databaseexploration-fastopt.js.map
