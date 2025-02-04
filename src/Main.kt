import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main() {
    JFrame.setDefaultLookAndFeelDecorated(true)
    SwingUtilities.invokeLater {
        EditorFrame().apply {
            isVisible = true
        }
    }
}