import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.support.label.Category


class ClassificationsHelper(categories: List<Category>, firstIndex: Int) : Classifications() {
    private val photocategories: List<Category> = categories
    private val firstIndex: Int = firstIndex

    override fun getCategories(): List<Category> {
        return photocategories
    }
    override fun getHeadIndex(): Int {
        return firstIndex
    }
    override fun toString(): String {
        val sb = StringBuilder()
        for (category in photocategories) {
            val scorePercentage = "${(category.score * 100).toInt()}%"
            sb.append("${category.label}: $scorePercentage\n")
        }
        return sb.toString()
    }




}