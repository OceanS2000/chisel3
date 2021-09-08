package chisel3.experimental

import chisel3.internal.HasId
import chisel3.{Aggregate, Data, Element, Module}
import firrtl.AnnotationSeq
import firrtl.annotations.{Annotation, CompleteTarget, SingleTargetAnnotation}
import firrtl.transforms.DontTouchAnnotation

// This API provides a util to trace name.
object trace {

  def traceName(x: Module): Unit = {
    new ChiselAnnotation {
      def toFirrtl: Annotation = TraceNameAnnotation(x.toTarget, x.toTarget)
    }
  }

  def traceName(x: Data): Unit = {
    x match {
      case aggregate: Aggregate =>
        annotate(new ChiselAnnotation {
          def toFirrtl: Annotation = TraceNameAnnotation(aggregate.toAbsoluteTarget, aggregate.toAbsoluteTarget)
        })
        annotate(new ChiselAnnotation {
          def toFirrtl = DontTouchAnnotation(aggregate.toTarget)
        })
        aggregate.getElements.foreach(traceName)
      case element: Element =>
        annotate(new ChiselAnnotation {
          def toFirrtl: Annotation = TraceNameAnnotation(element.toAbsoluteTarget, element.toAbsoluteTarget)
        })
        annotate(new ChiselAnnotation {
          def toFirrtl = DontTouchAnnotation(element.toTarget)
        })
    }
  }

  case class TraceNameAnnotation[T <: CompleteTarget](target: T, chiselTarget: T)
    extends SingleTargetAnnotation[T]
      with HasChiselTarget[T] {
    // rename map only update target, hash is to used to match via the view API.
    def duplicate(n: T): Annotation = this.copy(target = n)
  }

  /** API to view final target of a [[Data]] */
  implicit class TraceFromAnnotations(annos: AnnotationSeq) {
    def finalName(x: HasId): Seq[String] = finalTarget(x).map(_.toString)

    def finalTarget(x: HasId): Seq[CompleteTarget] = finalTargetMap.filter {
      case (k, _) => x.toAbsoluteTarget == k
    }.map(_._2)

    def finalTargetMap: Seq[(CompleteTarget, CompleteTarget)] = annos.collect {
      case TraceNameAnnotation(t, chiselTarget) => chiselTarget -> t
    }
  }
}
