/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalajs.js.typedarray

import scala.scalajs.js

/** <span class="badge badge-ecma6" style="float: right;">ECMAScript 6</span>
 *  An ArrayBufferView allows accessing the data of an [[ArrayBuffer]]
 */
@js.native
trait ArrayBufferView extends js.Object {
  /** The underlying buffer of this ArrayBufferView */
  val buffer: ArrayBuffer = js.native

  /** The number of bytes of this ArrayBufferView */
  val byteLength: Int = js.native

  /** The offset of this ArrayBufferView in the underlying buffer */
  val byteOffset: Int = js.native
}
