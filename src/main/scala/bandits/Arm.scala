package bandits

case class Arm(issue:String, label: String, alpha: Int, beta: Int, score: Double) {
  def key = s"${issue}_${label}"
}
