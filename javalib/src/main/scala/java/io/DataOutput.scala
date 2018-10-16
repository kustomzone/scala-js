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

package java.io

trait DataOutput {
  def write(b: Int): Unit
  def write(b: Array[Byte]): Unit
  def write(b: Array[Byte], off: Int, len: Int): Unit

  def writeBoolean(v: Boolean): Unit
  def writeByte(v: Int): Unit
  def writeShort(v: Int): Unit
  def writeChar(v: Int): Unit
  def writeInt(v: Int): Unit
  def writeLong(v: Long): Unit
  def writeFloat(v: Float): Unit
  def writeDouble(v: Double): Unit

  def writeBytes(s: String): Unit
  def writeChars(s: String): Unit
  def writeUTF(s: String): Unit
}
