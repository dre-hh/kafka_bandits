package util

object OptUtil {
  implicit class SeqOption[A](lo: Seq[Option[A]]) {
    def sequence =
      lo.foldRight (Option(Seq[A]())) { (opt, ol) =>
        ol flatMap (l => opt map(o => o +: l))
      }
  }
}
