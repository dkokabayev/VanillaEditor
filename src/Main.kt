import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        EditorFrame().apply {
            isVisible = true
        }
    }
}