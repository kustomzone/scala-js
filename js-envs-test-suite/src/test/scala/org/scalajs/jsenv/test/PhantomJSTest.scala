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

package org.scalajs.jsenv.test

import org.scalajs.jsenv.phantomjs.PhantomJSEnv

class PhantomJSTest extends JSEnvTest with ComTests {
  protected def newJSEnv: PhantomJSEnv = new PhantomJSEnv
}
