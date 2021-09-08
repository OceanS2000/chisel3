// SPDX-License-Identifier: Apache-2.0

package chisel3.experimental
import firrtl.annotations.{CompleteTarget, SingleTargetAnnotation}

/** An Annotation that records the original target annotate from Chisel. */
trait HasChiselTarget[T <: CompleteTarget] { x: SingleTargetAnnotation[T] =>

  /** original annotated target, which should not be changed or renamed in FIRRTL. */
  val chiselTarget: T
}
