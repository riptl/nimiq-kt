package nimiq

@ExperimentalUnsignedTypes
inline fun min(a: UInt, b: UInt) =
        if (a < b) a
        else b

@ExperimentalUnsignedTypes
inline fun min(a: ULong, b: ULong) =
        if (a < b) a
        else b

@ExperimentalUnsignedTypes
inline fun max(a: UInt, b: UInt) =
    if (a > b) a
    else b

@ExperimentalUnsignedTypes
inline fun max(a: ULong, b: ULong) =
    if (a > b) a
    else b
