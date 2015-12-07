package doobie.util

import doobie.hi._
import doobie.util.capture.Capture
import doobie.syntax.catchable._

import scalaz.{ :+: => _, _ }, Scalaz._

import shapeless._

object trace {

  // a supertype of all the Op types, parameterized on the return type
  trait Op[J,A]

  // An effectful logger
  trait Trace[M[_]] {

    // log the initiation of the given operation, returning a continuation to be called
    // on completion. this may not be immediate; unless Op is a primitive there will likely be 
    // intervening operations. however the contract guarantees stacklike behavior; continuations
    // will never be invoked out-of-order.
    def log[J,A](j: J, op: Op[J,A]): M[Throwable \/ A => M[Unit]]

  }

  object ConsoleFirehose {
    import Predef.augmentString
    def apply[M[_]: Monad: Catchable](implicit delay: Capture[M]): Trace[M] =
      new Trace[M] {
        var indent = 1
        def log[J,A](j: J, op: Op[J,A]): M[Throwable \/ A => M[Unit]] =
          delay { 
            Console.println(" " * indent + op)
            indent += 1 
          } >| (_ => delay(indent -= 1))
      }
  }

}