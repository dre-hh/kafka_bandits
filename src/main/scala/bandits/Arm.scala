package bandits

case class Arm(issue:String, name: String, alpha: Int, beta: Int, score: Double) {
  def key = s"${issue}_${name}"
}
